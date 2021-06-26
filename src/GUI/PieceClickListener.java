package GUI;

import Core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import static Core.Driver.movesAvailable;

public class PieceClickListener implements MouseListener {
    private final Color lightRed = new Color(255, 141, 135);
    private final Color darkRed = new Color(242, 66, 53);
    private final Color lightYellow = new Color(250, 222, 145);
    private final Color darkYellow = new Color(232, 197, 102);

    ArrayList<Pair> highlightedSq = new ArrayList<>();
    ArrayList<Move> highlightedMove = new ArrayList<>(); // parallel to above list
    Pair selectedPiece;

    @Override
    public void mouseClicked(MouseEvent e) {
        int iClicked =(int) e.getPoint().getY() / BoardPanel.SQUARE_LENGTH_PX;
        int jClicked = (int) e.getPoint().getX() / BoardPanel.SQUARE_LENGTH_PX;

        /* if square clicked is one of the legal squares, then move piece. otherwise, reset light sq dark sq
         * and do following code
         */

        SquareLabel[][] squares = BoardPanel.getSquares();

        //TODO implement mate here.
        for (int i = 0; i < highlightedSq.size(); i++) {
            Pair pair = highlightedSq.get(i);
            if (pair.equals(iClicked, jClicked)) {
                Move moveToMake = highlightedMove.get(i);

                GUIFrame.makeMove(moveToMake, squares); // external board change

                Driver.mainBoard.makeMove(moveToMake); // internal board change
                Driver.movesPlayed.add(moveToMake);
                resetHighlights();

                movesAvailable = LegalMoves.getAllMoves(Driver.mainBoard, Driver.mainBoard.isWhitesTurn());
                return;
            }
        }

        char pieceChar = squares[iClicked][jClicked].getPieceChar();
        resetHighlights();

        //if user chooses a square with no piece, return
        if (pieceChar == '-') {
            return;
        }

        //if user selected piece that opposite to their color, return
        if (LegalMoves.isWhitePiece(pieceChar) != Driver.mainBoard.isWhitesTurn()) {
            return;
        }

        //user wants to check options for other pieces
        Color yellow = (iClicked + jClicked) % 2 == 0 ? lightYellow : darkYellow;
        squares[iClicked][jClicked].setBackground(yellow); // selected piece is green
        selectedPiece = new Pair(iClicked, jClicked);

        ArrayList<Move> allMoves = Driver.movesAvailable;

        //highlight potential squares the selected piece could go to

        for (Move move : allMoves) {
            int beforeSq = move.getBeforeSquare();
            int afterSq = move.getAfterSquare();

            int beforeI = beforeSq / BoardPanel.BOARD_LENGTH_SQ;
            int beforeJ = beforeSq % BoardPanel.BOARD_LENGTH_SQ;

            int afterI = afterSq / BoardPanel.BOARD_LENGTH_SQ;
            int afterJ = afterSq % BoardPanel.BOARD_LENGTH_SQ;
            if (iClicked == beforeI && jClicked == beforeJ) {
                Color red = ((afterI + afterJ) % 2 == 0) ? lightRed : darkRed;
                squares[afterI][afterJ].setBackground(red);
                highlightedSq.add(new Pair(afterI, afterJ));
                highlightedMove.add(move);
            }
        }
    }

    private void resetHighlights() {
        JLabel[][] squares = BoardPanel.getSquares();

        if (selectedPiece == null) {
            return;
        }

        for (Pair pair : highlightedSq) { // removing red highlight sq
            int i = pair.getI();
            int j = pair.getJ();
            Color color = (i + j) % 2 == 0 ? SquareLabel.lightSq : SquareLabel.darkSq;
            squares[i][j].setBackground(color);
        }

        //removing piece select highlight
        int i = selectedPiece.getI();
        int j = selectedPiece.getJ();
        Color color = (i + j) % 2 == 0 ? SquareLabel.lightSq : SquareLabel.darkSq;
        squares[i][j].setBackground(color);

        highlightedSq.clear();
        highlightedMove.clear();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}