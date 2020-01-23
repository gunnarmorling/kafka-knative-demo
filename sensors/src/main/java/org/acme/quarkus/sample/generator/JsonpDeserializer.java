package org.acme.quarkus.sample.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.kafka.common.serialization.Deserializer;

public class JsonpDeserializer implements Deserializer<JsonObject> {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public JsonObject deserialize(String topic, byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                JsonReader reader = Json.createReader(bais)) {
            return reader.readObject();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
    }
}
