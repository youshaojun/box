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

import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.common.exception.CommonErrorCode;
import org.apache.seatunnel.connectors.seatunnel.file.exception.FileConnectorException;
import org.apache.seatunnel.connectors.seatunnel.file.sink.config.FileSinkConfig;
import org.apache.seatunnel.connectors.seatunnel.file.sink.util.ExcelGenerator;

import org.apache.hadoop.fs.FSDataOutputStream;

import lombok.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelWriteStrategy extends AbstractWriteStrategy {
    private final Map<String, ExcelGenerator> beingWrittenWriter;

    public ExcelWriteStrategy(FileSinkConfig fileSinkConfig) {
        super(fileSinkConfig);
        this.beingWrittenWriter = new HashMap<>();
    }

    @Override
    public void write(SeaTunnelRow seaTunnelRow) {
        super.write(seaTunnelRow);
        String filePath = getOrCreateFilePathBeingWritten(seaTunnelRow);
        ExcelGenerator excelGenerator = getOrCreateExcelGenerator(filePath);
        excelGenerator.writeData(seaTunnelRow);
    }

    @Override
    public void finishAndCloseFile() {
        this.beingWrittenWriter.forEach(
                (k, v) -> {
                    try {
                        fileSystemUtils.createFile(k);
                        FSDataOutputStream fileOutputStream = fileSystemUtils.getOutputStream(k);
                        v.flushAndCloseExcel(fileOutputStream);
                        fileOutputStream.close();
                    } catch (IOException e) {
                        throw new FileConnectorException(
                                CommonErrorCode.FILE_OPERATION_FAILED,
                                "can not get output file stream");
                    }
                    needMoveFiles.put(k, getTargetLocation(k));
                });
    }

    private ExcelGenerator getOrCreateExcelGenerator(@NonNull String filePath) {
        ExcelGenerator excelGenerator = this.beingWrittenWriter.get(filePath);
        if (excelGenerator == null) {
            excelGenerator =
                    new ExcelGenerator(sinkColumnsIndexInRow, seaTunnelRowType, fileSinkConfig);
            this.beingWrittenWriter.put(filePath, excelGenerator);
        }
        return excelGenerator;
    }
}
