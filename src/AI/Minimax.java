package AI;

import Core.*;

import java.util.ArrayList;

import static AI.Stats.*;

public class Minimax {
    public static boolean mateDetected = false;
    public static int bestEval;
    public static Board mainBoard = Driver.mainBoard;

    public static Move execute() {
        boardsEval = 0;
        prunes = 0;

        final long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        ArrayList<Move> moves = LegalMoves.getAllMoves(mainBoard);
        for (final Move move : moves) {
            if (move.toString().equals("b2a1")) {
                System.out.println();
            }
            mainBoard.makeMove(move);
            currentValue = mainBoard.isWhitesTurn() ?
                    max(maxDepth - 1, highestSeenValue, lowestSeenValue) :
                    min(maxDepth - 1, highestSeenValue, lowestSeenValue);

            mainBoard.undoMove(move);

            if (mainBoard.isWhitesTurn() &&
                    currentValue >= highestSeenValue) {
                highestSeenValue = currentValue;
                bestMove = move;
            } else if (!mainBoard.isWhitesTurn() &&
                    currentValue <= lowestSeenValue) {
                lowestSeenValue = currentValue;
                bestMove = move;
            }
        }

        bestEval = mainBoard.isWhitesTurn() ? highestSeenValue : lowestSeenValue;

        long executionTime = System.currentTimeMillis() - startTime;
        System.out.printf("Execution time: %d ms\n", executionTime);
        System.out.println("Boards evaluated: " + boardsEval);
        System.out.println("Prunes: " +  prunes);

        return bestMove;
    }

    /*
       min() corresponds to black since black is the minimizing player.
     */

    private static int min(final int depth, int alpha, int beta) {
        if (depth == 0) {
            boardsEval++;
            return evaluate();
        }

        int minEval = Integer.MAX_VALUE;
        ArrayList<Move> moves = LegalMoves.getAllMoves(mainBoard);

        if (moves.size() == 0) { // mate = return mate score
            boardsEval++;
            if (SquareControl.isCheck()) { // black is in checkmate = good for white -> return large num
                return checkmateVal + depth; // larger depth = mate is quicker since depth is decreasing every iteraiton
            } else { // stalemate = draw
                return 0;
            }
        }

        for (final Move move : moves) {
            mainBoard.makeMove(move);
            int eval = max(depth - 1, alpha, beta);
            minEval = Math.min(minEval, eval);
            beta = Math.min(beta, eval);
            mainBoard.undoMove(move);

            /*if (beta <= alpha) {
                break;
            }

             */
        }
        return minEval;
    }

    /*
       max() corresponds to black since black is the minimizing player.
     */

    private static int max(final int depth, int alpha, int beta) {
        if(depth == 0) {
            boardsEval++;
            return evaluate();
        }

        int maxEval = Integer.MIN_VALUE;
        ArrayList<Move> moves = LegalMoves.getAllMoves(mainBoard);

        if (moves.size() == 0) { // mate = return mate score
            boardsEval++;
            if (SquareControl.isCheck()) { // white is in checkmate = good for black -> return large negative num
                return -(checkmateVal + depth);
            } else { // stalemate = draw
                return 0;
            }
        }

        for (final Move move : moves) {
            mainBoard.makeMove(move);
            int eval = min(depth - 1, alpha, beta);
            maxEval = Math.max(maxEval, eval);
            alpha = Math.max(alpha, eval);
            mainBoard.undoMove(move);

            /*if (beta <= alpha) {
                break;
            }

             */
        }
        return maxEval;
    }

    private static int evaluate() {
        ArrayList<Piece> whitePieces = mainBoard.getWhitePieces();
        ArrayList<Piece> blackPieces = mainBoard.getBlackPieces();

        int whiteCount = countMaterial(whitePieces);
        int blackCount = countMaterial(blackPieces);

        int eval = whiteCount - blackCount;

        boolean isEndGame = whitePieces.size() + blackPieces.size() < 12;

        if (isEndGame) {
            int whiteKSq = findKingSq(whitePieces);
            int blackKSq = findKingSq(blackPieces);

            boolean isWhitesTurn = mainBoard.isWhitesTurn();



            /*int friendlyKingSq = isWhitesTurn ? whiteKSq : blackKSq;
            int opponentKingSq = isWhitesTurn ? blackKSq : whiteKSq;

            eval += kingToEdgeEval(board, friendlyKingSq, opponentKingSq);

             */
        }

        return eval;
    }

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
}
