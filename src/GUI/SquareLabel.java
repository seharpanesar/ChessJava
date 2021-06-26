package GUI;

import Core.LegalMoves;

import javax.swing.*;
import java.awt.*;

public class SquareLabel extends JLabel {
    final static Color darkSq = new Color(150, 77, 5);
    final static Color lightSq = new Color(247, 199, 151);

    private char pieceChar;

    public SquareLabel(char piece, int i, int j) {
        pieceChar = piece;

        this.setOpaque(true);
        this.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon pieceIcon = getPieceIcon(piece);
        if (piece != '-') {
            this.setIcon(pieceIcon);
        }

        boolean isLightSquare = (i + j) % 2 == 0;
        if (isLightSquare) {
            this.setBackground(lightSq);
        } else {
            this.setBackground(darkSq);
        }
    }

    public static ImageIcon getPieceIcon(char piece) {
        int index = 0;
        if (LegalMoves.isBlackPiece(piece)) {
            index += 6;
            piece = Character.toUpperCase(piece);
        }
        index += switch (piece) {
            case 'K' -> 0;
            case 'Q' -> 1;
            case 'B' -> 2;
            case 'N' -> 3;
            case 'R' -> 4;
            case 'P' -> 5;
            default -> -10;
        };

        if (index > -1) {
            return GUIFrame.images[index];
        }
        return null;
    }

    public char getPieceChar() {
        return pieceChar;
    }

    public void setPieceChar(char pieceChar) {
        this.pieceChar = pieceChar;
    }
}
