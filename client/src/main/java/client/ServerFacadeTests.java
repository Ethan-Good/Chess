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


    
}

