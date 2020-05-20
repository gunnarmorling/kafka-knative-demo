# Kubernetes Set-up

## Database

Run the database:

```shell
kubectl run --image=quay.io/gunnarmorling/javaland2020-knativedemo-postgres \
    weatherdb \
    --port=5432 \
    --env="POSTGRES_USER=postgresuser" \
    --env="POSTGRES_PASSWORD=postgrespw" \
    --env="POSTGRES_DB=weatherdb"
```

Expose it as service:

```shell
cat <<EOF | kubectl -n default apply -f -
apiVersion: v1
kind: Service
metadata:
  name: weatherdb
  labels:
    run: weatherdb
spec:
  type: NodePort
  ports:
   - port: 5432
  selector:
   run: weatherdb
EOF
```

## Kafka Connect

```shell
cat <<EOF | kubectl -n kafka apply -f -
apiVersion: kafka.strimzi.io/v1beta1
kind: KafkaConnect
metadata:
  name: my-connect-cluster
  annotations:
  # use-connector-resources configures this KafkaConnect
  # to use KafkaConnector resources to avoid
  # needing to call the Connect REST API directly
    strimzi.io/use-connector-resources: "true"
spec:
  image: quay.io/gunnarmorling/javaland2020-knativedemo-kafkaconnect-debezium
  replicas: 1
  bootstrapServers: my-cluster-kafka-bootstrap.kafka:9093
  tls:
    trustedCertificates:
      - secretName: my-cluster-cluster-ca-cert
        certificate: ca.crt
  config:
    config.storage.replication.factor: 1
    offset.storage.replication.factor: 1
    status.storage.replication.factor: 1
    config.providers: file
    config.providers.file.class: org.apache.kafka.common.config.provider.FileConfigProvider
EOF
```

## Connector Instance

```shell
cat <<EOF | kubectl -n kafka apply -f -
apiVersion: "kafka.strimzi.io/v1alpha1"
kind: "KafkaConnector"
metadata:
  name: "inventory-connector"
  labels:
    strimzi.io/cluster: my-connect-cluster
spec:
  class: io.debezium.connector.postgresql.PostgresConnector
  tasksMax: 1
  config:
    database.hostname: weatherdb.default.svc
    database.port: "5432"
    database.user: "postgresuser"
    database.password: "postgrespw"
    database.server.name: "dbserver1"
    database.dbname : "weatherdb"
    schema.whitelist : "weather"
    decimal.handling.mode : "double"
    key.converter : "org.apache.kafka.connect.json.JsonConverter"
    key.converter.schemas.enable : "false"
    value.converter : "org.apache.kafka.connect.json.JsonConverter"
    value.converter.schemas.enable : "false"
EOF
```

## The Apps

### Aggregator

```shell
kubectl run --image=docker.io/gunnarmorling/debezium-knative-demo-aggregator aggregator --env="QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS=my-cluster-kafka-bootstrap.kafka:9092"
```

### Sensors

```shell
kubectl run --image=docker.io/gunnarmorling/debezium-knative-demo-sensors sensors --env="KAFKA_BOOTSTRAP_SERVERS=my-cluster-kafka-bootstrap.kafka:9092"
```

