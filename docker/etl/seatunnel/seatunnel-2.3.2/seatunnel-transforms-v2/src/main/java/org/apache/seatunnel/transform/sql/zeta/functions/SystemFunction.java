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

package org.apache.seatunnel.transform.sql.zeta.functions;

import org.apache.seatunnel.common.exception.CommonErrorCode;
import org.apache.seatunnel.transform.exception.TransformException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class SystemFunction {
    public static Object coalesce(List<Object> args) {
        Object v = null;
        for (Object v2 : args) {
            if (v2 != null) {
                v = v2;
                break;
            }
        }
        return v;
    }

    public static Object ifnull(List<Object> args) {
        if (args.size() != 2) {
            throw new TransformException(
                    CommonErrorCode.UNSUPPORTED_OPERATION,
                    String.format("Unsupported function IFNULL() arguments: %s", args));
        }
        return coalesce(args);
    }

    public static Object nullif(List<Object> args) {
        Object v1 = args.get(0);
        Object v2 = args.get(1);
        if (v1 == null) {
            return null;
        }
        if (v1.equals(v2)) {
            return null;
        }
        return v1;
    }

    public static Object castAs(List<Object> args) {
        Object v1 = args.get(0);
        String v2 = (String) args.get(1);
        if (v1 == null) {
            return null;
        }
        if (v1.equals(v2)) {
            return null;
        }
        switch (v2) {
            case "VARCHAR":
            case "STRING":
                return v1.toString();
            case "INT":
            case "INTEGER":
                return Integer.parseInt(v1.toString());
            case "BIGINT":
            case "LONG":
                return Long.parseLong(v1.toString());
            case "BYTE":
                return Byte.parseByte(v1.toString());
            case "DOUBLE":
                return Double.parseDouble(v1.toString());
            case "FLOAT":
                return Float.parseFloat(v1.toString());
            case "TIMESTAMP":
            case "DATETIME":
                if (v1 instanceof LocalDateTime) {
                    return v1;
                }
                if (v1 instanceof LocalDate) {
                    return LocalDateTime.of((LocalDate) v1, LocalTime.of(0, 0, 0));
                }
                if (v1 instanceof LocalTime) {
                    return LocalDateTime.of(LocalDate.now(), (LocalTime) v1);
                }
            case "DATE":
                if (v1 instanceof LocalDateTime) {
                    return ((LocalDateTime) v1).toLocalDate();
                }
                if (v1 instanceof LocalDate) {
                    return v1;
                }
            case "TIME":
                if (v1 instanceof LocalDateTime) {
                    return ((LocalDateTime) v1).toLocalTime();
                }
                if (v1 instanceof LocalDate) {
                    return LocalDateTime.of((LocalDate) v1, LocalTime.of(0, 0, 0));
                }
                if (v1 instanceof LocalTime) {
                    return LocalDateTime.of(LocalDate.now(), (LocalTime) v1);
                }
            case "DECIMAL":
                BigDecimal bigDecimal = new BigDecimal(v1.toString());
                Integer scale = (Integer) args.get(3);
                return bigDecimal.setScale(scale, RoundingMode.CEILING);
        }
        throw new TransformException(
                CommonErrorCode.UNSUPPORTED_OPERATION,
                String.format("Unsupported CAST AS type: %s", v2));
    }
}
