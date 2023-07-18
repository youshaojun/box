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

package org.apache.seatunnel.format.compatible.debezium.json;

import org.apache.seatunnel.common.utils.ReflectionUtils;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.json.JsonConverterConfig;
import org.apache.kafka.connect.source.SourceRecord;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

@RequiredArgsConstructor
public class DebeziumJsonConverter implements Serializable {
    private static final String INCLUDE_SCHEMA_METHOD = "convertToJsonWithEnvelope";
    private static final String EXCLUDE_SCHEMA_METHOD = "convertToJsonWithoutEnvelope";

    private final boolean keySchemaEnable;
    private final boolean valueSchemaEnable;
    private transient JsonConverter keyConverter;
    private transient JsonConverter valueConverter;
    private transient Method keyConverterMethod;
    private transient Method valueConverterMethod;

    public String serializeKey(SourceRecord record)
            throws InvocationTargetException, IllegalAccessException {
        tryInit();
        JsonNode jsonNode =
                (JsonNode)
                        keyConverterMethod.invoke(keyConverter, record.keySchema(), record.key());
        return jsonNode.toString();
    }

    public String serializeValue(SourceRecord record)
            throws InvocationTargetException, IllegalAccessException {
        tryInit();
        JsonNode jsonNode =
                (JsonNode)
                        valueConverterMethod.invoke(
                                valueConverter, record.valueSchema(), record.value());
        return jsonNode.toString();
    }

    private void tryInit() {
        if (keyConverter == null) {
            synchronized (this) {
                if (keyConverter == null) {
                    keyConverter = new JsonConverter();
                    keyConverter.configure(
                            Collections.singletonMap(
                                    JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, keySchemaEnable),
                            true);
                    keyConverterMethod =
                            ReflectionUtils.getDeclaredMethod(
                                            JsonConverter.class,
                                            keySchemaEnable
                                                    ? INCLUDE_SCHEMA_METHOD
                                                    : EXCLUDE_SCHEMA_METHOD,
                                            Schema.class,
                                            Object.class)
                                    .get();
                }
            }
        }
        if (valueConverter == null) {
            synchronized (this) {
                if (valueConverter == null) {
                    valueConverter = new JsonConverter();
                    valueConverter.configure(
                            Collections.singletonMap(
                                    JsonConverterConfig.SCHEMAS_ENABLE_CONFIG, valueSchemaEnable),
                            false);
                    valueConverterMethod =
                            ReflectionUtils.getDeclaredMethod(
                                            JsonConverter.class,
                                            valueSchemaEnable
                                                    ? INCLUDE_SCHEMA_METHOD
                                                    : EXCLUDE_SCHEMA_METHOD,
                                            Schema.class,
                                            Object.class)
                                    .get();
                }
            }
        }
    }
}
