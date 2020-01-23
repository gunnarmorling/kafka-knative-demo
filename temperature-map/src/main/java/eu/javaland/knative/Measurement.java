package eu.javaland.knative;

public class Measurement {
    public int stationId;
    public String stationName;
    public double longitude;
    public double latitude;
    public String ts;
    public double value;
    public double min;
    public double max;
    public double average;
    public String icon;

    @Override
    public String toString() {
        return "Measurement [average=" + average + ", icon=" + icon + ", latitude=" + latitude + ", longitude="
                + longitude + ", max=" + max + ", min=" + min + ", stationId=" + stationId + ", stationName="
                + stationName + ", ts=" + ts + ", value=" + value + "]";
    }
}
