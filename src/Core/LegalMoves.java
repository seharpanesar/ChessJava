package Core;

import java.util.ArrayList;
import java.util.Collection;

public class LegalMoves {
    final static int BOARD_LENGTH = 8;
    final static int SAME_COLOR_CAPTURE = -1;
    final static int LEGAL_CAPTURE = 1;
    final static int SQUARE_FREE = 0;

    /*
     * If isWhite = true, we are returning whites moves. isWhite = false -> return blacks moves
     */
    public static ArrayList<Move> getAllMoves(Board board) {
        boolean gettingWhiteMoves = board.isWhitesTurn();
        SquareControl.calculateControlGrid(board, !gettingWhiteMoves); // sets up control grid of opposite color, checks and pins

        ArrayList<Move> allMoves = new ArrayList<>();
        ArrayList<Piece> attackingPieces = gettingWhiteMoves ? board.getWhitePieces() : board.getBlackPieces();
        ArrayList<Piece> defendingPieces = gettingWhiteMoves ? board.getBlackPieces() : board.getWhitePieces();


        char[][] boardRep = board.getRepresentation();

        // rare case, but necessary: double checks. In this case, only the king can move. Optimization could be used here
        if (SquareControl.getChecks().size() == 2) {
            char kingToMove = gettingWhiteMoves ? 'K' : 'k';
            for (Piece piece : attackingPieces) {
                if (piece.getPieceRep() == kingToMove) {
                    allMoves.addAll(getKingMove(board, piece));
                    setCapturedPieces(allMoves, defendingPieces); // keeps track of capture boolean and capture piece
                    return allMoves;
                }
            }
        }

        //take care of pins first
        ArrayList<Integer> pinSquares = new ArrayList<>();
        ArrayList<Pin> pins = SquareControl.getPins();
        for (Pin pin : pins) {
            allMoves.addAll(pinnedMoves(pin, board));
            pinSquares.add(pin.getPinnedPiece().getSquareNum());
        }

        // the rest of the possible piece moves are calculated
        for (Piece piece : attackingPieces) {
            char pieceRep = piece.getPieceRep();
            int squareNum = piece.getSquareNum();

            if (pinSquares.contains(squareNum)) { // pinned piece movement has already been calculated
                continue;
            }

            pieceRep = Character.toLowerCase(pieceRep); // to have one switch statement below

            switch (pieceRep) {
                case 'p' -> allMoves.addAll(getPawnMove(board, piece));
                case 'n' -> allMoves.addAll(getKnightMove(boardRep, piece));
                case 'b', 'r', 'q' -> allMoves.addAll(generateSlidingMoves(boardRep, piece));
                case 'k' -> allMoves.addAll(getKingMove(board, piece));
            }
        }

        setCapturedPieces(allMoves, defendingPieces); // keeps track of capture boolean and capture piece

        //in the case of checks (single checks, that is), we must filter out the moves that blunder a king

        if (SquareControl.getChecks().size() == 1) {
            ArrayList<Move> movesThatSaveKing = new ArrayList<>();
            Check check = SquareControl.getChecks().get(0);
            ArrayList<Integer> blockSq = check.getBlockSquares();

            /* A move is valid if it:
                1) is a safe king move, already generated by "getKingMove()"
                2) is a move that blocks the check
                3) is a move that captures the checker
                4) RARE: is an enpassant capture that blocks a pawn check
             */

            for (Move move : allMoves) {
                if (Character.toLowerCase(move.getPiece().getPieceRep()) == 'k') { // case 1
                    movesThatSaveKing.add(move);
                    continue;
                }

                if (blockSq.contains(move.getAfterSquare())) { // case 2,3
                    movesThatSaveKing.add(move);
                }

                if (move.enpassantFlag()) {
                    int checkerSq = check.getCheckerSquare();
                    int checkerI = checkerSq / BOARD_LENGTH;
                    int checkerJ = checkerSq % BOARD_LENGTH;
                    int captureI = move.getBeforeSquare() / BOARD_LENGTH;
                    int captureJ = move.getAfterSquare() % BOARD_LENGTH;
                    if ((captureI == checkerI) && (captureJ == checkerJ)) {
                        movesThatSaveKing.add(move);
                    }
                }
            }
            return movesThatSaveKing;
        } else {
            return allMoves;
        }
    }

