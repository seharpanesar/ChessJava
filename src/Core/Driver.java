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

    public static void main(String[] args) throws IOException {
        movesAvailable = LegalMoves.getAllMoves(mainBoard);
        new GUIFrame(mainBoard.getRepresentation());
    }
}
