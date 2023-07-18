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

package org.apache.seatunnel.translation.spark.source.partition.batch;

import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.source.SupportCoordinate;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;

import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.connector.read.InputPartition;
import org.apache.spark.sql.connector.read.PartitionReader;
import org.apache.spark.sql.connector.read.PartitionReaderFactory;

public class SeaTunnelBatchPartitionReaderFactory implements PartitionReaderFactory {

    private final SeaTunnelSource<SeaTunnelRow, ?, ?> source;

    private final int parallelism;

    public SeaTunnelBatchPartitionReaderFactory(
            SeaTunnelSource<SeaTunnelRow, ?, ?> source, int parallelism) {
        this.source = source;
        this.parallelism = parallelism;
    }

    @Override
    public PartitionReader<InternalRow> createReader(InputPartition partition) {
        SeaTunnelBatchInputPartition inputPartition = (SeaTunnelBatchInputPartition) partition;
        int partitionId = inputPartition.getPartitionId();
        ParallelBatchPartitionReader partitionReader;
        if (source instanceof SupportCoordinate) {
            partitionReader = new CoordinatedBatchPartitionReader(source, parallelism, partitionId);
        } else {
            partitionReader = new ParallelBatchPartitionReader(source, parallelism, partitionId);
        }
        return new SeaTunnelBatchPartitionReader(partitionReader);
    }
}
