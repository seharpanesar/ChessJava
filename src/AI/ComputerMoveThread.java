package AI;

import Core.Driver;
import Core.Move;
import GUI.GUIFrame;

import static AI.Book.bookMovesLeft;
import static Core.Driver.mainBoard;
import static Core.Driver.movesAvailable;

public class ComputerMoveThread extends Thread{
    int depth = 5;

    public void run() {
        if (bookMovesLeft) {
            Move compMove = Driver.book.findBookMove();
            GUIFrame.playMove(compMove);
            return;
        }
        long start = System.currentTimeMillis();

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        int value;

        MoveOrdering.orderMoves(movesAvailable);

        Minimax.BOARDS_EVALUATED = 0;

        //no need to check for mates because if program gets this far, it has benn checked for

        //computer move
        Move computerMove = movesAvailable.get(0); // we know that there is at least 1 move because mates have been checked for
        if (mainBoard.isWhitesTurn()) { // white is maximizing player
            value = Integer.MIN_VALUE;
            for (Move move : movesAvailable) {
                mainBoard.makeMove(move);
                int currPosEval = Minimax.search(depth, 1, mainBoard, alpha, beta);
                mainBoard.undoMove(move);

                if (currPosEval > value) {
                    value = currPosEval;
                    computerMove = move;
                }
                if (value >= beta) {
                    break;
                }
                alpha = Math.max(alpha, value);
            }
        }
        else { // black is minimizing player
            value = Integer.MAX_VALUE;
            for (Move move : movesAvailable) {
                mainBoard.makeMove(move);
                int currPosEval = Minimax.search(depth, 1, mainBoard, alpha, beta);
                mainBoard.undoMove(move);

                if (currPosEval < value) {
                    value = currPosEval;
                    computerMove = move;
                }

                if (value <= alpha) {
                    break;
                }

                beta = Math.min(beta, value);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("depth of 5 executed in " + (end - start) + " ms ");
        System.out.println("Boards Eval: " + Minimax.BOARDS_EVALUATED + "\n");
        GUIFrame.playMove(computerMove);
        GUIFrame.setEval(value);

    }
}
