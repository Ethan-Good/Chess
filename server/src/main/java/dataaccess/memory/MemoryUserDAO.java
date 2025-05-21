package dataaccess.memory;

import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();

    public void clear() {
        users.clear();
    }
}
