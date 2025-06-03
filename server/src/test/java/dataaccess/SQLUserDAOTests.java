package dataaccess;

import dataaccess.SQL.SQLUserDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTests {

    private UserDAO userDAO;

    @BeforeAll
    static void setUpDatabase() {
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
        } catch (DataAccessException e) {
            e.printStackTrace();
            fail("Failed to initialize database for tests: " + e.getMessage());
        }
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        clearUsersTable();
        userDAO = new SQLUserDAO();
    }

    private void clearUsersTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM users");
        } catch (Exception e) {
            throw new DataAccessException("Failed to clear users table", e);
        }
    }

    @Test
    void clear_Positive() throws DataAccessException {
        var user = new UserData("user", "pass", "email");
        userDAO.createUser(user);
        assertNotNull(userDAO.getUser("user"));

        userDAO.clear();
        assertNull(userDAO.getUser("user"));
    }

    @Test
    void createUser_Positive() throws DataAccessException {
        var user = new UserData("user", "pass", "email");
        userDAO.createUser(user);

        var found = userDAO.getUser("user");
        assertNotNull(found);
        assertEquals("user", found.username());
    }

    @Test
    void createUser_Negative() throws DataAccessException {
        var user = new UserData("user", "pass", "email");
        userDAO.createUser(user);

        var ex = assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
        assertTrue(ex.getMessage().contains("User already exists"));
    }

    @Test
    void getUser_Positive() throws DataAccessException {
        var user = new UserData("user", "pass", "email");
        userDAO.createUser(user);

        var result = userDAO.getUser("user");
        assertNotNull(result);
        assertTrue(BCrypt.checkpw("pass", result.password()));
    }

    @Test
    void getUser_Negative() throws DataAccessException {
        assertNull(userDAO.getUser("nonexistent"));
    }

    @Test
    void checkPassword_Positive() throws DataAccessException {
        var user = new UserData("user", "pass", "email");
        userDAO.createUser(user);

        assertTrue(userDAO.checkPassword("user", "pass"));
    }

    @Test
    void checkPassword_Negative() throws DataAccessException {
        var user = new UserData("user", "pass", "email");
        userDAO.createUser(user);

        assertFalse(userDAO.checkPassword("user", "wrongPass"));
    }

    @Test
    void hashPassword_Positive() {
        var dao = new SQLUserDAO();
        String password = "pass";
        String hashed = dao.hashPassword(password);

        assertNotNull(hashed);
        assertTrue(BCrypt.checkpw(password, hashed));
    }

    @Test
    void hashPassword_Negative() {
        var dao = new SQLUserDAO();
        String password = "pass";
        String wrongPassword = "wrongPass";
        String hashed = dao.hashPassword(password);

        assertFalse(BCrypt.checkpw(wrongPassword, hashed));
    }
}

