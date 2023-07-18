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

package org.apache.seatunnel.translation.spark.sink;

import org.apache.seatunnel.api.sink.SeaTunnelSink;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.common.Constants;
import org.apache.seatunnel.common.utils.SerializationUtils;
import org.apache.seatunnel.translation.spark.sink.write.SeaTunnelWriteBuilder;
import org.apache.seatunnel.translation.spark.utils.TypeConverterUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.connector.catalog.SupportsWrite;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCapability;
import org.apache.spark.sql.connector.write.LogicalWriteInfo;
import org.apache.spark.sql.connector.write.WriteBuilder;
import org.apache.spark.sql.types.StructType;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

public class SeaTunnelSinkTable implements Table, SupportsWrite {

    private static final String SINK_TABLE_NAME = "SeaTunnelSinkTable";

    private final Map<String, String> properties;

    private final SeaTunnelSink<SeaTunnelRow, ?, ?, ?> sink;

    public SeaTunnelSinkTable(Map<String, String> properties) {
        this.properties = properties;
        String sinkSerialization = properties.getOrDefault(Constants.SINK_SERIALIZATION, "");
        if (StringUtils.isBlank(sinkSerialization)) {
            throw new IllegalArgumentException("sink.serialization must be specified");
        }
        this.sink = SerializationUtils.stringToObject(sinkSerialization);
    }

    @Override
    public WriteBuilder newWriteBuilder(LogicalWriteInfo info) {
        return new SeaTunnelWriteBuilder<>(sink);
    }

    @Override
    public String name() {
        return SINK_TABLE_NAME;
    }

    @Override
    public StructType schema() {
        return (StructType) TypeConverterUtils.convert(sink.getConsumedType());
    }

    @Override
    public Set<TableCapability> capabilities() {
        return Sets.newHashSet(TableCapability.BATCH_WRITE, TableCapability.STREAMING_WRITE);
    }
}
