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

package org.apache.seatunnel.engine.common.config.server;

import lombok.Data;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;

@Data
@SuppressWarnings("checkstyle:MagicNumber")
public class CheckpointConfig implements Serializable {

    public static final long MINIMAL_CHECKPOINT_TIME = 10;

    private long checkpointInterval = ServerConfigOptions.CHECKPOINT_INTERVAL.defaultValue();
    private long checkpointTimeout = ServerConfigOptions.CHECKPOINT_TIMEOUT.defaultValue();
    private int maxConcurrentCheckpoints =
            ServerConfigOptions.CHECKPOINT_MAX_CONCURRENT.defaultValue();
    private int tolerableFailureCheckpoints =
            ServerConfigOptions.CHECKPOINT_TOLERABLE_FAILURE.defaultValue();

    private CheckpointStorageConfig storage = ServerConfigOptions.CHECKPOINT_STORAGE.defaultValue();

    public void setCheckpointInterval(long checkpointInterval) {
        checkArgument(
                checkpointInterval >= MINIMAL_CHECKPOINT_TIME,
                "The minimum checkpoint interval is 10 mills.");
        this.checkpointInterval = checkpointInterval;
    }

    public void setCheckpointTimeout(long checkpointTimeout) {
        checkArgument(
                checkpointTimeout >= MINIMAL_CHECKPOINT_TIME,
                "The minimum checkpoint timeout is 10 mills.");
        this.checkpointTimeout = checkpointTimeout;
    }

    public void setMaxConcurrentCheckpoints(int maxConcurrentCheckpoints) {
        checkArgument(
                maxConcurrentCheckpoints >= 1,
                "The minimum number of concurrent checkpoints is 1.");
        this.maxConcurrentCheckpoints = maxConcurrentCheckpoints;
    }

    public void setTolerableFailureCheckpoints(int tolerableFailureCheckpoints) {
        checkArgument(
                maxConcurrentCheckpoints >= 0,
                "The number of tolerance failed checkpoints must be a natural number.");
        this.tolerableFailureCheckpoints = tolerableFailureCheckpoints;
    }
}
