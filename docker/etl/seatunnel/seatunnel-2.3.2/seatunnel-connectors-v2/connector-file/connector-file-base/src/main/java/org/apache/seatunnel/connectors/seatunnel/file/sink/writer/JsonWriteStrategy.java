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

package org.apache.seatunnel.connectors.seatunnel.file.sink.writer;

import org.apache.seatunnel.api.serialization.SerializationSchema;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.common.exception.CommonErrorCode;
import org.apache.seatunnel.connectors.seatunnel.file.exception.FileConnectorException;
import org.apache.seatunnel.connectors.seatunnel.file.sink.config.FileSinkConfig;
import org.apache.seatunnel.format.json.JsonSerializationSchema;

import org.apache.hadoop.fs.FSDataOutputStream;

import io.airlift.compress.lzo.LzopCodec;
import lombok.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class JsonWriteStrategy extends AbstractWriteStrategy {
    private final byte[] rowDelimiter;
    private SerializationSchema serializationSchema;
    private final Map<String, FSDataOutputStream> beingWrittenOutputStream;
    private final Map<String, Boolean> isFirstWrite;

    public JsonWriteStrategy(FileSinkConfig textFileSinkConfig) {
        super(textFileSinkConfig);
        this.beingWrittenOutputStream = new HashMap<>();
        this.isFirstWrite = new HashMap<>();
        this.rowDelimiter = textFileSinkConfig.getRowDelimiter().getBytes();
    }

    @Override
    public void setSeaTunnelRowTypeInfo(SeaTunnelRowType seaTunnelRowType) {
        super.setSeaTunnelRowTypeInfo(seaTunnelRowType);
        this.serializationSchema =
                new JsonSerializationSchema(
                        buildSchemaWithRowType(seaTunnelRowType, sinkColumnsIndexInRow));
    }

    @Override
    public void write(@NonNull SeaTunnelRow seaTunnelRow) {
        super.write(seaTunnelRow);
        String filePath = getOrCreateFilePathBeingWritten(seaTunnelRow);
        FSDataOutputStream fsDataOutputStream = getOrCreateOutputStream(filePath);
        try {
            byte[] rowBytes =
                    serializationSchema.serialize(
                            seaTunnelRow.copy(
                                    sinkColumnsIndexInRow.stream()
                                            .mapToInt(Integer::intValue)
                                            .toArray()));
            if (isFirstWrite.get(filePath)) {
                isFirstWrite.put(filePath, false);
            } else {
                fsDataOutputStream.write(rowDelimiter);
            }
            fsDataOutputStream.write(rowBytes);
        } catch (IOException e) {
            throw new FileConnectorException(
                    CommonErrorCode.FILE_OPERATION_FAILED,
                    String.format("Write data to file [%s] failed", filePath),
                    e);
        }
    }

    @Override
    public void finishAndCloseFile() {
        beingWrittenOutputStream.forEach(
                (key, value) -> {
                    try {
                        value.flush();
                    } catch (IOException e) {
                        throw new FileConnectorException(
                                CommonErrorCode.FLUSH_DATA_FAILED,
                                String.format("Flush data to this file [%s] failed", key),
                                e);
                    } finally {
                        try {
                            value.close();
                        } catch (IOException e) {
                            log.warn("Close file output stream {} failed", key, e);
                        }
                    }
                    needMoveFiles.put(key, getTargetLocation(key));
                });
    }

    private FSDataOutputStream getOrCreateOutputStream(@NonNull String filePath) {
        FSDataOutputStream fsDataOutputStream = beingWrittenOutputStream.get(filePath);
        if (fsDataOutputStream == null) {
            try {
                switch (compressFormat) {
                    case LZO:
                        LzopCodec lzo = new LzopCodec();
                        OutputStream out =
                                lzo.createOutputStream(fileSystemUtils.getOutputStream(filePath));
                        fsDataOutputStream = new FSDataOutputStream(out, null);
                        break;
                    case NONE:
                        fsDataOutputStream = fileSystemUtils.getOutputStream(filePath);
                        break;
                    default:
                        log.warn(
                                "Json file does not support this compress type: {}",
                                compressFormat.getCompressCodec());
                        fsDataOutputStream = fileSystemUtils.getOutputStream(filePath);
                        break;
                }
                beingWrittenOutputStream.put(filePath, fsDataOutputStream);
                isFirstWrite.put(filePath, true);
            } catch (IOException e) {
                throw new FileConnectorException(
                        CommonErrorCode.FILE_OPERATION_FAILED,
                        String.format("Open file output stream [%s] failed", filePath),
                        e);
            }
        }
        return fsDataOutputStream;
    }
}
