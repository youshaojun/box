/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.seatunnel.connectors.seatunnel.elasticsearch.source;

import org.apache.seatunnel.shade.com.typesafe.config.Config;

import org.apache.seatunnel.api.source.Collector;
import org.apache.seatunnel.api.source.SourceReader;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.seatunnel.elasticsearch.client.EsRestClient;
import org.apache.seatunnel.connectors.seatunnel.elasticsearch.dto.source.ScrollResult;
import org.apache.seatunnel.connectors.seatunnel.elasticsearch.dto.source.SourceIndexInfo;
import org.apache.seatunnel.connectors.seatunnel.elasticsearch.serialize.source.DefaultSeaTunnelRowDeserializer;
import org.apache.seatunnel.connectors.seatunnel.elasticsearch.serialize.source.ElasticsearchRecord;
import org.apache.seatunnel.connectors.seatunnel.elasticsearch.serialize.source.SeaTunnelRowDeserializer;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ElasticsearchSourceReader
        implements SourceReader<SeaTunnelRow, ElasticsearchSourceSplit> {

    SourceReader.Context context;

    private Config pluginConfig;

    private EsRestClient esRestClient;

    private final SeaTunnelRowDeserializer deserializer;

    Deque<ElasticsearchSourceSplit> splits = new LinkedList<>();
    boolean noMoreSplit;

    private final long pollNextWaitTime = 1000L;

    public ElasticsearchSourceReader(
            SourceReader.Context context, Config pluginConfig, SeaTunnelRowType rowTypeInfo) {
        this.context = context;
        this.pluginConfig = pluginConfig;
        this.deserializer = new DefaultSeaTunnelRowDeserializer(rowTypeInfo);
    }

    @Override
    public void open() {
        esRestClient = EsRestClient.createInstance(this.pluginConfig);
    }

    @Override
    public void close() throws IOException {
        esRestClient.close();
    }

    @Override
    public void pollNext(Collector<SeaTunnelRow> output) throws Exception {
        synchronized (output.getCheckpointLock()) {
            ElasticsearchSourceSplit split = splits.poll();
            if (split != null) {
                SourceIndexInfo sourceIndexInfo = split.getSourceIndexInfo();
                ScrollResult scrollResult =
                        esRestClient.searchByScroll(
                                sourceIndexInfo.getIndex(),
                                sourceIndexInfo.getSource(),
                                sourceIndexInfo.getQuery(),
                                sourceIndexInfo.getScrollTime(),
                                sourceIndexInfo.getScrollSize());
                outputFromScrollResult(scrollResult, sourceIndexInfo.getSource(), output);
                while (scrollResult.getDocs() != null && scrollResult.getDocs().size() > 0) {
                    scrollResult =
                            esRestClient.searchWithScrollId(
                                    scrollResult.getScrollId(), sourceIndexInfo.getScrollTime());
                    outputFromScrollResult(scrollResult, sourceIndexInfo.getSource(), output);
                }
            } else if (noMoreSplit) {
                // signal to the source that we have reached the end of the data.
                log.info("Closed the bounded ELasticsearch source");
                context.signalNoMoreElement();
            } else {
                Thread.sleep(pollNextWaitTime);
            }
        }
    }

    private void outputFromScrollResult(
            ScrollResult scrollResult, List<String> source, Collector<SeaTunnelRow> output) {
        for (Map<String, Object> doc : scrollResult.getDocs()) {
            SeaTunnelRow seaTunnelRow =
                    deserializer.deserialize(new ElasticsearchRecord(doc, source));
            output.collect(seaTunnelRow);
        }
    }

    @Override
    public List<ElasticsearchSourceSplit> snapshotState(long checkpointId) throws Exception {
        return new ArrayList<>(splits);
    }

    @Override
    public void addSplits(List<ElasticsearchSourceSplit> splits) {
        this.splits.addAll(splits);
    }

    @Override
    public void handleNoMoreSplits() {
        noMoreSplit = true;
    }

    @Override
    public void notifyCheckpointComplete(long checkpointId) throws Exception {}
}
