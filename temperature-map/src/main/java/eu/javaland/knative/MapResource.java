package eu.javaland.knative;

import java.io.StringReader;
import java.net.URI;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.runtime.StartupEvent;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@Path("/")
public class MapResource {

    @ConfigProperty(name = "map.read.from.topic", defaultValue = "false")
    boolean readFromTopic;

    @Inject
    MapEndpoint websocketEndpoint;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello() {
        return Response.status(301).location(URI.create("/ui/map.html")).build();
    }

    // TODO manually parsing the payload for now, as JAX-RS didn't seem to like the CE media type
    @POST
    @Consumes(MediaType.WILDCARD)
    public Response newMeasurement(String request) {
        System.out.println("Received measurement via HTTP: " + request);

        JsonReader reader = Json.createReader(new StringReader(request));
        JsonObject measurement = reader.readObject();

        websocketEndpoint.onMeasurement(initializeMeasurementFromJson(measurement));

        return Response.ok().build();
    }

    @Incoming("temperature-values")
    public void onMeasurement(JsonObject measurement) {
        if (!readFromTopic) {
            return;
        }

        websocketEndpoint.onMeasurement(initializeMeasurementFromJson(measurement));
    }

    private Measurement initializeMeasurementFromJson(JsonObject measurement) {
        Measurement temperatureMeasurement = new Measurement();

        temperatureMeasurement.stationId = measurement.getInt("stationId");
        temperatureMeasurement.stationName = measurement.getString("stationName");
        temperatureMeasurement.latitude = measurement.getJsonNumber("latitude").doubleValue();
        temperatureMeasurement.longitude = measurement.getJsonNumber("longitude").doubleValue();
        temperatureMeasurement.min = measurement.getJsonNumber("min").doubleValue();
        temperatureMeasurement.max = measurement.getJsonNumber("max").doubleValue();
        temperatureMeasurement.average = measurement.getJsonNumber("avg").doubleValue();

        temperatureMeasurement.ts = measurement.getString("ts");
        temperatureMeasurement.value = measurement.getJsonNumber("value").doubleValue();
        temperatureMeasurement.icon = measurement.getString("icon");
        return temperatureMeasurement;
    }

    public void onStartup(@Observes StartupEvent se) {
        System.out.println("### Starting up ###");
    }
}
