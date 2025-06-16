package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.GameData;
import ui.GameplayRepl;

import javax.websocket.*;

@ClientEndpoint
public class WebsocketClientEndpoint {

    private final Gson gson = new Gson();
    private final GameplayRepl gameplayRepl;

    public WebsocketClientEndpoint(GameplayRepl gameplayRepl) {
        this.gameplayRepl = gameplayRepl;
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket opened: " + session.getId());
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

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket closed: " + closeReason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }
}
