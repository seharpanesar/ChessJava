package Core;

import AI.Book;
import AI.ComputerMoveThread;
import GUI.GUIFrame;

import java.io.IOException;
import java.util.ArrayList;

public class Driver {
    public static ArrayList<Move> movesPlayed = new ArrayList<>();
    public static ArrayList<Move> movesAvailable = new ArrayList<>();
    public static Board mainBoard = new Board(); // initialize board
    public static Book book; // book will be used for opening moves

    static {
        try {
            book = new Book();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        movesAvailable = LegalMoves.getAllMoves(mainBoard);
        new GUIFrame(mainBoard.getRepresentation());

        if (!mainBoard.isWhitesTurn()) { // black to move = computer move
            ComputerMoveThread thread = new ComputerMoveThread();
            thread.start();
        }
    }
}
