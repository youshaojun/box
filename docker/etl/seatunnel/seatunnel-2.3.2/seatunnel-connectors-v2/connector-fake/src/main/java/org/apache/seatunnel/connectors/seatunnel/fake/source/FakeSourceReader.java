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

package org.apache.seatunnel.connectors.seatunnel.fake.source;

import org.apache.seatunnel.api.source.Boundedness;
import org.apache.seatunnel.api.source.Collector;
import org.apache.seatunnel.api.source.SourceReader;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.seatunnel.fake.config.FakeConfig;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
public class FakeSourceReader implements SourceReader<SeaTunnelRow, FakeSourceSplit> {

    private final SourceReader.Context context;
    private final Deque<FakeSourceSplit> splits = new ConcurrentLinkedDeque<>();

    private final FakeConfig config;
    private final FakeDataGenerator fakeDataGenerator;
    private volatile boolean noMoreSplit;
    private volatile long latestTimestamp = 0;

    public FakeSourceReader(
            SourceReader.Context context, SeaTunnelRowType rowType, FakeConfig fakeConfig) {
        this.context = context;
        this.config = fakeConfig;
        this.fakeDataGenerator = new FakeDataGenerator(rowType, fakeConfig);
    }

    @Override
    public void open() {
        // nothing
    }

    @Override
    public void close() {
        // nothing
    }

    @Override
    @SuppressWarnings("MagicNumber")
    public void pollNext(Collector<SeaTunnelRow> output) throws InterruptedException {
        long currentTimestamp = Instant.now().toEpochMilli();
        if (currentTimestamp <= latestTimestamp + config.getSplitReadInterval()) {
            return;
        }
        latestTimestamp = currentTimestamp;
        synchronized (output.getCheckpointLock()) {
            FakeSourceSplit split = splits.poll();
            if (null != split) {
                // Randomly generated data are sent directly to the downstream operator
                fakeDataGenerator.collectFakedRows(split.getRowNum(), output);
                log.info(
                        "{} rows of data have been generated in split({}). Generation time: {}",
                        split.getRowNum(),
                        split.splitId(),
                        latestTimestamp);
            } else {
                if (!noMoreSplit) {
                    log.info("wait split!");
                }
            }
        }
        if (noMoreSplit
                && splits.isEmpty()
                && Boundedness.BOUNDED.equals(context.getBoundedness())) {
            // signal to the source that we have reached the end of the data.
            log.info("Closed the bounded fake source");
            context.signalNoMoreElement();
        }
        Thread.sleep(1000L);
    }

    @Override
    public List<FakeSourceSplit> snapshotState(long checkpointId) throws Exception {
        return new ArrayList<>(splits);
    }

    @Override
    public void addSplits(List<FakeSourceSplit> splits) {
        log.debug("reader {} add splits {}", context.getIndexOfSubtask(), splits);
        this.splits.addAll(splits);
    }

    @Override
    public void handleNoMoreSplits() {
        noMoreSplit = true;
    }

    @Override
    public void notifyCheckpointComplete(long checkpointId) throws Exception {}
}
