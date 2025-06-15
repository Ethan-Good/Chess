package ui;

import chess.*;
import client.ServerFacade;
import client.WebsocketCommunicator;
import dataaccess.DataAccessException;
import model.GameData;
import service.GameService;

import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class GameplayRepl {
    private final Scanner scanner;
    private final ServerFacade facade;
    private final GameService gameService;
    private final WebsocketCommunicator communicator;

    private int currentGameID = -1;
    private ChessGame.TeamColor playerColor;
    private ChessGame currentGame;

    public GameplayRepl(Scanner scanner, ServerFacade facade, GameService gameService, WebsocketCommunicator communicator) {
        this.scanner = scanner;
        this.facade = facade;
        this.gameService = gameService;
        this.communicator = communicator;
    }

    public void run(int gameID, ChessGame.TeamColor color) throws DataAccessException {
        this.currentGameID = gameID;
        this.playerColor = color;

        GameData gameData = gameService.getGameDAO().getGame(gameID);
        if (gameData == null) {
            System.out.println("Error: Game not found");
            return;
        }

        this.currentGame = gameData.game();
        System.out.println("Joined game ID: " + gameID + " as " + color);
        printHelp();
        printBoard();

        boolean running = true;
        while (running) {
            System.out.print("[GAMEPLAY] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printHelp();
                case "redraw" -> printBoard();
                case "leave" -> {
                    System.out.println("Leaving game...");
                    running = false;
                }
                case "resign" -> {
                    boolean shouldExit = doResign();
                    if (shouldExit) running = false;
                }
                case "move" -> doMove();
                case "highlight" -> doHighlight();
                default -> System.out.println("Unknown command. Type 'help' for list.");
            }
        }
    }

    private void printHelp() {
        System.out.println("""
            Commands:
            - help: Display this help menu
            - redraw: Redraw the chess board
            - leave: Leave the game (return to main menu)
            - move: Make a move (e.g. 'e2 e4')
            - resign: Forfeit the game (requires confirmation)
            - highlight: Highlight legal moves for a piece
            """);
    }

    private void printBoard() {
        ChessBoardPrinter printer = new ChessBoardPrinter();
        printer.drawBoard(currentGame, playerColor);
    }

    private boolean doResign() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) {
            System.out.println("Resignation canceled.");
            return false;
        }

        try {
            currentGame.resign(playerColor);
            GameData oldGameData = gameService.getGameDAO().getGame(currentGameID);

            GameData updatedGameData = new GameData(
                    oldGameData.gameID(),
                    oldGameData.whiteUsername(),
                    oldGameData.blackUsername(),
                    oldGameData.gameName(),
                    currentGame);

            gameService.getGameDAO().updateGame(updatedGameData);

            gameService.getGameDAO().deleteGame(currentGameID);

            System.out.println("You resigned. Game over.");
            return true;  // Indicate gameplay should exit
        } catch (Exception e) {
            System.out.println("Error resigning: " + e.getMessage());
            return false;
        }
    }


    private void doMove() {
        System.out.print("Enter move (e.g. e2 e4): ");
        String input = scanner.nextLine().trim().toLowerCase();
        String[] parts = input.split("\\s+");

        if (parts.length != 2 || parts[0].length() != 2 || parts[1].length() != 2) {
            System.out.println("Invalid input format. Use 'e2 e4'.");
            return;
        }

        try {
            ChessPosition from = parsePosition(parts[0]);
            ChessPosition to = parsePosition(parts[1]);

            ChessPiece.PieceType promotion = null;
            ChessPiece piece = currentGame.getBoard().getPiece(from);
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN
                    && (to.getRow() == 1 || to.getRow() == 8)) {
                promotion = ChessPiece.PieceType.QUEEN; // auto promote to queen for now
            }

            ChessMove move = new ChessMove(from, to, promotion);

            currentGame.makeMove(move);

            GameData oldGameData = gameService.getGameDAO().getGame(currentGameID);

            gameService.getGameDAO().updateGame(new GameData(
                    oldGameData.gameID(),
                    oldGameData.whiteUsername(),
                    oldGameData.blackUsername(),
                    oldGameData.gameName(),
                    currentGame));

            System.out.println("Move made.");
            printBoard();

        } catch (InvalidMoveException e) {
            System.out.println("Invalid move: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private void doHighlight() {
        System.out.print("Enter position (e.g. e2): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.length() != 2) {
            System.out.println("Invalid position.");
            return;
        }

        try {
            ChessPosition pos = parsePosition(input);
            ChessPiece piece = currentGame.getBoard().getPiece(pos);

            if (piece == null) {
                System.out.println("No piece at that position.");
                return;
            }

            Collection<ChessMove> legalMoves = currentGame.validMoves(pos);
            ChessBoardPrinter printer = new ChessBoardPrinter();

            Collection<ChessPosition> highlights = legalMoves.stream()
                    .map(ChessMove::getEndPosition)
                    .toList();

            printer.drawHighlightedBoard(currentGame, playerColor, highlights);
        } catch (Exception e) {
            System.out.println("Error highlighting: " + e.getMessage());
        }
    }

    private ChessPosition parsePosition(String algebraic) {
        int col = algebraic.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(algebraic.charAt(1));
        return new ChessPosition(row, col);
    }
}
