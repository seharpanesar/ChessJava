package AI;

import Core.Driver;
import Core.Move;
import GUI.GUIFrame;

import static AI.Book.bookMovesLeft;

public class ComputerMoveThread extends Thread{
    public void run() {
        if (bookMovesLeft) {
            Move compMove = Driver.book.findBookMove();
            if (compMove != null) {
                GUIFrame.playMove(compMove);
                System.out.println("Book Move played");
                return;
            }
        }


        Move computerMove = AlphaBetaPruning.execute();
        //System.out.println("Boards Eval: " + Minimax.BOARDS_EVALUATED + "\n");
        GUIFrame.playMove(computerMove);
        GUIFrame.setEval(AlphaBetaPruning.bestEval);


    }
}
