package org.acme.quarkus.sample.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Flowable;
import io.smallrye.reactive.messaging.kafka.KafkaMessage;

/**
 * A bean producing random temperature data every second.
 * The values are written to a Kafka topic (temperature-values).
 * The Kafka configuration is specified in the application configuration.
 */
@ApplicationScoped
public class ValuesGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ValuesGenerator.class);

    private Map<Integer, WeatherStation> stations = new HashMap<>();
    private Random random = new Random();

    @Outgoing("temperature-values")
    public Flowable<KafkaMessage<Integer, String>> generate() {

        return Flowable.interval(1000, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .filter(l -> !stations.isEmpty())
                .map(tick -> {
                    int stationId = random.nextInt(stations.size()) + 1;
                    WeatherStation station = stations.get(stationId);

                    double temperature = new BigDecimal(random.nextGaussian() * 15 + station.averageTemperature)
                            .setScale(1, RoundingMode.HALF_UP)
                            .doubleValue();

                    LOG.info("station: {}, temperature: {}", station.name, temperature);
                    return KafkaMessage.of(station.id, Instant.now() + ";" + temperature);
                });
    }

    /**
     * Retrieving weather station master data via CDC. This defeats a bit the
     * purpose of showing how to join the two topics via Kafka Streams in the
     * aggregator, but it's done here so to obtain the existing weather station ids
     * and their average temperature, as it's needed as the basis for the "sensor
     * measurements".
     */
    @Incoming("weather-stations")
    public void onWeatherStation(JsonObject station) {
        JsonObject after = station.getJsonObject("after");

        WeatherStation weatherStation = new WeatherStation(
                after.getInt("id"),
                after.getString("name"),
                after.getJsonNumber("average_temperature").doubleValue()
        );
        System.out.println("Received weather station: " + weatherStation);

        stations.put(weatherStation.id, weatherStation);
    }
}
