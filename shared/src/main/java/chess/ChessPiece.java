package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceType type = getPieceType();
        ArrayList<ChessMove> moves = new ArrayList<>();
        switch (type) {
            case KING:
                addSingleMoves(1, 1, board, myPosition, moves);
                addSingleMoves(-1, -1, board, myPosition, moves);
                addSingleMoves(1, -1, board, myPosition, moves);
                addSingleMoves(-1, 1, board, myPosition, moves);
                addSingleMoves(1, 0, board, myPosition, moves);
                addSingleMoves(-1, 0, board, myPosition, moves);
                addSingleMoves(0, -1, board, myPosition, moves);
                addSingleMoves(0, 1, board, myPosition, moves);
                break;
            case QUEEN:
                addLoopMoves(1, 1, board, myPosition, moves);
                addLoopMoves(-1, -1, board, myPosition, moves);
                addLoopMoves(1, -1, board, myPosition, moves);
                addLoopMoves(-1, 1, board, myPosition, moves);
                addLoopMoves(1, 0, board, myPosition, moves);
                addLoopMoves(-1, 0, board, myPosition, moves);
                addLoopMoves(0, -1, board, myPosition, moves);
                addLoopMoves(0, 1, board, myPosition, moves);
                break;
            case BISHOP:
                addLoopMoves(1, 1, board, myPosition, moves);
                addLoopMoves(-1, -1, board, myPosition, moves);
                addLoopMoves(1, -1, board, myPosition, moves);
                addLoopMoves(-1, 1, board, myPosition, moves);
                break;
            case KNIGHT:
                addSingleMoves(2, 1, board, myPosition, moves);
                addSingleMoves(2, -1, board, myPosition, moves);
                addSingleMoves(1, 2, board, myPosition, moves);
                addSingleMoves(1, -2, board, myPosition, moves);
                addSingleMoves(-2, 1, board, myPosition, moves);
                addSingleMoves(-2, -1, board, myPosition, moves);
                addSingleMoves(-1, 2, board, myPosition, moves);
                addSingleMoves(-1, -2, board, myPosition, moves);
                break;
            case ROOK:
                addLoopMoves(1, 0, board, myPosition, moves);
                addLoopMoves(-1, 0, board, myPosition, moves);
                addLoopMoves(0, -1, board, myPosition, moves);
                addLoopMoves(0, 1, board, myPosition, moves);
                break;
            case PAWN:
                // Logic for pawn's moves
                break;
        }

        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    public void addLoopMoves(int rowStep, int colStep, ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves){
        int row = myPosition.getRow() + rowStep;
        int col = myPosition.getColumn() + colStep;
        ChessPosition startPos = new ChessPosition(myPosition.getRow(), myPosition.getColumn());
        while (row <= 8 && row >= 1 && col <= 8 && col >= 1) {
            ChessPosition newPos = new ChessPosition(row, col);
            ChessPiece pieceAtNewPos = board.getPiece(newPos);

            if (pieceAtNewPos == null) {
                ChessMove newMove = new ChessMove(startPos,newPos,null);
                moves.add(newMove);
//                System.out.println("(" + row + "," + col + ")");
            }
            else if (pieceAtNewPos.getTeamColor() != this.getTeamColor()) {
                ChessMove newMove = new ChessMove(startPos,newPos,null);
                moves.add(newMove);
//                System.out.println("(" + row + "," + col + ")");
                break;
            }
            else {
                break;
            }
            row += rowStep;
            col += colStep;
        }
    }
    public void addSingleMoves(int rowStep, int colStep, ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves){
        int row = myPosition.getRow() + rowStep;
        int col = myPosition.getColumn() + colStep;
        ChessPosition startPos = new ChessPosition(myPosition.getRow(), myPosition.getColumn());
        if (row <= 8 && row >= 1 && col <= 8 && col >= 1) {
            ChessPosition newPos = new ChessPosition(row, col);
            ChessPiece pieceAtNewPos = board.getPiece(newPos);

            if (pieceAtNewPos == null || pieceAtNewPos.getTeamColor() != this.getTeamColor()) {
                ChessMove newMove = new ChessMove(startPos, newPos, null);
                moves.add(newMove);
//                System.out.println("(" + row + "," + col + ")");
            }
        }
    }
}
