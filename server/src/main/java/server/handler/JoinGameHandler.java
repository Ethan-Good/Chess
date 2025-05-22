package server.handler;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.Request.JoinGameRequest;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            gameService.joinGame(joinRequest, authToken);

            res.status(200);
            return gson.toJson(new Object());
        } catch (UnauthorizedException e) {
            res.status(401);
            return gson.toJson(new ErrorMessage(e.getMessage()));
        } catch (BadRequestException e) {
            res.status(400);
            return gson.toJson(new ErrorMessage(e.getMessage()));
        } catch (AlreadyTakenException e) {
            res.status(403);
            return gson.toJson(new ErrorMessage(e.getMessage()));
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }
    private record ErrorMessage(String message) {}
}
