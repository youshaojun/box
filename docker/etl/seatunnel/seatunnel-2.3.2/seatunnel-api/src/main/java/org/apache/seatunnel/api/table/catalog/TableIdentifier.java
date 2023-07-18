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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public final class TableIdentifier implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String catalogName;

    private final String databaseName;

    private final String schemaName;

    private final String tableName;

    public static TableIdentifier of(String catalogName, String databaseName, String tableName) {
        return new TableIdentifier(catalogName, databaseName, null, tableName);
    }

    public static TableIdentifier of(
            String catalogName, String databaseName, String schemaName, String tableName) {
        return new TableIdentifier(catalogName, databaseName, schemaName, tableName);
    }

    public TablePath toTablePath() {
        return TablePath.of(databaseName, schemaName, tableName);
    }

    public TableIdentifier copy() {
        return TableIdentifier.of(catalogName, databaseName, schemaName, tableName);
    }

    @Override
    public String toString() {
        if (schemaName == null) {
            return String.join(".", catalogName, databaseName, tableName);
        }
        return String.join(".", catalogName, databaseName, schemaName, tableName);
    }
}
