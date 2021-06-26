package Core;

import GUI.BoardPanel;
import GUI.GUIFrame;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Driver {
    public static ArrayList<Move> movesPlayed = new ArrayList<>();
    public static ArrayList<Move> movesAvailable = new ArrayList<>();
    public static Board mainBoard = new Board(); // initialize board

    /* Todo: start here
        - gui
        - recursive move generator test: see if it lines up with accepted answers
        - minimax
     */

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in); // temporary input

        new GUIFrame(mainBoard.getRepresentation());

        while (true) {
            long start = System.currentTimeMillis();
            movesAvailable = LegalMoves.getAllMoves(mainBoard, mainBoard.isWhitesTurn());
            long end = System.currentTimeMillis();
            System.out.printf("Legal moves calculated in %d milliseconds\n", end-start);

            if (movesAvailable.size() == 0) { // mate
                if (SquareControl.getChecks().size() == 0) {
                    System.out.println("Draw by stalemate!");
                }
                else {
                    String colorThatWon = mainBoard.isWhitesTurn() ? "Black" : "White";
                    System.out.println("Checkmate! " + colorThatWon + " won");
                }
                break;
            }

            Move toPlay = null;
            boolean flag = true;
            do {
                System.out.println("\nplay move");
                String input = scanner.nextLine();

                for (Move move : movesAvailable) {
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

        }
    }


}
