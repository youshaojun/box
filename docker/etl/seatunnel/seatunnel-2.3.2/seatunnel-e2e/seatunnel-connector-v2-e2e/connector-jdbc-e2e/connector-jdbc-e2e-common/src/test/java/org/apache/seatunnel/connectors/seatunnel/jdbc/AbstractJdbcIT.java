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

package org.apache.seatunnel.connectors.seatunnel.jdbc;

import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.common.exception.SeaTunnelRuntimeException;
import org.apache.seatunnel.common.utils.ExceptionUtils;
import org.apache.seatunnel.e2e.common.TestResource;
import org.apache.seatunnel.e2e.common.TestSuiteBase;
import org.apache.seatunnel.e2e.common.container.ContainerExtendedFactory;
import org.apache.seatunnel.e2e.common.container.TestContainer;
import org.apache.seatunnel.e2e.common.junit.TestContainerExtension;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestTemplate;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startables;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.given;

@Slf4j
public abstract class AbstractJdbcIT extends TestSuiteBase implements TestResource {

    protected static final String HOST = "HOST";

    @TestContainerExtension
    protected final ContainerExtendedFactory extendedFactory =
            container -> {
                Container.ExecResult extraCommands =
                        container.execInContainer(
                                "bash",
                                "-c",
                                "mkdir -p /tmp/seatunnel/plugins/Jdbc/lib && cd /tmp/seatunnel/plugins/Jdbc/lib && wget "
                                        + driverUrl());
                Assertions.assertEquals(0, extraCommands.getExitCode(), extraCommands.getStderr());
            };

    protected GenericContainer<?> dbServer;
    protected JdbcCase jdbcCase;
    protected Connection connection;

    abstract JdbcCase getJdbcCase();

    abstract void compareResult() throws SQLException, IOException;

    abstract String driverUrl();

    abstract Pair<String[], List<SeaTunnelRow>> initTestData();

    abstract GenericContainer<?> initContainer();

    protected void initializeJdbcConnection(String jdbcUrl)
            throws SQLException, ClassNotFoundException, MalformedURLException,
                    InstantiationException, IllegalAccessException {
        URLClassLoader urlClassLoader =
                new URLClassLoader(
                        new URL[] {new URL(driverUrl())}, AbstractJdbcIT.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(urlClassLoader);
        Driver driver = (Driver) urlClassLoader.loadClass(jdbcCase.getDriverClass()).newInstance();
        Properties props = new Properties();

        if (StringUtils.isNotBlank(jdbcCase.getUserName())) {
            props.put("user", jdbcCase.getUserName());
        }

        if (StringUtils.isNotBlank(jdbcCase.getPassword())) {
            props.put("password", jdbcCase.getPassword());
        }

        this.connection = driver.connect(jdbcUrl.replace(HOST, dbServer.getHost()), props);
        connection.setAutoCommit(false);
    }

    protected void insertTestData() {
        try (PreparedStatement preparedStatement =
                connection.prepareStatement(jdbcCase.getInsertSql())) {

            List<SeaTunnelRow> rows = jdbcCase.getTestData().getValue();

            for (SeaTunnelRow row : rows) {
                for (int index = 0; index < row.getArity(); index++) {
                    preparedStatement.setObject(index + 1, row.getField(index));
                }
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();

            connection.commit();
        } catch (Exception exception) {
            log.error(ExceptionUtils.getMessage(exception));
            throw new SeaTunnelRuntimeException(JdbcITErrorCode.INSERT_DATA_FAILED, exception);
        }
    }

    protected void createSchemaIfNeeded() {}

    protected void createNeededTables() {
        try (Statement statement = connection.createStatement()) {
            String createTemplate = jdbcCase.getCreateSql();

            String createSource =
                    String.format(
                            createTemplate,
                            buildTableInfoWithSchema(
                                    jdbcCase.getDatabase(), jdbcCase.getSourceTable()));
            String createSink =
                    String.format(
                            createTemplate,
                            buildTableInfoWithSchema(
                                    jdbcCase.getDatabase(), jdbcCase.getSinkTable()));

            statement.execute(createSource);
            statement.execute(createSink);

            connection.commit();
        } catch (Exception exception) {
            log.error(ExceptionUtils.getMessage(exception));
            throw new SeaTunnelRuntimeException(JdbcITErrorCode.CREATE_TABLE_FAILED, exception);
        }
    }

    public String insertTable(String schema, String table, String... fields) {
        String columns =
                Arrays.stream(fields).map(this::quoteIdentifier).collect(Collectors.joining(", "));
        String placeholders = Arrays.stream(fields).map(f -> "?").collect(Collectors.joining(", "));

        return "INSERT INTO "
                + buildTableInfoWithSchema(schema, table)
                + " ("
                + columns
                + " )"
                + " VALUES ("
                + placeholders
                + ")";
    }

    public void clearTable(String schema, String table) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE " + buildTableInfoWithSchema(schema, table));
        } catch (SQLException e) {
            throw new SeaTunnelRuntimeException(JdbcITErrorCode.CLEAR_TABLE_FAILED, e);
        }
    }

    /**
     * Some rdbms need quote field.
     *
     * @param field field of rdbms.
     * @return quoted field.
     */
    public String quoteIdentifier(String field) {
        return "`" + field + "`";
    }

    public String buildTableInfoWithSchema(String schema, String table) {
        if (StringUtils.isNotBlank(schema)) {
            return quoteIdentifier(schema) + "." + quoteIdentifier(table);
        } else {
            return quoteIdentifier(table);
        }
    }

    @BeforeAll
    @Override
    public void startUp() {
        dbServer = initContainer();
        jdbcCase = getJdbcCase();

        Startables.deepStart(Stream.of(dbServer)).join();

        given().ignoreExceptions()
                .await()
                .atMost(360, TimeUnit.SECONDS)
                .untilAsserted(() -> this.initializeJdbcConnection(jdbcCase.getJdbcUrl()));

        createSchemaIfNeeded();
        createNeededTables();
        insertTestData();
    }

    @Override
    public void tearDown() throws SQLException {
        if (dbServer != null) {
            dbServer.close();
        }

        if (connection != null) {
            connection.close();
        }
    }

    @TestTemplate
    public void testJdbcDb(TestContainer container)
            throws IOException, InterruptedException, SQLException {
        List<String> configFiles = jdbcCase.getConfigFile();
        for (String configFile : configFiles) {
            Container.ExecResult execResult = container.executeJob(configFile);
            Assertions.assertEquals(0, execResult.getExitCode(), execResult.getStderr());
        }

        compareResult();
        clearTable(jdbcCase.getDatabase(), jdbcCase.getSinkTable());
    }
}
