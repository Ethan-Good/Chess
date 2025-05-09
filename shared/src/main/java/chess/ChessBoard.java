package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    public ChessPosition findPiece(ChessPiece.PieceType type, ChessGame.TeamColor color) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece currentPiece = getPiece(new ChessPosition(row,col));
                if (currentPiece != null && currentPiece.getPieceType() == type && currentPiece.getTeamColor() == color) {
                    return new ChessPosition(row,col);
                }
            }
        }
        return null;
    }

    public Collection<ChessPosition> findAllPiecesOfColor(ChessGame.TeamColor color) {
        ArrayList<ChessPosition> positions = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece currentPiece = getPiece(new ChessPosition(row,col));
                if (currentPiece != null && currentPiece.getTeamColor() == color) {
                    positions.add(new ChessPosition(row,col));
                }
            }
        }
        return  positions;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //clear board
        for (int row = 0; row < 8; row++){
            for (int col = 0; col < 8; col++){
                squares[row][col] = null;
            }
        }

        ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;
        ChessPiece.PieceType rook = ChessPiece.PieceType.ROOK;
        ChessPiece.PieceType knight = ChessPiece.PieceType.KNIGHT;
        ChessPiece.PieceType bishop = ChessPiece.PieceType.BISHOP;
        ChessPiece.PieceType king = ChessPiece.PieceType.KING;
        ChessPiece.PieceType queen = ChessPiece.PieceType.QUEEN;
        ChessPiece.PieceType pawn = ChessPiece.PieceType.PAWN;

        addPiece(new ChessPosition(1,1), new ChessPiece(white, rook));
        addPiece(new ChessPosition(1,2), new ChessPiece(white, knight));
        addPiece(new ChessPosition(1,3), new ChessPiece(white, bishop));
        addPiece(new ChessPosition(1,4), new ChessPiece(white, queen));
        addPiece(new ChessPosition(1,5), new ChessPiece(white, king));
        addPiece(new ChessPosition(1,6), new ChessPiece(white, bishop));
        addPiece(new ChessPosition(1,7), new ChessPiece(white, knight));
        addPiece(new ChessPosition(1,8), new ChessPiece(white, rook));
        addPiece(new ChessPosition(2,1), new ChessPiece(white, pawn));
        addPiece(new ChessPosition(2,2), new ChessPiece(white, pawn));
        addPiece(new ChessPosition(2,3), new ChessPiece(white, pawn));
        addPiece(new ChessPosition(2,4), new ChessPiece(white, pawn));
        addPiece(new ChessPosition(2,5), new ChessPiece(white, pawn));
        addPiece(new ChessPosition(2,6), new ChessPiece(white, pawn));
        addPiece(new ChessPosition(2,7), new ChessPiece(white, pawn));
        addPiece(new ChessPosition(2,8), new ChessPiece(white, pawn));

        ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
        addPiece(new ChessPosition(8,1), new ChessPiece(black, rook));
        addPiece(new ChessPosition(8,2), new ChessPiece(black, knight));
        addPiece(new ChessPosition(8,3), new ChessPiece(black, bishop));
        addPiece(new ChessPosition(8,4), new ChessPiece(black, queen));
        addPiece(new ChessPosition(8,5), new ChessPiece(black, king));
        addPiece(new ChessPosition(8,6), new ChessPiece(black, bishop));
        addPiece(new ChessPosition(8,7), new ChessPiece(black, knight));
        addPiece(new ChessPosition(8,8), new ChessPiece(black, rook));
        addPiece(new ChessPosition(7,1), new ChessPiece(black, pawn));
        addPiece(new ChessPosition(7,2), new ChessPiece(black, pawn));
        addPiece(new ChessPosition(7,3), new ChessPiece(black, pawn));
        addPiece(new ChessPosition(7,4), new ChessPiece(black, pawn));
        addPiece(new ChessPosition(7,5), new ChessPiece(black, pawn));
        addPiece(new ChessPosition(7,6), new ChessPiece(black, pawn));
        addPiece(new ChessPosition(7,7), new ChessPiece(black, pawn));
        addPiece(new ChessPosition(7,8), new ChessPiece(black, pawn));
    }
}
