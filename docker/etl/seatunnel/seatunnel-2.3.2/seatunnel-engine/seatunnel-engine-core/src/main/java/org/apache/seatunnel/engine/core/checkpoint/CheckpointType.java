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

package org.apache.seatunnel.engine.core.checkpoint;

public enum CheckpointType {

    /** Automatically triggered by the CheckpointCoordinator. */
    CHECKPOINT_TYPE(true, "checkpoint"),

    /** Triggered by the user. */
    SAVEPOINT_TYPE(false, "savepoint"),

    /** Automatically triggered by the Task. */
    COMPLETED_POINT_TYPE(true, "completed-point");

    private final boolean auto;
    private final String name;

    public static CheckpointType fromName(String name) {
        for (CheckpointType type : CheckpointType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown checkpoint type: " + name);
    }

    CheckpointType(boolean auto, String name) {
        this.auto = auto;
        this.name = name;
    }

    public boolean isAuto() {
        return auto;
    }

    public String getName() {
        return name;
    }
}
