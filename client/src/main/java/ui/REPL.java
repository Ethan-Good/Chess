package ui;

import client.*;
import java.util.Scanner;

public class REPL {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;
    private String authToken = null;
    private String username = null;
    private final PreLoginUI preloginUI;
    private final PostLoginUI postloginUI;

    public REPL() {
        this.facade = new ServerFacade(8080);
        this.preloginUI = new PreLoginUI(scanner, facade, this);
        this.postloginUI = new PostLoginUI(scanner, facade, this);
    }

    public void run() {
        System.out.println("Welcome to My cs240 Chess Client!");
        while (true) {
            if (authToken == null) {
                preloginUI.prompt();
            } else {
                postloginUI.prompt();
            }
        }
    }

    public void setAuth(String authToken, String username) {
        this.authToken = authToken;
        this.username = username;
    }

    public void clearAuth() {
        this.authToken = null;
        this.username = null;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }
}
