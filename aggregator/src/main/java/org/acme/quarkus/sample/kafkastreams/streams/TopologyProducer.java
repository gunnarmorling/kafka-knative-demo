package org.acme.quarkus.sample.kafkastreams.streams;

import java.util.Collections;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.json.Json;

import org.acme.quarkus.sample.kafkastreams.model.Aggregation;
import org.acme.quarkus.sample.kafkastreams.model.WeatherStation;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.debezium.serde.DebeziumSerdes;
import io.quarkus.kafka.client.serialization.JsonbSerde;

@ApplicationScoped
public class TopologyProducer {

    @ConfigProperty(name = "weather.stations.topic")
    String weatherStationsTopic;

    @ConfigProperty(name = "temperature.values.topic")
    String temperatureValuesTopic;

    @ConfigProperty(name = "temperature.values.enriched.topic")
    String temperatureValuesEnrichedTopic;

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        Serde<Integer> weatherStationKeySerde = DebeziumSerdes.payloadJson(Integer.class);
        weatherStationKeySerde.configure(Collections.emptyMap(), true);
        Serde<WeatherStation> weatherStationSerde = DebeziumSerdes.payloadJson(WeatherStation.class);
        weatherStationSerde.configure(Collections.singletonMap("from.field", "after"), false);
        JsonObjectSerde jsonObjectSerde = new JsonObjectSerde();
        JsonbSerde<Aggregation> aggregationSerde = new JsonbSerde<>(Aggregation.class);
        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore("aggregations-store");


        KTable<Integer, WeatherStation> stations = builder.table(
                weatherStationsTopic,
                Consumed.with(weatherStationKeySerde, weatherStationSerde)
        )
        .filter((stationId, station) -> station.active);

        builder.stream(temperatureValuesTopic, Consumed.with(Serdes.Integer(), Serdes.String()))
                .map((Integer stationId, String measurement) -> {
                    String[] parts = measurement.split(";");
                    String ts = parts[0];
                    Double value = Double.valueOf(parts[1]);

                    return KeyValue.pair(stationId, Json.createObjectBuilder()
                            .add("stationId", stationId)
                            .add("ts", ts)
                            .add("value", value)
                            .add("icon", getIcon(value))
                            .build());
                })
                .join(
                        stations,
                        (measurement, station) -> Json.createObjectBuilder(measurement)
                                .add("stationName", station.name)
                                .add("longitude", station.longitude)
                                .add("latitude", station.latitude)
                                .build(),
                        Joined.with(Serdes.Integer(), jsonObjectSerde, weatherStationSerde)
                )
                .groupByKey()
                .aggregate(
                        Aggregation::new,
                        (stationId, value, aggregation) -> aggregation.updateFrom(value),
                        Materialized.<Integer, Aggregation> as(storeSupplier)
                                .withKeySerde(Serdes.Integer())
                                .withValueSerde(aggregationSerde)
                )
                .toStream()
                .to(
                        temperatureValuesEnrichedTopic,
                        Produced.with(Serdes.Integer(), aggregationSerde)
                );

        return builder.build();
    }

    private String getIcon(double temperature) {
        return temperature < 0 ? "⛄⛄⛄" :
            temperature < 10 ? "🌱🌱🌱" :
            temperature < 20 ? "🌷🌷🌷" :
            temperature < 30 ? "🌳🌳🌳" :
                "🌴🌴🌴";
    }
}
