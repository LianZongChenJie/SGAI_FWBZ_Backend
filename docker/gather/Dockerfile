FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:17-anolis

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    mkdir -p /sgai-cloud-gather

WORKDIR /sgai-cloud-gather

EXPOSE 7101

ADD ./sgai-module-gather.jar ./

CMD sleep 100;java -Dfile.encoding=utf-8 -Djava.security.egd=file:/dev/./urandom -jar sgai-module-gather.jar