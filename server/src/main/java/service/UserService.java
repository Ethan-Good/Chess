package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.Request.LoginRequest;
import model.UserData;
import java.util.UUID;
import spark.Request;
import spark.Response;
import model.Request.RegisterRequest;
import model.Result.RegisterResult;
import model.Request.LoginRequest;
import model.Result.LoginResult;
import model.Request.LogoutRequest;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public class AlreadyTakenException extends Exception {
        public AlreadyTakenException(String message) {
            super(message);
        }
    }
    public class UnauthorizedException extends Exception {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    public RegisterResult register(RegisterRequest registerRequest) throws AlreadyTakenException, DataAccessException {
        if (userDAO.getUser(registerRequest.username()) != null) {
            throw new AlreadyTakenException("Username is already taken");
        }
        UserData newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        userDAO.createUser(newUser);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, newUser.username());
        authDAO.createAuth(authData);

        return new RegisterResult(newUser.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException, UnauthorizedException {
        if (userDAO.getUser(loginRequest.username()) == null) {
            throw new UnauthorizedException("No Username or incorrect Password");
        }
        if (!userDAO.checkPassword(loginRequest.username(), loginRequest.password())) {
            throw new UnauthorizedException("No Username or incorrect Password");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, loginRequest.username());
        authDAO.createAuth(authData);

        return new LoginResult(loginRequest.username(), authToken);
    }
}