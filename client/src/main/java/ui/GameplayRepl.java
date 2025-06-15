package ui;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.ChessGame;
import chess.ChessGame.TeamColor;
import model.GameData;
import service.GameService;
import client.WebsocketCommunicator;

import javax.websocket.Session;

public class GameplayRepl {

    private final GameService gameService;
    private final WebsocketCommunicator communicator;

    public GameplayRepl(GameService gameService, WebsocketCommunicator communicator) {
        this.gameService = gameService;
        this.communicator = communicator;
    }

    /**
     * Process a UserGameCommand from a client.
     * @param command the user command to process
     * @param session the websocket session of the user sending the command
     * @return ServerMessage to send back to the user session
     * @throws Exception on auth or data errors
     */
    public ServerMessage processCommand(UserGameCommand command, Session session) throws Exception {
        String authToken = command.getAuthToken();
        var auth = gameService.authenticate(authToken);
        String username = auth.username();

        int gameID = command.getGameID();
        GameData gameData = gameService.getGameDAO().getGame(gameID);
        if (gameData == null) {
            return ServerMessage.error("Game not found");
        }
        ChessGame chessGame = gameData.game();

        switch (command.getCommandType()) {
            case CONNECT:
                communicator.addSessionToGame(gameID, session);
                return ServerMessage.loadGame(gameData);

            case MAKE_MOVE:
                if (command.getMove() == null) {
                    return ServerMessage.error("No move provided");
                }

                boolean isPlayer = username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername());
                if (!isPlayer) {
                    return ServerMessage.error("You are not a player in this game");
                }

                boolean moveSuccess = chessGame.makeMove(command.getMove(), username);
                if (!moveSuccess) {
                    return ServerMessage.error("Invalid move");
                }

                gameService.getGameDAO().updateGame(new GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        chessGame));

                ServerMessage updateMsg = ServerMessage.loadGame(gameData);
                communicator.broadcastToGame(gameID, serializeMessage(updateMsg));
                return updateMsg;

            case LEAVE:
                communicator.removeSession(session);
                return ServerMessage.notification("You left the game");

            case RESIGN:
                if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                    return ServerMessage.error("You are not a player in this game");
                }

                chessGame.resign(username);

                gameService.getGameDAO().updateGame(new GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        chessGame));

                ServerMessage resignMsg = ServerMessage.notification(username + " resigned. Game over.");
                communicator.broadcastToGame(gameID, serializeMessage(resignMsg));
                return resignMsg;

            case HELP:
                String helpText = """
                    Commands:
                    - Help: Show this help message
                    - Redraw: Redraw the chess board
                    - Leave: Leave the game and return to main menu
                    - Move: Make a move (send move data)
                    - Resign: Resign from the game
                    - Highlight: Highlight legal moves for a piece (client-only)
                    """;
                return ServerMessage.notification(helpText);

            case REDRAW:
                return ServerMessage.loadGame(gameData);

            case HIGHLIGHT:
                return ServerMessage.notification("Highlight legal moves (client-only)");

            default:
                return ServerMessage.error("Unknown command");
        }
    }

    private String serializeMessage(ServerMessage msg) {
        return new com.google.gson.Gson().toJson(msg);
    }
}
