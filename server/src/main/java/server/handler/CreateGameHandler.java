package server.handler;

import com.google.gson.Gson;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.Request.CreateGameRequest;
import model.Result.CreateGameResult;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.Map;

public class CreateGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            CreateGameRequest requestObj = gson.fromJson(req.body(), CreateGameRequest.class);
            CreateGameResult result = gameService.createGame(authToken, requestObj);
            res.status(200);
            return gson.toJson(result);
        } catch (BadRequestException e) {
            res.status(400);
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (UnauthorizedException e) {
            res.status(401);
            return gson.toJson(Map.of("message", e.getMessage()));
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
