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

package org.apache.seatunnel.connectors.seatunnel.kafka.serialize;

import org.apache.seatunnel.api.serialization.SerializationSchema;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.common.exception.CommonErrorCode;
import org.apache.seatunnel.connectors.seatunnel.kafka.config.MessageFormat;
import org.apache.seatunnel.connectors.seatunnel.kafka.exception.KafkaConnectorException;
import org.apache.seatunnel.format.compatible.debezium.json.CompatibleDebeziumJsonDeserializationSchema;
import org.apache.seatunnel.format.compatible.debezium.json.CompatibleDebeziumJsonSerializationSchema;
import org.apache.seatunnel.format.json.JsonSerializationSchema;
import org.apache.seatunnel.format.json.canal.CanalJsonSerializationSchema;
import org.apache.seatunnel.format.json.exception.SeaTunnelJsonFormatException;
import org.apache.seatunnel.format.text.TextSerializationSchema;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class DefaultSeaTunnelRowSerializer implements SeaTunnelRowSerializer {
    private final Function<SeaTunnelRow, String> topicExtractor;
    private final Function<SeaTunnelRow, Integer> partitionExtractor;
    private final Function<SeaTunnelRow, Long> timestampExtractor;
    private final Function<SeaTunnelRow, byte[]> keyExtractor;
    private final Function<SeaTunnelRow, byte[]> valueExtractor;
    private final Function<SeaTunnelRow, Iterable<Header>> headersExtractor;

    @Override
    public ProducerRecord serializeRow(SeaTunnelRow row) {
        return new ProducerRecord(
                topicExtractor.apply(row),
                partitionExtractor.apply(row),
                timestampExtractor.apply(row),
                keyExtractor.apply(row),
                valueExtractor.apply(row),
                headersExtractor.apply(row));
    }

    public static DefaultSeaTunnelRowSerializer create(
            String topic, SeaTunnelRowType rowType, MessageFormat format, String delimiter) {
        return new DefaultSeaTunnelRowSerializer(
                topicExtractor(topic, rowType, format),
                partitionExtractor(null),
                timestampExtractor(),
                keyExtractor(null, rowType, format, delimiter),
                valueExtractor(rowType, format, delimiter),
                headersExtractor());
    }

    public static DefaultSeaTunnelRowSerializer create(
            String topic,
            Integer partition,
            SeaTunnelRowType rowType,
            MessageFormat format,
            String delimiter) {
        return new DefaultSeaTunnelRowSerializer(
                topicExtractor(topic, rowType, format),
                partitionExtractor(partition),
                timestampExtractor(),
                keyExtractor(null, rowType, format, delimiter),
                valueExtractor(rowType, format, delimiter),
                headersExtractor());
    }

    public static DefaultSeaTunnelRowSerializer create(
            String topic,
            List<String> keyFields,
            SeaTunnelRowType rowType,
            MessageFormat format,
            String delimiter) {
        return new DefaultSeaTunnelRowSerializer(
                topicExtractor(topic, rowType, format),
                partitionExtractor(null),
                timestampExtractor(),
                keyExtractor(keyFields, rowType, format, delimiter),
                valueExtractor(rowType, format, delimiter),
                headersExtractor());
    }

    private static Function<SeaTunnelRow, Integer> partitionExtractor(Integer partition) {
        return row -> partition;
    }

    private static Function<SeaTunnelRow, Long> timestampExtractor() {
        return row -> null;
    }

    private static Function<SeaTunnelRow, Iterable<Header>> headersExtractor() {
        return row -> null;
    }

    private static Function<SeaTunnelRow, String> topicExtractor(
            String topic, SeaTunnelRowType rowType, MessageFormat format) {
        if (MessageFormat.COMPATIBLE_DEBEZIUM_JSON.equals(format)) {
            int topicFieldIndex =
                    rowType.indexOf(CompatibleDebeziumJsonDeserializationSchema.FIELD_TOPIC);
            return row -> row.getField(topicFieldIndex).toString();
        }

        String regex = "\\$\\{(.*?)\\}";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(topic);
        boolean isExtractTopic = matcher.find();
        if (!isExtractTopic) {
            return row -> topic;
        }

        String topicField = matcher.group(1);
        List<String> fieldNames = Arrays.asList(rowType.getFieldNames());
        if (!fieldNames.contains(topicField)) {
            throw new KafkaConnectorException(
                    CommonErrorCode.ILLEGAL_ARGUMENT,
                    String.format("Field name { %s } is not found!", topic));
        }
        int topicFieldIndex = rowType.indexOf(topicField);
        return row -> {
            Object topicFieldValue = row.getField(topicFieldIndex);
            if (topicFieldValue == null) {
                throw new KafkaConnectorException(
                        CommonErrorCode.ILLEGAL_ARGUMENT, "The column value is empty!");
            }
            return topicFieldValue.toString();
        };
    }

    private static Function<SeaTunnelRow, byte[]> keyExtractor(
            List<String> keyFields,
            SeaTunnelRowType rowType,
            MessageFormat format,
            String delimiter) {
        if (MessageFormat.COMPATIBLE_DEBEZIUM_JSON.equals(format)) {
            CompatibleDebeziumJsonSerializationSchema serializationSchema =
                    new CompatibleDebeziumJsonSerializationSchema(rowType, true);
            return row -> serializationSchema.serialize(row);
        }

        if (keyFields == null || keyFields.isEmpty()) {
            return row -> null;
        }

        SeaTunnelRowType keyType = createKeyType(keyFields, rowType);
        Function<SeaTunnelRow, SeaTunnelRow> keyRowExtractor =
                createKeyRowExtractor(keyType, rowType);
        SerializationSchema serializationSchema =
                createSerializationSchema(keyType, format, delimiter, true);
        return row -> serializationSchema.serialize(keyRowExtractor.apply(row));
    }

    private static Function<SeaTunnelRow, byte[]> valueExtractor(
            SeaTunnelRowType rowType, MessageFormat format, String delimiter) {
        SerializationSchema serializationSchema =
                createSerializationSchema(rowType, format, delimiter, false);
        return row -> serializationSchema.serialize(row);
    }

    private static SeaTunnelRowType createKeyType(
            List<String> keyFieldNames, SeaTunnelRowType rowType) {
        int[] keyFieldIndexArr = new int[keyFieldNames.size()];
        SeaTunnelDataType[] keyFieldDataTypeArr = new SeaTunnelDataType[keyFieldNames.size()];
        for (int i = 0; i < keyFieldNames.size(); i++) {
            String keyFieldName = keyFieldNames.get(i);
            int rowFieldIndex = rowType.indexOf(keyFieldName);
            keyFieldIndexArr[i] = rowFieldIndex;
            keyFieldDataTypeArr[i] = rowType.getFieldType(rowFieldIndex);
        }
        return new SeaTunnelRowType(keyFieldNames.toArray(new String[0]), keyFieldDataTypeArr);
    }

    private static Function<SeaTunnelRow, SeaTunnelRow> createKeyRowExtractor(
            SeaTunnelRowType keyType, SeaTunnelRowType rowType) {
        int[] keyIndex = new int[keyType.getTotalFields()];
        for (int i = 0; i < keyType.getTotalFields(); i++) {
            keyIndex[i] = rowType.indexOf(keyType.getFieldName(i));
        }
        return row -> {
            Object[] fields = new Object[keyType.getTotalFields()];
            for (int i = 0; i < keyIndex.length; i++) {
                fields[i] = row.getField(keyIndex[i]);
            }
            return new SeaTunnelRow(fields);
        };
    }

    private static SerializationSchema createSerializationSchema(
            SeaTunnelRowType rowType, MessageFormat format, String delimiter, boolean isKey) {
        switch (format) {
            case JSON:
                return new JsonSerializationSchema(rowType);
            case TEXT:
                return TextSerializationSchema.builder()
                        .seaTunnelRowType(rowType)
                        .delimiter(delimiter)
                        .build();
            case CANAL_JSON:
                return new CanalJsonSerializationSchema(rowType);
            case COMPATIBLE_DEBEZIUM_JSON:
                return new CompatibleDebeziumJsonSerializationSchema(rowType, isKey);
            default:
                throw new SeaTunnelJsonFormatException(
                        CommonErrorCode.UNSUPPORTED_DATA_TYPE, "Unsupported format: " + format);
        }
    }
}
