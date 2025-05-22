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

    public ListGamesResult listGames(String authToken) throws DataAccessException, UnauthorizedException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new dataaccess.UnauthorizedException("Error: unauthorized");
        }


        List<GameData> allGameData = gameDAO.getAllGames();

        List<ListGamesResult.Game> gameInfos = new ArrayList<>();
        for (GameData gameData : allGameData) {
            ListGamesResult.Game gameInfo = new ListGamesResult.Game(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName());
            gameInfos.add(gameInfo);
        }
        return new ListGamesResult(gameInfos);
    }

    public void joinGame(JoinGameRequest request, String authToken) throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new dataaccess.UnauthorizedException("Error: unauthorized");
        }

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