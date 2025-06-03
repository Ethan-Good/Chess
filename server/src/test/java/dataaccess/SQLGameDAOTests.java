package dataaccess;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.SQL.SQLAuthDAO;
import dataaccess.SQL.SQLGameDAO;
import dataaccess.SQL.SQLUserDAO;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTests {

    private SQLGameDAO gameDAO;
    private SQLUserDAO userDAO;
    private SQLAuthDAO authDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        gameDAO = new SQLGameDAO();
        authDAO = new SQLAuthDAO();
        userDAO = new SQLUserDAO();

        gameDAO.clear();
        authDAO.clear();
        userDAO.clear();

        var user = new UserData("user", "pass", "email");
        userDAO.createUser(user);
    }

    @Test
    void clear_Positive() throws DataAccessException {
        var game = new GameData(0, "user", null, "Game", new ChessGame());
        gameDAO.createGame(game);
        assertFalse(gameDAO.getAllGames().isEmpty());

        gameDAO.clear();
        assertTrue(gameDAO.getAllGames().isEmpty());
    }

    @Test
    void createGame_Positive() throws DataAccessException {
        var game = new GameData(0, "user", null, "Game", new ChessGame());
        int gameId = gameDAO.createGame(game);
        var result = gameDAO.getGame(gameId);

        assertNotNull(result);
        assertEquals("Game", result.gameName());
        assertEquals("user", result.whiteUsername());
    }
    @Test
    void createGame_Negative() {
        var invalidGame = new GameData(0, "user", "user", null, new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(invalidGame));
    }

    @Test
    void getGame_Positive() throws DataAccessException {
        var game = new GameData(0, "user", null, "Game", new ChessGame());
        int gameId = gameDAO.createGame(game);
        var found = gameDAO.getGame(gameId);

        assertNotNull(found);
        assertEquals("Game", found.gameName());
    }

    @Test
    void getGame_Negative() throws DataAccessException {
        var notFound = gameDAO.getGame(9999);
        assertNull(notFound);
    }

    @Test
    void getAllGames_Positive() throws DataAccessException {
        var game1 = new GameData(0, "user", null, "Game1", new ChessGame());
        var game2 = new GameData(0, "user", null, "Game2", new ChessGame());

        gameDAO.createGame(game1);
        gameDAO.createGame(game2);

        List<GameData> games = gameDAO.getAllGames();
        assertEquals(2, games.size());
    }

    @Test
    void updateGame_Positive() throws DataAccessException {
        var game = new GameData(0, "user", null, "Original", new ChessGame());
        int gameId = gameDAO.createGame(game);

        var updatedGame = new GameData(gameId, "user", "user", "Updated Game", new ChessGame());
        gameDAO.updateGame(updatedGame);

        var result = gameDAO.getGame(gameId);
        assertEquals("Updated Game", result.gameName());
        assertEquals("user", result.blackUsername());
    }

    @Test
    void updateGame_Negative() {
        var fakeGame = new GameData(9999, "user", null, "No Game", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(fakeGame));
    }
}
