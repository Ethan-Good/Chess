package server.handler;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import java.io.IOException;

@WebSocket
public class WebsocketHandler {
    private static final Gson gson = new Gson();

    private final GameService gameService;
    private final AuthDAO authDAO;
    private final WebsocketSessions sessions;

    public WebsocketHandler(GameService gameService, AuthDAO authDAO, WebsocketSessions sessions) {

        this.gameService = gameService;
        this.authDAO = authDAO;
        this.sessions = sessions;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
        sessions.removeSessionFromAllGames(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("[Received from client]: " + message);
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            String authToken = command.getAuthToken();
            int gameID = command.getGameID();

            AuthData auth = authDAO.getAuth(authToken);
            if (auth == null) {
                session.getRemote().sendString(gson.toJson(ServerMessage.error("Invalid auth token")));
                return;
            }

            String username = auth.username();

            switch (command.getCommandType()) {
                case CONNECT -> {
                    sessions.addSession(gameID, session);
                    GameData game = gameService.getGameDAO().getGame(gameID);
                    String color;
                    if (username.equals(game.blackUsername())) {
                        color = "black";
                    } else if (username.equals(game.whiteUsername())) {
                        color = "white";
                    }
                    else {
                        color = "observer";
                    }
                    session.getRemote().sendString(gson.toJson(ServerMessage.loadGame(game)));
                    sessions.broadcast(gameID, gson.toJson(ServerMessage.notification(username + " connected as " + color)));
                }

                case MAKE_MOVE -> {
                    ChessMove move = command.getMove();
                    GameData game = gameService.getGameDAO().getGame(gameID);
                    ChessGame chessGame = game.game();
                    chessGame.makeMove(move);

                    GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chessGame);
                    gameService.getGameDAO().updateGame(updatedGame);

                    sessions.broadcast(gameID, gson.toJson(ServerMessage.loadGame(updatedGame)));
                    sessions.broadcast(gameID, gson.toJson(ServerMessage.notification(username + " moved e2 e4")));
                }

                case RESIGN -> {
                    sessions.broadcast(gameID, gson.toJson(ServerMessage.notification(username + " has resigned.")));
                }

                case LEAVE -> {
                    sessions.removeSession(gameID, session);
                    sessions.broadcast(gameID, gson.toJson(ServerMessage.notification(username + " has left the game")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                session.getRemote().sendString(gson.toJson(ServerMessage.error("Error: " + e.getMessage())));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    private String formatPosition(ChessPosition pos) {
        char colChar = (char) ('a' + pos.getColumn() - 1);  // column 1 → 'a'
        int rowNum = pos.getRow();                         // row is already 1-based
        return String.valueOf(colChar) + rowNum;
    }
}
