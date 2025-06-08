package client;

import com.google.gson.Gson;
import model.Request.*;
import model.Result.*;

import java.io.*;
import java.net.*;
import java.util.List;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public void clear() throws ResponseException {
        doDelete("/db", null);
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        String json = gson.toJson(request);
        String response = doPost("/user", json, null);
        return gson.fromJson(response, RegisterResult.class);
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        String json = gson.toJson(request);
        String response = doPost("/session", json, null);
        return gson.fromJson(response, LoginResult.class);
    }

    public void logout(String authToken) throws ResponseException {
        doDelete("/session", authToken);
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) throws ResponseException {
        String json = gson.toJson(request);
        String response = doPost("/game", json, authToken);
        return gson.fromJson(response, CreateGameResult.class);
    }

    public List<ListGamesResult.Game> listGames(String authToken) throws ResponseException {
        String response = doGet("/game", authToken);
        ListGamesResult result = gson.fromJson(response, ListGamesResult.class);
        return result.games();
    }

    public void joinGame(JoinGameRequest request, String authToken) throws ResponseException {
        String json = gson.toJson(request);
        doPut("/game", json, authToken);
    }

    private String doPost(String path, String body, String authToken) throws ResponseException {
        try {
            HttpURLConnection connection = createConnection("POST", path, authToken);
            writeRequestBody(connection, body);
            return readResponse(connection);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private String doPut(String path, String body, String authToken) throws ResponseException {
        try {
            HttpURLConnection connection = createConnection("PUT", path, authToken);
            writeRequestBody(connection, body);
            return readResponse(connection);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private String doGet(String path, String authToken) throws ResponseException {
        try {
            HttpURLConnection connection = createConnection("GET", path, authToken);
            return readResponse(connection);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void doDelete(String path, String authToken) throws ResponseException {
        try {
            HttpURLConnection connection = createConnection("DELETE", path, authToken);
            readResponse(connection); // Ensure errors are caught
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private HttpURLConnection createConnection(String method, String path, String authToken) throws IOException {
        URL url = new URL(serverUrl + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setDoInput(true);
        if (method.equals("POST") || method.equals("PUT")) {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
        }
        if (authToken != null) {
            connection.setRequestProperty("Authorization", authToken);
        }
        return connection;
    }

    private void writeRequestBody(HttpURLConnection connection, String body) throws IOException {
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.getBytes();
            os.write(input, 0, input.length);
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException, ResponseException {
        int status = connection.getResponseCode();
        InputStream stream = (status >= 200 && status < 300) ?
                connection.getInputStream() : connection.getErrorStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder response = new StringBuilder();
            for (String line; (line = reader.readLine()) != null;) {
                response.append(line);
            }
            if (status >= 200 && status < 300) {
                return response.toString();
            } else {
                throw new ResponseException(status, response.toString());
            }
        }
    }
}

