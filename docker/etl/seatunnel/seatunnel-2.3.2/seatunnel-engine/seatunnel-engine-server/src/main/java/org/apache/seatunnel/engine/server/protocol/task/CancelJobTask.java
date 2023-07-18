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

package org.apache.seatunnel.engine.server.protocol.task;

import org.apache.seatunnel.engine.core.protocol.codec.SeaTunnelCancelJobCodec;
import org.apache.seatunnel.engine.server.operation.CancelJobOperation;

import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.nio.Connection;
import com.hazelcast.spi.impl.operationservice.Operation;

public class CancelJobTask extends AbstractSeaTunnelMessageTask<Long, Void> {
    protected CancelJobTask(ClientMessage clientMessage, Node node, Connection connection) {
        super(
                clientMessage,
                node,
                connection,
                SeaTunnelCancelJobCodec::decodeRequest,
                x -> SeaTunnelCancelJobCodec.encodeResponse());
    }

    @Override
    protected Operation prepareOperation() {
        return new CancelJobOperation(parameters);
    }

    @Override
    public String getMethodName() {
        return "cancelJob";
    }

    @Override
    public Object[] getParameters() {
        return new Object[0];
    }
}
