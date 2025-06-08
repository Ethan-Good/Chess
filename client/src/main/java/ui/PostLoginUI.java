package ui;

import client.ServerFacade;
import model.Request.*;
import model.Result.*;

import java.util.*;

public class PostLoginUI {
    private final Scanner scanner;
    private final ServerFacade facade;
    private final REPL controller;
    private List<ListGamesResult.Game> cachedGames = new ArrayList<>();

    public PostLoginUI(Scanner scanner, ServerFacade facade, REPL controller) {
        this.scanner = scanner;
        this.facade = facade;
        this.controller = controller;
    }

    public void prompt() {
        System.out.print("[LOGGED IN] >>> ");
        String input = scanner.nextLine().trim().toLowerCase();

        switch (input) {
            case "help" -> printHelp();
            case "logout" -> doLogout();
            case "create game" -> doCreateGame();
            case "list games" -> doListGames();
            case "play game" -> doPlayGame();
            case "observe game" -> doObserveGame();
            default -> System.out.println("Given command isn't known. Type 'help' to list available options.");
        }
    }

    private void printHelp() {
        System.out.println("""
                    Commands:
                    - help : Lists available options
                    - logout : Logs you out of the current account
                    - create game : Create a new chess game
                    - list games : List all current games
                    - play game : Join a game to play
                    - observe game : Join a game as an observer
                """);
    }

    private void doLogout() {
        try {
            facade.logout(controller.getAuthToken());
            controller.clearAuth();
            System.out.println("Logged out.");
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    private void doCreateGame() {
        System.out.print("Enter game name: ");
        String name = scanner.nextLine();
        try {
            CreateGameResult result = facade.createGame(new CreateGameRequest(name), controller.getAuthToken());
            System.out.println("Created game with ID: " + result.gameID());
        } catch (Exception e) {
            System.out.println("Game creation failed: " + e.getMessage());
        }
    }

    private void doListGames() {
        try {
            cachedGames = facade.listGames(controller.getAuthToken());
            for (int i = 0; i < cachedGames.size(); i++) {
                var game = cachedGames.get(i);
                System.out.printf("%d. %s | White: %s | Black: %s%n", (i + 1), game.gameName(),
                        game.whiteUsername(), game.blackUsername());
            }
        } catch (Exception e) {
            System.out.println("Failed to list games: " + e.getMessage());
        }
    }

    
}