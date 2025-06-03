package dataaccess.SQL;

import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;

import java.sql.SQLException;
import java.util.Objects;
import org.mindrot.jbcrypt.BCrypt;

public class SQLUserDAO implements UserDAO {

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM users";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user", e);
        }
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userData.username());
            stmt.setString(2, hashPassword(userData.password()));
            stmt.setString(3, userData.email());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new DataAccessException("User already exists", e);
            }
            throw new DataAccessException("Error creating user", e);
        }
    }

    public String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    @Override
    public Boolean checkPassword(String username, String password) throws DataAccessException {
        UserData user = getUser(username);

        return BCrypt.checkpw(password, user.password());
    }
}

