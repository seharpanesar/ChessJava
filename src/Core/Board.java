package Core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Board {
    final int BOARD_LENGTH = 8;

    private char[][] representation;
    private final ArrayList<Piece> whitePieces = new ArrayList<>();
    private final ArrayList<Piece> blackPieces = new ArrayList<>();

    private boolean whitesTurn;

    // castling flags. true means that this castling is available.
    private Boolean whiteKSideCastle = false;
    private Boolean whiteQSideCastle = false;
    private Boolean blackKSideCastle = false;
    private Boolean blackQSideCastle = false;

    StringBuilder currentPGN = new StringBuilder();

    private final Stack<String> castleStates = new Stack<>(); // remembers the castle booleans. useful for undoing moves

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
                case 'K' -> whiteKSideCastle = true;
                case 'Q' -> whiteQSideCastle = true;
                case 'k' -> blackKSideCastle = true;
                case 'q' -> blackQSideCastle = true;
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
        } else { // no enpassant = put random move
            targetEPMove = new Move(new Piece( -100), -100);
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

        ArrayList<Piece> attackingPieces = whitesTurn ? whitePieces : blackPieces;
        ArrayList<Piece> opposingPieces = whitesTurn ? blackPieces : whitePieces;

        if (toPlay.enpassantFlag()) {
            representation[beforeI][afterJ] = '-'; // remove pawn that is was taken by enpassant (board update)
        }

        boolean promotion = toPlay.pawnPromotionFlag();

        // updating internal board representation
        representation[beforeI][beforeJ] = '-';
        representation[afterI][afterJ] = promotion ? toPlay.getPromotionPiece() : toPlay.getPiece().getPieceRep();


        //updating piece arraylist if capture
        if (toPlay.captureFlag()) {
            opposingPieces.remove(toPlay.getCapturedPiece());
        }

        for (Piece attackingPiece : attackingPieces) { // update squareNum of moved piece
            if (attackingPiece.getSquareNum() == beforeSq) {
                attackingPiece.setSquareNum(afterSq);
                if (promotion) { // promotion case
                    attackingPiece.setPieceRep(toPlay.getPromotionPiece());
                }
            }
        }

        castlingUpdate(toPlay);

        Driver.movesPlayed.add(toPlay);

        whitesTurn = !whitesTurn;
    }

    public void addMoveToPgn(Move toPlay) {
        ArrayList<Move> movesPlayed = Driver.movesPlayed;
        System.out.println("Moves Played : " + movesPlayed.size());

        int numMovesPlayed = movesPlayed.size();
        SquareControl.calculateControlGrid(this, !whitesTurn); // sets up control grid player who ust played, checking for checks

        //castling = corner case
        if (toPlay.castlingFlag()) { // king side
            if (toPlay.getAfterSquare() > toPlay.getBeforeSquare()) {
                currentPGN.append("O-O");
            } else { // queen side
                currentPGN.append("O-O-O");
            }
            checkForChecks();
            return;
        }

        //pawn promotion = corner case
        if (toPlay.pawnPromotionFlag()) {
            currentPGN.append(LegalMoves.squareNumToAlgebraic(toPlay.getAfterSquare()));
            currentPGN.append('=');
            currentPGN.append(Character.toUpperCase(toPlay.getPiece().getPieceRep()));
            checkForChecks();
            return;
        }

        //assign letter to piece that moved. Pawn = no letter UNLESS it is a capture, in which case, place alg notation

        char firstLetter = Character.toUpperCase(toPlay.getPiece().getPieceRep());

        if (firstLetter == 'P') {
            if (toPlay.captureFlag()) {
                firstLetter = findFile(toPlay.getBeforeSquare());
            } else {
                firstLetter = '\0';
            }
        }

        currentPGN.append(firstLetter);

        if (toPlay.captureFlag()) {
            currentPGN.append('x');
        }

        currentPGN.append(LegalMoves.squareNumToAlgebraic(toPlay.getAfterSquare()));
        checkForChecks();
    }

    private void checkForChecks() {
        if (SquareControl.getChecks().size() > 0) {
            currentPGN.append("+");
        }
        currentPGN.append(" ");
    }

    /*
        This method returns the a-h depending on which file the squareNum is in
     */

    private char findFile(int squareNum) {
        int j = squareNum % 8;

        return switch (j) {
            case 0 -> 'a';
            case 1 -> 'b';
            case 2 -> 'c';
            case 3 -> 'd';
            case 4 -> 'e';
            case 5 -> 'f';
            case 6 -> 'g';
            case 7 -> 'h';
            default -> '-';
        };
    }

    /* This method updates the appropriate flags and move the rook next to the king if the move is castling
     */

    private void castlingUpdate(Move toPlay) {
        rememberCastleState();

        Piece movedPiece = toPlay.getPiece();
        char currPieceRep = movedPiece.getPieceRep();

        movedPiece.incrementNumMoves();

        int afterSq = toPlay.getAfterSquare();
        int beforeSq = toPlay.getBeforeSquare();

        //rook moves -> no castling rights
        if ((currPieceRep == 'r' || currPieceRep == 'R') && (movedPiece.getNumMoves() == 1)) {
            if (beforeSq == WKingSideRookNum) {
                whiteKSideCastle = false;
            }
            else if (beforeSq == WQueenSideRookNum) {
                whiteQSideCastle = false;
            }
            else if (beforeSq == BKingSideRookNum) {
                blackKSideCastle = false;
            }
            else if (beforeSq == BQueenSideRookNum) {
                blackQSideCastle = false;
            }
        }

        //rook captured -> no castling rights
        switch (afterSq) {
            case WKingSideRookNum -> whiteKSideCastle = false;
            case WQueenSideRookNum -> whiteQSideCastle = false;
            case BKingSideRookNum -> blackKSideCastle = false;
            case BQueenSideRookNum -> blackQSideCastle = false;
        }

        // king moves = castling rights taken away for that king
        if (currPieceRep == 'K') {
            whiteKSideCastle = false;
            whiteQSideCastle = false;
        }
        else if (currPieceRep == 'k') {
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

    private void rememberCastleState() {
        StringBuilder sb = new StringBuilder();
        Boolean[] castleBools = {whiteKSideCastle, whiteQSideCastle, blackKSideCastle, blackQSideCastle};
        for (Boolean bool : castleBools) {
            if (bool) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }
        castleStates.push(sb.toString());
    }

    // placing moved piece back: place piece on before sq,
        //pawn promotion -> boardRep piece on before square becomes a pawn. and arraylist is updated
    //captures -> place captured piece on after square. Add captured piece into Arraylist
        //enpassant -> captured piece = goes to different position on boardRep
    //castling -> put rook back on correct square. and update board Booleans

    public void undoMove(Move move) {
        boolean colorThatJustMoved = move.getPiece().isWhite();

        Piece movedPiece = move.getPiece();
        movedPiece.decrementNumMoves();

        boolean isPromotion = move.pawnPromotionFlag();
        boolean isCastle = move.castlingFlag();
        boolean isCapture = move.captureFlag();

        ArrayList<Piece> attackingPieces = colorThatJustMoved ? whitePieces : blackPieces;
        ArrayList<Piece> opposingPieces = colorThatJustMoved ? blackPieces : whitePieces;

        int afterSq = move.getAfterSquare();
        int beforeSq = move.getBeforeSquare();

        //placing moved Piece back (arraylist update)
        for (Piece piece : attackingPieces) {
            if (piece.equals(new Piece(afterSq))) { //identified moved piece
                piece.setSquareNum(beforeSq);
                if (isPromotion) {
                    char pawnRep = colorThatJustMoved ? 'P' : 'p';
                    piece.setPieceRep(pawnRep);
                }
            }
        }

        int afterI = afterSq / BOARD_LENGTH;
        int afterJ = afterSq % BOARD_LENGTH;
        int beforeI = beforeSq / BOARD_LENGTH;
        int beforeJ = beforeSq % BOARD_LENGTH;

        //placing moved piece back (boardRep update)
        char movedPieceRep;
        if (isPromotion) {
            movedPieceRep = colorThatJustMoved ? 'P' : 'p';
        } else {
            movedPieceRep = movedPiece.getPieceRep();
        }
        representation[beforeI][beforeJ] = movedPieceRep;
        representation[afterI][afterJ] = '-'; // may change if capture

        //placing captured piece back (arraylist and board update)
        if (isCapture) {
            Piece capturedPiece = move.getCapturedPiece();
            opposingPieces.add(capturedPiece);

            int capPieceI = capturedPiece.getSquareNum() / 8;
            int capPieceJ = capturedPiece.getSquareNum() % 8;
            representation[capPieceI][capPieceJ] = capturedPiece.getPieceRep();
        }

        setPreviousCastlingRights();

        //castling: put rook back on correct square
        if (isCastle) {
            char rookRep = colorThatJustMoved ? 'R' : 'r';
            int rookAfterSq;
            int rookBeforeSq;

            //setting above 2 variables
            if (afterJ - beforeJ == 2) { // king side castle
                rookAfterSq = beforeSq + 1;
                rookBeforeSq = beforeSq + 3;

            } else {
                rookAfterSq = beforeSq - 1;
                rookBeforeSq = beforeSq - 4;
            }

            //identify rook and update position in list and board
            for (Piece piece : attackingPieces) {
                if (piece.getSquareNum() == rookAfterSq) {
                    piece.setSquareNum(rookBeforeSq);
                    int rookBeforeI = rookBeforeSq / 8;
                    int rookBeforeJ = rookBeforeSq % 8;
                    int rookAfterI = rookAfterSq / 8;
                    int rookAfterJ = rookAfterSq % 8;
                    representation[rookBeforeI][rookBeforeJ] = rookRep;
                    representation[rookAfterI][rookAfterJ] = '-';
                }
            }
        }

        whitesTurn = !whitesTurn;

        Driver.movesPlayed.remove(move);
    }

    private void setPreviousCastlingRights() {
        String previousState = castleStates.pop();

        whiteKSideCastle = previousState.charAt(0) == '1';
        whiteQSideCastle = previousState.charAt(1) == '1';
        blackKSideCastle = previousState.charAt(2) == '1';
        blackQSideCastle = previousState.charAt(3) == '1';
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

    public String getCurrentPGN() {
        return currentPGN.toString();
    }
}
