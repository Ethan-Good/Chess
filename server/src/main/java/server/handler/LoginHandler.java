package server.handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
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

            LoginResult result = userService.login(loginRequest);

            res.status(200);
            return gson.toJson(result);

        } catch (UserService.UnauthorizedException e) {
            res.status(401);
            return gson.toJson(new ErrorResult("Error: Incorrect Username or Password"));

        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(new ErrorResult("Error: " + e.getMessage()));
        }
    }
    
    private record ErrorResult(String message) {}
}
