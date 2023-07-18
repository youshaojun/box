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

package org.apache.seatunnel.translation.spark.source;

import org.apache.seatunnel.api.common.CommonOptions;
import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.common.Constants;
import org.apache.seatunnel.common.utils.SerializationUtils;
import org.apache.seatunnel.translation.spark.source.scan.SeaTunnelScanBuilder;
import org.apache.seatunnel.translation.spark.utils.TypeConverterUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.spark.sql.connector.catalog.SupportsRead;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCapability;
import org.apache.spark.sql.connector.read.Scan;
import org.apache.spark.sql.connector.read.ScanBuilder;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/** The basic unit of SeaTunnel DataSource generated, supporting read and write */
public class SeaTunnelSourceTable implements Table, SupportsRead {
    private static final String SOURCE_TABLE_NAME = "SeaTunnelSourceTable";

    private final Map<String, String> properties;

    private final SeaTunnelSource<SeaTunnelRow, ?, ?> source;

    public SeaTunnelSourceTable(Map<String, String> properties) {
        this.properties = properties;
        String sourceSerialization = properties.getOrDefault(Constants.SOURCE_SERIALIZATION, "");
        if (StringUtils.isBlank(sourceSerialization)) {
            throw new IllegalArgumentException("source.serialization must be specified");
        }
        this.source = SerializationUtils.stringToObject(sourceSerialization);
    }

    /**
     * Returns a {@link ScanBuilder} which can be used to build a {@link Scan}
     *
     * @param caseInsensitiveStringMap The options for reading, which is an immutable
     *     case-insensitive string-to-string map.
     */
    @Override
    public ScanBuilder newScanBuilder(CaseInsensitiveStringMap caseInsensitiveStringMap) {
        int parallelism =
                Integer.parseInt(properties.getOrDefault(CommonOptions.PARALLELISM.key(), "1"));
        return new SeaTunnelScanBuilder(source, parallelism, caseInsensitiveStringMap);
    }

    /** A name to identify this table */
    @Override
    public String name() {
        return SOURCE_TABLE_NAME;
    }

    /** Returns the schema of this table */
    @Override
    public StructType schema() {
        return (StructType) TypeConverterUtils.convert(source.getProducedType());
    }

    /** Returns the set of capabilities for this table */
    @Override
    public Set<TableCapability> capabilities() {
        return Sets.newHashSet(TableCapability.BATCH_READ, TableCapability.MICRO_BATCH_READ);
    }
}
