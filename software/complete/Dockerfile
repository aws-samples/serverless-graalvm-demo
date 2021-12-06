FROM arm64v8/amazonlinux:2 AS graalvm
ARG GRAALVM_VERSION
ARG GRAALVM_JVM_VERSION
ARG CLASS_NAME
ENV LANG=en_US.UTF-8
RUN yum update -y && yum install -y gcc gcc-c++ zlib-devel zip tar gzip && yum clean all
RUN curl -4 -L https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAALVM_VERSION}/graalvm-ce-${GRAALVM_JVM_VERSION}-linux-aarch64-${GRAALVM_VERSION}.tar.gz -o /tmp/graalvm.tar.gz \
    && tar -zxf /tmp/graalvm.tar.gz -C /tmp \
    && mv /tmp/graalvm-ce-${GRAALVM_JVM_VERSION}-${GRAALVM_VERSION} /usr/lib/graalvm \
    && rm -rf /tmp/*
RUN /usr/lib/graalvm/bin/gu install native-image
ENV PATH=/usr/lib/graalvm/bin:${PATH}
WORKDIR /home/app
COPY classes /home/app/classes
COPY dependency/* /home/app/libs/
ARG GRAALVM_ARGS=""
RUN native-image ${GRAALVM_ARGS} -H:Class=${CLASS_NAME} -H:Name=application --no-fallback -cp "/home/app/libs/*:/home/app/classes/"

FROM arm64v8/amazonlinux:2
WORKDIR /function
RUN yum install -y zip && yum clean all
COPY --from=graalvm /home/app/application /function/func
RUN echo "#!/bin/sh" >> bootstrap && echo "set -euo pipefail" >> bootstrap && echo "./func -Djava.library.path=$(pwd)" >> bootstrap
RUN chmod 777 bootstrap
RUN chmod 777 func
RUN zip -j function.zip bootstrap func