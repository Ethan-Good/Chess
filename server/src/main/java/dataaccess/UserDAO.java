package dataaccess;

import model.UserData;
import java.util.Collection;

public interface UserDAO {
    void clear() throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData userData) throws DataAccessException;
    Boolean checkPassword(String username, String password) throws DataAccessException;

}
