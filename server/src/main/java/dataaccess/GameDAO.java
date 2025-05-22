package dataaccess;

import model.GameData;
import java.util.List;
import chess.ChessGame;


public interface GameDAO {
    void clear() throws DataAccessException;

    int createGame(GameData gameData) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    List<GameData> listGames() throws DataAccessException;

    void updateGame(GameData updatedGame) throws DataAccessException;

    void joinGame(int gameID, String username, ChessGame.TeamColor playerColor) throws DataAccessException;
}
