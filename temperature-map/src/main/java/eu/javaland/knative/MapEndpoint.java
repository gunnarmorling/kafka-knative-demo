package eu.javaland.knative;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/map/ws")
@ApplicationScoped
public class MapEndpoint {

    @Inject
    Jsonb jsonb;

    private final Set<Session> sessions = Collections.newSetFromMap( new ConcurrentHashMap<>() );

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        //broadcast("User " + username + " left on error: " + throwable);
    }

    public void onMeasurement(Measurement measurement) {
        System.out.println("Received: " + measurement);
        String json = jsonb.toJson(measurement);
        broadcast(json);
    }

    private void broadcast(String message) {
        sessions.forEach(s -> {
            s.getAsyncRemote().sendObject(message, result ->  {
                if (result.getException() != null) {
                    System.out.println("Unable to send message: " + result.getException());
                }
            });
        });
    }
}
