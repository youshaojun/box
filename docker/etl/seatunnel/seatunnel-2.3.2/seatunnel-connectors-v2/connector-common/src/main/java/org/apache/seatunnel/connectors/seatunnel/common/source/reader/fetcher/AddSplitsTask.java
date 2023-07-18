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

package org.apache.seatunnel.connectors.seatunnel.common.source.reader.fetcher;

import org.apache.seatunnel.api.source.SourceSplit;
import org.apache.seatunnel.connectors.seatunnel.common.source.reader.splitreader.SplitReader;
import org.apache.seatunnel.connectors.seatunnel.common.source.reader.splitreader.SplitsAddition;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
@ToString(of = {"splitsToAdd"})
class AddSplitsTask<SplitT extends SourceSplit> implements SplitFetcherTask {
    private final SplitReader<?, SplitT> splitReader;
    private final Collection<SplitT> splitsToAdd;
    private final Map<String, SplitT> assignedSplits;

    @Override
    public void run() {
        for (SplitT s : splitsToAdd) {
            assignedSplits.put(s.splitId(), s);
        }
        splitReader.handleSplitsChanges(new SplitsAddition<>(splitsToAdd));
    }

    @Override
    public void wakeUp() {}
}
