package GUI;

import Core.Driver;
import Core.Move;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GUIFrame extends JFrame{
    final int IMAGE_LENGTH = 200;
    static ImageIcon[] images = new ImageIcon[12];

    public GUIFrame(char[][] representation) throws IOException {
        this.setTitle("Chess Engine");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(600,600));
        this.setResizable(false);


        //center the JFrame
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        //initializing images
        BufferedImage allPieces = ImageIO.read(new File("C:/Users/sehar/IdeaProjects/ChessPart2/src/GUI/ChessPieces.png"));
        int index = 0;
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 6; x++) {
                Image rawImage = allPieces.getSubimage(x*IMAGE_LENGTH, y*IMAGE_LENGTH, IMAGE_LENGTH, IMAGE_LENGTH)
                        .getScaledInstance(70,70, BufferedImage.SCALE_SMOOTH);
                images[index++] = new ImageIcon(rawImage);
            }
        }

        BoardPanel boardPanel = new BoardPanel(representation);

        this.add(boardPanel);
        this.getContentPane().addMouseListener(new PieceClickListener());

        this.pack();
        this.setVisible(true);

        System.out.println(boardPanel.getSize());
    }

    /*
     * This function updates the GUI (external representation of board) 
     */

    public static void makeMove(Move moveToMake, SquareLabel[][] squares) {
        int beforeSq = moveToMake.getBeforeSquare();
        int afterSq = moveToMake.getAfterSquare();

        int beforeI = beforeSq / BoardPanel.BOARD_LENGTH_SQ;
        int beforeJ = beforeSq % BoardPanel.BOARD_LENGTH_SQ;

        int afterI = afterSq / BoardPanel.BOARD_LENGTH_SQ;
        int afterJ = afterSq % BoardPanel.BOARD_LENGTH_SQ;

        char pieceChar = moveToMake.getPiece().getPieceRep();

        if (moveToMake.isEnpassant()) { // remove pawn that is was taken by enpassant (board update)
            updateBoard(squares[beforeI][afterJ], '-');
        }

        // updating board representation
        boolean promotion = moveToMake.pawnPromotionFlag();

        updateBoard(squares[beforeI][beforeJ], '-'); // leaving square is empty

        if (promotion) {
            char promotionPiece = moveToMake.getPromotionPiece();
            updateBoard(squares[afterI][afterJ], promotionPiece);
        } else {
            updateBoard(squares[afterI][afterJ], pieceChar);
        }

        //castling
        if (moveToMake.castlingFlag()) {
            boolean kingSideCastle = afterSq - beforeSq == 2;
            char rookRep = Driver.mainBoard.isWhitesTurn() ? 'R' : 'r';

            int rookBeforeSq = kingSideCastle ? beforeSq + 3 : beforeSq - 4;
            int rookBeforeI = rookBeforeSq / 8;
            int rookBeforeJ = rookBeforeSq % 8;

            int rookAfterSq = kingSideCastle ? afterSq - 1 : afterSq + 1;
            int rookAfterI = rookAfterSq / 8;
            int rookAfterJ = rookAfterSq % 8;

            //updating rook position
            updateBoard(squares[rookBeforeI][rookBeforeJ], '-');
            updateBoard(squares[rookAfterI][rookAfterJ], rookRep);
        }
    }

    private static void updateBoard(SquareLabel squareLabel, char c) {
        squareLabel.setPieceChar(c);
        squareLabel.setIcon(SquareLabel.getPieceIcon(c));
    }

}
