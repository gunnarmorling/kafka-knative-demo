package org.acme.quarkus.sample.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;

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

    public ValuesGenerator() {
        // align with init.sql when doing changes
        stations.put(1, new WeatherStation(1, "Hamburg", 13));
        stations.put(2, new WeatherStation(2, "Snowdonia", 5));
        stations.put(3, new WeatherStation(3, "Boston", 11));
        stations.put(4, new WeatherStation(4, "Tokio", 16));
        stations.put(5, new WeatherStation(5, "Cusco", 12));
        stations.put(6, new WeatherStation(6, "Svalbard", -7));
        stations.put(7, new WeatherStation(7, "Porthsmouth", 11));
        stations.put(8, new WeatherStation(8, "Oslo", 7));
        stations.put(9, new WeatherStation(9, "Marrakesh", 20));
        stations.put(10, new WeatherStation(10, "Johannesburg", 25));
        stations.put(11, new WeatherStation(11, "Anchorage", -2));
        stations.put(12, new WeatherStation(12, "San Francisco", 15));
        stations.put(13, new WeatherStation(13, "Canberra", 25));
        stations.put(14, new WeatherStation(14, "Novosibirsk", 10));
    }

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
}
