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

package org.apache.seatunnel.connectors.seatunnel.clickhouse;

import org.apache.seatunnel.connectors.seatunnel.clickhouse.sink.ClickhouseSinkFactory;
import org.apache.seatunnel.connectors.seatunnel.clickhouse.sink.file.ClickhouseFileSinkFactory;
import org.apache.seatunnel.connectors.seatunnel.clickhouse.source.ClickhouseSourceFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClickhouseFactoryTest {

    @Test
    public void testOptionRule() {
        Assertions.assertNotNull((new ClickhouseSourceFactory()).optionRule());
        Assertions.assertNotNull((new ClickhouseSinkFactory()).optionRule());
        Assertions.assertNotNull((new ClickhouseFileSinkFactory()).optionRule());
    }
}
