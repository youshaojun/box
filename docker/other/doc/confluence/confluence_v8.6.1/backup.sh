#! /bin/bash
# Create by yousj
# 迁移Confluence备份文件

# Confluence根目录
CONFLUENCE_HOME=/opt/confluence

# Confluence服务容器名
CONTAINER_NAME=confluence-srv

# Confluence应用内部备份文件存放地址
BACKUP_HOME=/var/atlassian/application-data/confluence/backups

# 备份文件名
BACKUP_FILE=${BACKUP_HOME}/backup-$(date "+%Y_%m_%d").zip

# 备份文件要迁移到新的目录
TARGET_BACKUP_HOME=${CONFLUENCE_HOME}/backups

# 保留近7天备份文件
find ${TARGET_BACKUP_HOME} -name "backup-*" -mtime +7  -exec rm -f {} \;

# 不将文件挂载出来是由于之前遇到过confluence漏洞被利用导致所有备份文件被攻击者加密
# 定时将容器内备份文件拷贝出来以保证数据不被修改
docker cp ${CONTAINER_NAME}:${BACKUP_FILE} ${TARGET_BACKUP_HOME}

# 删除已迁移备份文件
docker exec ${CONTAINER_NAME} rm ${BACKUP_FILE}
