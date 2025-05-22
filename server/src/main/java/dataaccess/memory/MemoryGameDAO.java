package dataaccess.memory;

import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.GameData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    public List<GameData> getAllGames() throws DataAccessException {
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
}
