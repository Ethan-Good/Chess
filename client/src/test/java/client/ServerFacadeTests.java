package client;

import model.Request.*;
import model.Result.*;
import org.junit.jupiter.api.*;
import server.Server;
import chess.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0); // random available port
        System.out.println("Started test HTTP server on port " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws ResponseException {
        facade.clear();
    }

    @Test
    public void registerPositive() throws ResponseException {
        var request = new RegisterRequest("user", "pass", "email");
        var result = facade.register(request);
        assertEquals("user", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerNegative() throws ResponseException {
        var request = new RegisterRequest("user", "pass", "email");
        facade.register(request);
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.register(request));
        assertEquals(403, ex.getStatusCode());
    }

    @Test
    public void loginPositive() throws ResponseException {
        facade.register(new RegisterRequest("user", "pass", "email"));
        var request = new LoginRequest("user", "pass");
        var result = facade.login(request);
        assertEquals("user", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginNegative() throws ResponseException {
        facade.register(new RegisterRequest("user", "pass", "email"));
        var request = new LoginRequest("user", "wrong");
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.login(request));
        assertEquals(401, ex.getStatusCode());
    }


    @Test
    public void createGamePositive() throws ResponseException {
        var auth = facade.register(new RegisterRequest("user", "pass", "email"));
        var result = facade.createGame(new CreateGameRequest("My Game"), auth.authToken());
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameNegative() {
        var request = new CreateGameRequest("Bad Game");
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.createGame(request, "invalidToken"));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    public void listGamesPositive() throws ResponseException {
        var auth = facade.register(new RegisterRequest("user", "pass", "email"));
        facade.createGame(new CreateGameRequest("Game 1"), auth.authToken());
        facade.createGame(new CreateGameRequest("Game 2"), auth.authToken());

        List<ListGamesResult.Game> games = facade.listGames(auth.authToken());
        assertEquals(2, games.size());
        assertEquals("Game 1", games.get(0).gameName());
        assertEquals("Game 2", games.get(1).gameName());
    }

    @Test
    public void listGamesNegative() {
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.listGames("badToken"));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    public void logoutPositve() throws ResponseException {
        var auth = facade.register(new RegisterRequest("user", "pass", "email"));
        facade.logout(auth.authToken());
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.listGames(auth.authToken()));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    public void logoutNegative() {
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.logout("badToken"));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    public void joinGamePositive() throws ResponseException {
        var auth = facade.register(new RegisterRequest("user", "pass", "email"));
        var game = facade.createGame(new CreateGameRequest("My Game"), auth.authToken());

        var request = new JoinGameRequest(ChessGame.TeamColor.WHITE, game.gameID());
        facade.joinGame(request, auth.authToken());
    }

    @Test
    public void joinGameNegative() throws ResponseException {
        var auth = facade.register(new RegisterRequest("user", "pass", "email"));
        var request = new JoinGameRequest(ChessGame.TeamColor.BLACK, 99999);
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.joinGame(request, auth.authToken()));
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    public void joinGameInvalidToken() {
        var request = new JoinGameRequest(ChessGame.TeamColor.WHITE, 1);
        ResponseException ex = assertThrows(ResponseException.class, () -> facade.joinGame(request, "invalidToken"));
        assertEquals(401, ex.getStatusCode());
    }
}

