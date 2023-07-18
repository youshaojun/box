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

package org.apache.seatunnel.translation.spark.serialization;

import org.apache.seatunnel.api.table.type.ArrayType;
import org.apache.seatunnel.api.table.type.BasicType;
import org.apache.seatunnel.api.table.type.MapType;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.translation.serialization.RowConverter;

import org.apache.spark.unsafe.types.UTF8String;

import scala.Tuple2;
import scala.collection.immutable.HashMap.HashTrieMap;
import scala.collection.mutable.WrappedArray;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SeaTunnelRowConverter extends RowConverter<SeaTunnelRow> {
    public SeaTunnelRowConverter(SeaTunnelDataType<?> dataType) {
        super(dataType);
    }

    @Override
    public SeaTunnelRow convert(SeaTunnelRow seaTunnelRow) throws IOException {
        validate(seaTunnelRow);
        return (SeaTunnelRow) convert(seaTunnelRow, dataType);
    }

    private Object convert(Object field, SeaTunnelDataType<?> dataType) {
        if (field == null) {
            return null;
        }
        switch (dataType.getSqlType()) {
            case ROW:
                SeaTunnelRow seaTunnelRow = (SeaTunnelRow) field;
                SeaTunnelRowType rowType = (SeaTunnelRowType) dataType;
                return convert(seaTunnelRow, rowType);
            case DATE:
                return Date.valueOf((LocalDate) field);
            case TIMESTAMP:
                return Timestamp.valueOf((LocalDateTime) field);
            case TIME:
                return Time.valueOf((LocalTime) field);
            case STRING:
                return field.toString();
            case MAP:
                return convertMap((Map<?, ?>) field, (MapType<?, ?>) dataType);
            case ARRAY:
                // if string array, we need to covert every item in array from String to UTF8String
                if (((ArrayType<?, ?>) dataType).getElementType().equals(BasicType.STRING_TYPE)) {
                    Object[] fields = (Object[]) field;
                    Object[] objects =
                            Arrays.stream(fields)
                                    .map(v -> UTF8String.fromString((String) v))
                                    .toArray();
                    return convertArray(objects, (ArrayType<?, ?>) dataType);
                }
                // except string, now only support convert boolean int tinyint smallint bigint float
                // double, because SeaTunnel Array only support these types
                return convertArray((Object[]) field, (ArrayType<?, ?>) dataType);
            default:
                if (field instanceof scala.Some) {
                    return ((scala.Some<?>) field).get();
                }
                return field;
        }
    }

    private SeaTunnelRow convert(SeaTunnelRow seaTunnelRow, SeaTunnelRowType rowType) {
        int arity = rowType.getTotalFields();
        Object[] values = new Object[arity];
        for (int i = 0; i < arity; i++) {
            Object fieldValue = convert(seaTunnelRow.getField(i), rowType.getFieldType(i));
            if (fieldValue != null) {
                values[i] = fieldValue;
            }
        }
        return new SeaTunnelRow(values);
    }

    private scala.collection.immutable.HashMap<Object, Object> convertMap(
            Map<?, ?> mapData, MapType<?, ?> mapType) {
        scala.collection.immutable.HashMap<Object, Object> newMap =
                new scala.collection.immutable.HashMap<>();
        if (mapData.size() == 0) {
            return newMap;
        }
        int num = mapData.size();
        Object[] keys = mapData.keySet().toArray();
        Object[] values = mapData.values().toArray();
        for (int i = 0; i < num; i++) {
            keys[i] = convert(keys[i], mapType.getKeyType());
            values[i] = convert(values[i], mapType.getValueType());
            Tuple2<Object, Object> tuple2 = new Tuple2<>(keys[i], values[i]);
            newMap = newMap.$plus(tuple2);
        }

        return newMap;
    }

    private WrappedArray.ofRef<?> convertArray(Object[] arrayData, ArrayType<?, ?> arrayType) {
        if (arrayData.length == 0) {
            return new WrappedArray.ofRef<>(new Object[0]);
        }
        int num = arrayData.length;
        for (int i = 0; i < num; i++) {
            arrayData[i] = convert(arrayData[i], arrayType.getElementType());
        }
        return new WrappedArray.ofRef<>(arrayData);
    }

    @Override
    public SeaTunnelRow reconvert(SeaTunnelRow engineRow) throws IOException {
        return (SeaTunnelRow) reconvert(engineRow, dataType);
    }

    private Object reconvert(Object field, SeaTunnelDataType<?> dataType) {
        if (field == null) {
            return null;
        }
        switch (dataType.getSqlType()) {
            case ROW:
                return reconvert((SeaTunnelRow) field, (SeaTunnelRowType) dataType);
            case DATE:
                return ((Date) field).toLocalDate();
            case TIMESTAMP:
                return ((Timestamp) field).toLocalDateTime();
            case TIME:
                return ((Time) field).toLocalTime();
            case STRING:
                return field.toString();
            case MAP:
                return reconvertMap((HashTrieMap<?, ?>) field, (MapType<?, ?>) dataType);
            case ARRAY:
                return reconvertArray((WrappedArray.ofRef<?>) field, (ArrayType<?, ?>) dataType);
            default:
                return field;
        }
    }

    private SeaTunnelRow reconvert(SeaTunnelRow engineRow, SeaTunnelRowType rowType) {
        int num = engineRow.getFields().length;
        Object[] fields = new Object[num];
        for (int i = 0; i < num; i++) {
            fields[i] = reconvert(engineRow.getFields()[i], rowType.getFieldType(i));
        }
        return new SeaTunnelRow(fields);
    }

    /**
     * Convert HashTrieMap to LinkedHashMap
     *
     * @param hashTrieMap HashTrieMap data
     * @param mapType fields type map
     * @return java.util.LinkedHashMap
     * @see HashTrieMap
     */
    private Map<Object, Object> reconvertMap(HashTrieMap<?, ?> hashTrieMap, MapType<?, ?> mapType) {
        if (hashTrieMap == null || hashTrieMap.size() == 0) {
            return Collections.emptyMap();
        }
        int num = hashTrieMap.size();
        Map<Object, Object> newMap = new LinkedHashMap<>(num);
        SeaTunnelDataType<?> keyType = mapType.getKeyType();
        SeaTunnelDataType<?> valueType = mapType.getValueType();
        scala.collection.immutable.List<?> keyList = hashTrieMap.keySet().toList();
        scala.collection.immutable.List<?> valueList = hashTrieMap.values().toList();
        for (int i = 0; i < num; i++) {
            Object key = keyList.apply(i);
            Object value = valueList.apply(i);
            key = reconvert(key, keyType);
            value = reconvert(value, valueType);
            newMap.put(key, value);
        }
        return newMap;
    }

    /**
     * Convert WrappedArray.ofRef to Objects array
     *
     * @param arrayData WrappedArray.ofRef data
     * @param arrayType fields type array
     * @return Objects array
     * @see WrappedArray.ofRef
     */
    private Object reconvertArray(WrappedArray.ofRef<?> arrayData, ArrayType<?, ?> arrayType) {
        if (arrayData == null || arrayData.size() == 0) {
            return Collections.emptyList().toArray();
        }
        Object[] newArray = new Object[arrayData.size()];
        for (int i = 0; i < arrayData.size(); i++) {
            newArray[i] = reconvert(arrayData.apply(i), arrayType.getElementType());
        }
        return newArray;
    }
}
