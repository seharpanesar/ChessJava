package GUI;

import Core.Driver;
import Core.Piece;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static Core.Driver.mainBoard;
import static GUI.GUIFrame.images;

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

    @Override
    public void paint(Graphics g) {
        if (Driver.movesPlayed.size() == 0) {
            super.paint(g);
            return;
        }
        int pxLen = BoardPanel.SQUARE_LENGTH_PX;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((i+j) % 2 == 1) {
                    g.setColor(SquareLabel.darkSq);
                } else {
                    g.setColor(SquareLabel.lightSq);
                }
                g.fillRect(i*pxLen, j*pxLen, pxLen, pxLen);
            }
        }

        ArrayList<Piece> allPieces = new ArrayList<>();
        allPieces.addAll(mainBoard.getWhitePieces());
        allPieces.addAll(mainBoard.getBlackPieces());

        for (Piece piece : allPieces) {
            int sqNum = piece.getSquareNum();
            int xPix = (sqNum % 8) * pxLen;
            int yPix = (sqNum / 8) * pxLen;
            switch (Character.toLowerCase(piece.getPieceRep())) {
                case 'p':
                    if (piece.isWhite()) {
                        g.drawImage(images[5].getImage(), xPix, yPix, this);
                    } else {
                        g.drawImage(images[11].getImage(), xPix, yPix, this);
                    }
                    break;
                case 'n':
                    if (piece.isWhite()) {
                        g.drawImage(images[3].getImage(), xPix, yPix, this);
                    } else {
                        g.drawImage(images[9].getImage(), xPix, yPix, this);
                    }
                    break;
                case 'b':
                    if (piece.isWhite()) {
                        g.drawImage(images[2].getImage(), xPix, yPix, this);
                    } else {
                        g.drawImage(images[8].getImage(), xPix, yPix, this);
                    }
                    break;
                case 'r':
                    if (piece.isWhite()) {
                        g.drawImage(images[4].getImage(), xPix, yPix,this);
                    } else {
                        g.drawImage(images[10].getImage(), xPix, yPix, this);
                    }
                    break;
                case 'q':
                    if (piece.isWhite()) {
                        g.drawImage(images[1].getImage(), xPix, yPix, this);
                    } else {
                        g.drawImage(images[7].getImage(), xPix, yPix, this);
                    }
                    break;
                case 'k':
                    if (piece.isWhite()) {
                        g.drawImage(images[0].getImage(), xPix, yPix, this);
                    } else {
                        g.drawImage(images[6].getImage(), xPix, yPix, this);
                    }
            } // switch
        } // for
    } // paint()
}
