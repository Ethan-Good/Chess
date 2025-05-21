package dataaccess.memory;

import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        if (users.containsKey(userData.username())) { //don't think i need this but maybe
            throw new DataAccessException("User already exists");
        }
        users.put(userData.username(), userData);
    }
}
