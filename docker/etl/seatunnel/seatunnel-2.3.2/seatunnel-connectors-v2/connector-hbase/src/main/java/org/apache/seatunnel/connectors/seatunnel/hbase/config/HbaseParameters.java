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

package org.apache.seatunnel.connectors.seatunnel.hbase.config;

import org.apache.seatunnel.shade.com.typesafe.config.Config;

import org.apache.seatunnel.common.config.TypesafeConfigUtils;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.ENCODING;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.FAMILY_NAME;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.HBASE_EXTRA_CONFIG;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.NULL_MODE;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.ROWKEY_COLUMNS;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.ROWKEY_DELIMITER;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.TABLE;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.VERSION_COLUMN;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.WAL_WRITE;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.WRITE_BUFFER_SIZE;
import static org.apache.seatunnel.connectors.seatunnel.hbase.config.HbaseConfig.ZOOKEEPER_QUORUM;

@Builder
@Getter
public class HbaseParameters implements Serializable {

    private String zookeeperQuorum;

    private String table;

    private List<String> rowkeyColumns;

    private Map<String, String> familyNames;

    private String versionColumn;

    private Map<String, String> hbaseExtraConfig;

    @Builder.Default private String rowkeyDelimiter = ROWKEY_DELIMITER.defaultValue();

    @Builder.Default private HbaseConfig.NullMode nullMode = NULL_MODE.defaultValue();

    @Builder.Default private boolean walWrite = WAL_WRITE.defaultValue();

    @Builder.Default private int writeBufferSize = WRITE_BUFFER_SIZE.defaultValue();

    @Builder.Default private HbaseConfig.EnCoding enCoding = ENCODING.defaultValue();

    public static HbaseParameters buildWithConfig(Config pluginConfig) {
        HbaseParametersBuilder builder = HbaseParameters.builder();

        // required parameters
        builder.zookeeperQuorum(pluginConfig.getString(ZOOKEEPER_QUORUM.key()));
        builder.table(pluginConfig.getString(TABLE.key()));
        builder.rowkeyColumns(pluginConfig.getStringList(ROWKEY_COLUMNS.key()));
        builder.familyNames(
                TypesafeConfigUtils.configToMap(pluginConfig.getConfig(FAMILY_NAME.key())));

        // optional parameters
        if (pluginConfig.hasPath(ROWKEY_DELIMITER.key())) {
            builder.rowkeyDelimiter(pluginConfig.getString(ROWKEY_DELIMITER.key()));
        }
        if (pluginConfig.hasPath(VERSION_COLUMN.key())) {
            builder.versionColumn(pluginConfig.getString(VERSION_COLUMN.key()));
        }
        if (pluginConfig.hasPath(NULL_MODE.key())) {
            String nullMode = pluginConfig.getString(NULL_MODE.key());
            builder.nullMode(HbaseConfig.NullMode.valueOf(nullMode.toUpperCase()));
        }
        if (pluginConfig.hasPath(WAL_WRITE.key())) {
            builder.walWrite(pluginConfig.getBoolean(WAL_WRITE.key()));
        }
        if (pluginConfig.hasPath(WRITE_BUFFER_SIZE.key())) {
            builder.writeBufferSize(pluginConfig.getInt(WRITE_BUFFER_SIZE.key()));
        }
        if (pluginConfig.hasPath(ENCODING.key())) {
            String encoding = pluginConfig.getString(ENCODING.key());
            builder.enCoding(HbaseConfig.EnCoding.valueOf(encoding.toUpperCase()));
        }
        if (pluginConfig.hasPath(HBASE_EXTRA_CONFIG.key())) {
            Config extraConfig = pluginConfig.getConfig(HBASE_EXTRA_CONFIG.key());
            builder.hbaseExtraConfig(TypesafeConfigUtils.configToMap(extraConfig));
        }
        return builder.build();
    }
}
