package dataaccess.memory;

import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.GameData;
import chess.ChessGame;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public void clear() {
        games.clear();
        nextId.set(1);
    }

    @Override
    public int createGame(GameData gameData) throws DataAccessException {
        int gameID = nextId.getAndIncrement();
        GameData newGame = new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        games.put(gameID, newGame);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {
        int gameID = updatedGame.gameID();
        if (!games.containsKey(gameID)) {
            throw new DataAccessException("Game not found.");
        }
        games.put(gameID, updatedGame);
    }

    @Override
    public void joinGame(int gameID, String username, ChessGame.TeamColor playerColor) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found.");
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();

        switch (playerColor) {
            case WHITE -> {
                if (white != null) throw new DataAccessException("White already taken.");
                white = username;
            }
            case BLACK -> {
                if (black != null) throw new DataAccessException("Black already taken.");
                black = username;
            }
            default -> throw new DataAccessException("Invalid color.");
        }

        GameData updated = new GameData(gameID, white, black, game.gameName(), game.game());
        updateGame(updated);
    }
}
