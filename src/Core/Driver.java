package Core;

import GUI.GUIFrame;

import java.util.ArrayList;
import java.util.Scanner;

public class Driver {
    static ArrayList<Move> movesPlayed = new ArrayList<>();

    /* Todo: start here
        - gui
        - recursive move generator test: see if it lines up with accepted answers
        - minimax
     */

    public static void main(String[] args) {
        Board mainBoard = new Board(); // initialize board
        boolean whitesTurn = mainBoard.isWhitesTurn();

        Scanner scanner = new Scanner(System.in); // temporary input

        new GUIFrame();

        while (true) {
            mainBoard.printBoard();
            long start = System.currentTimeMillis();

            ArrayList<Move> moves = LegalMoves.getAllMoves(mainBoard, whitesTurn);
            long end = System.currentTimeMillis();
            System.out.printf("Legal moves calculated in %d milliseconds\n", end-start);

            if (moves.size() == 0) { // mate
                if (SquareControl.getChecks().size() == 0) {
                    System.out.println("Draw by stalemate!");
                }
                else {
                    String colorThatWon = whitesTurn ? "Black" : "White";
                    System.out.println("Checkmate! " + colorThatWon + " won");
                }
                break;
            }

            for (Move move : moves) {
                System.out.println(move);
            }
            Move toPlay = null;
            boolean flag = true;
            do {
                System.out.println("\nplay move");
                String input = scanner.nextLine();

                for (Move move : moves) {
                    if (move.toString().equals(input)) {
                        toPlay = move;
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    System.out.println("Move not found");
                }
            } while (flag);

            mainBoard.makeMove(toPlay);
            movesPlayed.add(toPlay);

            whitesTurn = !whitesTurn;
        }
    }
}
