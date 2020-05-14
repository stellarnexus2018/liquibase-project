FROM dev.covox.ru/java-oracle:jre_8
WORKDIR /app
ARG APP_NAME
ENV APP_NAME=${APP_NAME}
COPY build/libs/*.jar ./${APP_NAME}.jar
EXPOSE 8083
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ./${APP_NAME}.jar --docker.instanceId=${INSTANCE_ID} -Pargs=--spring.cloud.bootstrap.location="bootstrap.yml"