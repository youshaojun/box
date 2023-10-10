#! /bin/bash
BASE_DIR=/opt/dinky
ADMIN_JAR=dlink-admin-0.7.2.jar

docker cp dinky:${BASE_DIR}/lib/${ADMIN_JAR} ${BASE_DIR}/backup/${ADMIN_JAR}

docker cp ${BASE_DIR}/${ADMIN_JAR} dinky:${BASE_DIR}/lib/${ADMIN_JAR}

docker restart dinky
