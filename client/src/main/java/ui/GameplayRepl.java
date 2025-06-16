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
    private final REPL repl;

    public GameplayRepl(Scanner scanner, ServerFacade facade, GameService gameService, WebsocketCommunicator communicator, REPL repl) {
        this.scanner = scanner;
        this.facade = facade;
        this.gameService = gameService;
        this.communicator = communicator;
        this.repl = repl;
    }

    public void run(int gameID, ChessGame.TeamColor color) throws DataAccessException {
        this.currentGameID = gameID;
        this.playerColor = color;

        communicator.sendConnect(gameID, repl.getAuthToken());

        GameData gameData = gameService.getGameDAO().getGame(gameID);
        if (gameData == null) {
            System.out.println("Error: Game not found");
            return;
        }

        this.currentGame = gameData.game();

        boolean running = true;
        while (running) {
            System.out.print("[GAMEPLAY] >>> ");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "help" -> printHelp();
                case "redraw" -> printBoard();
                case "leave" -> {
                    communicator.sendLeave(currentGameID, repl.getAuthToken());
                    System.out.println("Leaving game...");
                    running = false;
                }
                case "resign" -> doResign();
                case "move" -> doMove();
                case "highlight" -> doHighlight();
                default -> System.out.println("Unknown command. Type 'help' for commands");
            }
            if (running) {
                System.out.println();
            }
        }
    }

    private void printHelp() {
        System.out.println("""
            Commands:
            - help: Lists available options
            - redraw: Redraw the chess board
            - leave: Leave the game
            - move: Make a move
            - resign: Forfeit the game
            - highlight: Highlight legal moves for a piece
            """);
    }

    public void printBoard() {
        ChessBoardPrinter printer = new ChessBoardPrinter();
        printer.drawBoard(currentGame, playerColor);
    }
    public void setCurrentGame(ChessGame newGame) {
        this.currentGame = newGame;
    }

    private void doResign() {
        System.out.print("Do you want to rage quit? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes")) {
            System.out.println("Resignation canceled");
            return;
        }

        try {
            communicator.sendResign(currentGameID, repl.getAuthToken());
        } catch (Exception e) {
            System.out.println("Error resigning: " + e.getMessage());
        }
    }



    private void doMove() {
        System.out.print("Enter move (e2 e4): ");
        String input = scanner.nextLine().trim().toLowerCase();
        String[] parts = input.split("\\s+");

        if (parts.length != 2 || !isValidSquare(parts[0]) || !isValidSquare(parts[1])) {
            System.out.println("Invalid input. Use positions like 'e2 e4'.");
            return;
        }

        try {
            ChessPosition from = parsePosition(parts[0]);
            ChessPosition to = parsePosition(parts[1]);

            ChessPiece piece = currentGame.getBoard().getPiece(from);
            if (piece == null) {
                System.out.println("There is no piece at " + parts[0]);
                return;
            }

            String currentUser = repl.getUsername();
            ChessGame.TeamColor playerColor = null;
            GameData gameData = gameService.getGameDAO().getGame(currentGameID);

            if (currentUser.equals(gameData.whiteUsername())) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (currentUser.equals(gameData.blackUsername())) {
                playerColor = ChessGame.TeamColor.BLACK;
            }

            if (playerColor == null) {
                System.out.println("You are not a player in this game.");
                return;
            }

            if (piece.getTeamColor() != playerColor) {
                System.out.println("That is not your piece.");
                return;
            }

            if (currentGame.getTeamTurn() != playerColor) {
                System.out.println("It's not your turn.");
                return;
            }

            ChessMove move = new ChessMove(from, to, null);
            Collection<ChessMove> legalMoves = currentGame.validMoves(from);

            if (!legalMoves.contains(move)) {
                System.out.println("That is not a legal move");
                return;
            }

            if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                    (to.getRow() == 1 || to.getRow() == 8)) {
                move = new ChessMove(from, to, ChessPiece.PieceType.QUEEN);
            }

            communicator.sendMove(currentGameID, move, repl.getAuthToken());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private boolean isValidSquare(String pos) {
        return pos.length() == 2 &&
                pos.charAt(0) >= 'a' && pos.charAt(0) <= 'h' &&
                pos.charAt(1) >= '1' && pos.charAt(1) <= '8';
    }

    private void doHighlight() {
        System.out.print("Enter position (e.g. e2): ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (!isValidSquare(input)) {
            System.out.println("Invalid. Use a valid position like 'e2'.");
            return;
        }

        try {
            ChessPosition pos = parsePosition(input);
            ChessPiece piece = currentGame.getBoard().getPiece(pos);

            if (piece == null) {
                System.out.println("There is no piece there.");
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
