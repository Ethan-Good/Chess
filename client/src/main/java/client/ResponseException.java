package client;

public class ResponseException extends Exception {
    private final int statusCode;

    public ResponseException(int statusCode, String message) {
        super("Error " + statusCode + ": " + message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
