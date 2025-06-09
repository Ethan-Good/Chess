package ui;

import client.ServerFacade;
import model.Request.*;
import model.Result.*;
import java.util.Scanner;

public class PreLoginUI {
    private final Scanner scanner;
    private final ServerFacade facade;
    private final REPL controller;

    public PreLoginUI(Scanner scanner, ServerFacade facade, REPL controller) {
        this.scanner = scanner;
        this.facade = facade;
        this.controller = controller;
    }

    public void prompt() {
        System.out.println("type help for options");
        System.out.print("[LOGGED OUT] >>> ");
        String input = scanner.nextLine().trim().toLowerCase();
        switch (input) {
            case "help" -> printHelp();
            case "quit" -> quit();
            case "register" -> doRegister();
            case "login" -> doLogin();
            default -> System.out.println("Given command isn't known. Type 'help' to list available options.");
        }
    }

    private void printHelp() {
        System.out.println("""
            Commands:
            - help: Lists available options
            - quit: Exits the program
            - register: Registers a new user
            - login: Login as an existing user
        """);
    }

    private void quit() {
        System.out.println("Bye Bye!");
        System.exit(0);
    }

    private void doRegister() {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        try {
            RegisterResult result = facade.register(new RegisterRequest(user, pass, email));
            controller.setAuth(result.authToken(), result.username());
            System.out.println("Registered as " + result.username());
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void doLogin() {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        try {
            LoginResult result = facade.login(new LoginRequest(user, pass));
            controller.setAuth(result.authToken(), result.username());
            System.out.println("Logged in as " + result.username());
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }
}

