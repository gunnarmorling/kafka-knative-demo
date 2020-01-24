package org.acme.quarkus.sample.kafkastreams.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.json.JsonObject;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Aggregation {

    public int stationId;
    public String stationName;
    public String icon;
    public String ts;
    public double value;
    public double min = Double.MAX_VALUE;
    public double max = Double.MIN_VALUE;
    public int count;
    public double sum;
    public double avg;
    public double longitude;
    public double latitude;

    public Aggregation updateFrom(JsonObject measurement) {
        stationId = measurement.getInt("stationId");
        stationName = measurement.getString("stationName");
        longitude = measurement.getJsonNumber("longitude").doubleValue();
        latitude = measurement.getJsonNumber("latitude").doubleValue();

        icon = measurement.getString("icon");
        ts = measurement.getString("ts");
        value = measurement.getJsonNumber("value").doubleValue();

        count++;
        sum += measurement.getJsonNumber("value").doubleValue();
        avg = BigDecimal.valueOf(sum / count)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        min = Math.min(min, measurement.getJsonNumber("value").doubleValue());
        max = Math.max(max, measurement.getJsonNumber("value").doubleValue());

        return this;
    }
}