    private static void setCapturedPieces(ArrayList<Move> allMoves, ArrayList<Piece> defendingPieces) {
        for (Move move : allMoves) {
            int beforeSq = move.getBeforeSquare();
            int afterSq = move.getAfterSquare();

            int beforeI = beforeSq / BOARD_LENGTH;
            int afterJ = afterSq % BOARD_LENGTH;

            int captureSq = move.enpassantFlag() ? beforeI * 8 + afterJ : afterSq;
            for (Piece piece : defendingPieces) {
                if (piece.getSquareNum() == captureSq) {
                    move.setCaptureFlag(true);
                    move.setCapturedPiece(piece);
                }
            }
        }
    }

    private static ArrayList<Move> pinnedMoves(Pin pin, Board board) {
        ArrayList<Move> legalMoves = new ArrayList<>();

        char[][] boardRep = board.getRepresentation();

        Piece pinnedPiece = pin.getPinnedPiece();
        int pinnedPieceSquare = pinnedPiece.getSquareNum();
        int pinnedPieceI = pinnedPieceSquare / 8;
        int pinnedPieceJ = pinnedPieceSquare % 8;

        ArrayList<Integer> availableSquares = pin.getLegalSquares();

        char pinnedPieceRep = boardRep[pinnedPieceI][pinnedPieceJ];
        char lowerCasePinnedPiece = Character.toLowerCase(pinnedPieceRep);

        boolean pieceIsWhite = pinnedPiece.isWhite();

        // knight can never move when it is pinned
        if (lowerCasePinnedPiece == 'n') {
            return legalMoves;
        }

        //pawns can capture diagonally if pin is diagonal. pawns can be pushed if pin is straight. all other cases, pawns have no moves
        if (lowerCasePinnedPiece == 'p') {
            boolean pawnPromotionFlag = pieceIsWhite ? pinnedPieceI == 1 : pinnedPieceI == 6;

            if (pin.isDiagonal()) {
                int[][] diagOffsets = pieceIsWhite ? new int[][]{{-1, -1}, {-1, 1}} : new int[][]{{1, -1}, {1, 1}};
                for (int[] offset : diagOffsets) {
                    int diagI = pinnedPieceI + offset[0];
                    int diagJ = pinnedPieceJ + offset[1];
                    int diagNum = diagI * BOARD_LENGTH + diagJ;
                    char capturePiece = boardRep[diagI][diagJ];
                    if (availableSquares.contains(diagNum) &&
                            isInBounds(diagI, diagJ) &&
                            capturePiece != '-' &&
                            isBlackPiece(capturePiece) == pieceIsWhite) {
                        if (pawnPromotionFlag) { // pawn promotion case
                            legalMoves.addAll(pawnPromotionMoves(pinnedPiece, diagNum));
                        } else { // normal case
                            legalMoves.add(new Move(pinnedPiece, diagNum));
                        }

                    }
                }

                /* Enpassant may be valid for diagonal pins. These conditions ensure that the last move was a pawn push
                of 2 squares. this will trigger a scan for enpassant validity.
                */

                int numMovesPlayed = Driver.movesPlayed.size();
                Move lastMove = (numMovesPlayed != 0) ? Driver.movesPlayed.get(numMovesPlayed - 1) : board.getTargetEPMove();

                int targetI = pieceIsWhite ? 3 : 4;
                char targetPiece = pieceIsWhite ? 'p' : 'P';
                int targetPush = pieceIsWhite ? 16 : -16;


                if (pinnedPieceI == targetI
                        && lastMove.getPiece().getPieceRep() == targetPiece
                        && lastMove.getAfterSquare() - lastMove.getBeforeSquare() == targetPush) {
                    Move enpassant = checkForEnpassant(pinnedPieceJ, lastMove.getAfterSquare() % 8, pinnedPiece);
                    if (enpassant != null && availableSquares.contains(enpassant.getAfterSquare())) {
                        legalMoves.add(enpassant);
                    }
                }
            } else { //pin is straight == pushes only
                int numMoves;
                if (pieceIsWhite) { // starting position of pawns depends on color
                    numMoves = (pinnedPieceI == 6) ? 2 : 1; // can go up 2 squares if in starting position
                } else {
                    numMoves = (pinnedPieceI == 1) ? 2 : 1;
                }
                for (int k = 1; k <= numMoves; k++) {
                    int newI = pieceIsWhite ? pinnedPieceI - k : pinnedPieceI + k;
                    int newSquareNum = (newI) * BOARD_LENGTH + pinnedPieceJ;
                    if (!isInBounds(newI, pinnedPieceJ)) {
                        break;
                    }

                    // pawn is blocked or pinned
                    if (boardRep[newI][pinnedPieceJ] != '-' || !availableSquares.contains(newSquareNum)) {
                        break;
                    }
                    if (pawnPromotionFlag) { // pawn promotion case
                        legalMoves.addAll(pawnPromotionMoves(pinnedPiece, newSquareNum));
                    } else { // normal case
                        legalMoves.add(new Move(pinnedPiece, newSquareNum));
                    }
                }
            }
            return legalMoves;
        } // if: pawn case

        // only sliding moves (bishop queens and rooks) are left

        if (pin.isDiagonal()) {
            switch (lowerCasePinnedPiece) {
                case 'r': // rook can not move on a diag pin
                    return legalMoves;
                case 'b': // bishops and queens can move diagonally on a diag pin
                case 'q':
                    availableSquares.remove(Integer.valueOf(pinnedPieceSquare)); // can't stay in place
                    for (Integer sq : availableSquares) {
                        legalMoves.add(new Move(pinnedPiece, sq));
                    }
            }
        } else {
            switch (lowerCasePinnedPiece) {
                case 'b': // bishop can never move on a non diag pin
                    return legalMoves;
                case 'r':
                case 'q': // bishops and queens can move horizontally/vertically a non diag pin
                    availableSquares.remove(Integer.valueOf(pinnedPieceSquare));
                    for (Integer sq : availableSquares) {
                        legalMoves.add(new Move(pinnedPiece, sq));
                    }
            }
        }
        return legalMoves;
    }

