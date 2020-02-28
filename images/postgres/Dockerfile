####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode
#
# Before building the docker image run:
#
# mvn package -Pnative -Dquarkus.native.container-build=true
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile.native -t quarkus/kafka-knative-demo .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/kafka-knative-demo
#
###
FROM debezium/example-postgres:1.0
COPY init.sql /docker-entrypoint-initdb.d/init.sql
