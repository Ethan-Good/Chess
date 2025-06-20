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
                    GameData game = gameService.getGameDAO().getGame(gameID);
                    ChessGame chessGame = game.game();

                    if (chessGame.isGameOver()) {
                        sessions.broadcast(gameID, gson.toJson(ServerMessage.error("Game is over. No more moves are allowed.")));
                        return;
                    }

                    ChessMove move = command.getMove();
                    chessGame.makeMove(move);

                    ChessGame.TeamColor opponentColor = chessGame.getTeamTurn();
                    String opponentUsername = (opponentColor == ChessGame.TeamColor.WHITE) ? game.whiteUsername() : game.blackUsername();

                    if (chessGame.isInCheckmate(opponentColor)) {
                        chessGame.setGameOver(true);
                        sessions.broadcast(gameID, gson.toJson(ServerMessage.notification("Checkmate! " + opponentUsername + " loses.")));
                    } else if (chessGame.isInCheck(opponentColor)) {
                        sessions.broadcast(gameID, gson.toJson(ServerMessage.notification(opponentUsername + " is in check.")));
                    }

                    GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chessGame);
                    gameService.getGameDAO().updateGame(updatedGame);

                    sessions.broadcast(gameID, gson.toJson(ServerMessage.loadGame(updatedGame)));
                    String moveString = formatPosition(move.getStartPosition()) + " " + formatPosition(move.getEndPosition());
                    sessions.broadcast(gameID, gson.toJson(ServerMessage.notification(username + " moved " + moveString)));
                }


                case RESIGN -> {
                    GameData game = gameService.getGameDAO().getGame(gameID);
                    ChessGame chessGame = game.game();
                    chessGame.setGameOver(true);

                    GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chessGame);
                    gameService.getGameDAO().updateGame(updatedGame);

                    sessions.broadcast(gameID, gson.toJson(ServerMessage.notification(username + " has resigned.")));
                }

                case LEAVE -> {
                    sessions.removeSession(gameID, session);

                    GameData game = gameService.getGameDAO().getGame(gameID);
                    if (game != null) {
                        ChessGame chessGame = game.game();

                        String white = game.whiteUsername();
                        String black = game.blackUsername();

                        if (username.equals(white)) {
                            white = null;
                        } else if (username.equals(black)) {
                            black = null;
                        }

                        GameData updatedGame = new GameData(
                                game.gameID(),
                                white,
                                black,
                                game.gameName(),
                                chessGame
                        );
                        gameService.getGameDAO().updateGame(updatedGame);
                    }

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
        char colChar = (char) ('a' + pos.getColumn() - 1);
        int rowNum = pos.getRow();
        return String.valueOf(colChar) + rowNum;
    }
}
