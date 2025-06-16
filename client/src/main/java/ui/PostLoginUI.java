package ui;

import client.ServerFacade;
import model.GameData;
import model.Request.*;
import model.Result.*;
import chess.*;

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
            case "create" -> doCreateGame();
            case "list" -> doListGames();
            case "join" -> doPlayGame();
            case "watch" -> doObserveGame();
            default -> System.out.println("Given command isn't known. Type 'help' to list available options.");
        }
    }

    private void printHelp() {
        System.out.println("""
                    Commands:
                    - help: Lists available options
                    - logout: Logs you out of the current account
                    - create: Create a new chess game
                    - list: List all current games
                    - join: Join a game to play
                    - watch: Join a game to watch
                """);
    }

    private void doLogout() {
        try {
            facade.logout(controller.getAuthToken());
            controller.clearAuth();
            System.out.println("Logged out.");
        } catch (Exception e) {
            System.out.println("Logout failed");
        }
    }

    private void doCreateGame() {
        System.out.print("Enter game name: ");
        String name = scanner.nextLine();
        try {
            CreateGameResult result = facade.createGame(new CreateGameRequest(name), controller.getAuthToken());
            System.out.println("Created game");
        } catch (Exception e) {
            System.out.println("Game creation failed");
        }
    }

    private void doListGames() {
        try {
            cachedGames = facade.listGames(controller.getAuthToken());
            if (cachedGames.size() == 0) {
                System.out.println("There are no games");
            }
            for (int i = 0; i < cachedGames.size(); i++) {
                var game = cachedGames.get(i);
                String white = (game.whiteUsername() != null) ? game.whiteUsername() : "EMPTY";
                String black = (game.blackUsername() != null) ? game.blackUsername() : "EMPTY";
                System.out.printf("%d. Game Name: %s   White: %s   Black: %s%n", (i + 1), game.gameName(), white, black);
            }
        } catch (Exception e) {
            System.out.println("Failed to list games");
        }
    }

    private void doPlayGame() {
        doListGames();
        System.out.print("Enter game number: ");
        int number;

        try {
            number = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        System.out.print("Color (white/black): ");
        String color = scanner.nextLine().toLowerCase();
        ChessGame.TeamColor chessColor = parseTeamColor(color);

        if (chessColor == null) {
            System.out.println("Invalid color.");
            return;
        }

        if (number < 1 || number > cachedGames.size()) {
            System.out.println("Invalid game number.");
            return;
        }

        int gameID = cachedGames.get(number - 1).gameID();
        try {
            facade.joinGame(new JoinGameRequest(chessColor, gameID), controller.getAuthToken());
            controller.gameplayUI.run(gameID, chessColor);
        } catch (Exception e) {
            System.out.println("Failed to join game: Already Taken");
        }
    }

    private void doObserveGame() {
        doListGames();
        System.out.print("Enter game number: ");
        int number;

        try {
            number = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        if (number < 1 || number > cachedGames.size()) {
            System.out.println("Invalid game number.");
            return;
        }

        int gameID = cachedGames.get(number - 1).gameID();

        try {
            controller.enterGameplay(gameID, null);
        } catch (Exception e) {
            System.out.println("Failed to observe game: " + e.getMessage());
        }
    }


    private ChessGame.TeamColor parseTeamColor(String input) {
        if (input.equalsIgnoreCase("white")) {
            return ChessGame.TeamColor.WHITE;
        } else if (input.equalsIgnoreCase("black")) {
            return ChessGame.TeamColor.BLACK;
        } else {
            return null;
        }
    }
}