    private static ArrayList<Move> getPawnMove(Board board, Piece pawn) {
        char[][] representation = board.getRepresentation();
        int squareNum = pawn.getSquareNum();
        boolean isWhite = pawn.isWhite();

        ArrayList<Move> pawnMoves = new ArrayList<>();

        int i = squareNum / BOARD_LENGTH; // this is the y position of the board. top = 0, bottom = 7
        int j = squareNum % BOARD_LENGTH;

        boolean pawnPromotionFlag = isWhite ? i == 1 : i == 6;

        //used for enpassant
        int numMovesPlayed = Driver.movesPlayed.size();
        Move lastMove = (numMovesPlayed != 0) ? Driver.movesPlayed.get(numMovesPlayed - 1) : board.getTargetEPMove();

        //white case
        if (isWhite) {
            // if pawn at start, it can go either 1 or 2 squares up. otherwise it can only go up 1
            int numMoves = (i == 6) ? 2 : 1;
            for (int k = 1; k <= numMoves; k++) {
                if (isInBounds(i - k, j) &&representation[i - k][j] == '-') {
                    int afterSq = (i - k) * BOARD_LENGTH + j;
                    if (pawnPromotionFlag) { // pawn promotion case
                        pawnMoves.addAll(pawnPromotionMoves(pawn, afterSq));
                    } else { // normal case
                        pawnMoves.add(new Move(pawn, afterSq));
                    }
                } else {
                    break;
                }
            }

            //diagonal captures
            int[][] diagonalOffset = {{-1, -1}, {-1, 1}}; // {{i1, j1}, {i2,j2} ...} if I change the current coordinate by these offsets, i will end up on diagonal square
            for (int[] diagonal : diagonalOffset) {
                int diagI = i + diagonal[0];
                int diagJ = j + diagonal[1];
                int diagNum = diagI * BOARD_LENGTH + diagJ;
                if (isInBounds(diagI, diagJ) && isBlackPiece(representation[diagI][diagJ])) {
                    if (pawnPromotionFlag) { // pawn promotion case
                        pawnMoves.addAll(pawnPromotionMoves(pawn, diagNum));
                    } else { // normal case
                        pawnMoves.add(new Move(pawn, diagNum));
                    }
                }
            }

            //enpassant

            /* condition ensures the last move was a pawn push of 2 squares. this will trigger a scan for enpassant
               validity. "16" represents a 2 square jump
             */
            if (i == 3
                    && lastMove.getPiece().getPieceRep() == 'p'
                    && lastMove.getAfterSquare() - lastMove.getBeforeSquare() == 16) {
                Move enpassant = checkForEnpassant(j, lastMove.getAfterSquare() % 8, pawn);
                if (enpassant != null) {
                    if (!inCheckAfterEnpassant(representation, enpassant, true)) {
                        pawnMoves.add(enpassant);
                    }
                }
            }

        }

        // black case
        else {
            // if pawn at start, it can go either 1 or 2 squares up. otherwise it can only go up 1
            int numMoves = (i == 1) ? 2 : 1;
            for (int k = 1; k <= numMoves; k++) {
                if (isInBounds(i + k, j) && representation[i + k][j] == '-') {
                    int afterNum = (i + k) * BOARD_LENGTH + j;
                    if (pawnPromotionFlag) { // pawn promotion case
                        pawnMoves.addAll(pawnPromotionMoves(pawn, afterNum));
                    } else { // normal case
                        pawnMoves.add(new Move(pawn, afterNum));
                    }
                } else {
                    break;
                }
            }

            //diagonal captures

            int[][] diagonalOffset = {{1, -1}, {1, 1}}; // {{i1, j1}, {i2,j2} ...} if I change the current coordinate by these offsets, i will end up on diagonal square
            for (int[] diagonal : diagonalOffset) {
                int diagI = i + diagonal[0];
                int diagJ = j + diagonal[1];
                int diagNum = diagI * BOARD_LENGTH + diagJ;
                if (isInBounds(diagI, diagJ) && isWhitePiece(representation[diagI][diagJ])) {
                    if (pawnPromotionFlag) { // pawn promotion case
                        pawnMoves.addAll(pawnPromotionMoves(pawn, diagNum));
                    } else { // normal case
                        pawnMoves.add(new Move(pawn, diagNum));
                    }
                }
            }

            //enpassant

            /* condition ensures the last move was a pawn push of 2 squares. this will trigger a scan for enpassant
               validity. "16" represents a 2 square jump
             */
            if (i == 4
                    && lastMove.getPiece().getPieceRep() == 'P'
                    && lastMove.getAfterSquare() - lastMove.getBeforeSquare() == -16) {
                Move enpassant = checkForEnpassant(j, lastMove.getAfterSquare() % 8, pawn);
                if (enpassant != null) {
                    if (!inCheckAfterEnpassant(representation, enpassant, false)) {
                        pawnMoves.add(enpassant);
                    }
                }
            }

            //pawn promotion
        }

        return pawnMoves;
    }

