package server.handler;

import com.google.gson.Gson;
import dataaccess.*;
import model.Request.LogoutRequest;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {

            String authToken = req.headers("Authorization");

            if (authToken == null || authToken.isEmpty()) {
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            LogoutRequest logoutRequest = new LogoutRequest(authToken);
            userService.logout(logoutRequest);

            res.status(200);
            return gson.toJson(new Object());

        } catch (dataaccess.UnauthorizedException e) {
            res.status(401);
            return gson.toJson(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
}
