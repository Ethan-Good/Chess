package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.Request.CreateGameRequest;
import model.Result.CreateGameResult;
import chess.ChessGame;
import model.Result.ListGamesResult;
import java.util.List;

import java.util.ArrayList;
import java.util.UUID;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws DataAccessException, UnauthorizedException, BadRequestException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new dataaccess.UnauthorizedException("Error: unauthorized");
        }

        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new dataaccess.BadRequestException("Error: bad request");
        }

        ChessGame chessGame = new ChessGame();
        GameData gameData = new GameData(0, null, null, request.gameName(), chessGame);
        int gameID = gameDAO.createGame(gameData);

        return new CreateGameResult(gameID);
    }

    public ListGamesResult getAllGames(String authToken) throws DataAccessException, UnauthorizedException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new dataaccess.UnauthorizedException("Error: unauthorized");
        }


        List<GameData> allGameData = gameDAO.listGames();

        List<ListGamesResult.Game> gameInfos = new ArrayList<>();
        for (GameData gameData : allGameData) {
            ListGamesResult.Game gameInfo = new ListGamesResult.Game(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName());
            gameInfos.add(gameInfo);
        }
        return new ListGamesResult(gameInfos);
    }
}