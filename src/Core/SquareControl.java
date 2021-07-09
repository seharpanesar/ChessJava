package Core;

import java.util.ArrayList;

public class SquareControl {
    private static int oppKingSquareNum = 0;
    private static ArrayList<Check> checks;
    private static ArrayList<Pin> pins;
    private static String controlGrid;

    public static String getEmptyBitBoard() {
        return "0".repeat(64);
    }

    /* This method is somewhat similar to getLegalMoves, except it ignores checks ON the control color. It returns the squares that a color
     * controls in bitboard String format. For example, if square 0, 1 and 23 are controlled by the opponent, index 0, 1
     * and 23 of the 64 bit bit string are 1. All of the other bits are 0.
     *
     * Control color represents which color we are scanning for. True = find white's control. False = find black's
     * control
     *
     * This function also updates the number of checks and pins imposed BY the control color. These checks and pins
     * can later be retrieved with their get methods.
     */

    public static void calculateControlGrid(Board board, boolean controlColor) {
        checks = new ArrayList<>();
        pins = new ArrayList<>();

        ArrayList<Piece> piecesToMove = controlColor ? board.getWhitePieces() : board.getBlackPieces();

        StringBuilder threatsBitBoard = new StringBuilder(getEmptyBitBoard());

        char[][] originalBoard = board.getRepresentation();
        char[][] modifiedBoard = new char[8][8];

        char kingToRemove = controlColor ? 'k' : 'K'; // need to remove because a king could be blocking the path of control of a sliding piece
        oppKingSquareNum = 0; //still need to keep track of location to check for checks

        // making a new copy of board because we need to remove the !controlColor's king.
        for (int i = 0; i < originalBoard.length; i++) {
            for (int j = 0; j < originalBoard[i].length; j++) {
                if (originalBoard[i][j] == kingToRemove) { //not copying king
                    oppKingSquareNum = i*8 + j;
                    modifiedBoard[i][j] = '-';
                }
                else {
                    modifiedBoard[i][j] = originalBoard[i][j];
                }
            }
        }

        //iterating through board and finding squares that are controlled by controlColor

        for (Piece piece : piecesToMove) {
            char pieceRep = Character.toLowerCase(piece.getPieceRep());
            int squareNum = piece.getSquareNum();

            int i = squareNum / board.BOARD_LENGTH;
            int j = squareNum % board.BOARD_LENGTH;

            switch (pieceRep) {
                case 'p' -> pawnThreats(i, j, controlColor, threatsBitBoard);
                case 'n' -> knightThreats(i, j, threatsBitBoard);
                case 'b', 'r', 'q' -> slidingPieceThreats(modifiedBoard, pieceRep, i, j, controlColor, threatsBitBoard);
                case 'k' -> kingThreats(i, j, threatsBitBoard);
            }
        }

        controlGrid = threatsBitBoard.toString();
    }

    private static void pawnThreats(int i, int j, boolean controlColor, StringBuilder threatsBitBoard) {
        int[][] pawnRange = controlColor ? new int[][] {{-1,-1}, {-1,1}} : new int[][] {{1,-1}, {1,1}};

        for (int[] move : pawnRange) {
            int jumpI = i + move[0];
            int jumpJ = j + move[1];
            int newSquareNum = jumpI * 8 + jumpJ;

            if (LegalMoves.isInBounds(jumpI, jumpJ)) {
                threatsBitBoard.setCharAt(newSquareNum, '1');

                // if piece is checking the king
                if (newSquareNum == oppKingSquareNum) {
                    checks.add(new Check(i*8 + j));
                }
            }
        }
    }

    private static void knightThreats(int i, int j, StringBuilder threatsBitBoard) {
        int[][] knightRange = {{1,2}, {-1,2}, {1,-2}, {-1,-2}, {2,1}, {-2,1}, {2,-1}, {-2,-1}};

        for (int[] move : knightRange) {
            int jumpI = i + move[0];
            int jumpJ = j + move[1];
            int newSquareNum = jumpI * 8 + jumpJ;

            if (LegalMoves.isInBounds(jumpI, jumpJ)) {
                threatsBitBoard.setCharAt(newSquareNum, '1');

                // if piece is checking the king
                if (newSquareNum == oppKingSquareNum) {
                    checks.add(new Check(i*8 + j));
                }
            }
        }
    }

