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

package org.apache.seatunnel.connectors.seatunnel.assertion.sink;

import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.api.table.type.SeaTunnelRowType;
import org.apache.seatunnel.connectors.seatunnel.assertion.excecutor.AssertExecutor;
import org.apache.seatunnel.connectors.seatunnel.assertion.exception.AssertConnectorErrorCode;
import org.apache.seatunnel.connectors.seatunnel.assertion.exception.AssertConnectorException;
import org.apache.seatunnel.connectors.seatunnel.assertion.rule.AssertFieldRule;
import org.apache.seatunnel.connectors.seatunnel.common.sink.AbstractSinkWriter;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAccumulator;

public class AssertSinkWriter extends AbstractSinkWriter<SeaTunnelRow, Void> {

    private final SeaTunnelRowType seaTunnelRowType;
    private final List<AssertFieldRule> assertFieldRules;
    private final List<AssertFieldRule.AssertRule> assertRowRules;
    private static final AssertExecutor ASSERT_EXECUTOR = new AssertExecutor();
    private static final LongAccumulator LONG_ACCUMULATOR = new LongAccumulator(Long::sum, 0);

    public AssertSinkWriter(
            SeaTunnelRowType seaTunnelRowType,
            List<AssertFieldRule> assertFieldRules,
            List<AssertFieldRule.AssertRule> assertRowRules) {
        this.seaTunnelRowType = seaTunnelRowType;
        this.assertFieldRules = assertFieldRules;
        this.assertRowRules = assertRowRules;
    }

    @Override
    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void write(SeaTunnelRow element) {
        LONG_ACCUMULATOR.accumulate(1);
        if (Objects.nonNull(assertFieldRules)) {
            ASSERT_EXECUTOR
                    .fail(element, seaTunnelRowType, assertFieldRules)
                    .ifPresent(
                            failRule -> {
                                throw new AssertConnectorException(
                                        AssertConnectorErrorCode.RULE_VALIDATION_FAILED,
                                        "row :" + element + " fail rule: " + failRule);
                            });
        }
    }

    @Override
    public void close() {
        if (Objects.nonNull(assertRowRules)) {
            assertRowRules.stream()
                    .filter(
                            assertRule -> {
                                switch (assertRule.getRuleType()) {
                                    case MAX_ROW:
                                        return !(LONG_ACCUMULATOR.longValue()
                                                <= assertRule.getRuleValue());
                                    case MIN_ROW:
                                        return !(LONG_ACCUMULATOR.longValue()
                                                >= assertRule.getRuleValue());
                                    default:
                                        return false;
                                }
                            })
                    .findFirst()
                    .ifPresent(
                            failRule -> {
                                throw new AssertConnectorException(
                                        AssertConnectorErrorCode.RULE_VALIDATION_FAILED,
                                        "row num :"
                                                + LONG_ACCUMULATOR.longValue()
                                                + " fail rule: "
                                                + failRule);
                            });
        }
    }
}
