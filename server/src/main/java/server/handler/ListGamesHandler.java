package server.handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.Result.ListGamesResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ListGamesHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");

            ListGamesResult result = gameService.getAllGames(authToken);
            res.status(200);
            return gson.toJson(result);

        } catch (UnauthorizedException e) {
            res.status(401);
            return gson.toJson(new ErrorMessage(e.getMessage()));

        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}

