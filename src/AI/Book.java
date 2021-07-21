package AI;

import Core.*;

import java.io.*;
import java.util.ArrayList;

public class Book {
    public static boolean bookMovesLeft = true;
    private ArrayList<String[]> bookPGNS;

    public Book() throws IOException {
        initializePGNS();
    }

    private void initializePGNS() throws IOException {
        bookPGNS = new ArrayList<>();
        File file = new File("C:/Users/sehar/IdeaProjects/ChessPart2/src/AI/MasterGames.txt");
        BufferedReader bufferedReader = (new BufferedReader(new FileReader(file)));

        String line;

        while ((line = bufferedReader.readLine()) != null) {
            bookPGNS.add(line.split(" "));
        }
    }


    public Move findBookMove() {
        String[] currentPGN = Driver.mainBoard.getCurrentPGN().split(" ");

        ArrayList<Move> movesPlayed = Driver.movesPlayed;
        int indexOfNewMove = movesPlayed.size() - 1;

        String newMove = currentPGN[indexOfNewMove];
        newMove = newMove.replaceAll("\0","");

        int n = bookPGNS.size();

        ArrayList<String> potentialNextMoves = new ArrayList<>();
        ArrayList<Integer> tally = new ArrayList<>(); // parallel to above list. keeps track of how many times move has been played.

        //identify all games that do not include played move. Otherwise keep track of the most common move
        //going in backwards order so that gamesToRemove will be in backwards order
        for (int i = n - 1; i >= 0; i--) {
            String[] bookPGN = bookPGNS.get(i);
            String bookMove = bookPGN[indexOfNewMove];

            if (!bookMove.equals(newMove)) { // move not included. likely case. remove pgn
                bookPGNS.remove(i);
            } else { // move included. keep track of next move.
                String nextMove = bookPGN[indexOfNewMove + 1];
                boolean moveAlreadyRecordedFlag = false;
                int numPotentialMoves = potentialNextMoves.size();
                for (int j = 0; j < numPotentialMoves; j++) {
                    String recordedBookMove = potentialNextMoves.get(j);
                    if (recordedBookMove.equals(nextMove)) {
                        moveAlreadyRecordedFlag = true;
                        tally.set(j, tally.get(j) + 1);
                    }
                }
                if (!moveAlreadyRecordedFlag) {
                    potentialNextMoves.add(nextMove);
                    tally.add(1);
                }
            }
        }

        if (bookPGNS.size() < 1) { // book moves over. use minimax now
            bookMovesLeft = false;
            return null;
        }

        //find most common move
        int numPotentialMoves = potentialNextMoves.size();
        int maxIndex = 0;
        String moveToPlayString = potentialNextMoves.get(maxIndex);
        for (int i = 1; i < numPotentialMoves; i++) {
            if (tally.get(i) > tally.get(maxIndex)) {
                maxIndex = i;
                moveToPlayString = potentialNextMoves.get(i);
            }
        }

        //TMRW
        /*todo why (after Bb5) does the computer sac the queen

         */
        //TODO incentivize pieces being in certain locations


        //LATER WEEK
        //todo implement quiescence search
        //todo 50 move rule and repetition by zobrist hashing
        // transposition table to keep track of positions already eval

        n = bookPGNS.size();

        //remove all pgns that do not use computermove
        for (int i = n - 1; i >= 0; i--) {
            String[] PGN = bookPGNS.get(i);
            if (!PGN[indexOfNewMove + 1].equals(moveToPlayString)) {
                bookPGNS.remove(i);
            }
        }

        //moveToPlayString is currently a pgn. convert to actual move.

        return convertPGNToMove(moveToPlayString);

    }

    private Move convertPGNToMove(String moveToPlayString) {
        int indexOfAfterSq = 0;

        char pieceRepToMove = moveToPlayString.charAt(0);
        indexOfAfterSq++;
        int afterSq;

        //pawn move case
        if (pieceRepToMove >= 'a' && pieceRepToMove <= 'h') {
            pieceRepToMove = 'P';
            indexOfAfterSq--;
        }

        //castling case
        if (pieceRepToMove == 'O') {
            pieceRepToMove = 'K';

            Board board = Driver.mainBoard;
            //setting kings after square (depending on color and kind of castling)
            afterSq = (moveToPlayString.length() > 3) ? (board.isWhitesTurn() ? 58 : 2) : board.isWhitesTurn() ? 62 : 6;

            return searchForMove(afterSq, pieceRepToMove);
        }

        if (moveToPlayString.charAt(indexOfAfterSq) == 'x') {
            if (pieceRepToMove == 'P') {
                indexOfAfterSq += 2;
            } else {
                indexOfAfterSq++;
            }
        }

        afterSq = LegalMoves.algebraicToSquareNum(moveToPlayString.substring(indexOfAfterSq, indexOfAfterSq + 2));

        return searchForMove(afterSq, pieceRepToMove);
    }

    private Move searchForMove(int targetAfterSq, char targetPieceRep) {
        Board board = Driver.mainBoard;
        ArrayList<Move> moves = LegalMoves.getAllMoves(board);

        for (Move move : moves) {
            int afterSq = move.getAfterSquare();
            char pieceRep = Character.toUpperCase(move.getPiece().getPieceRep());

            if ((afterSq == targetAfterSq) && (pieceRep == targetPieceRep)) {
                return move;
            }
        }
        return null;
    }
}
