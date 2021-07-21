package AI;

import Core.*;

import java.util.ArrayList;

import static AI.Stats.*;
import static Core.Driver.*;

public class AlphaBetaPruning {
    static ArrayList<String> movesPlayedMinimax = new ArrayList<>(); // used to debug.
    public static int bestEval;

    public static Move execute() {
        initStats();

        final long startTime = System.currentTimeMillis();

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        System.out.println("THINKING with depth = " + Stats.maxDepth);

        //get moves and order them
        ArrayList<Move> moves = LegalMoves.getAllMoves(mainBoard);
        MoveOrdering.orderMoves(moves);

        Move bestMove = moves.get(0);

        for (final Move move : moves) {
            boolean isWhitesTurn = mainBoard.isWhitesTurn();

            mainBoard.makeMove(move);
            movesPlayedMinimax.add(move.toString()); //TODO: delete later
            currentValue = isWhitesTurn ?
                    min(Stats.maxDepth - 1, highestSeenValue, lowestSeenValue) :
                    max(Stats.maxDepth - 1, highestSeenValue, lowestSeenValue);
            mainBoard.undoMove(move);
            movesPlayedMinimax.remove(move.toString()); //TODO: delete later

            if (mainBoard.getRepresentation()[2][2] == 'b') {
                System.out.println();
            }

            if (isWhitesTurn && currentValue > highestSeenValue) {
                highestSeenValue = currentValue;
                bestMove = move;
            }
            else if (!isWhitesTurn && currentValue < lowestSeenValue) {
                lowestSeenValue = currentValue;
                bestMove = move;

            }
        }
        bestEval = mainBoard.isWhitesTurn() ? highestSeenValue : lowestSeenValue;

        long executionTime = System.currentTimeMillis() - startTime;
        System.out.println("Execution time: " + executionTime + " ms");
        System.out.println("Boards eval: " + boardsEval);
        System.out.println("Prunes: " + prunes);
        System.out.println("numMoves : "+ movesPlayed.size());

        return bestMove;
    }

    private static void initStats() {
        prunes = 0;
        boardsEval = 0;
    }

    public static int max(final int depth, final int highest, final int lowest) {
        if (depth == 0) {
            Stats.boardsEval++;
            return evaluate();
        }
        int currentHighest = highest;

        //get moves
        ArrayList<Move> moves = LegalMoves.getAllMoves(mainBoard);

        //check for checkmate
        if (moves.size() == 0) { // mate = return mate score
            boardsEval++;
            if (SquareControl.isCheck()) { // white is in checkmate = good for black -> return large negative num
                return -(checkmateVal + depth);
            } else { // stalemate = draw
                return 0;
            }
        }


        MoveOrdering.orderMoves(moves);

        for (final Move move : moves) {

            mainBoard.makeMove(move);
            movesPlayedMinimax.add(move.toString()); //TODO: delete later

            if (movesPlayedMinimax.get(0).equals("a5c3")
                    && movesPlayedMinimax.size() == 2) {
                System.out.println();
            }

            currentHighest = Math.max(currentHighest, min(depth - 1, currentHighest, lowest));

            movesPlayedMinimax.remove(move.toString()); //TODO: delete later
            mainBoard.undoMove(move);

            //prune if further minimax analysis will not affect outcome.
            if (lowest <= currentHighest) {
                Stats.prunes++;
                break;
            }



        }
        return currentHighest;
    }

    public static int min(final int depth, final int highest, final int lowest) {
        if (depth == 0) {
            boardsEval++;
            return evaluate();
        }
        int currentLowest = lowest;

        //get moves and order them
        ArrayList<Move> moves = LegalMoves.getAllMoves(mainBoard);

        //check for mates
        if (moves.size() == 0) { // mate = return mate score
            boardsEval++;
            if (SquareControl.isCheck()) { // black is in checkmate = good for white -> return large num
                return checkmateVal + depth; // larger depth = mate is quicker since depth is decreasing every iteraiton
            } else { // stalemate = draw
                return 0;
            }
        }

        //order moves
        MoveOrdering.orderMoves(moves);

        for (final Move move : moves) {

            mainBoard.makeMove(move);
            movesPlayedMinimax.add(move.toString()); //TODO: delete later
            currentLowest = Math.min(currentLowest, max(depth - 1, highest, currentLowest));
            movesPlayedMinimax.remove(move.toString()); //TODO: delete later
            mainBoard.undoMove(move);

            //prune if further minimax analysis will not affect outcome.

            if (currentLowest <= highest) {
                prunes++;
                break;
            }


        }

        if (currentLowest == Integer.MAX_VALUE) {
            System.out.println();
        }

        return currentLowest;
    }

    private static int evaluate() {
        ArrayList<Piece> whitePieces = mainBoard.getWhitePieces();
        ArrayList<Piece> blackPieces = mainBoard.getBlackPieces();

        int whiteCount = countMaterialAndPosition(whitePieces);
        int blackCount = countMaterialAndPosition(blackPieces);

        int eval = whiteCount - blackCount;

        boolean isEndGame = whitePieces.size() + blackPieces.size() < 12;

        if (isEndGame) {
            int whiteKSq = findKingSq(whitePieces);
            int blackKSq = findKingSq(blackPieces);

            boolean isWhitesTurn = mainBoard.isWhitesTurn();

            int friendlyKingSq = isWhitesTurn ? whiteKSq : blackKSq;
            int opponentKingSq = isWhitesTurn ? blackKSq : whiteKSq;

            eval += kingToEdgeEval(friendlyKingSq, opponentKingSq);


        }

        return eval;
    }

    /*
        In the endgame, checkmate needs to be delivered. Some times the engine doesn't have enough depth to
        see a checkmate, so it shuffles pieces around hopelessly. This function will guide the engine to put enemy king
        in corner and bring its own king towards the enemy king to hopefully be able to find checkmate.
     */

    private static int kingToEdgeEval(int friendlyKingSq, int opponentKingSq) {
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

        if (!mainBoard.isWhitesTurn()) {
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

    public static int countMaterialAndPosition(ArrayList<Piece> pieces) {
        int value = 0;
        for (Piece piece : pieces) {
            value += getValue(piece);
        }
        return value;
    }

    public static int getValue(Piece piece) {
        char rep = Character.toLowerCase(piece.getPieceRep());

        int sqNum = piece.getSquareNum();
        int i = piece.isWhite() ? sqNum/8 : (7 - (sqNum/8));
        int j = piece.isWhite() ? sqNum%8 : (7 - (sqNum%8));

        int points = 0;
        switch (rep) {
            case 'p':
                points += pawnVal;
                points += pawnPositionMap[i][j];
                break;
            case 'n':
                points += knightVal;
                points += knightPositionMap[i][j];
                break;
            case 'b':
                points += bishopVal;
                points += bishopPositionMap[i][j];
                break;
            case 'r':
                points += rookVal;
                points += rookPositionMap[i][j];
                break;
            case 'q':
                points += queenVal;
                points += queenPositionMap[i][j];
                break;
        };

        return points;
    }
}