    private static Collection<? extends Move> pawnPromotionMoves(Piece pinnedPiece, int diagNum) {
        ArrayList<Move> promotions = new ArrayList<>();
        char[] promotionPieces = pinnedPiece.isWhite() ?
                new char[]{'Q', 'N', 'R', 'B'} :
                new char[]{'q', 'n', 'r', 'b'};

        for (char piece : promotionPieces) {
            Move promoMove = new Move(pinnedPiece, diagNum);
            promoMove.setPawnPromotionFlag(true);
            promoMove.setPromotionPiece(piece);
            promotions.add(promoMove);
        }

        return promotions;
    }

    private static boolean inCheckAfterEnpassant(char[][] representation, Move enpassant, boolean isWhite) {
        int beforeI = enpassant.getBeforeSquare() / BOARD_LENGTH;
        int beforeJ = enpassant.getBeforeSquare() % BOARD_LENGTH;

        int afterJ = enpassant.getAfterSquare() % BOARD_LENGTH;

        char capturer = isWhite ? 'P' : 'p';
        char captured = isWhite ? 'p' : 'P';

        //temporarily remove captured and capturing pawn.

        representation[beforeI][beforeJ] = '-';
        representation[beforeI][afterJ] = '-';

        /* there needs to be a king AND opposite colored rook/queen on that file. The path between those 2 pieces must
           be unobstructed. if path is available, we return true. All other cases = false.
         */

        boolean kingFound = false;
        boolean RQFound = false;

        char kingToBeFound = isWhite ? 'K' : 'k';
        char rookToBeFound = isWhite ? 'r' : 'R';
        char queenToBeFound = isWhite ? 'q' : 'Q';

        for (int j = 0; j < BOARD_LENGTH; j++) {
            char curPiece = representation[beforeI][j];

            if (curPiece == kingToBeFound) {
                kingFound = true;
            } else if (curPiece == rookToBeFound || curPiece == queenToBeFound) {
                RQFound = true;
            } else if (curPiece != '-') { // random piece found on file
                if (kingFound || RQFound) { // curPiece is obstructing king -> RQ path = no check
                    representation[beforeI][beforeJ] = capturer;
                    representation[beforeI][afterJ] = captured;
                    return false;
                }
            }

            if (kingFound && RQFound) {
                representation[beforeI][beforeJ] = capturer;
                representation[beforeI][afterJ] = captured;
                return true;
            }
        }

        //reach this far means king and RQ Piece were not on same file, so no check

        //resetting board
        representation[beforeI][beforeJ] = capturer;
        representation[beforeI][afterJ] = captured;
        return false;
    }

