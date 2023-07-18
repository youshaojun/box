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

package org.apache.seatunnel.engine.server.resourcemanager.thirdparty.yarn;

import org.apache.seatunnel.engine.server.resourcemanager.AbstractResourceManager;
import org.apache.seatunnel.engine.server.resourcemanager.resource.ResourceProfile;
import org.apache.seatunnel.engine.server.resourcemanager.thirdparty.CreateWorkerResult;
import org.apache.seatunnel.engine.server.resourcemanager.thirdparty.ThirdPartyResourceManager;

import com.hazelcast.spi.impl.NodeEngine;

import java.util.concurrent.CompletableFuture;

public class YarnResourceManager extends AbstractResourceManager
        implements ThirdPartyResourceManager {
    public YarnResourceManager(NodeEngine nodeEngine) {
        super(nodeEngine);
    }

    @Override
    public CompletableFuture<CreateWorkerResult> createNewWorker(ResourceProfile resourceProfile) {
        return null;
    }

    @Override
    public CompletableFuture<Void> releaseWorker(String workerID) {
        return null;
    }
}
