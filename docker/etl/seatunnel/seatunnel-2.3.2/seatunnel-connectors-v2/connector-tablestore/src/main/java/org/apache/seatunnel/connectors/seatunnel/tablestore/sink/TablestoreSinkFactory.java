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

package org.apache.seatunnel.connectors.seatunnel.tablestore.sink;

import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.api.table.catalog.CatalogTableUtil;
import org.apache.seatunnel.api.table.factory.Factory;
import org.apache.seatunnel.api.table.factory.TableSinkFactory;

import com.google.auto.service.AutoService;

import static org.apache.seatunnel.connectors.seatunnel.tablestore.config.TablestoreConfig.ACCESS_KEY_ID;
import static org.apache.seatunnel.connectors.seatunnel.tablestore.config.TablestoreConfig.ACCESS_KEY_SECRET;
import static org.apache.seatunnel.connectors.seatunnel.tablestore.config.TablestoreConfig.BATCH_INTERVAL_MS;
import static org.apache.seatunnel.connectors.seatunnel.tablestore.config.TablestoreConfig.BATCH_SIZE;
import static org.apache.seatunnel.connectors.seatunnel.tablestore.config.TablestoreConfig.END_POINT;
import static org.apache.seatunnel.connectors.seatunnel.tablestore.config.TablestoreConfig.INSTANCE_NAME;
import static org.apache.seatunnel.connectors.seatunnel.tablestore.config.TablestoreConfig.PRIMARY_KEYS;
import static org.apache.seatunnel.connectors.seatunnel.tablestore.config.TablestoreConfig.TABLE;

@AutoService(Factory.class)
public class TablestoreSinkFactory implements TableSinkFactory {
    @Override
    public String factoryIdentifier() {
        return "Tablestore";
    }

    @Override
    public OptionRule optionRule() {
        return OptionRule.builder()
                .required(
                        END_POINT,
                        TABLE,
                        INSTANCE_NAME,
                        ACCESS_KEY_ID,
                        ACCESS_KEY_SECRET,
                        PRIMARY_KEYS,
                        CatalogTableUtil.SCHEMA)
                .optional(BATCH_INTERVAL_MS, BATCH_SIZE)
                .build();
    }
}
