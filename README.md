# Debezium / Quarkus / Kafka Streams / Knative Demo

This is the source code of the demo from the DevNation Tech Talk [Serverless stream processing of Debezium data change events with Knative](https://developers.redhat.com/devnation/tech-talks/serverless-stream-debezium/) by Matthias Wessendorf and Gunnar Morling.

It shows:

* Capturing changes from an RDBMS and exporting them into a Kafka topic using Debezium
* Aggregating values and joining multiple Kafka topics via Kafka Streams, running on top of Quarkus
* Feeding data from Kafka to dynamically scaled consumers via Knative
* Running a web application with WebSockets on Knative

## Anatomy

The demo is made up of the following components:

* Apache Kafka and ZooKeeper
* Postgres with weather station master data (name, location etc)
* _sensors_, a Quarkus application that simulates temperature sensor measurements which are sent to a Kafka topic
* _aggregator_, a Quarkus application processing the measurements topic with the master data topic, using the Kafka Streams API
* _temperature-map_, a Quarkus application that streams the measurement values to a client via WebSockets, where they are visualized on a world map

TODO: Add Knative parts. For this Docker Compose based set-up the _temperature-map_ app reads directly from the Kafka topic with enriched measurement values. In the actual demo this is done via Knative Eventing.

## Building

```
mvn clean package
export DEBEZIUM_VERSION=1.1
```

### Running

```
docker-compose up --build
```

Register Debezium connector for master data:

```
http PUT http://localhost:8083/connectors/weather-connector/config < register-postgres.json
```

Browse change data topic with weather stations:

```
docker run --tty --rm \
    --network weather-network \
    debezium/tooling:1.1 \
    kafkacat -b kafka:9092 -C -o beginning -q \
    -t dbserver1.weather.weatherstations | jq .
```

Browse sensor data topic:

```
docker run --tty --rm \
    --network weather-network \
    debezium/tooling:1.1 \
    kafkacat -b kafka:9092 -C -o beginning -q \
    -t temperature-values
```

Browse enriched sensor data topic:

```
docker run --tty --rm \
    --network weather-network \
    debezium/tooling:1.1 \
    kafkacat -b kafka:9092 -C -o beginning -q \
    -t temperature-values-enriched | jq .
```

Initially, only some weather stations are active.
Non-active stations are filtered out in the Kafka Streams application.
To show how master data updates in the RDBMS are propagated via Debezium,
enable all stations in the database;
Get a shell in the Postgres database and run this DML:

```
docker run --tty --rm -i \
        --network weather-network \
        debezium/tooling:1.1 \
        bash -c 'pgcli postgresql://postgresuser:postgrespw@weather-db:5432/weatherdb'

# In pgcli
update weather.weatherstations set active=true;
```

You'll then see values for all the stations being shown on the map.

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
