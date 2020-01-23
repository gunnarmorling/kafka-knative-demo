package eu.javaland.knative;

import java.net.URI;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Incoming;

@Path("/")
public class MapResource {

    @Inject
    MapEndpoint websocketEndpoint;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
        return Response.status(301).location(URI.create("/ui")).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newMeasurement(Measurement measurement) {
        websocketEndpoint.onMeasurement(measurement);
        return Response.ok().build();
    }

    @Incoming("temperature-values")
    public void onWeatherStation(JsonObject measurement) {
        Measurement temperatureMeasurement = new Measurement();

/*
        public double max;
        public double average;
        public String icon;
*/
        temperatureMeasurement.stationId = measurement.getInt("stationId");
        temperatureMeasurement.stationName = measurement.getString("stationName");
        temperatureMeasurement.latitude = measurement.getJsonNumber("latitude").doubleValue();
        temperatureMeasurement.longitude = measurement.getJsonNumber("longitude").doubleValue();

        temperatureMeasurement.ts = measurement.getString("ts");
        temperatureMeasurement.value = measurement.getJsonNumber("value").doubleValue();
        temperatureMeasurement.icon = measurement.getString("icon");

        websocketEndpoint.onMeasurement(temperatureMeasurement);
    }
}
