package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods. hiii
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

    public boolean isInCheckAfterMove(ChessMove move, TeamColor color) {
        ChessPosition startPos = move.getStartPosition();
        ChessPosition endPos =  move.getEndPosition();
        ChessPiece king = board.getPiece(startPos);
        ChessPiece captured = board.getPiece(endPos);

        board.addPiece(startPos, null);
        board.addPiece(endPos, king);

        boolean isInCheck = false;
        if (isInCheck(color)) {
            isInCheck = true;
        }

        board.addPiece(startPos, king);
        board.addPiece(endPos, captured);

        return isInCheck;
    }

    public Collection<ChessPosition> getPiecesBetween(ChessPosition kingPos, ChessPosition rookPos) {
        ArrayList<ChessPosition> positions = new ArrayList<>();
        if (kingPos.getRow() != rookPos.getRow()) {
            return positions;
        }
        int kingCol = kingPos.getColumn();
        int rookCol = rookPos.getColumn();
        int min = Math.min(rookCol, kingCol);
        int max = Math.max(rookCol, kingCol);
        for (int i = min+1; i < max; i++) {
            ChessPosition pos = new ChessPosition(kingPos.getRow(), i);
            ChessPiece piece = board.getPiece(pos);
            if (piece != null) {
                positions.add(pos);
            }
        }

        return positions;
    }

    public boolean canCastleKingsSide(TeamColor color) {
        if (color == TeamColor.WHITE) {
            ChessPosition kingPos = new ChessPosition(1, 5);
            ChessPosition rookPos = new ChessPosition(1, 8);
            if (whiteKingMoved || whiteKingRookMoved || !getPiecesBetween(kingPos, rookPos).isEmpty()
                    || isInCheck(color) || isInCheckAfterMove(new ChessMove(kingPos, new ChessPosition(1, 6), null), color)
                    || isInCheckAfterMove(new ChessMove(kingPos, new ChessPosition(1, 7), null), color)) {
                return false;
            }
        }
        else {
            ChessPosition kingPos = new ChessPosition(8, 5);
            ChessPosition rookPos = new ChessPosition(8, 8);
            if (blackKingMoved || blackKingRookMoved || !getPiecesBetween(kingPos, rookPos).isEmpty()
                    || isInCheck(color) || isInCheckAfterMove(new ChessMove(kingPos, new ChessPosition(8, 6), null), color)
                    || isInCheckAfterMove(new ChessMove(kingPos, new ChessPosition(8, 7), null), color)) {
                return false;
            }
        }

        return true;
    }

    public boolean canCastleQueensSide(TeamColor color) {
        if (color == TeamColor.WHITE) {
            ChessPosition kingPos = new ChessPosition(1, 5);
            ChessPosition rookPos = new ChessPosition(1, 1);
            if (whiteKingMoved || whiteQueenRookMoved || !getPiecesBetween(kingPos, rookPos).isEmpty()
                    || isInCheck(color) || isInCheckAfterMove(new ChessMove(kingPos, new ChessPosition(1, 4), null), color)
                    || isInCheckAfterMove(new ChessMove(kingPos, new ChessPosition(1, 3), null), color)) {
                return false;
            }
        }
        else {
            ChessPosition kingPos = new ChessPosition(8, 5);
            ChessPosition rookPos = new ChessPosition(8, 1);
            if (blackKingMoved || blackQueenRookMoved || !getPiecesBetween(kingPos, rookPos).isEmpty()
                    || isInCheck(color) || isInCheckAfterMove(new ChessMove(kingPos, new ChessPosition(8, 4), null), color)
                    || isInCheckAfterMove(new ChessMove(kingPos, new ChessPosition(8, 3), null), color)) {
                return false;
            }
        }

        return true;
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
