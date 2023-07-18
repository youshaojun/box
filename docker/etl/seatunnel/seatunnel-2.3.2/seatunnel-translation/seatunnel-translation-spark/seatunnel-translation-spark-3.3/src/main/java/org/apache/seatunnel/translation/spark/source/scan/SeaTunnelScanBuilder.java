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

package org.apache.seatunnel.translation.spark.source.scan;

import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;

import org.apache.spark.sql.connector.read.Scan;
import org.apache.spark.sql.connector.read.ScanBuilder;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

/** The builder for {@link SeaTunnelScan} used to build {@link SeaTunnelScan} */
public class SeaTunnelScanBuilder implements ScanBuilder {
    private final SeaTunnelSource<SeaTunnelRow, ?, ?> source;

    private final int parallelism;

    private final CaseInsensitiveStringMap caseInsensitiveStringMap;

    public SeaTunnelScanBuilder(
            SeaTunnelSource<SeaTunnelRow, ?, ?> source,
            int parallelism,
            CaseInsensitiveStringMap caseInsensitiveStringMap) {
        this.source = source;
        this.parallelism = parallelism;
        this.caseInsensitiveStringMap = caseInsensitiveStringMap;
    }

    /** Returns the {@link SeaTunnelScan} */
    @Override
    public Scan build() {
        return new SeaTunnelScan(source, parallelism, caseInsensitiveStringMap);
    }
}
