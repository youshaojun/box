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

package org.apache.seatunnel.api.table.catalog;

import org.apache.seatunnel.api.common.CommonOptions;
import org.apache.seatunnel.api.configuration.Option;
import org.apache.seatunnel.api.configuration.Options;
import org.apache.seatunnel.api.configuration.util.OptionRule;

import java.util.List;
import java.util.Map;

public interface CatalogOptions {
    Option<Map<String, String>> CATALOG_OPTIONS =
            Options.key("catalog")
                    .mapType()
                    .noDefaultValue()
                    .withDescription("configuration options for the catalog.");

    Option<String> NAME =
            Options.key("name").stringType().noDefaultValue().withDescription("catalog name");

    Option<List<String>> TABLE_NAMES =
            Options.key("table-names")
                    .listType()
                    .noDefaultValue()
                    .withDescription(
                            "List of table names of databases to capture."
                                    + "The table name needs to include the database name, for example: database_name.table_name");

    Option<String> DATABASE_PATTERN =
            Options.key("database-pattern")
                    .stringType()
                    .defaultValue(".*")
                    .withDescription("The database names RegEx of the database to capture.");

    Option<String> TABLE_PATTERN =
            Options.key("table-pattern")
                    .stringType()
                    .noDefaultValue()
                    .withDescription(
                            "The table names RegEx of the database to capture."
                                    + "The table name needs to include the database name, for example: database_.*\\.table_.*");

    OptionRule.Builder BASE_RULE =
            OptionRule.builder()
                    .optional(CommonOptions.FACTORY_ID)
                    .optional(NAME)
                    .optional(DATABASE_PATTERN)
                    .exclusive(TABLE_PATTERN, TABLE_NAMES);
}
