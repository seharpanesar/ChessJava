package Core;

import java.util.ArrayList;
import java.util.Arrays;

public class Board {
    final int BOARD_LENGTH = 8;

    private char[][] representation;
    private final ArrayList<Piece> whitePieces = new ArrayList<>();
    private final ArrayList<Piece> blackPieces = new ArrayList<>();

    private boolean whitesTurn;

    // castling flags. true means that this castling is available.
    private boolean whiteKSideCastle = true;
    private boolean whiteQSideCastle = true;
    private boolean blackKSideCastle = true;
    private boolean blackQSideCastle = true;

    //enpassant target move
    private Move targetEPMove; // rarely used, but whn fen string indicates that enpassant can be done in that position, this is initialized

    private final int WKingSideRookNum = 63;
    private final int WQueenSideRookNum = 56;
    private final int BKingSideRookNum = 7;
    private final int BQueenSideRookNum = 0;

    public Board() {
        setUpBoardFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public Board(String fen) {
        setUpBoardFen(fen);

    }

    /* This method takes in a partial fen string and sets the pieces on the char board representation accordingly.
     * It also places white and black pieces in their resp arraylists. Argument only takes in the piece placement string.
     */

    private void setUpBoardFen(String fen) {
        String[] parts = fen.split(" ");
        representation = new char[BOARD_LENGTH][BOARD_LENGTH];

        //initializes all of representation with '-'

        for (char[] chars : representation) {
            Arrays.fill(chars, '-');
        }

        int i = 0;
        int j = 0;


        // 1. piece placement
        int piecePlacementLength = parts[0].length();
        for (int k = 0; k < piecePlacementLength; k++) {
            char c = parts[0].charAt(k);
            if (Character.isDigit(c)) {
                j += Character.getNumericValue(c);
            }
            else if (c == '/') {
                i++;
                j = 0;
            } else {
                if (LegalMoves.isWhitePiece(c)) {
                    whitePieces.add(new Piece(c, i*8 + j));
                } else {
                    blackPieces.add(new Piece(c, i*8 + j));
                }
                representation[i][j++] = c;
            }
        }

        // 2. to move
        whitesTurn = parts[1].charAt(0) == 'w';

        // 3. castling
        int castlingFieldLength = parts[2].length();
        for (int k = 0; k < castlingFieldLength; k++) {
            char castlingRight = parts[2].charAt(k);
            switch (castlingRight) {
                case 'K':
                    whiteKSideCastle = true;
                    break;
                case 'Q':
                    whiteQSideCastle = true;
                    break;
                case 'k':
                    blackKSideCastle = true;
                    break;
                case 'q':
                    blackQSideCastle = true;
                    break;
                default: // never should reach here so stop program safely.
                    assert false;

            }
        }

        //4. enpassant
        if (parts[3].charAt(0) != '-') {
            char pawnThatJustMoved = whitesTurn ? 'p' : 'P';
            int diagCapSq = LegalMoves.algebraicToSquareNum(parts[3]);

            int afterSq = whitesTurn ? diagCapSq + BOARD_LENGTH : diagCapSq - BOARD_LENGTH;
            int beforeSq = whitesTurn ? afterSq - 2*BOARD_LENGTH : afterSq + 2*BOARD_LENGTH;

            Piece piece = new Piece(pawnThatJustMoved, beforeSq);
            targetEPMove = new Move(piece, afterSq);
        }

        //5. TODO Halfmove counter since the last capture or pawn advance

        //6. TODO Total full moves

    }

    public char[][] getRepresentation() {
        return representation;
    }

    public void printBoard() {
        for (char[] chars : representation) {
            for (char pieceInRow : chars) {
                System.out.printf("%c ", pieceInRow);
            }
            System.out.println();
        }
        System.out.println();
    }


    public void makeMove(Move toPlay) {
        assert toPlay.getPiece().isWhite() == whitesTurn;

        int beforeSq = toPlay.getBeforeSquare();
        int afterSq = toPlay.getAfterSquare();

        int beforeI = beforeSq / BOARD_LENGTH;
        int beforeJ = beforeSq % BOARD_LENGTH;

        int afterI = afterSq / BOARD_LENGTH;
        int afterJ = afterSq % BOARD_LENGTH;

        boolean promotion = toPlay.pawnPromotionFlag();

        ArrayList<Piece> attackingPieces = whitesTurn ? whitePieces : blackPieces;
        ArrayList<Piece> opposingPieces = whitesTurn ? blackPieces : whitePieces;

        if (toPlay.isEnpassant()) {
            representation[beforeI][afterJ] = '-'; // remove pawn that is was taken by enpassant (board update)
            opposingPieces.remove(new Piece('p', beforeI * 8 + afterJ)); // piece list update
        }

        // updating internal board representation
        representation[beforeI][beforeJ] = '-';
        representation[afterI][afterJ] = promotion ? toPlay.getPromotionPiece() : toPlay.getPiece().getPieceRep();


        //updating piece arraylist
        opposingPieces.remove(new Piece(afterSq)); // removing captured piece.

        for (Piece attackingPiece : attackingPieces) { // update squareNum of moved piece
            if (attackingPiece.getSquareNum() == beforeSq) {
                attackingPiece.setSquareNum(afterSq);
                if (promotion) { // promotion case
                    attackingPiece.setPieceRep(toPlay.getPromotionPiece());
                }
            }
        }


        castlingUpdate(toPlay);

        whitesTurn = !whitesTurn;
    }

    /* This method updates the appropriate flags and move the rook next to the king if the move is castling
     */

    private void castlingUpdate(Move toPlay) {
        int afterSq = toPlay.getAfterSquare();
        int beforeSq = toPlay.getBeforeSquare();

        //if rook was captured, rook doesn't have castling rights
        switch (afterSq) {
            case WKingSideRookNum -> whiteKSideCastle = false;
            case WQueenSideRookNum -> whiteQSideCastle = false;
            case BKingSideRookNum -> blackKSideCastle = false;
            case BQueenSideRookNum -> blackQSideCastle = false;
        }

        // king moves = castling rights taken away for that king
        if (whitesTurn && toPlay.getPiece().getPieceRep() == 'K') {
            whiteKSideCastle = false;
            whiteQSideCastle = false;
        }
         else if (!whitesTurn && toPlay.getPiece().getPieceRep() == 'k') {
             blackKSideCastle = false;
             blackQSideCastle = false;
        }

        if (!toPlay.castlingFlag()) {
            return;
        }

        // reach here = move was castling.
        boolean kingSideCastle = afterSq - beforeSq == 2;
        char rookRep = whitesTurn ? 'R' : 'r';

        int rookBeforeSq = kingSideCastle ? beforeSq + 3 : beforeSq - 4;
        int rookBeforeI = rookBeforeSq / BOARD_LENGTH;
        int rookBeforeJ = rookBeforeSq % BOARD_LENGTH;

        int rookAfterSq = kingSideCastle ? afterSq - 1 : afterSq + 1;
        int rookAfterI = rookAfterSq / BOARD_LENGTH;
        int rookAfterJ = rookAfterSq % BOARD_LENGTH;

        // updating internal board representation
        representation[rookBeforeI][rookBeforeJ] = '-';
        representation[rookAfterI][rookAfterJ] = rookRep;

        //updating pieces list
        ArrayList<Piece> piecesToUpdate = whitesTurn ? whitePieces : blackPieces;
        for (Piece piece : piecesToUpdate) {
            if (piece.getSquareNum() == rookBeforeSq) {
                piece.setSquareNum(rookAfterSq);
            }
        }
    }

    public boolean whiteKSideCastle() {
        return whiteKSideCastle;
    }

    public boolean whiteQSideCastle() {
        return whiteQSideCastle;
    }

    public boolean blackKSideCastle() {
        return blackKSideCastle;
    }

    public boolean blackQSideCastle() {
        return blackQSideCastle;
    }

    public ArrayList<Piece> getWhitePieces() {
        return whitePieces;
    }

    public ArrayList<Piece> getBlackPieces() {
        return blackPieces;
    }

    public Move getTargetEPMove() {
        return targetEPMove;
    }

    public boolean isWhitesTurn() {
        return whitesTurn;
    }
}
