package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();
    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            System.out.println("no piece there");
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board,startPosition);
        Iterator<ChessMove> iterator = moves.iterator();
        while (iterator.hasNext()) {
            ChessMove move = iterator.next();
            ChessPosition startPos = move.getStartPosition();
            ChessPosition endPos = move.getEndPosition();
            ChessPiece.PieceType promotion = move.getPromotionPiece();
            System.out.println("current move = (" + endPos.getRow() + "," + endPos.getColumn() + ")");

            //execute move
            board.addPiece(startPos, null);
            if (promotion == null) {
                board.addPiece(endPos, piece);
            }
            else {
                board.addPiece(endPos, new ChessPiece(piece.getTeamColor(), promotion));
            }

            //check for check
            boolean removeFromList = false;
            if (isInCheck(piece.getTeamColor())) {
                removeFromList = true;
            }

            //revert the board back to normal
            board.addPiece(endPos, null);
            board.addPiece(startPos, piece);

            //remove if in check
            if (removeFromList) {
                iterator.remove();
            }
        }

        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        //check if there is anything on the board
        Collection<ChessPosition> whites = board.findAllPiecesOfColor(TeamColor.WHITE);
        Collection<ChessPosition> blacks = board.findAllPiecesOfColor(TeamColor.BLACK);

        if (blacks.isEmpty() && whites.isEmpty()) {
            System.out.println("there are no pieces on the board lol");
        }

        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece.PieceType promotion = move.getPromotionPiece();

        ChessPiece piece = board.getPiece(startPos);
        if (piece == null) {
            System.out.println("no piece there - makeMove");
        }

        //make sure move is valid
        if (piece != null && piece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("It's not your turn");
        }

        Collection<ChessMove> validMoves = validMoves(startPos);
        boolean throwException = true;
        if (validMoves == null) {
            throw new InvalidMoveException("No valid moves");
        }
        for (ChessMove validMove : validMoves) {
            if (validMove.equals(move)) {
                System.out.println("move is valid");
                throwException = false;
            }
        }
        if (throwException) {

            throw new InvalidMoveException("Not a valid move");
        }

        //execute move
        board.addPiece(startPos, null);
        if (promotion == null) {
            board.addPiece(endPos, piece);
        }
        else {
            board.addPiece(endPos, new ChessPiece(piece.getTeamColor(), promotion));
        }

        //update turn
        TeamColor opposingColor;
        if (piece.getTeamColor() == TeamColor.WHITE) {
            opposingColor = TeamColor.BLACK;
        }
        else {
            opposingColor = TeamColor.WHITE;
        }

        setTeamTurn(opposingColor);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = board.findPiece(ChessPiece.PieceType.KING, teamColor);
        Collection<ChessPosition> opponentPositions;
        if (teamColor == TeamColor.WHITE) {
            opponentPositions = board.findAllPiecesOfColor(TeamColor.BLACK);
        }
        else {
            opponentPositions = board.findAllPiecesOfColor(TeamColor.WHITE);
        }
        for (ChessPosition pos : opponentPositions) {
            ChessPiece piece = board.getPiece(pos);
            Collection<ChessMove> moves = piece.pieceMoves(board, pos);
            for (ChessMove move : moves) {
                ChessPosition endPos = move.getEndPosition();
                if (endPos == kingPos) { // maybe -> (endPos.equals(kingPos))
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        //get all pieces of this team
        //loop thru
        //check valid moves of each piece
        //if valid moves aren't empty -> return false

        if (!isInCheck(teamColor)) {
            return false;
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        //get all pieces of this team
        //loop thru
        //check valid moves of each piece
        //if valid moves aren't empty -> return false

        if (isInCheck(teamColor)) {
            return false;
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
