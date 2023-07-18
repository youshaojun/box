#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#This script is used to download the connector plug-ins required during the running process. 
#All are downloaded by default. You can also choose what you need. 
#You only need to configure the plug-in name in config/plugin_config.

# get seatunnel home
SEATUNNEL_HOME=$(cd $(dirname $0);cd ../;pwd)

# connector default version is 2.3.2, you can also choose a custom version. eg: 2.1.2:  sh install-plugin.sh 2.1.2
version=2.3.2

if [ -n "$1" ]; then
    version="$1"
fi

echo "Install hadoop shade jar, usage version is ${version}"

${SEATUNNEL_HOME}/mvnw dependency:get -DgroupId=org.apache.seatunnel -Dclassifier=optional -DartifactId=seatunnel-hadoop3-3.1.4-uber -Dversion=${version} -Ddest=${SEATUNNEL_HOME}/lib

echo "Install SeaTunnel connectors plugins, usage version is ${version}"

# create the connectors directory
if [ ! -d ${SEATUNNEL_HOME}/connectors ];
  then
      mkdir ${SEATUNNEL_HOME}/connectors
      echo "create connectors directory"
fi

# create the seatunnel connectors directory (for v2)
if [ ! -d ${SEATUNNEL_HOME}/connectors/seatunnel ];
  then
      mkdir ${SEATUNNEL_HOME}/connectors/seatunnel
      echo "create seatunnel connectors directory"
fi  

while read line; do
    if  [ ${line:0:1} != "-" ] && [ ${line:0:1} != "#" ]
      	then
      		echo "install connector : " $line
      		${SEATUNNEL_HOME}/mvnw dependency:get -DgroupId=org.apache.seatunnel -DartifactId=${line} -Dversion=${version} -Ddest=${SEATUNNEL_HOME}/connectors/seatunnel
    fi

done < ${SEATUNNEL_HOME}/config/plugin_config