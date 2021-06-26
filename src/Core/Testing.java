package Core;

import java.util.ArrayList;

public class Testing {
    public int moveGenerationTest(int depth, Board board) {
        if (depth == 0) {
            return 1;
        }

        ArrayList<Move> moves = LegalMoves.getAllMoves(board, board.isWhitesTurn());
        int numPositions = 0;

        for (Move move : moves) {
            /*todo: work on undo function for board:

             */
        }

        return numPositions;
    }

    public static void main(String[] args) {
        Board board = new Board();
        Testing testing = new Testing();

        System.out.println(testing.moveGenerationTest(3, board));
    }
}
