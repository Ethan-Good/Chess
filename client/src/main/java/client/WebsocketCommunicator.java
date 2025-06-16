package client;

import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.websocket.*;

public class WebsocketCommunicator {

    private Session clientSession;

    public void setClientSession(Session session) {
        this.clientSession = session;
    }

    public void sendMessage(String message) {
        if (clientSession != null && clientSession.isOpen()) {
            clientSession.getAsyncRemote().sendText(message);
        } else {
            System.err.println("No open WebSocket session to send message.");
        }
    }

    public void sendMove(int gameID, ChessMove move, String authToken) {
        Gson gson = new Gson();
        JsonObject message = new JsonObject();
        message.addProperty("commandType", "MAKE_MOVE");
        message.addProperty("gameID", gameID);
        message.addProperty("authToken", authToken);
        message.add("move", gson.toJsonTree(move));
        sendMessage(message.toString());
    }

    public void sendConnect(int gameID, String authToken) {
        JsonObject message = new JsonObject();
        message.addProperty("commandType", "CONNECT");
        message.addProperty("gameID", gameID);
        message.addProperty("authToken", authToken);
        sendMessage(message.toString());
    }

    public void sendResign(int gameID, String authToken) {
        JsonObject message = new JsonObject();
        message.addProperty("commandType", "RESIGN");
        message.addProperty("gameID", gameID);
        message.addProperty("authToken", authToken);
        sendMessage(message.toString());
    }
    public void sendLeave(int gameID, String authToken) {
        JsonObject message = new JsonObject();
        message.addProperty("commandType", "LEAVE");
        message.addProperty("gameID", gameID);
        message.addProperty("authToken", authToken);
        sendMessage(message.toString());
    }
}

