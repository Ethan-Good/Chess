package server;

import server.handler.*;
import service.GameService;
import service.UserService;
import spark.*;
import service.ClearService;
import dataaccess.*;
import dataaccess.memory.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();
        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);

        Spark.delete("/db", new ClearHandler(clearService));
        Spark.post("/user", new RegisterHandler(userService));
        Spark.post("/session", new LoginHandler(userService));
        Spark.delete("/session", new LogoutHandler(userService));
        Spark.post("/game", new CreateGameHandler(gameService));
        Spark.get("/game", new ListGamesHandler(gameService));
        Spark.put("/game", new JoinGameHandler(gameService));

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
