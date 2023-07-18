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

package org.apache.seatunnel.engine.server.resourcemanager.worker;

import org.apache.seatunnel.engine.server.resourcemanager.resource.ResourceProfile;
import org.apache.seatunnel.engine.server.resourcemanager.resource.SlotProfile;
import org.apache.seatunnel.engine.server.serializable.ResourceDataSerializerHook;

import com.hazelcast.cluster.Address;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import lombok.Data;

import java.io.IOException;

/**
 * Used to describe the status of the current Worker, including address and resource assign status
 */
@Data
public class WorkerProfile implements IdentifiedDataSerializable {

    private Address address;

    private ResourceProfile profile;

    private ResourceProfile unassignedResource;

    private SlotProfile[] assignedSlots;

    private SlotProfile[] unassignedSlots;

    public WorkerProfile(Address address) {
        this.address = address;
        this.unassignedResource = new ResourceProfile();
    }

    public WorkerProfile() {
        address = new Address();
    }

    @Override
    public int getFactoryId() {
        return ResourceDataSerializerHook.FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return ResourceDataSerializerHook.WORKER_PROFILE_TYPE;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeObject(address);
        out.writeObject(profile);
        out.writeObject(unassignedResource);
        out.writeInt(assignedSlots.length);
        for (SlotProfile assignedSlot : assignedSlots) {
            out.writeObject(assignedSlot);
        }
        out.writeInt(unassignedSlots.length);
        for (SlotProfile unassignedSlot : unassignedSlots) {
            out.writeObject(unassignedSlot);
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        address = in.readObject();
        profile = in.readObject();
        unassignedResource = in.readObject();
        int assignedSlotsLength = in.readInt();
        assignedSlots = new SlotProfile[assignedSlotsLength];
        for (int i = 0; i < assignedSlots.length; i++) {
            assignedSlots[i] = in.readObject();
        }
        int unassignedSlotsLength = in.readInt();
        unassignedSlots = new SlotProfile[unassignedSlotsLength];
        for (int i = 0; i < unassignedSlots.length; i++) {
            unassignedSlots[i] = in.readObject();
        }
    }
}
