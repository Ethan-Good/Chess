package ui;

import chess.*;

public class ChessBoardPrinter {
    private static final String LIGHT_SQUARE = EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE = EscapeSequences.SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_COLOR = EscapeSequences.SET_TEXT_COLOR_BLUE;
    private static final String RESET = EscapeSequences.RESET_TEXT_COLOR + EscapeSequences.RESET_BG_COLOR;

    public void drawBoard(ChessGame game, ChessGame.TeamColor perspective) {
        ChessBoard board = game.getBoard();

        System.out.print(EscapeSequences.ERASE_SCREEN);
        printFileLabels(perspective);

        int startRow = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRow = (perspective == ChessGame.TeamColor.WHITE) ? 0 : 9;
        int step = (perspective == ChessGame.TeamColor.WHITE) ? -1 : 1;

        for (int row = startRow; row != endRow; row += step) {
            System.out.print(BORDER_COLOR + " " + row + " " + RESET);
            for (int col = 1; col <= 8; col++) {
                int actualCol = (perspective == ChessGame.TeamColor.WHITE) ? col : (9 - col);
                boolean isLight = (row + actualCol) % 2 == 0;
                String squareColor = isLight ? LIGHT_SQUARE : DARK_SQUARE;
                ChessPiece piece = board.getPiece(new ChessPosition(row, actualCol));
                String pieceSymbol = getSymbol(piece);
                System.out.print(squareColor + pieceSymbol + RESET);
            }
            System.out.print(BORDER_COLOR + " " + row + RESET + "\n");
        }

        printFileLabels(perspective);
    }

    private void printFileLabels(ChessGame.TeamColor perspective) {
        System.out.print(BORDER_COLOR + "   ");

        boolean useFour = true;
        if (perspective == ChessGame.TeamColor.WHITE) {
            for (char file = 'a'; file <= 'h'; file++) {
                printAlternatingWidths(file, useFour);
                useFour = !useFour;
            }
        } else {
            for (char file = 'h'; file >= 'a'; file--) {
                printAlternatingWidths(file, useFour);
                useFour = !useFour;
            }
        }

        System.out.println(RESET);
    }

    private void printAlternatingWidths(char file, boolean wide) {
        if (wide) {
            System.out.print(" " + file + "  "); 
        } else {
            System.out.print(" " + file + " ");
        }
    }

    private String getSymbol(ChessPiece piece) {
        if (piece == null) return EscapeSequences.EMPTY;

        return switch (piece.getTeamColor()) {
            case WHITE -> switch (piece.getPieceType()) {
                case KING -> EscapeSequences.WHITE_KING;
                case QUEEN -> EscapeSequences.WHITE_QUEEN;
                case ROOK -> EscapeSequences.WHITE_ROOK;
                case BISHOP -> EscapeSequences.WHITE_BISHOP;
                case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                case PAWN -> EscapeSequences.WHITE_PAWN;
            };
            case BLACK -> switch (piece.getPieceType()) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case PAWN -> EscapeSequences.BLACK_PAWN;
            };
        };
    }
}
