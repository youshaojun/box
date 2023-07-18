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

package org.apache.seatunnel.app.domain.response.user;

import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Map;

@ApiModel(value = "userSimpleInfoRes", description = "user simple information")
@Data
public class UserSimpleInfoRes extends BaseUserInfoRes {

    private String token;

    public Map<String, Object> toMap() {
        final Map<String, Object> userMap = Maps.newHashMap();
        userMap.put("id", getId());
        userMap.put("name", getName());
        userMap.put("status", getStatus());
        userMap.put("type", getType());
        return userMap;
    }
}
