####
#
# Build the image with:
#
# docker build -f Dockerfile -t quay.io/gunnarmorling/javaland2020-knativedemo-postgres .
#
###
FROM debezium/example-postgres:1.0
COPY init.sql /docker-entrypoint-initdb.d/init.sql
