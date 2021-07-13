package AI;
import Core.Move;
import Core.SquareControl;

import java.util.ArrayList;
import java.util.Collections;

public class MoveOrdering {
    /* this method will optimize the alpha beta pruning because it will order moves from (likely) good moves and (likely)
       bad moves
     */
    public static void orderMoves(ArrayList<Move> moves) {
        String opponentControlMap = SquareControl.getControlGrid();

        for (Move move : moves) {
            int moveScoreGuess = 0; // higher = more likely to be better move

            if (move.captureFlag()) { // capturing a higher value piece is likely good
                moveScoreGuess = Minimax.getValue(move.getCapturedPiece().getPieceRep()) -
                                Minimax.getValue(move.getPiece().getPieceRep());
            }

            if (move.pawnPromotionFlag()) { // pawn promotion is likely good
                moveScoreGuess += Minimax.getValue(move.getPromotionPiece());
            }

            //going to a square that opponent controls is likely not good. higher piece value -> worse it is
            if (opponentControlMap.charAt(move.getAfterSquare()) == '1') {
                moveScoreGuess -= (0.5) * Minimax.getValue(move.getPiece().getPieceRep());
            }

            move.setMoveScore(moveScoreGuess);
        }

        //sort by moveScore and speedup pruning!
        Collections.sort(moves);
    }
}
