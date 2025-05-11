package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn = TeamColor.WHITE;
    private ChessBoard board = new ChessBoard();

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingRookMoved = false;
    private boolean whiteQueenRookMoved = false;
    private boolean blackKingRookMoved = false;
    private boolean blackQueenRookMoved = false;

    public ChessGame() {
        board.resetBoard();
    }

    public boolean hasWhiteKingMoved() {
        return whiteKingMoved;
    }
    public boolean hasBlackKingMoved() {
        return blackKingMoved;
    }
    public boolean hasWhiteKingRookMoved() {
        return whiteKingRookMoved;
    }
    public boolean hasBlackKingRookMoved() {
        return blackKingRookMoved;
    }
    public boolean hasWhiteQueenRookMoved() {
        return whiteQueenRookMoved;
    }
    public boolean hasBlackQueenRookMoved() {
        return blackQueenRookMoved;
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
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board,startPosition);
        if (piece.getTeamColor() == TeamColor.WHITE) {
            if (piece.getPieceType() == ChessPiece.PieceType.KING && !whiteKingMoved) {
                if (!whiteKingRookMoved) {
                    
                }
                else if (!whiteQueenRookMoved) {

                }
            }
        }
        else {

        }
        Iterator<ChessMove> iterator = moves.iterator();
        while (iterator.hasNext()) {
            ChessMove move = iterator.next();
            ChessPosition startPos = move.getStartPosition();
            ChessPosition endPos = move.getEndPosition();
            ChessPiece.PieceType promotion = move.getPromotionPiece();

            //see if a piece is at endPos
            ChessPiece captured = board.getPiece(endPos);
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
            if (captured == null) {
                board.addPiece(endPos, null);
            }
            else {
                board.addPiece(endPos, captured);
            }

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

        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos = move.getEndPosition();
        ChessPiece.PieceType promotion = move.getPromotionPiece();

        ChessPiece piece = board.getPiece(startPos);

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

        //set flags for castling
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK && startPos.getRow() == 1 && startPos.getColumn() == 1) {
            whiteQueenRookMoved = true;
         }
        else if (piece.getPieceType() == ChessPiece.PieceType.ROOK && startPos.getRow() == 1 && startPos.getColumn() == 8) {
            whiteKingRookMoved = true;
        }
        else if (piece.getPieceType() == ChessPiece.PieceType.ROOK && startPos.getRow() == 8 && startPos.getColumn() == 1) {
            blackQueenRookMoved = true;
        }
        else if (piece.getPieceType() == ChessPiece.PieceType.ROOK && startPos.getRow() == 8 && startPos.getColumn() == 8) {
            blackKingRookMoved = true;
        }
        else if (piece.getPieceType() == ChessPiece.PieceType.KING && startPos.getRow() == 1 && startPos.getColumn() == 5) {
            whiteKingMoved = true;
        }
        else if (piece.getPieceType() == ChessPiece.PieceType.KING && startPos.getRow() == 8 && startPos.getColumn() == 5) {
            blackKingMoved = true;
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
                if (endPos.equals(kingPos)) {
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

        Collection<ChessPosition> positions = board.findAllPiecesOfColor(teamColor);
        for (ChessPosition pos : positions) {
            Collection<ChessMove> validMoves = validMoves(pos);
            if (!validMoves.isEmpty()){
                return false;
            }
        }

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
        Collection<ChessPosition> positions = board.findAllPiecesOfColor(teamColor);
        for (ChessPosition pos : positions) {
            Collection<ChessMove> validMoves = validMoves(pos);
            if (!validMoves.isEmpty()){
                return false;
            }
        }

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
