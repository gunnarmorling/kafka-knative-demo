# OpenShift set-up

## Database

oc new-app --name=weatherdb quay.io/gunnarmorling/javaland2020-knativedemo-postgres \
    -e POSTGRES_USER=postgresuser \
    -e POSTGRES_PASSWORD=postgrespw \
    -e POSTGRES_DB=weatherdb
