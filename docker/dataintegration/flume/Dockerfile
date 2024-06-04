# https://github.com/bigcontainer/bigcont/blob/master/flume/Dockerfile
FROM openjdk:8-alpine

ARG FL_MIRROR=https://mirror.bit.edu.cn/apache
ARG FL_VERSION=1.9.0

# Verify https://hub.docker.com/r/bigcontainer/flume/dockerfile
ADD ${FL_MIRROR}/flume/${FL_VERSION}/apache-flume-${FL_VERSION}-bin.tar.gz /opt/
ADD hello.conf /etc/flume/flume.conf
RUN cd /opt && tar zxvf apache-flume-${FL_VERSION}-bin.tar.gz && mv apache-flume-${FL_VERSION}-bin flume

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories && \
    apk add bash

WORKDIR /opt/flume
ENTRYPOINT ["bin/flume-ng", "agent"]
CMD ["-n", "agent", "-c", "conf", "-f", "/etc/flume/flume.conf", "-Dflume.root.logger=INFO,console"]