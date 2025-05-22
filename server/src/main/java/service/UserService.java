package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import java.util.UUID;
import model.Request.RegisterRequest;
import model.Result.RegisterResult;
import model.Request.LoginRequest;
import model.Result.LoginResult;
import model.Request.LogoutRequest;
import dataaccess.*;

public class UserService extends BaseService{
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        super(authDAO);
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws AlreadyTakenException, DataAccessException {
        if (userDAO.getUser(registerRequest.username()) != null) {
            throw new dataaccess.AlreadyTakenException("Username is already taken");
        }
        UserData newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        userDAO.createUser(newUser);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, newUser.username());
        authDAO.createAuth(authData);

        return new RegisterResult(newUser.username(), authToken);
    }

    public LoginResult login(LoginRequest loginRequest) throws DataAccessException, UnauthorizedException, BadRequestException {
        if (userDAO.getUser(loginRequest.username()) == null) {
            throw new dataaccess.UnauthorizedException("Error: Unauthorized");
        }
        if (!userDAO.checkPassword(loginRequest.username(), loginRequest.password())) {
            throw new dataaccess.UnauthorizedException("Incorrect Username or Password");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, loginRequest.username());
        authDAO.createAuth(authData);

        return new LoginResult(loginRequest.username(), authToken);
    }

    public void logout(LogoutRequest logoutRequest) throws UnauthorizedException {
        AuthData auth = authenticate(logoutRequest.authToken());

        AuthData authData = authDAO.getAuth(logoutRequest.authToken());
        authDAO.deleteAuth(authData);
    }
}