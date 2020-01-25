package org.acme.quarkus.sample.kafkastreams.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class WeatherStation {

    public int id;
    public String name;
    public double longitude;
    public double latitude;

    @JsonProperty("average_temperature")
    public double averageTemperature;

    public boolean active;

    @Override
    public String toString() {
        return "WeatherStation [id=" + id + ", name=" + name + ", longitude=" + longitude + ", latitude=" + latitude
                + ", averageTemperature=" + averageTemperature + ", active=" + active + "]";
    }
}
