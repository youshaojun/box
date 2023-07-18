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

package org.apache.seatunnel.connectors.seatunnel.starrocks.exception;

import org.apache.seatunnel.common.exception.SeaTunnelErrorCode;

public enum StarRocksConnectorErrorCode implements SeaTunnelErrorCode {
    FLUSH_DATA_FAILED("STARROCKS-01", "Flush batch data to sink connector failed"),
    WRITE_RECORDS_FAILED("STARROCKS-02", "Writing records to StarRocks failed."),
    CLOSE_BE_READER_FAILED("STARROCKS-03", "Close StarRocks BE reader failed"),
    CREATE_BE_READER_FAILED("STARROCKS-04", "Create StarRocks BE reader failed"),
    SCAN_BE_DATA_FAILED("STARROCKS-05", "Scan data from StarRocks BE failed"),
    QUEST_QUERY_PLAN_FAILED("STARROCKS-06", "Request query Plan failed"),
    READER_ARROW_DATA_FAILED("STARROCKS-07", "Read Arrow data failed");

    private final String code;
    private final String description;

    StarRocksConnectorErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
