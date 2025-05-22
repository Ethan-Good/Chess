package model.Result;

import java.util.List;

public record ListGamesResult(List<Game> games) {
    public record Game(int gameID, String whiteUsername, String blackUsername, String gameName) {}
}