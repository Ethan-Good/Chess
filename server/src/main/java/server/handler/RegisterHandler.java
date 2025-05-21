package server.handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
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
            // Deserialize the request body into a RegisterRequest
            RegisterRequest request = gson.fromJson(req.body(), RegisterRequest.class);

            // Check for missing or blank fields
            if (request.username() == null || request.password() == null || request.email() == null ||
                    request.username().isBlank() || request.password().isBlank() || request.email().isBlank()) {
                res.status(400);
                return gson.toJson(new ErrorMessage("Error: bad request"));
            }

            // Call the service method
            RegisterResult result = userService.register(request);
            res.status(200);
            return gson.toJson(result);

        } catch (UserService.AlreadyTakenException e) {
            res.status(403);
            return gson.toJson(new ErrorMessage("Error: already taken"));
        } catch (DataAccessException e) {
            res.status(500);
            return gson.toJson(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    record ErrorMessage(String message) {}
}
