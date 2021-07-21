package Core;

import java.util.ArrayList;

public class Testing {
    public static void main(String[] args) {
        Testing testing = new Testing();
        Board board = new Board();

        ArrayList<Integer> testNums = new ArrayList<>();
        testNums.add(2);
        testNums.add(3);
        testNums.add(4);
        testing.allTests(testNums);

        //testing.runcode();
/*
        long start = System.currentTimeMillis();
        System.out.println(testing.moveGenerationTest(6, board));
        long end = System.currentTimeMillis();





        System.out.println("done in " + (end - start) + "ms");


 */

/*
        for (int i = 1; i < 7; i++) {
            long start = System.currentTimeMillis();
            System.out.println("Depth = "+ i +", moves: "+ testing.moveGenerationTest(i, board));
            long end = System.currentTimeMillis();

            System.out.println("Done in " + (end - start) + " ms\n");
        }


 */


    }

    private void runcode() {
        int inte = Integer.MIN_VALUE;
        System.out.println(inte);
        System.out.println(inte + 3);
    }

    public void perft(int depth, Board board) {
        ArrayList<Move> moves = LegalMoves.getAllMoves(board);
        int positionsEval = 0;

        char[][] copyOfBoard = copyBoard(board);

        for (Move move : moves) {
            board.makeMove(move);
            int numPositions = moveGenerationTest(depth - 1, board);
            board.undoMove(move);

            boolean isSame = boardIsSame(board.getRepresentation(), copyOfBoard);

            assert isSame;

            positionsEval += numPositions;

            StringBuilder output = new StringBuilder();
            output.append(LegalMoves.squareNumToAlgebraic(move.getBeforeSquare()));
            output.append(LegalMoves.squareNumToAlgebraic(move.getAfterSquare()));
            output.append(": ");
            output.append(numPositions);
            System.out.println(output);
        }

        System.out.println("\nPositions evaluated : " + positionsEval);
    }

    private boolean boardIsSame(char[][] original, char[][] copy) {
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                if (original[i][j] != copy[i][j]) {
                    System.out.println("differs at " + LegalMoves.squareNumToAlgebraic(i*8 + j));
                    return false;
                }
            }
        }

        return true;
    }

    private char[][] copyBoard(Board board) {
        char[][] copy = new char[8][8];
        char[][] original = board.getRepresentation();

        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, 8);
        }

        return copy;
    }


    public int moveGenerationTest(int depth, Board board) {
        if (depth == 0) {
            return 1;
        }

        ArrayList<Move> moves = LegalMoves.getAllMoves(board);
        int numPositions = 0;

        char[][] copyOfBoard = copyBoard(board);

        for (Move move : moves) {
            board.makeMove(move);
            numPositions += moveGenerationTest(depth - 1, board);
            board.undoMove(move);

            assert boardIsSame(board.getRepresentation(), copyOfBoard);
        }

        return numPositions;
    }

    public void algebraicFunctionTest() {
        String test1 = LegalMoves.squareNumToAlgebraic(49);
        System.out.println("Test passed: " + (test1.equals("b2")));
        System.out.println(test1);
    }

    private void allTests(ArrayList<Integer> testNumbers) {
        Board board1 = new Board("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        Board board2 = new Board("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -");
        Board board3 = new Board("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -");
        Board board4 = new Board("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1");
        Board board5 = new Board("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
        Board board6 = new Board("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10");

        Board[] allBoards = {board1, board2, board3, board4, board5, board6};
        int[][] allAnswers = {{1, 20, 400, 8902, 197281, 4865609},
                {1, 48, 2039, 97862, 4085603, 193690690},
                {1, 14, 191, 2812, 43238, 674624},
                {1,6,264, 9467, 422333, 15833292},
                {1, 44,  1486, 62379, 2103487, 89941194},
                {1, 46, 2079, 89890, 3894594, 164075551}};

        ArrayList<Board> selectBoards = new ArrayList<>();
        ArrayList<int[]> selectAnswers = new ArrayList<>();

        for (Integer testNum : testNumbers) {
            selectBoards.add(allBoards[testNum-1]);
            selectAnswers.add(allAnswers[testNum-1]);
        }

        for (int i = 0; i < selectBoards.size(); i++) {
            for (int j = 0; j < selectAnswers.get(i).length; j++) {
                int result = moveGenerationTest(j, selectBoards.get(i));
                System.out.printf("perft board %d depth %d ", testNumbers.get(i), j);
                if (result == selectAnswers.get(i)[j]) {
                    System.out.println("passed");
                } else {
                    System.out.printf("failed: expected %d but received %d\n", selectAnswers.get(i)[j], result);
                }
            }
            System.out.println();
        }

    }
}
