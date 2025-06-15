package client;

import jakarta.websocket.Session;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebsocketCommunicator {

    private final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();

    private final Map<Session, Integer> sessionToGame = new ConcurrentHashMap<>();

    public void addSessionToGame(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionToGame.put(session, gameID);
    }

    public void removeSession(Session session) {
        Integer gameID = sessionToGame.remove(session);
        if (gameID != null) {
            Set<Session> sessions = gameSessions.get(gameID);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    gameSessions.remove(gameID);
                }
            }
        }
    }

    public void broadcastToGame(int gameID, String message) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace(); // Consider proper logging
                    }
                }
            }
        }
    }

    public void sendToSession(Session session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace(); // Consider proper logging
            }
        }
    }
}
