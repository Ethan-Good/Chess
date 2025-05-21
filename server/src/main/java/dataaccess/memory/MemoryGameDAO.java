package dataaccess.memory;

import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {
    private final HashMap<Integer, GameData> games = new HashMap<>();

    public void clear() {
        games.clear();
    }
}
