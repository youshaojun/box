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

package org.apache.seatunnel.connectors.seatunnel.hudi.source;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.table.factory.TableSourceFactory;
import org.apache.seatunnel.connectors.seatunnel.hudi.config.HudiSourceConfig;

import com.google.auto.service.AutoService;

@AutoService(SeaTunnelSource.class)
public class HudiSourceFactory implements TableSourceFactory {

    @Override
    public String factoryIdentifier() {
        return "Hudi";
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder()
                .required(
                        HudiSourceConfig.TABLE_PATH,
                        HudiSourceConfig.TABLE_TYPE,
                        HudiSourceConfig.CONF_FILES)
                .optional(HudiSourceConfig.USE_KERBEROS)
                .conditional(
                        HudiSourceConfig.USE_KERBEROS,
                        true,
                        HudiSourceConfig.KERBEROS_PRINCIPAL,
                        HudiSourceConfig.KERBEROS_PRINCIPAL_FILE)
                .build();
    }

    @Override
    public Class<? extends SeaTunnelSource> getSourceClass() {
        return HudiSource.class;
    }
}
