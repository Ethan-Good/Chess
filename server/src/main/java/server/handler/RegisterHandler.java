package server.handler;

import com.google.gson.Gson;
import dataaccess.*;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;
import model.Request.RegisterRequest;
import model.Result.RegisterResult;

public class RegisterHandler implements Route {
    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Object handle(Request req, Response res) {
        try {

            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

            if (request.username() == null || request.password() == null || request.email() == null ||
                    request.username().isBlank() || request.password().isBlank() || request.email().isBlank()) {
                res.status(400);
                return gson.toJson(new ErrorMessage("Error: bad request"));
            }

            RegisterResult result = userService.register(request);
            res.status(200);
            return gson.toJson(result);

        } catch (dataaccess.AlreadyTakenException e) {
            res.status(403);
            return gson.toJson(new ErrorMessage("Error: already taken"));
        } catch (dataaccess.DataAccessException e) {
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    record ErrorMessage(String message) {}
}
