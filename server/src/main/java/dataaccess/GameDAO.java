package dataaccess;

import model.GameData;
import java.util.List;


public interface GameDAO {
    void clear() throws DataAccessException;
    int createGame(GameData gameData) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    List<GameData> getAllGames() throws DataAccessException;
    void updateGame(GameData updatedGame) throws DataAccessException;
    void deleteGame(int gameID) throws DataAccessException;
}
