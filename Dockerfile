FROM nexus.dev.sberinsur.local/base-alpine-jre-11.0.11:2

WORKDIR /app

# Copy created artifact to working directory
COPY build/libs/*.jar ./application.jar

# Default port for running service
EXPOSE 8080/tcp

# Default command to start service with required variables
ENTRYPOINT exec java -XX:+UseContainerSupport -Dspring.profiles.active=stand $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar ./application.jar \
            -Pargs=--spring.cloud.bootstrap.location="bootstrap.yml" \
            --docker.instanceId=${INSTANCE_ID} \
            --docker.consulHost=${CONSUL_HOST} \
            --docker.consulConfigPrefix=${CONSUL_CONFIG_PREFIX} \
            --docker.vaultHost=${VAULT_HOST} \
            --docker.vaultToken=${VAULT_TOKEN}

# Checking the status of a running service
HEALTHCHECK --start-period=30s --interval=30s --timeout=3s --retries=3 \
            CMD curl -m 5 --silent --fail --request GET http://localhost:8080/actuator/health \
            | jq --exit-status -n 'inputs | if has("status") then .status=="UP" else false end' > /dev/null || exit 1
