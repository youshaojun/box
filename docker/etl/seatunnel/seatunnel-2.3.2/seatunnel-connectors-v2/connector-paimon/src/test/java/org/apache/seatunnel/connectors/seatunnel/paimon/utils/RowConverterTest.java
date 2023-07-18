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

package org.apache.seatunnel.connectors.seatunnel.paimon.utils;

import org.apache.seatunnel.api.table.type.ArrayType;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.DecimalType;
import org.apache.seatunnel.api.table.type.LocalTimeType;
import org.apache.seatunnel.api.table.type.MapType;
import org.apache.seatunnel.api.table.type.PrimitiveByteArrayType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;

import org.apache.paimon.data.BinaryArray;
import org.apache.paimon.data.BinaryArrayWriter;
import org.apache.paimon.data.BinaryMap;
import org.apache.paimon.data.BinaryRow;
import org.apache.paimon.data.BinaryRowWriter;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.Decimal;
import org.apache.paimon.data.InternalRow;
import org.apache.paimon.data.Timestamp;
import org.apache.paimon.data.serializer.InternalArraySerializer;
import org.apache.paimon.data.serializer.InternalMapSerializer;
import org.apache.paimon.types.DataTypes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/** Unit tests for {@link RowConverter} */
public class RowConverterTest {

    private SeaTunnelRow seaTunnelRow;

    private InternalRow internalRow;

    private SeaTunnelRowType seaTunnelRowType;

    @BeforeEach
    public void before() {
        seaTunnelRowType =
                new SeaTunnelRowType(
                        new String[] {
                            "c_tinyint",
                            "c_smallint",
                            "c_int",
                            "c_bigint",
                            "c_float",
                            "c_double",
                            "c_decimal",
                            "c_string",
                            "c_bytes",
                            "c_boolean",
                            "c_date",
                            "c_timestamp",
                            "c_map",
                            "c_array"
                        },
                        new SeaTunnelDataType<?>[] {
                            BasicType.BYTE_TYPE,
                            BasicType.SHORT_TYPE,
                            BasicType.INT_TYPE,
                            BasicType.LONG_TYPE,
                            BasicType.FLOAT_TYPE,
                            BasicType.DOUBLE_TYPE,
                            new DecimalType(30, 8),
                            BasicType.STRING_TYPE,
                            PrimitiveByteArrayType.INSTANCE,
                            BasicType.BOOLEAN_TYPE,
                            LocalTimeType.LOCAL_DATE_TYPE,
                            LocalTimeType.LOCAL_DATE_TIME_TYPE,
                            new MapType<>(BasicType.STRING_TYPE, BasicType.STRING_TYPE),
                            ArrayType.STRING_ARRAY_TYPE
                        });
        byte tinyint = 1;
        short smallint = 2;
        int intNum = 3;
        long bigint = 4L;
        float floatNum = 5.0f;
        double doubleNum = 6.789;
        BigDecimal decimal = new BigDecimal("123456789.00000000");
        String string = "paimon";
        byte[] bytes = new byte[] {1, 2, 3, 4};
        boolean booleanValue = false;
        LocalDate date = LocalDate.of(1996, 3, 16);
        LocalDateTime timestamp = LocalDateTime.of(1996, 3, 16, 4, 16, 20);
        Map<String, String> map = new HashMap<>();
        map.put("name", "paimon");
        String[] strings = new String[] {"paimon", "seatunnel"};
        Object[] objects = new Object[14];
        objects[0] = tinyint;
        objects[1] = smallint;
        objects[2] = intNum;
        objects[3] = bigint;
        objects[4] = floatNum;
        objects[5] = doubleNum;
        objects[6] = decimal;
        objects[7] = string;
        objects[8] = bytes;
        objects[9] = booleanValue;
        objects[10] = date;
        objects[11] = timestamp;
        objects[12] = map;
        objects[13] = strings;
        seaTunnelRow = new SeaTunnelRow(objects);
        BinaryRow binaryRow = new BinaryRow(14);
        BinaryRowWriter binaryRowWriter = new BinaryRowWriter(binaryRow);
        binaryRowWriter.writeByte(0, tinyint);
        binaryRowWriter.writeShort(1, smallint);
        binaryRowWriter.writeInt(2, intNum);
        binaryRowWriter.writeLong(3, bigint);
        binaryRowWriter.writeFloat(4, floatNum);
        binaryRowWriter.writeDouble(5, doubleNum);
        binaryRowWriter.writeDecimal(6, Decimal.fromBigDecimal(decimal, 30, 8), 30);
        binaryRowWriter.writeString(7, BinaryString.fromString(string));
        binaryRowWriter.writeBinary(8, bytes);
        binaryRowWriter.writeBoolean(9, booleanValue);
        binaryRowWriter.writeTimestamp(
                10, Timestamp.fromLocalDateTime(LocalDateTime.of(date, LocalTime.of(0, 0, 0))), 3);
        binaryRowWriter.writeTimestamp(11, Timestamp.fromLocalDateTime(timestamp), 6);
        BinaryArray binaryArray = new BinaryArray();
        BinaryArrayWriter binaryArrayWriter =
                new BinaryArrayWriter(
                        binaryArray, 1, BinaryArray.calculateFixLengthPartSize(DataTypes.STRING()));
        binaryArrayWriter.writeString(0, BinaryString.fromString("name"));
        binaryArrayWriter.complete();
        BinaryArray binaryArray1 = new BinaryArray();
        BinaryArrayWriter binaryArrayWriter1 =
                new BinaryArrayWriter(
                        binaryArray1,
                        1,
                        BinaryArray.calculateFixLengthPartSize(DataTypes.STRING()));
        binaryArrayWriter1.writeString(0, BinaryString.fromString("paimon"));
        binaryArrayWriter1.complete();
        BinaryMap binaryMap = BinaryMap.valueOf(binaryArray, binaryArray1);
        binaryRowWriter.writeMap(
                12, binaryMap, new InternalMapSerializer(DataTypes.STRING(), DataTypes.STRING()));
        BinaryArray binaryArray2 = new BinaryArray();
        BinaryArrayWriter binaryArrayWriter2 =
                new BinaryArrayWriter(
                        binaryArray2,
                        2,
                        BinaryArray.calculateFixLengthPartSize(DataTypes.STRING()));
        binaryArrayWriter2.writeString(0, BinaryString.fromString("paimon"));
        binaryArrayWriter2.writeString(1, BinaryString.fromString("seatunnel"));
        binaryArrayWriter2.complete();
        binaryRowWriter.writeArray(
                13, binaryArray2, new InternalArraySerializer(DataTypes.STRING()));
        internalRow = binaryRow;
    }

    @Test
    public void seaTunnelToPaimon() {
        InternalRow convert = RowConverter.convert(seaTunnelRow, seaTunnelRowType);
        Assertions.assertEquals(convert, internalRow);
    }

    @Test
    public void paimonToSeaTunnel() {
        SeaTunnelRow convert = RowConverter.convert(internalRow, seaTunnelRowType);
        Assertions.assertEquals(convert, seaTunnelRow);
    }
}
