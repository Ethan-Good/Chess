package websocket.messages;

import model.GameData;
import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {

    private final ServerMessageType serverMessageType;
    private GameData game;
    private String message;
    private String errorMessage;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    private ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public static ServerMessage loadGame(GameData game) {
        ServerMessage msg = new ServerMessage(ServerMessageType.LOAD_GAME);
        msg.game = game;
        return msg;
    }

    public static ServerMessage notification(String message) {
        ServerMessage msg = new ServerMessage(ServerMessageType.NOTIFICATION);
        msg.message = message;
        return msg;
    }

    public static ServerMessage error(String errorMessage) {
        ServerMessage msg = new ServerMessage(ServerMessageType.ERROR);
        msg.errorMessage = errorMessage;
        return msg;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public GameData getGame() {
        return game;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
