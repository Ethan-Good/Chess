package server.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessage.ServerMessageType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

    private final Gson gson = new Gson();

    private final Map<Session, UserSessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    private final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received from client: " + message);

        UserGameCommand command;
        try {
            command = gson.fromJson(message, UserGameCommand.class);
        } catch (JsonSyntaxException e) {
            sendError(session, "Error: Invalid JSON format");
            return;
        }

        if (command == null || command.getCommandType() == null) {
            sendError(session, "Error: Missing commandType");
            return;
        }

        try {
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command);
                case MAKE_MOVE -> handleMakeMove(session, command);
                case LEAVE -> handleLeave(session, command);
                case RESIGN -> handleResign(session, command);
                default -> sendError(session, "Error: Unknown commandType");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Error: Internal server error");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + session + " reason: " + reason);
        UserSessionInfo info = sessionInfoMap.remove(session);
        if (info != null) {
            removeSessionFromGame(session, info.gameID);
            broadcastNotification(info.gameID, info.username + " has disconnected.");
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket error on session " + session);
        error.printStackTrace();
    }

    private void handleConnect(Session session, UserGameCommand command) throws IOException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        // Basic validation
        if (authToken == null || gameID == null) {
            sendError(session, "Error: Missing authToken or gameID for CONNECT");
            return;
        }

        String username = authenticate(authToken);
        if (username == null) {
            sendError(session, "Error: Invalid authToken");
            return;
        }

        Game game = GameManager.getGame(gameID);
        if (game == null) {
            sendError(session, "Error: Invalid gameID");
            return;
        }

        sessionInfoMap.put(session, new UserSessionInfo(gameID, username));
        addSessionToGame(session, gameID);

        ServerMessage loadGameMsg = ServerMessage.loadGame(game);
        sendMessage(session, gson.toJson(loadGameMsg));

        String role = game.isPlayer(username) ?
                (game.isWhitePlayer(username) ? "white" : "black") : "observer";

        String notifyMsg = username + " connected as " + role + ".";
        broadcastNotificationExcept(gameID, notifyMsg, session);
    }

    private void handleMakeMove(Session session, UserGameCommand command) throws IOException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        if (authToken == null || gameID == null) {
            sendError(session, "Error: Missing authToken or gameID for MAKE_MOVE");
            return;
        }

        String username = authenticate(authToken);
        if (username == null) {
            sendError(session, "Error: Invalid authToken");
            return;
        }

        Game game = GameManager.getGame(gameID);
        if (game == null) {
            sendError(session, "Error: Invalid gameID");
            return;
        }

        if (!game.isPlayer(username)) {
            sendError(session, "Error: You are not a player in this game");
            return;
        }

        if (!sessionInfoMap.containsKey(session)) {
            sendError(session, "Error: Session not connected properly");
            return;
        }

        Map<?, ?> rawMap = gson.fromJson(gson.toJson(command), Map.class);
        Object moveObj = rawMap.get("move");
        if (moveObj == null) {
            sendError(session, "Error: Missing move for MAKE_MOVE");
            return;
        }
        ChessMove move = gson.fromJson(gson.toJson(moveObj), ChessMove.class);

        if (!game.isValidMove(username, move)) {
            sendError(session, "Error: Invalid move");
            return;
        }

        game.applyMove(move);

        GameManager.updateGame(game);

        ServerMessage loadGameMsg = ServerMessage.loadGame(game);
        broadcastToGame(gameID, gson.toJson(loadGameMsg));

        String notifyMsg = username + " made move: " + move.toString();
        broadcastNotificationExcept(gameID, notifyMsg, session);

        if (game.isInCheck()) {
            broadcastNotification(gameID, "Check to " + (game.isWhiteTurn() ? "white" : "black") + " player.");
        }
        if (game.isCheckmate()) {
            broadcastNotification(gameID, "Checkmate! " + username + " wins.");
        }
        if (game.isStalemate()) {
            broadcastNotification(gameID, "Stalemate! The game is a draw.");
        }
    }

    private void handleLeave(Session session, UserGameCommand command) throws IOException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        if (authToken == null || gameID == null) {
            sendError(session, "Error: Missing authToken or gameID for LEAVE");
            return;
        }

        String username = authenticate(authToken);
        if (username == null) {
            sendError(session, "Error: Invalid authToken");
            return;
        }

        Game game = GameManager.getGame(gameID);
        if (game == null) {
            sendError(session, "Error: Invalid gameID");
            return;
        }

        game.removePlayerOrObserver(username);
        GameManager.updateGame(game);

        removeSessionFromGame(session, gameID);
        sessionInfoMap.remove(session);

        broadcastNotification(gameID, username + " left the game.");
    }

    private void handleResign(Session session, UserGameCommand command) throws IOException {
        String authToken = command.getAuthToken();
        Integer gameID = command.getGameID();

        if (authToken == null || gameID == null) {
            sendError(session, "Error: Missing authToken or gameID for RESIGN");
            return;
        }

        String username = authenticate(authToken);
        if (username == null) {
            sendError(session, "Error: Invalid authToken");
            return;
        }

        Game game = GameManager.getGame(gameID);
        if (game == null) {
            sendError(session, "Error: Invalid gameID");
            return;
        }

        if (!game.isPlayer(username)) {
            sendError(session, "Error: You are not a player in this game");
            return;
        }

        game.resign(username);
        GameManager.updateGame(game);

        broadcastNotification(gameID, username + " resigned. Game over.");
    }

    private void sendMessage(Session session, String json) {
        if (session != null && session.isOpen()) {
            try {
                session.getRemote().sendString(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendError(Session session, String errorMsg) {
        ServerMessage error = ServerMessage.error(errorMsg);
        sendMessage(session, gson.toJson(error));
    }

    private void broadcastNotification(int gameID, String message) {
        ServerMessage notification = ServerMessage.notification(message);
        broadcastToGame(gameID, gson.toJson(notification));
    }

    private void broadcastNotificationExcept(int gameID, String message, Session excludeSession) {
        ServerMessage notification = ServerMessage.notification(message);
        broadcastToGameExcept(gameID, gson.toJson(notification), excludeSession);
    }

    private void broadcastToGame(int gameID, String message) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            for (Session s : sessions) {
                sendMessage(s, message);
            }
        }
    }

    private void broadcastToGameExcept(int gameID, String message, Session excludeSession) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            for (Session s : sessions) {
                if (!s.equals(excludeSession)) {
                    sendMessage(s, message);
                }
            }
        }
    }

    private void addSessionToGame(Session session, int gameID) {
        gameSessions.computeIfAbsent(gameID, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
    }

    private void removeSessionFromGame(Session session, int gameID) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                gameSessions.remove(gameID);
            }
        }
    }

    private String authenticate(String authToken) {
        // Implement authentication logic here,
        // return username if valid, otherwise null
        return authToken;
    }
    
    private static class UserSessionInfo {
        final int gameID;
        final String username;

        UserSessionInfo(int gameID, String username) {
            this.gameID = gameID;
            this.username = username;
        }
    }
}
