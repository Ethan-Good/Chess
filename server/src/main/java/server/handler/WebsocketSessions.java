package server.handler;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketSessions {
    private final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();

    public void addSession(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(int gameID, Session session) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                gameSessions.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, String message) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            for (Session session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.getRemote().sendString(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // Optional: log instead
                }
            }
        }
    }
}
