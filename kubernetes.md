# Kubernetes Set-up

```shell
kubectl create namespace kafka
kubectl create namespace debezium-knative-demo
oc project debezium-knative-demo
```

## Strimzi Cluster Operator

```shell
curl -L https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.16.1/strimzi-cluster-operator-0.16.1.yaml \
  | sed 's/namespace: .*/namespace: kafka/' \
  | kubectl apply -f - -n kafka
```

## Apache Kafka

```shell
kubectl -n kafka \
    apply -f https://raw.githubusercontent.com/strimzi/strimzi-kafka-operator/0.16.1/examples/kafka/kafka-persistent-single.yaml \
  && kubectl wait kafka/my-cluster --for=condition=Ready --timeout=300s -n kafka
```

## Kafka Connect

```shell
kubectl -n kafka apply -f debezium/020-connect.yaml
```

## Database

Run the database:

```shell
# (To run container as root on OpenShift)
oc adm policy add-scc-to-user anyuid system:serviceaccount:debezium-knative-demo:default

kubectl -n debezium-knative-demo run --image=quay.io/gunnarmorling/javaland2020-knativedemo-postgres \
    weatherdb \
    --port=5432 \
    --env="POSTGRES_USER=postgresuser" \
    --env="POSTGRES_PASSWORD=postgrespw" \
    --env="POSTGRES_DB=weatherdb"
```

Expose it as service:

```shell
cat <<EOF | kubectl -n debezium-knative-demo apply -f -
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

## The Sensors App

```shell
kubectl run --image=docker.io/gunnarmorling/debezium-knative-demo-sensors sensors --env="KAFKA_BOOTSTRAP_SERVERS=my-cluster-kafka-bootstrap.kafka:9092"
```

## Knative installation

To install Knative on Openshift, follow the instructions for the [Openshift Serverless product](https://docs.openshift.com/container-platform/4.4/serverless/installing_serverless/installing-openshift-serverless.html)

# Demo

Start a tooling pod:

```shell
kubectl run tooling -it --image=debezium/tooling --restart=Never
```

Examining temperature value topic:

```shell
[root@tooling /] kafkacat -b my-cluster-kafka-bootstrap.kafka:9092 -C -o end -q -u -t temperature-values \
    -f 'key: %k, value: %s\n' -s key='i$'
```

Selecting existing weather stations:

```shell
[root@tooling /] pgcli postgresql://postgresuser:postgrespw@weatherdb.debezium-knative-demo.svc:5432/weatherdb

weatherdb> select * from weather.weatherstations;
```

Registering a Debezium connector:

```shell
kubectl -n kafka apply -f debezium/030-connector.yaml
```

Getting its status:

```shell
[root@tooling /] kubectl get kafkaconnector inventory-connector -n kafka -o json | jq .
```

Examining weather station topic:

```shell
[root@tooling /] kafkacat -b my-cluster-kafka-bootstrap.kafka:9092 -C -o beginning -q -u -t dbserver1.weather.weatherstations | jq .
```

Run aggregator app:

```shell
kubectl run --image=docker.io/gunnarmorling/debezium-knative-demo-aggregator aggregator --env="QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS=my-cluster-kafka-bootstrap.kafka:9092"
```

Fallback to another topic:

```shell
kubectl run --image=docker.io/gunnarmorling/debezium-knative-demo-aggregator aggregator --env="QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS=my-cluster-kafka-bootstrap.kafka:9092" --env="WEATHER_STATIONS_TOPIC=dbserver2.weather.weatherstations"
```

Examining enriched topic:

```shell
[root@tooling /] kafkacat -b my-cluster-kafka-bootstrap.kafka:9092 -C -o end -q -u -t temperature-values-enriched | jq .
```

Enabling more weather stations:

```sql
update weather.weatherstations set active = true;
```

Stop tooling pod:

```shell
kubectl delete pod tooling
```

## Knative application

Create the default Knative broker:

```shell
kubectl label namespace debezium-knative-demo knative-eventing-injection=enabled
```

Start the `KafkaSource`:

```shell
kubectl apply -f 010-kafka-source-enriched.yml
```


Run the maps app:

```shell
kubectl apply -f 020-ksvc.yaml
```

Create the Trigger to send messages to the maps app:

```shell
kubectl apply -f 030-trigger.yaml
```

# Optional: Running maps app without Knative

```shell
kubectl run --image=docker.io/gunnarmorling/debezium-knative-demo-temperature-map temperature-map \
    --env="MAP_READ_FROM_TOPIC=true" \
    --env="KAFKA_BOOTSTRAP_SERVERS=my-cluster-kafka-bootstrap.kafka:9092"
```

```shell
cat <<EOF | kubectl -n debezium-knative-demo apply -f -
apiVersion: v1
kind: Service
metadata:
  name: temperature-map
  labels:
    run: temperature-map
spec:
  type: NodePort
  ports:
   - port: 8080
  selector:
   run: temperature-map
EOF
```

```shell
oc expose svc temperature-map
```
