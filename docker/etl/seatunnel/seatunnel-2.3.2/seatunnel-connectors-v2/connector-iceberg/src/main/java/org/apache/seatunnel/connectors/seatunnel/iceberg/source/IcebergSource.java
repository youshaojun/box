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

package org.apache.seatunnel.connectors.seatunnel.iceberg.source;

import org.apache.seatunnel.shade.com.typesafe.config.Config;

import org.apache.seatunnel.api.common.JobContext;
import org.apache.seatunnel.api.common.PrepareFailException;
import org.apache.seatunnel.api.source.Boundedness;
import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.source.SourceReader;
import org.apache.seatunnel.api.source.SourceSplitEnumerator;
import org.apache.seatunnel.api.source.SupportColumnProjection;
import org.apache.seatunnel.api.source.SupportParallelism;
import org.apache.seatunnel.api.table.catalog.CatalogTableUtil;
import org.apache.seatunnel.api.table.type.SeaTunnelDataType;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.common.config.CheckConfigUtil;
import org.apache.seatunnel.common.config.CheckResult;
import org.apache.seatunnel.common.constants.JobMode;
import org.apache.seatunnel.connectors.seatunnel.iceberg.IcebergTableLoader;
import org.apache.seatunnel.connectors.seatunnel.iceberg.config.SourceConfig;
import org.apache.seatunnel.connectors.seatunnel.iceberg.data.IcebergTypeMapper;
import org.apache.seatunnel.connectors.seatunnel.iceberg.source.enumerator.IcebergBatchSplitEnumerator;
import org.apache.seatunnel.connectors.seatunnel.iceberg.source.enumerator.IcebergSplitEnumeratorState;
import org.apache.seatunnel.connectors.seatunnel.iceberg.source.enumerator.IcebergStreamSplitEnumerator;
import org.apache.seatunnel.connectors.seatunnel.iceberg.source.enumerator.scan.IcebergScanContext;
import org.apache.seatunnel.connectors.seatunnel.iceberg.source.reader.IcebergSourceReader;
import org.apache.seatunnel.connectors.seatunnel.iceberg.source.split.IcebergFileScanTaskSplit;

import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;

import com.google.auto.service.AutoService;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@AutoService(SeaTunnelSource.class)
public class IcebergSource
        implements SeaTunnelSource<
                        SeaTunnelRow, IcebergFileScanTaskSplit, IcebergSplitEnumeratorState>,
                SupportParallelism,
                SupportColumnProjection {

    private static final long serialVersionUID = 4343414808223919870L;

    private SourceConfig sourceConfig;
    private Schema tableSchema;
    private Schema projectedSchema;
    private SeaTunnelRowType seaTunnelRowType;
    private JobContext jobContext;

    @Override
    public String getPluginName() {
        return "Iceberg";
    }

    @Override
    public void prepare(Config pluginConfig) throws PrepareFailException {
        this.sourceConfig = SourceConfig.loadConfig(pluginConfig);
        this.tableSchema = loadIcebergSchema(sourceConfig);
        this.seaTunnelRowType = loadSeaTunnelRowType(tableSchema, pluginConfig);
        this.projectedSchema = tableSchema.select(seaTunnelRowType.getFieldNames());
    }

    @SneakyThrows
    private Schema loadIcebergSchema(SourceConfig sourceConfig) {
        try (IcebergTableLoader icebergTableLoader = IcebergTableLoader.create(sourceConfig)) {
            icebergTableLoader.open();
            return icebergTableLoader.loadTable().schema();
        }
    }

    private SeaTunnelRowType loadSeaTunnelRowType(Schema tableSchema, Config pluginConfig) {
        List<String> columnNames = new ArrayList<>(tableSchema.columns().size());
        List<SeaTunnelDataType<?>> columnDataTypes = new ArrayList<>(tableSchema.columns().size());
        for (Types.NestedField column : tableSchema.columns()) {
            columnNames.add(column.name());
            columnDataTypes.add(IcebergTypeMapper.mapping(column.type()));
        }
        SeaTunnelRowType originalRowType =
                new SeaTunnelRowType(
                        columnNames.toArray(new String[0]),
                        columnDataTypes.toArray(new SeaTunnelDataType[0]));

        CheckResult checkResult =
                CheckConfigUtil.checkAllExists(pluginConfig, CatalogTableUtil.SCHEMA.key());
        if (checkResult.isSuccess()) {
            SeaTunnelRowType projectedRowType =
                    CatalogTableUtil.buildWithConfig(pluginConfig).getSeaTunnelRowType();
            for (int i = 0; i < projectedRowType.getFieldNames().length; i++) {
                String fieldName = projectedRowType.getFieldName(i);
                SeaTunnelDataType<?> projectedFieldType = projectedRowType.getFieldType(i);
                int originalFieldIndex = originalRowType.indexOf(fieldName);
                SeaTunnelDataType<?> originalFieldType =
                        originalRowType.getFieldType(originalFieldIndex);
                checkArgument(
                        projectedFieldType.equals(originalFieldType),
                        String.format(
                                "Illegal field: %s, original: %s <-> projected: %s",
                                fieldName, originalFieldType, projectedFieldType));
            }
            return projectedRowType;
        }
        return originalRowType;
    }

    @Override
    public Boundedness getBoundedness() {
        return JobMode.BATCH.equals(jobContext.getJobMode())
                ? Boundedness.BOUNDED
                : Boundedness.UNBOUNDED;
    }

    @Override
    public void setJobContext(JobContext jobContext) {
        this.jobContext = jobContext;
    }

    @Override
    public SeaTunnelDataType<SeaTunnelRow> getProducedType() {
        return seaTunnelRowType;
    }

    @Override
    public SourceReader<SeaTunnelRow, IcebergFileScanTaskSplit> createReader(
            SourceReader.Context readerContext) {
        return new IcebergSourceReader(
                readerContext, seaTunnelRowType, tableSchema, projectedSchema, sourceConfig);
    }

    @Override
    public SourceSplitEnumerator<IcebergFileScanTaskSplit, IcebergSplitEnumeratorState>
            createEnumerator(
                    SourceSplitEnumerator.Context<IcebergFileScanTaskSplit> enumeratorContext) {
        if (Boundedness.BOUNDED.equals(getBoundedness())) {
            return new IcebergBatchSplitEnumerator(
                    enumeratorContext,
                    IcebergScanContext.scanContext(sourceConfig, projectedSchema),
                    sourceConfig,
                    null);
        }
        return new IcebergStreamSplitEnumerator(
                enumeratorContext,
                IcebergScanContext.streamScanContext(sourceConfig, projectedSchema),
                sourceConfig,
                null);
    }

    @Override
    public SourceSplitEnumerator<IcebergFileScanTaskSplit, IcebergSplitEnumeratorState>
            restoreEnumerator(
                    SourceSplitEnumerator.Context<IcebergFileScanTaskSplit> enumeratorContext,
                    IcebergSplitEnumeratorState checkpointState) {
        if (Boundedness.BOUNDED.equals(getBoundedness())) {
            return new IcebergBatchSplitEnumerator(
                    enumeratorContext,
                    IcebergScanContext.scanContext(sourceConfig, projectedSchema),
                    sourceConfig,
                    checkpointState);
        }
        return new IcebergStreamSplitEnumerator(
                enumeratorContext,
                IcebergScanContext.streamScanContext(sourceConfig, projectedSchema),
                sourceConfig,
                checkpointState);
    }
}