    /* This function simply checks if an opposing pawn is 1 square next to attacking pawn. This is done by comparing the
       corresponding j values in (i,j) for the attacking and opposing pawn. If true, corresponding enpassant move is
       returned as all other enpassant conditions have been checked.
     */

    private static Move checkForEnpassant(int attackingJ, int opposingJ, Piece capturingPawn) {
        boolean color = capturingPawn.isWhite();
        int squareNum = capturingPawn.getSquareNum();
        if (opposingJ - 1 == attackingJ) { // right enpassant from the perspective of white
            int afterSq = color ? squareNum - 7 : squareNum + 9;
            Move rightEnpassant = new Move(capturingPawn, afterSq);
            rightEnpassant.setEnpassantFlag(true);
            return rightEnpassant;
        }
        if (opposingJ + 1 == attackingJ) { // left enpassant from the perspective of white
            int afterSq = color ? squareNum - 9 : squareNum + 7;
            Move leftEnpassant = new Move(capturingPawn, afterSq);
            leftEnpassant.setEnpassantFlag(true);
            return leftEnpassant;
        }
        return null;
    }


    private static ArrayList<Move> getKnightMove(char[][] representation, Piece knight) {
        ArrayList<Move> knightMoves = new ArrayList<>();

        int squareNum = knight.getSquareNum();
        boolean isWhite = knight.isWhite();

        int i = squareNum / BOARD_LENGTH; // this is the y position of the board. top = 0, bottom = 7
        int j = squareNum % BOARD_LENGTH; // x position

        int[][] knightJumps = {{1, 2}, {-1, 2}, {1, -2}, {-1, -2}, {2, 1}, {-2, 1}, {2, -1}, {-2, -1}};

        for (int[] jump : knightJumps) {
            int jumpI = i + jump[0];
            int jumpJ = j + jump[1];
            int jumpSquareNum = jumpI * BOARD_LENGTH + jumpJ;

            if (isInBounds(jumpI, jumpJ) && (squareStatus(representation[jumpI][jumpJ], isWhite) != SAME_COLOR_CAPTURE)) {
                knightMoves.add(new Move(knight, jumpSquareNum));
            }
        }

        return knightMoves;
    }

