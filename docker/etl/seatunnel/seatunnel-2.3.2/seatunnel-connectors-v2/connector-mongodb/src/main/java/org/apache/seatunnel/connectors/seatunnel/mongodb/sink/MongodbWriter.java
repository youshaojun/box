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

package org.apache.seatunnel.connectors.seatunnel.mongodb.sink;

import org.apache.seatunnel.api.sink.SinkWriter;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.connectors.seatunnel.common.sink.AbstractSinkWriter;
import org.apache.seatunnel.connectors.seatunnel.mongodb.exception.MongodbConnectorException;
import org.apache.seatunnel.connectors.seatunnel.mongodb.internal.MongodbClientProvider;
import org.apache.seatunnel.connectors.seatunnel.mongodb.internal.MongodbCollectionProvider;
import org.apache.seatunnel.connectors.seatunnel.mongodb.serde.DocumentSerializer;

import org.bson.BsonDocument;

import com.mongodb.MongoException;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.WriteModel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.apache.seatunnel.common.exception.CommonErrorCode.WRITER_OPERATION_FAILED;

@Slf4j
public class MongodbWriter extends AbstractSinkWriter<SeaTunnelRow, Void> {

    private MongodbClientProvider collectionProvider;

    private final DocumentSerializer<SeaTunnelRow> serializer;

    private long bulkActions;

    private final List<WriteModel<BsonDocument>> bulkRequests;

    private int maxRetries;

    private long retryIntervalMs;

    private long batchIntervalMs;

    private volatile long lastSendTime = 0L;

    private final SinkWriter.Context context;

    public MongodbWriter(
            DocumentSerializer<SeaTunnelRow> serializer,
            MongodbWriterOptions options,
            SinkWriter.Context context) {
        initOptions(options);
        this.context = context;
        this.serializer = serializer;
        this.bulkRequests = new ArrayList<>();
    }

    private void initOptions(MongodbWriterOptions options) {
        this.maxRetries = options.getRetryMax();
        this.retryIntervalMs = options.getRetryInterval();
        this.collectionProvider =
                MongodbCollectionProvider.builder()
                        .connectionString(options.getConnectString())
                        .database(options.getDatabase())
                        .collection(options.getCollection())
                        .build();
        this.bulkActions = options.getFlushSize();
        this.batchIntervalMs = options.getBatchIntervalMs();
    }

    @Override
    public void write(SeaTunnelRow o) throws IOException {
        bulkRequests.add(serializer.serializeToWriteModel(o));
        if (isOverMaxBatchSizeLimit() || isOverMaxBatchIntervalLimit()) {
            doBulkWrite();
        }
    }

    @Override
    public Optional<Void> prepareCommit() {
        doBulkWrite();
        return Optional.empty();
    }

    @Override
    public void close() throws IOException {
        doBulkWrite();
        if (collectionProvider != null) {
            collectionProvider.close();
        }
    }

    synchronized void doBulkWrite() {
        if (bulkRequests.isEmpty()) {
            // no records to write
            return;
        }

        boolean success =
                IntStream.rangeClosed(0, maxRetries)
                        .anyMatch(
                                i -> {
                                    try {
                                        lastSendTime = System.currentTimeMillis();
                                        collectionProvider
                                                .getDefaultCollection()
                                                .bulkWrite(
                                                        bulkRequests,
                                                        new BulkWriteOptions().ordered(true));
                                        bulkRequests.clear();
                                        return true;
                                    } catch (MongoException e) {
                                        log.debug(
                                                "Bulk Write to MongoDB failed, retry times = {}",
                                                i,
                                                e);
                                        if (i >= maxRetries) {
                                            throw new MongodbConnectorException(
                                                    WRITER_OPERATION_FAILED,
                                                    "Bulk Write to MongoDB failed",
                                                    e);
                                        }
                                        try {
                                            TimeUnit.MILLISECONDS.sleep(retryIntervalMs * (i + 1));
                                        } catch (InterruptedException ex) {
                                            Thread.currentThread().interrupt();
                                            throw new MongodbConnectorException(
                                                    WRITER_OPERATION_FAILED,
                                                    "Unable to flush; interrupted while doing another attempt",
                                                    e);
                                        }
                                        return false;
                                    }
                                });

        if (!success) {
            throw new MongodbConnectorException(
                    WRITER_OPERATION_FAILED, "Bulk Write to MongoDB failed after max retries");
        }
    }

    private boolean isOverMaxBatchSizeLimit() {
        return bulkActions != -1 && bulkRequests.size() >= bulkActions;
    }

    private boolean isOverMaxBatchIntervalLimit() {
        long lastSentInterval = System.currentTimeMillis() - lastSendTime;
        return batchIntervalMs != -1 && lastSentInterval >= batchIntervalMs;
    }
}
