package server.handler;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private final WebsocketCommunicator communicator = new WebsocketCommunicator();


    private final Map<Session, UserSessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received: " + message);

        if (message.startsWith("join")) {
            String[] parts = message.split(":");
            if (parts.length != 3) return;

            int gameID = Integer.parseInt(parts[1]);
            String username = parts[2];

            sessionInfoMap.put(session, new UserSessionInfo(gameID, username));
            communicator.addSessionToGame(gameID, session);

            communicator.broadcastToGame(gameID, username + " has joined the game.");
        } else if (message.startsWith("move")) {
            // Expected format: move:from-to (e.g., move:e2-e4)
            UserSessionInfo info = sessionInfoMap.get(session);
            if (info != null) {
                String move = message.substring(5);
                String broadcast = info.username + " moved " + move;
                communicator.broadcastToGame(info.gameID, broadcast);
            }
        } else {
            // Unknown command
            sendToSession(session, "Unrecognized command");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
        communicator.removeSession(session);
        UserSessionInfo info = sessionInfoMap.remove(session);
        if (info != null) {
            communicator.broadcastToGame(info.gameID, info.username + " has disconnected.");
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error:");
        error.printStackTrace();
    }

    private void sendToSession(Session session, String message) {
        if (session != null && session.isOpen()) {
            try {
                session.getRemote().sendString(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class UserSessionInfo {
        int gameID;
        String username;

        UserSessionInfo(int gameID, String username) {
            this.gameID = gameID;
            this.username = username;
        }
    }
}

