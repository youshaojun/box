#! /bin/sh

# 切割日志
find /data/logs  -name "*.log" -mtime +7  -exec rm -f {} \;
