package server.handler;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.ClearService;
import spark.Request;
import spark.Response;
import spark.Route;

public class ClearHandler implements Route {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    @Override
    public Object handle(Request req, Response res) throws DataAccessException {
        clearService.clear();
        res.status(200);
        res.type("application/json");
        return gson.toJson(new ClearResult());
    }

    private record ClearResult() {}
}
