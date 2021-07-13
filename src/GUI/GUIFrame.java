package GUI;

import AI.Minimax;
import Core.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static Core.Driver.*;
import static GUI.SquareLabel.darkSq;
import static GUI.SquareLabel.lightSq;

public class GUIFrame extends JFrame {
    final int IMAGE_LENGTH = 200;
    static ImageIcon[] images = new ImageIcon[12];

    BoardPanel boardPanel;
    JPanel eastPanel;
    JPanel westPanel;
    JPanel northPanel;
    JPanel southPanel;

    static JLabel evalLabel;

    public GUIFrame(char[][] representation) throws IOException {
        this.setTitle("Chess Engine");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(600,600));
        this.setResizable(false);
        this.setLayout(new BorderLayout(10, 10));


        //center the JFrame
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, (dim.height/2-this.getSize().height/2) - 100);

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

        boardPanel = new BoardPanel(representation);
        eastPanel = new JPanel();
        westPanel = new JPanel();
        northPanel = new JPanel(); //header and eval Panel
        southPanel = new JPanel();

        JLabel headerLabel = new JLabel("Sehar's Chess Engine!");
        headerLabel.setFont(new Font ("Verdana", Font.BOLD, 18));
        JPanel headerPanel = new JPanel();
        headerPanel.add(headerLabel);

        evalLabel = new JLabel("Evaluation: ");
        evalLabel.setFont(new Font ("Verdana", Font.BOLD, 18));
        JPanel evalPanel = new JPanel();
        evalPanel.setBorder(BorderFactory.createEmptyBorder(0,0,30,0));
        evalPanel.add(evalLabel);
        northPanel.setLayout(new BorderLayout());
        northPanel.add(headerPanel);
        northPanel.add(evalPanel, BorderLayout.SOUTH);


        this.add(boardPanel, BorderLayout.NORTH);
        this.add(eastPanel, BorderLayout.EAST);
        this.add(westPanel, BorderLayout.WEST);
        this.add(northPanel, BorderLayout.SOUTH);

        this.getContentPane().addMouseListener(new PieceClickListener());

        this.pack();
        this.setVisible(true);

    }

    public static void setEval(int integer) {
        if (Minimax.mateDetected) {
            evalLabel.setText("Mate in " + Minimax.mateScore);
            return;
        }
        evalLabel.setText("Evaluation: " + integer);
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

        if (moveToMake.enpassantFlag()) { // remove pawn that was taken by enpassant (board update)
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

    public static void checkmate() {
        String message;

        if (mainBoard.isWhitesTurn()) {
            message = "Checkmate! You lose :(";
        } else {
            message = "Checkmate! You win!";
        }

        JOptionPane.showMessageDialog(null,
                message);
    }

    public static void stalemate() {
        JOptionPane.showMessageDialog(null,
                "Draw by Stalemate");
    }

    public static void playMove(Move moveToMake) {
        SquareLabel[][] squares = BoardPanel.getSquares();

        GUIFrame.makeMove(moveToMake, squares); // external board change
        Driver.mainBoard.makeMove(moveToMake); // internal board change
        mainBoard.addMoveToPgn(moveToMake); // updating pgn for book moves

        System.out.println(mainBoard.getCurrentPGN());

        movesAvailable = LegalMoves.getAllMoves(Driver.mainBoard);

        if (movesAvailable.size() == 0) { //scan for mates
            ArrayList<Check> checks = SquareControl.getChecks();
            resetHighlights();
            if (checks.size() >= 1) { // at least 1 check = checkmate
                GUIFrame.checkmate();
            } else { // stalemate
                GUIFrame.stalemate();
            }
            System.exit(0);
        }
    }

    public static void resetHighlights() {
        SquareLabel[][] squares = BoardPanel.getSquares();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Color color = (i + j) % 2 == 0 ? lightSq : darkSq;
                squares[i][j].setBackground(color);
            }
        }
    }


}
