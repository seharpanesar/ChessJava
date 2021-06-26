package GUI;

import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {
    final static int BOARD_LENGTH_PX = 600;
    final static int BOARD_LENGTH_SQ = 8;
    final static int SQUARE_LENGTH_PX = BOARD_LENGTH_PX / BOARD_LENGTH_SQ;
    private static final SquareLabel[][] squares = new SquareLabel[8][8];

    public BoardPanel(char[][] representation) {
        this.setLayout(new GridLayout(8, 8));
        this.setPreferredSize(new Dimension(BOARD_LENGTH_PX, BOARD_LENGTH_PX));

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                SquareLabel toAdd = new SquareLabel(representation[i][j], i , j);
                squares[i][j] = toAdd;
                this.add(toAdd);
            }
        }
    }

    public static SquareLabel[][] getSquares() {
        return squares;
    }
}
