package model.Request;

import chess.ChessGame;

public record JoinGameRequest (ChessGame.TeamColor playerColor, int gameID) {

}
