package server.handler;

import com.google.gson.Gson;
import dataaccess.*;
import model.Request.LoginRequest;
import model.Result.LoginResult;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {

            LoginRequest loginRequest = gson.fromJson(req.body(), LoginRequest.class);

            if (loginRequest.username() == null || loginRequest.password() == null ||
                    loginRequest.username().isBlank() || loginRequest.password().isBlank()) {
                res.status(400);
                return gson.toJson(new RegisterHandler.ErrorMessage("Error: bad request"));
            }

            LoginResult result = userService.login(loginRequest);

            res.status(200);
            return gson.toJson(result);

        } catch (dataaccess.BadRequestException e) {
            res.status(400);
            return gson.toJson(new ErrorResult("Error: Incorrect Username or Password"));

        } catch (dataaccess.UnauthorizedException e) {
            res.status(401);
            return gson.toJson(new ErrorResult("Error: Unauthorized"));

        } catch (dataaccess.DataAccessException e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }

    private record ErrorResult(String message) {}
}
