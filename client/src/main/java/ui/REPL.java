package ui;

import client.*;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.SQL.SQLAuthDAO;
import dataaccess.SQL.SQLGameDAO;
import service.GameService;

import javax.websocket.*;
import java.net.URI;
import java.util.Scanner;

public class REPL {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;
    private final GameService gameService;
    private final WebsocketCommunicator communicator;
    private String authToken = null;
    private String username = null;
    private final PreLoginUI preloginUI;
    private final PostLoginUI postloginUI;
    final GameplayRepl gameplayUI;
    private Session clientSession;

    public REPL() {
        this.facade = new ServerFacade(8080);

        GameDAO gameDAO = new SQLGameDAO();
        AuthDAO authDAO = new SQLAuthDAO();
        this.gameService = new GameService(gameDAO, authDAO);

        this.communicator = new WebsocketCommunicator();
        this.preloginUI = new PreLoginUI(scanner, facade, this);
        this.postloginUI = new PostLoginUI(scanner, facade, this);
        this.gameplayUI = new GameplayRepl(scanner, facade, gameService, communicator,this);
    }

    public void run() {
        System.out.println("Welcome to My cs240 Chess Client!");
        System.out.println("Type help for options");
        while (true) {
            if (authToken == null) {
                preloginUI.prompt();
            } else {
                if (clientSession == null || !clientSession.isOpen()) {
                    connectWebSocket();
                }
                postloginUI.prompt();
            }
        }
    }

    private void connectWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = new URI("ws://localhost:8080/connect");

            Endpoint endpoint = new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    System.out.println("WebSocket connected");
                    clientSession = session;
                    communicator.setClientSession(session);

                    session.addMessageHandler(String.class, message -> {
                        System.out.println("[Server] " + message);
                    });
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    System.out.println("WebSocket disconnected: " + closeReason);
                    clientSession = null;
                    communicator.setClientSession(null);
                }

                @Override
                public void onError(Session session, Throwable thr) {
                    System.err.println("WebSocket error: " + thr.getMessage());
                }
            };

            container.connectToServer(endpoint, uri);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to connect WebSocket.");
        }
    }

    public void setAuth(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
    }

    public void clearAuth() {
        this.authToken = null;
        this.username = null;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }
}
