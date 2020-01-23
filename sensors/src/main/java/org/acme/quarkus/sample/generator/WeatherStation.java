package org.acme.quarkus.sample.generator;

public class WeatherStation {

    public int id;
    public String name;
    public double averageTemperature;

    public WeatherStation() {
    }

    public WeatherStation(int id, String name, double averageTemperature) {
        this.id = id;
        this.name = name;
        this.averageTemperature = averageTemperature;
    }

    @Override
    public String toString() {
        return "WeatherStation [id=" + id + ", name=" + name + ", averageTemperature=" + averageTemperature + "]";
    }
}