    private static ArrayList<Move> getKingMove(Board board, Piece king) {
        ArrayList<Move> kingMoves = new ArrayList<>();

        char[][] representation = board.getRepresentation();
        int squareNum = king.getSquareNum();
        boolean isWhite = king.isWhite();


        int i = squareNum / BOARD_LENGTH; // this is the y position of the board. top = 0, bottom = 7
        int j = squareNum % BOARD_LENGTH; // x position

        int[][] offsets = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        String controlGrid = SquareControl.getControlGrid();

        for (int[] move : offsets) {
            int newI = i + move[0];
            int newJ = j + move[1];
            int newSquareNum = newI * BOARD_LENGTH + newJ;

            if (isInBounds(newI, newJ)
                    && (squareStatus(representation[newI][newJ], isWhite) != SAME_COLOR_CAPTURE)
                    && controlGrid.charAt(newSquareNum) == '0') {
                kingMoves.add(new Move(king, newSquareNum));
            }
        }

        kingMoves.addAll(castlingMoves(board, king, isWhite));

        return kingMoves;
    }

    /* This function will check the following castling conditions
     *   1) whether king is in check
     *   2) whether there are pieces in the way of the king in rook
     *   3) whether pass through squares are under attack
     *
     *   All other castling conditions are checked by the castling flags in Board class.
     */

    private static ArrayList<Move> castlingMoves(Board board, Piece king, boolean color) {
        ArrayList<Move> moves = new ArrayList<>();
        char[][] representation = board.getRepresentation();

        //condition 1
        if (SquareControl.getChecks().size() > 0) {
            return moves;
        }

        int kingSquareNum = king.getSquareNum();
        String controlGrid = SquareControl.getControlGrid();

        boolean[] castlingFlags = {board.whiteKSideCastle(), board.whiteQSideCastle(),
                                    board.blackKSideCastle(),board.blackQSideCastle()};

        int[][] passingSquares = {{kingSquareNum + 1, kingSquareNum + 2}, {kingSquareNum - 1, kingSquareNum - 2}};
        int[][] BNQSquares = {{kingSquareNum + 1, kingSquareNum + 2}, {kingSquareNum - 1, kingSquareNum - 2, kingSquareNum - 3}};

        int startInd = color ? 0 : 2;
        int endInd = color ? 2 : 4;

        // first pass of for loop = king side castle. second pass of for loop = queenside castle

        for (int k = startInd; k < endInd; k++) {
            if (castlingFlags[k]) { // rook and king haven't moved
                int[] setOfPassingSq = passingSquares[k % 2];
                int[] setOfBNQSq = BNQSquares[k % 2];
                boolean castlingAvailable = true;
                for (int squareNum : setOfBNQSq) {  // condition 2
                    int i = squareNum / BOARD_LENGTH;
                    int j = squareNum % BOARD_LENGTH;
                    if (representation[i][j] != '-') {
                        castlingAvailable = false;
                        break;
                    }
                }
                for (int squareNum : setOfPassingSq) { // condition 3
                    if (controlGrid.charAt(squareNum) == '1') {
                        castlingAvailable = false;
                        break;
                    }
                }
                if (castlingAvailable) {
                    Move castle = new Move(king, setOfPassingSq[1]);
                    castle.setCastlingFlag(true);
                    moves.add(castle);
                }
            }
        }

        return moves;
    }

