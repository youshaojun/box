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

package org.apache.seatunnel.translation.flink.source;

import org.apache.seatunnel.api.source.SeaTunnelSource;
import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.translation.source.BaseSourceFunction;
import org.apache.seatunnel.translation.source.CoordinatedSource;

/** The coordinated source function implementation of {@link BaseSeaTunnelSourceFunction} */
public class SeaTunnelCoordinatedSource extends BaseSeaTunnelSourceFunction {

    protected static final String COORDINATED_SOURCE_STATE_NAME = "coordinated-source-states";

    public SeaTunnelCoordinatedSource(SeaTunnelSource<SeaTunnelRow, ?, ?> source) {
        // TODO: Make sure the source is coordinated.
        super(source);
    }

    @Override
    protected BaseSourceFunction<SeaTunnelRow> createInternalSource() {
        return new CoordinatedSource<>(
                source, restoredState, getRuntimeContext().getNumberOfParallelSubtasks());
    }

    @Override
    protected String getStateName() {
        return COORDINATED_SOURCE_STATE_NAME;
    }
}
