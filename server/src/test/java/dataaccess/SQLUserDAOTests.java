package dataaccess;

import dataaccess.SQL.SQLUserDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;
import org.junit.jupiter.api.*;

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
        var user = new UserData("user1", "pass1", "email1@example.com");
        userDAO.createUser(user);
        assertNotNull(userDAO.getUser("user1"));

        userDAO.clear();
        assertNull(userDAO.getUser("user1"));
    }

    @Test
    void createUser_Positive() throws DataAccessException {
        var user = new UserData("alice", "pass", "alice@example.com");
        userDAO.createUser(user);

        var found = userDAO.getUser("alice");
        assertNotNull(found);
        assertEquals("alice", found.username());
    }

    @Test
    void createUser_Negative_Duplicate() throws DataAccessException {
        var user = new UserData("bob", "secret", "bob@example.com");
        userDAO.createUser(user);

        var ex = assertThrows(DataAccessException.class, () -> userDAO.createUser(user));
        assertTrue(ex.getMessage().contains("User already exists"));
    }

    @Test
    void getUser_Positive() throws DataAccessException {
        var user = new UserData("charlie", "mypw", "charlie@example.com");
        userDAO.createUser(user);

        var result = userDAO.getUser("charlie");
        assertNotNull(result);
        assertEquals("mypw", result.password());
    }

    @Test
    void getUser_Negative_NotFound() throws DataAccessException {
        assertNull(userDAO.getUser("nonexistent"));
    }

    @Test
    void checkPassword_Positive() throws DataAccessException {
        var user = new UserData("dave", "correctpass", "dave@example.com");
        userDAO.createUser(user);

        assertTrue(userDAO.checkPassword("dave", "correctpass"));
    }

    @Test
    void checkPassword_Negative_WrongPassword() throws DataAccessException {
        var user = new UserData("erin", "rightpass", "erin@example.com");
        userDAO.createUser(user);

        assertFalse(userDAO.checkPassword("erin", "wrongpass"));
    }

    @Test
    void checkPassword_Negative_UserNotFound() throws DataAccessException {
        assertFalse(userDAO.checkPassword("ghost", "any"));
    }
}

