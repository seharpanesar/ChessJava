package AI;

import Core.*;

import java.sql.PreparedStatement;
import java.util.ArrayList;

public class Minimax {
    final static int pawnVal = 100;
    final static int knightVal = 300;
    final static int bishopVal = 300;
    final static int rookVal = 500;
    final static int queenVal = 900;

    public static int evaluate(Board board) {
        ArrayList<Piece> whitePieces = board.getWhitePieces();
        ArrayList<Piece> blackPieces = board.getBlackPieces();

        int whiteCount = countMaterial(whitePieces);
        int blackCount = countMaterial(blackPieces);

        return whiteCount - blackCount;
    }

    public static int countMaterial(ArrayList<Piece> pieces) {
        int value = 0;
        for (Piece piece : pieces) {
            switch (Character.toLowerCase(piece.getPieceRep())) {
                case 'p': value += pawnVal;
                case 'n': value += knightVal;
                case 'b': value += bishopVal;
                case 'r': value += rookVal;
                case 'q': value += queenVal;
            }
        }
        return value;
    }

    public static int search(int depth, Board board) {
        ArrayList<Move> availMoves = LegalMoves.getAllMoves(board);

        if (availMoves.size() == 0) { // mate = return static evaluation
            if (SquareControl.getChecks().size() > 0) { // checkmate
                int eval = board.isWhitesTurn() ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                return eval;
            } else { // stalemate = draw
                return 0;
            }
        }

        if (depth == 0) { // depth reached = return static evaluation
            return evaluate(board);
        }

        //minimax implementation below

        if (board.isWhitesTurn()) { // white is maximizing player
            int maxEval = Integer.MIN_VALUE;
            for (Move move : availMoves) {
                board.makeMove(move);
                int currPosEval = search(depth - 1, board);
                maxEval = Math.max(maxEval, currPosEval);
                board.undoMove(move);
            }
            return maxEval;
        }
        else { // black is minimizing player
            int minEval = Integer.MAX_VALUE;
            for (Move move : availMoves) {
                board.makeMove(move);
                int currPosEval = search(depth - 1, board);
                minEval = Math.min(minEval, currPosEval);
                board.undoMove(move);
            }
            return minEval;
        }
    }
}