    private static ArrayList<Move> generateSlidingMoves(char[][] representation, Piece piece) {
        ArrayList<Move> legalMoves = new ArrayList<>();

        int squareNum = piece.getSquareNum();
        boolean isWhite = piece.isWhite();
        char pieceRep = Character.toLowerCase(piece.getPieceRep()); // lower case -> less if statements

        int[][] offsets = {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 1}, {1, 0}, {0, -1}, {-1, 0}};

        int startInd = (pieceRep == 'r') ? 4 : 0;
        int endInd = (pieceRep == 'b') ? 4 : offsets.length;


        int i = squareNum / BOARD_LENGTH; // this is the y position of the board. top = 0, bottom = 7
        int j = squareNum % BOARD_LENGTH; // x position

        for (int index = startInd; index < endInd; index++) {
            int depth = 1; // represents how far a sliding piece can go in 1 direction
            while (true) { // must keep checking in offset direction
                int newI = i + offsets[index][0] * depth;
                int newJ = j + offsets[index][1] * depth++;

                int newSquareNum = newI * BOARD_LENGTH + newJ;

                if (isInBounds(newI, newJ)) {
                    int status = squareStatus(representation[newI][newJ], isWhite);
                    if (status == SAME_COLOR_CAPTURE) { // can not capture own color, so move on to next direction
                        break;
                    }
                    legalMoves.add(new Move(piece, newSquareNum));
                    if (status == LEGAL_CAPTURE) { // can capture opponents piece, but cant go any further in this direction
                        break;
                    } // if
                } // if
                else {
                    break;
                }
            } // while
        } // for

        return legalMoves;
    }

    public static boolean isInBounds(int i, int j) {
        return i >= 0 && i <= 7 && j >= 0 && j <= 7;
    }

    public static boolean isWhitePiece(char c) {
        return c >= 'A' && c <= 'Z';
    }

    public static boolean isBlackPiece(char c) {
        return c >= 'a' && c <= 'z';
    }

    /* isWhite is the boolean that represents the color of the piece that is going to the square on i,j
     */

    public static int squareStatus(char piece, boolean isWhite) {
        if (piece == '-') {
            return SQUARE_FREE;
        }
        if ((isWhite && isWhitePiece(piece)) || (!isWhite && isBlackPiece(piece))) {
            return SAME_COLOR_CAPTURE;
        }
        return LEGAL_CAPTURE;
    }

    /* This method takes in 1 algebraic square like e3 or f6 and converts it into a square num in the range of 0 - 63
     */

    public static int algebraicToSquareNum(String algSquare) {
        assert algSquare.length() == 2;

        char alpha = algSquare.charAt(0);
        int num = Character.getNumericValue(algSquare.charAt(1));

        int i = -num + 8;
        int j = switch (alpha) {
            case 'a' -> 0;
            case 'b' -> 1;
            case 'c' -> 2;
            case 'd' -> 3;
            case 'e' -> 4;
            case 'f' -> 5;
            case 'g' -> 6;
            case 'h' -> 7;
            default -> throw new IllegalStateException("Unexpected value: " + alpha);
        };

        return i*BOARD_LENGTH + j;
    }

    public static String squareNumToAlgebraic(int sqNum) {
        int i = sqNum / BOARD_LENGTH;
        int j = sqNum % BOARD_LENGTH;

        StringBuilder algebraic = new StringBuilder();

        char alpha = (char) (97 + j);
        algebraic.append(alpha);

        int numInt = -i + 8;
        algebraic.append(numInt);

        return algebraic.toString();
    }
}