package AI;

import Core.*;

import java.util.ArrayList;

public class Minimax {
    final static int pawnVal = 100;
    final static int knightVal = 300;
    final static int bishopVal = 300;
    final static int rookVal = 500;
    final static int queenVal = 900;

    public static boolean mateDetected = false;
    public static int mateScore = Integer.MAX_VALUE;

    public static int BOARDS_EVALUATED = 0;

    public static int evaluate(Board board) {
        ArrayList<Piece> whitePieces = board.getWhitePieces();
        ArrayList<Piece> blackPieces = board.getBlackPieces();

        int whiteCount = countMaterial(whitePieces);
        int blackCount = countMaterial(blackPieces);

        int eval = whiteCount - blackCount;

        boolean isEndGame = whitePieces.size() + blackPieces.size() < 12;

        if (isEndGame) {
            int whiteKSq = findKingSq(whitePieces);
            int blackKSq = findKingSq(blackPieces);

            boolean isWhitesTurn = board.isWhitesTurn();

            int friendlyKingSq = isWhitesTurn ? whiteKSq : blackKSq;
            int opponentKingSq = isWhitesTurn ? blackKSq : whiteKSq;

            eval += kingToEdgeEval(board, friendlyKingSq, opponentKingSq);
        }

        return eval;
    }

    //TODO START HERE: incentivize OP king being in corner and incentivize moving friendly king towards op king

    /*
        In the endgame, checkmate needs to be delivered. Some times the engine doesn't have enough depth to
        see a checkmate, so it shuffles pieces around hopelessly. This function will guide the engine to put enemy king
        in corner and bring its own king towards the enemy king to hopefully be able to find checkmate.
     */

    private static int kingToEdgeEval(Board board, int friendlyKingSq, int opponentKingSq) {
        int incentive = 0;

        double centerI = 3.5;
        double centerJ = 3.5;

        int friendlyKingI = friendlyKingSq / 8;
        int friendlyKingJ = friendlyKingSq % 8;

        int opponentKingI = opponentKingSq / 8;
        int opponentKingJ = opponentKingSq % 8;

        //in endgame, the farther the enemy king from center -> higher chance of finding a mate
        incentive += (Math.abs(centerI - opponentKingI) + Math.abs(centerJ - opponentKingJ));

        //the closer the friendly king is to the opp king -> higher chance of finding a checkmate
        int distanceBetweenKings = Math.abs(opponentKingI - friendlyKingI) + Math.abs(opponentKingJ - friendlyKingJ);
        incentive += (10 - distanceBetweenKings);

        if (!board.isWhitesTurn()) {
            incentive *= -1;
        }

        return incentive;
    }

    private static int findKingSq(ArrayList<Piece> whitePieces) {
        for (Piece piece : whitePieces) {
            if (Character.toLowerCase(piece.getPieceRep()) == 'k') {
                return piece.getSquareNum();
            }
        }
        return 0;
    }

    public static int countMaterial(ArrayList<Piece> pieces) {
        int value = 0;
        for (Piece piece : pieces) {
            value += getValue(piece.getPieceRep());
        }
        return value;
    }

    public static int getValue(char pieceRep) {
        return switch (Character.toLowerCase(pieceRep)) {
            case 'p' -> pawnVal;
            case 'n' -> knightVal;
            case 'b' -> bishopVal;
            case 'r' -> rookVal;
            case 'q' -> queenVal;
            case 'k' -> 0;
            default -> throw new IllegalStateException("Unexpected value: " + Character.toLowerCase(pieceRep));
        };
    }

    public static int search(int depth, int plyFromRoot, Board board, int alpha, int beta) {
        ArrayList<Move> availMoves = LegalMoves.getAllMoves(board);
        MoveOrdering.orderMoves(availMoves);

        if (availMoves.size() == 0) { // mate = return static evaluation
            if (SquareControl.getChecks().size() > 0) { // checkmate
                mateDetected = true;
                mateScore = Math.min((plyFromRoot - 1) / 2 , mateScore);

                BOARDS_EVALUATED++;

                boolean whitesTurn = board.isWhitesTurn();
                int eval = whitesTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                eval += whitesTurn ? plyFromRoot : -plyFromRoot;

                return eval;
            } else { // stalemate = draw
                BOARDS_EVALUATED++;
                return 0;
            }
        }

        if (depth == 0) { // depth reached = return static evaluation
            BOARDS_EVALUATED++;
            return 0;
        }

        //minimax implementation below

        if (board.isWhitesTurn()) { // white is maximizing player
            int value = Integer.MIN_VALUE;
            for (Move move : availMoves) {
                board.makeMove(move);
                int currPosEval = search(depth - 1, plyFromRoot + 1, board, alpha, beta);
                board.undoMove(move);

                value = Math.max(value, currPosEval);

                if (value >= beta) {
                    break;
                }

                alpha = Math.max(alpha, value);
            }
            return value;
        }
        else { // black is minimizing player
            int value = Integer.MAX_VALUE;
            for (Move move : availMoves) {
                board.makeMove(move);
                int currPosEval = search(depth - 1, plyFromRoot + 1, board, alpha, beta);
                board.undoMove(move);

                value = Math.min(value, currPosEval);

                if (value <= alpha) {
                    break;
                }

                beta = Math.min(beta, value);
            }
            return value;
        }
    }

    /*
      This search only searched captures until a quiet position has been reached (no captures). This helps the engine
      get a better sense of how good a sequence of moves. This is called when the normal search has reached a depth
      of 0
     */

    private static int searchAllCaptures(Board board, int alpha, int beta, int depth) {
        ArrayList<Move> availMoves = LegalMoves.getAllMoves(board);
        MoveOrdering.orderMoves(availMoves);

        if (depth == 0) { // quiet pos reached, return static eval
            return evaluate(board);
        }

        //minimax implementation below

        if (board.isWhitesTurn()) { // white is maximizing player
            int value = Integer.MIN_VALUE;
            for (Move move : availMoves) {
                if (!move.captureFlag()) {
                    continue;
                }
                board.makeMove(move);
                int currPosEval = searchAllCaptures(board, alpha, beta, depth - 1);
                board.undoMove(move);

                value = Math.max(value, currPosEval);

                if (value >= beta) {
                    break;
                }

                alpha = Math.max(alpha, value);
            }
            return value;
        }
        else { // black is minimizing player
            int value = Integer.MAX_VALUE;
            for (Move move : availMoves) {
                if (!move.captureFlag()) {
                    continue;
                }
                board.makeMove(move);
                int currPosEval = searchAllCaptures( board, alpha, beta, depth - 1);
                board.undoMove(move);

                value = Math.min(value, currPosEval);

                if (value <= alpha) {
                    break;
                }

                beta = Math.min(beta, value);
            }
            return value;
        }
    }


}
