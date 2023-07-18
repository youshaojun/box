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

package org.apache.seatunnel.core.starter.spark.execution;

import org.apache.seatunnel.shade.com.typesafe.config.Config;

import org.apache.seatunnel.api.common.JobContext;
import org.apache.seatunnel.core.starter.execution.PluginExecuteProcessor;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;
import java.util.Optional;

public abstract class SparkAbstractPluginExecuteProcessor<T>
        implements PluginExecuteProcessor<Dataset<Row>, SparkRuntimeEnvironment> {
    protected SparkRuntimeEnvironment sparkRuntimeEnvironment;
    protected final List<? extends Config> pluginConfigs;
    protected final JobContext jobContext;
    protected final List<T> plugins;
    protected static final String ENGINE_TYPE = "seatunnel";
    protected static final String PLUGIN_NAME = "plugin_name";
    protected static final String RESULT_TABLE_NAME = "result_table_name";
    protected static final String SOURCE_TABLE_NAME = "source_table_name";

    protected SparkAbstractPluginExecuteProcessor(
            SparkRuntimeEnvironment sparkRuntimeEnvironment,
            JobContext jobContext,
            List<? extends Config> pluginConfigs) {
        this.sparkRuntimeEnvironment = sparkRuntimeEnvironment;
        this.jobContext = jobContext;
        this.pluginConfigs = pluginConfigs;
        this.plugins = initializePlugins(pluginConfigs);
    }

    @Override
    public void setRuntimeEnvironment(SparkRuntimeEnvironment sparkRuntimeEnvironment) {
        this.sparkRuntimeEnvironment = sparkRuntimeEnvironment;
    }

    protected abstract List<T> initializePlugins(List<? extends Config> pluginConfigs);

    protected void registerInputTempView(Config pluginConfig, Dataset<Row> dataStream) {
        if (pluginConfig.hasPath(RESULT_TABLE_NAME)) {
            String tableName = pluginConfig.getString(RESULT_TABLE_NAME);
            registerTempView(tableName, dataStream);
        }
    }

    protected Optional<Dataset<Row>> fromSourceTable(
            Config pluginConfig, SparkRuntimeEnvironment sparkRuntimeEnvironment) {
        if (!pluginConfig.hasPath(SOURCE_TABLE_NAME)) {
            return Optional.empty();
        }
        String sourceTableName = pluginConfig.getString(SOURCE_TABLE_NAME);
        return Optional.of(sparkRuntimeEnvironment.getSparkSession().read().table(sourceTableName));
    }

    private void registerTempView(String tableName, Dataset<Row> ds) {
        ds.createOrReplaceTempView(tableName);
    }
}
