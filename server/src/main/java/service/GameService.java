package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.Request.CreateGameRequest;
import model.Result.CreateGameResult;
import model.Request.JoinGameRequest;
import chess.ChessGame;
import model.Result.ListGamesResult;
import java.util.List;
import java.util.ArrayList;

public class GameService extends BaseService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        super(authDAO);
        this.gameDAO = gameDAO;
    }

    public GameDAO getGameDAO() {
        return gameDAO;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws DataAccessException, UnauthorizedException, BadRequestException {
        AuthData auth = authenticate(authToken);

        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new dataaccess.BadRequestException("Error: bad request");
        }

        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(0, null, null, request.gameName(), chessGame);
        int gameID = gameDAO.createGame(gameData);

        return new CreateGameResult(gameID);
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException, UnauthorizedException {
        AuthData auth = authenticate(authToken);

        List<GameData> allGameData = gameDAO.getAllGames();

        List<ListGamesResult.Game> gameInfos = new ArrayList<>();
        for (GameData gameData : allGameData) {
            ListGamesResult.Game gameInfo = new ListGamesResult.Game(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName());
            gameInfos.add(gameInfo);
        }
        return new ListGamesResult(gameInfos);
    }

    public void joinGame(JoinGameRequest request, String authToken) throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        AuthData auth = authenticate(authToken);

        if (request.playerColor() == null) {
            throw new dataaccess.BadRequestException("Error: bad request, color is null");
        }

        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new dataaccess.BadRequestException("Error: bad request, game is null");
        }

        String username = auth.username();
        if (request.playerColor() == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new dataaccess.AlreadyTakenException("Error: already taken");
            }
            game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else {
            if (game.blackUsername() != null) {
                throw new dataaccess.AlreadyTakenException("Error: already taken");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        }
        gameDAO.updateGame(game);
    }
}