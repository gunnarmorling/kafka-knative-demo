# Quarkus / Kafka Streams / Knative Demo for JavaLand 2020.

This is the source code of the demo from the JavaLand 2020 talk [Stream Processing with Quarkus, Kafka Streams and Knative](https://programm.javaland.eu/2020/#/scheduledEvent/590798) by Matthias Wessendorf and Gunnar Morling.

It shows:

* Aggregating values and joining multiple Kafka topics via Kafka Streams
* Capturing changes from an RDBMS and exporting them into a Kafka topic using Debezium
* Feeding data from Kafka to dynamically scaled consumers via Knative
* Running a web application with WebSockets on Knative

## Anatomy

The demo is made up of the following components:

* Apache Kafka and ZooKeeper
* Postgres with weather station master data (name, location etc)
* _sensors_, a Quarkus application that simulates temperature sensor measurements which are sent to a Kafka topic
* _aggregator_, a Quarkus application processing the measurements topic with the master data topic, using the Kafka Streams API
* _temperature-map_, a Quarkus application that streams the measurement values to a client via WebSockets, where they are visualized on a world map

TODO: Add Knative

## Building

```
mvn clean package
export DEBEZIUM_VERSION=1.0
```

### Running

```
docker-compose -f docker-compose-local.yaml up --build
```

Register Debezium connector for master data:

```
http PUT http://localhost:8083/connectors/weather-connector/config < register-postgres.json
```

Browse change data topic with weather stations:

```
docker run --tty --rm \
    --network weather-network \
    debezium/tooling:1.0 \
    kafkacat -b kafka:9092 -C -o beginning -q \
    -t dbserver1.weather.weatherstations | jq .
```

Browse sensor data topic:

```
docker run --tty --rm \
    --network weather-network \
    debezium/tooling:1.0 \
    kafkacat -b kafka:9092 -C -o beginning -q \
    -t temperature-values
```

Browse enriched sensor data topic:

```
docker run --tty --rm \
    --network weather-network \
    debezium/tooling:1.0 \
    kafkacat -b kafka:9092 -C -o beginning -q \
    -t temperature-values-enriched | jq .
```

Getting a shell in the Postgres database:

```
docker run --tty --rm -i \
        --network weather-network \
        debezium/tooling:1.0 \
        bash -c 'pgcli postgresql://postgresuser:postgrespw@weather-db:5432/weatherdb'
```

## Running locally

For development purposes it can be handy to run the Quarkus applications
directly on your local machine instead of via Docker.
To do so, runn the following:

```bash
# If not present yet:
# export HOSTNAME=<your hostname>

docker-compose up --scale sensors=0 --scale aggregator=0 --scale temperature-map=0

mvn compile quarkus:dev -f sensors/pom.xml
mvn compile quarkus:dev -f aggregator/pom.xml
mvn compile quarkus:dev -f temperature-map/pom.xml
```

## Running in native

To run the Quarkus applications as native binaries via GraalVM,
first run the Maven builds using the `native` profile:

```bash
mvn clean package -Pnative -Dnative-image.container-runtime=docker
```

Then create an environment variable named `QUARKUS_MODE` and with value set to "native":

```bash
export QUARKUS_MODE=native
```

Now start Docker Compose as described above.
