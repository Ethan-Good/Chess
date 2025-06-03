package dataaccess;

import dataaccess.SQL.SQLAuthDAO;
import dataaccess.SQL.SQLUserDAO;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTests {

    private SQLAuthDAO authDAO;
    private SQLUserDAO userDAO;

    @BeforeEach
    void setup() throws DataAccessException {
        userDAO = new SQLUserDAO();
        authDAO = new SQLAuthDAO();
        authDAO.clear();
        userDAO.clear();
        var user = new UserData("user", "pass", "email");
        try {
            userDAO.createUser(user);
        } catch (DataAccessException ex) {
            System.out.println("User already exists, continuing test...");
        }
        System.out.println("Inserted user: " + userDAO.getUser("user")); // sanity check
    }

    private void clearAuthTable() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth");
        } catch (Exception e) {
            throw new DataAccessException("Failed to clear auth table", e);
        }
    }

    // --- createAuth ---
    @Test
    void createAuth_Positive() throws DataAccessException {
        var auth = new AuthData("token", "user");
        authDAO.createAuth(auth);

        var result = authDAO.getAuth("token");
        assertNotNull(result);
        assertEquals("user", result.username());
    }

    @Test
    void createAuth_Negative() throws DataAccessException {
        var auth = new AuthData("token", "user");
        authDAO.createAuth(auth);

        // Attempting to insert same token again
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(auth));
    }

    // --- getAuth ---
    @Test
    void getAuth_Positive() throws DataAccessException {
        var auth = new AuthData("token", "user");
        authDAO.createAuth(auth);

        var result = authDAO.getAuth("token");
        assertNotNull(result);
        assertEquals("user", result.username());
    }

    @Test
    void getAuth_Negative() throws DataAccessException {
        var result = authDAO.getAuth("nonexistent");
        assertNull(result);
    }

    // --- deleteAuth ---
    @Test
    void deleteAuth_Positive() throws DataAccessException {
        var auth = new AuthData("token", "user");
        authDAO.createAuth(auth);

        authDAO.deleteAuth(auth);
        assertNull(authDAO.getAuth("token"));
    }

    @Test
    void deleteAuth_Negative() throws DataAccessException {
        // Should not throw even if the token doesn't exist
        var fakeAuth = new AuthData("noToken", "noUser");
        assertDoesNotThrow(() -> authDAO.deleteAuth(fakeAuth));
    }

    // --- clear ---
    @Test
    void clear_Positive() throws DataAccessException {
        var auth1 = new AuthData("token", "user");
        authDAO.createAuth(auth1);

        authDAO.clear();

        assertNull(authDAO.getAuth("token"));
    }
}
