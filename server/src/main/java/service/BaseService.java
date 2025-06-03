package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.AuthData;

public abstract class BaseService {
    protected final AuthDAO authDAO;

    public BaseService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    protected AuthData authenticate(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return auth;
    }
}
