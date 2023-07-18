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

package org.apache.seatunnel.api.table.catalog;

import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.common.exception.CommonErrorCode;
import org.apache.seatunnel.common.exception.SeaTunnelRuntimeException;

public class DataTypeConvertException extends SeaTunnelRuntimeException {
    private static final String CONVERT_TO_SEA_TUNNEL_ERROR_MSG =
            "Convert type: %s to SeaTunnel data type error.";

    private static final String CONVERT_TO_CONNECTOR_DATA_TYPE_ERROR_MSG =
            "Convert SeaTunnel data type: %s to connector data type error.";

    public DataTypeConvertException(String message) {
        this(message, null);
    }

    public DataTypeConvertException(String message, Throwable cause) {
        super(CommonErrorCode.UNSUPPORTED_DATA_TYPE, message, cause);
    }

    public static DataTypeConvertException convertToSeaTunnelDataTypeException(Object dataType) {
        return new DataTypeConvertException(
                String.format(CONVERT_TO_SEA_TUNNEL_ERROR_MSG, dataType));
    }

    public static DataTypeConvertException convertToSeaTunnelDataTypeException(
            Object dataType, Throwable cause) {
        return new DataTypeConvertException(
                String.format(CONVERT_TO_SEA_TUNNEL_ERROR_MSG, dataType), cause);
    }

    public static DataTypeConvertException convertToConnectorDataTypeException(
            SeaTunnelDataType<?> seaTunnelDataType) {
        return new DataTypeConvertException(
                String.format(CONVERT_TO_CONNECTOR_DATA_TYPE_ERROR_MSG, seaTunnelDataType));
    }

    public static DataTypeConvertException convertToConnectorDataTypeException(
            SeaTunnelDataType<?> seaTunnelDataType, Throwable cause) {
        return new DataTypeConvertException(
                String.format(CONVERT_TO_CONNECTOR_DATA_TYPE_ERROR_MSG, seaTunnelDataType), cause);
    }
}
