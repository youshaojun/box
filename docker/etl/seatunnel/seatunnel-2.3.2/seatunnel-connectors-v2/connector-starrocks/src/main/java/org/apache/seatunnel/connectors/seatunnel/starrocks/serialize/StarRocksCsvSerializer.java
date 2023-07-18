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

package org.apache.seatunnel.connectors.seatunnel.starrocks.serialize;

import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;

public class StarRocksCsvSerializer extends StarRocksBaseSerializer
        implements StarRocksISerializer {
    private static final long serialVersionUID = 1L;

    private final String columnSeparator;
    private final SeaTunnelRowType seaTunnelRowType;
    private final boolean enableUpsertDelete;

    public StarRocksCsvSerializer(
            String sp, SeaTunnelRowType seaTunnelRowType, boolean enableUpsertDelete) {
        this.columnSeparator = StarRocksDelimiterParser.parse(sp, "\t");
        this.seaTunnelRowType = seaTunnelRowType;
        this.enableUpsertDelete = enableUpsertDelete;
    }

    @Override
    public String serialize(SeaTunnelRow row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.getFields().length; i++) {
            Object value = convert(seaTunnelRowType.getFieldType(i), row.getField(i));
            sb.append(null == value ? "\\N" : value);
            if (i < row.getFields().length - 1) {
                sb.append(columnSeparator);
            }
        }
        if (enableUpsertDelete) {
            sb.append(columnSeparator).append(StarRocksSinkOP.parse(row.getRowKind()).ordinal());
        }
        return sb.toString();
    }
}