    private static void slidingPieceThreats(char[][] board, char pieceRep, int i, int j, boolean controlColor, StringBuilder threatsBitBoard) {
        int[][] offsets = {{1,1}, {-1,1}, {1,-1}, {-1,-1}, {0,1}, {1,0}, {0,-1}, {-1,0}};

        int startInd = (pieceRep == 'r') ? 4 : 0;
        int endInd = (pieceRep == 'b') ? 4 : offsets.length;

        boolean pinFound = false; // allows for optimization. If pin is found, we do not need to check other directions for pins
        boolean checkFound = false;

        for (int index = startInd; index < endInd; index++) {
            int depth = 1; // represents how far a sliding piece can go in 1 direction
            ArrayList<Integer> rayOfSquares = new ArrayList<>(); // this is the set of squares in a particular direction. Needed in the case of checks and pins
            rayOfSquares.add(i*8 + j);

            int newI;
            int newJ;
            int newSquareNum;

            while (true) { // must keep checking in offset direction
                newI = i + offsets[index][0] * depth;
                newJ = j + offsets[index][1] * depth++;
                newSquareNum = newI*8 + newJ;

                if (!LegalMoves.isInBounds(newI, newJ)) {
                    break;
                }

                threatsBitBoard.setCharAt(newSquareNum, '1');
                rayOfSquares.add(newSquareNum);

                // if sliding piece is checking the king
                if (newSquareNum == oppKingSquareNum) {
                        /* available squares to block the check do not include the king square, so we
                           remove that square
                         */
                    rayOfSquares.remove(rayOfSquares.size() - 1); //king removed from "available squares" in check object
                    checks.add(new Check(rayOfSquares, i*8 + j));
                    checkFound = true;
                }

                /* if there is anything in the way (including own pieces), it will control that square, but
                   not any further squares.
                 */
                if (board[newI][newJ] != '-') {
                    break;
                }
            }

            //checking for pins here: code before while loop is to ensure the conditions for a pinned piece

            if (!LegalMoves.isInBounds(newI, newJ)) { // must be inbounds
                continue;
            }

            if (pinFound || checkFound) { // no need to check for pin if pin is already found in a direction AND check already found = no pin
                continue;
            }

            char blockingPiece = board[newI][newJ];

            //ensuring blocking piece is correct color.

            if ((controlColor && LegalMoves.isWhitePiece(blockingPiece)) ||
                    (!controlColor && LegalMoves.isBlackPiece(blockingPiece))) {
                continue;
            }

            Piece pinnedPiece = new Piece(board[newI][newJ], newSquareNum);

            /* if code reaches this far, blocking piece is of correct color. Now we need to look for clear path
               to opp color king. Any obstruction = no pin
             */
            while (true) {
                newI = i + offsets[index][0] * depth;
                newJ = j + offsets[index][1] * depth++;
                newSquareNum = newI*8 + newJ;

                if (!LegalMoves.isInBounds(newI, newJ)) {
                    break;
                }

                // king found! -> pin established
                if (newSquareNum == oppKingSquareNum) {
                    pinFound = true;
                    pins.add(new Pin(rayOfSquares, pinnedPiece));
                    break;
                }

                rayOfSquares.add(newSquareNum);

                char currSquarePiece = board[newI][newJ];

                //obstruction -> no pin. else, keep looking for king
                if (currSquarePiece != '-') {
                    break;
                }
            }
        } // for
    }

    private static void kingThreats(int i, int j, StringBuilder threatsBitBoard) {
        int[][] kingRange = {{0,1}, {0,-1}, {1,0}, {-1,0}, {1,1}, {1,-1}, {-1,1}, {-1,-1}};

        for (int[] move : kingRange) {
            int jumpI = i + move[0];
            int jumpJ = j + move[1];
            int newSquareNum = jumpI * 8 + jumpJ;

            if (LegalMoves.isInBounds(jumpI, jumpJ)) {
                threatsBitBoard.setCharAt(newSquareNum, '1');
            }
            // no need to check for checks because a check by a king is logically impossible.
        }
    }

    public static ArrayList<Check> getChecks() {
        assert checks != null;
        return checks;
    }

    public static ArrayList<Pin> getPins() {
        assert pins != null;
        return pins;
    }

    public static String getControlGrid() {
        return controlGrid;
    }
}
