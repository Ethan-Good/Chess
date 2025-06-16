package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.GameData;
import ui.GameplayRepl;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import javax.websocket.*;

@ClientEndpoint
public class WebsocketClientEndpoint extends Endpoint {

    private final Gson gson = new Gson();
    private final GameplayRepl gameplayRepl;
    private final WebsocketCommunicator communicator;

    public WebsocketClientEndpoint(GameplayRepl gameplayRepl, WebsocketCommunicator communicator) {

        this.gameplayRepl = gameplayRepl;
        this.communicator = communicator;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("WebSocket connected");
        communicator.setClientSession(session);

        session.addMessageHandler(String.class, message -> {
//            System.out.println("[Server] " + message);
            handleMessage(message);
        });
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket disconnected: " + closeReason);
        communicator.setClientSession(null);
    }

    @Override
    public void onError(Session session, Throwable thr) {
        System.err.println("WebSocket error: " + thr.getMessage());
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("[Server] " + message);
        JsonObject json = gson.fromJson(message, JsonObject.class);

        String type = json.get("serverMessageType").getAsString();
        switch (type) {
            case "LOAD_GAME" -> {
                GameData gameData = gson.fromJson(json.get("game"), GameData.class);
                gameplayRepl.setCurrentGame(gameData.game());
                gameplayRepl.printBoard();
            }
            case "NOTIFICATION" -> {
                String note = json.get("message").getAsString();
                System.out.println("[Notification] " + note);
            }
            case "ERROR" -> {
                String error = json.get("error").getAsString();
                System.err.println("[Error] " + error);
            }
        }
    }
    private void handleMessage(String message) {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(message, JsonObject.class);

        String type = json.get("serverMessageType").getAsString();
        switch (type) {
            case "LOAD_GAME" -> {
                GameData gameData = gson.fromJson(json.get("game"), GameData.class);
                gameplayRepl.setCurrentGame(gameData.game());
                gameplayRepl.printBoard();
            }
            case "NOTIFICATION" -> {
                String note = json.get("message").getAsString();
                System.out.println("[Notification] " + note);
            }
            case "ERROR" -> {
                String error = json.get("error").getAsString();
                System.err.println("[Error] " + error);
            }
        }
    }

}
