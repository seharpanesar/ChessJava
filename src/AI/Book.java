package AI;

import Core.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Book {
    public static boolean bookMovesLeft = true;
    private static ArrayList<String[]> bookPGNS;

    public Book() throws IOException {
        initializePGNS();
    }

    private void initializePGNS() throws IOException {
        bookPGNS = new ArrayList<>();
        BufferedReader bufferedReader = (new BufferedReader(new FileReader("MasterGames.txt")));

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

        ArrayList<Integer> gamesToRemove = new ArrayList<>();
        int n = bookPGNS.size();

        ArrayList<String> potentialNextMoves = new ArrayList<>();
        ArrayList<Integer> tally = new ArrayList<>(); // parallel to above list. keeps track of how many times move has been played.

        //identify all games that do not include played move. Otherwise keep track of the most common move
        for (int i = 0; i < n; i++) {
            String bookMove = bookPGNS.get(i)[indexOfNewMove];
            if (!bookMove.equals(newMove)) { // move not included. likely case
                gamesToRemove.add(i);
            } else { // move included. keep track of next move.
                boolean moveAlreadyRecordedFlag = false;
                int numPotentialMoves = potentialNextMoves.size();
                for (int j = 0; j < numPotentialMoves; j++) {
                    String recordedBookMove = potentialNextMoves.get(j);
                    if (recordedBookMove.equals(bookMove)) {
                        moveAlreadyRecordedFlag = true;
                        tally.set(j, tally.get(j) + 1);
                    }
                }
                if (!moveAlreadyRecordedFlag) {
                    potentialNextMoves.add(bookMove);
                    tally.add(1);
                }
            }
        }

        //remove all games that do not include played move
        for (Integer index : gamesToRemove) {
            bookPGNS.remove(index);
        }

        if (potentialNextMoves.size() < 1) { // book moves over. use minimax now
            //TODO:  flag
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

        //TODO convert moveToPlayString into actual move object;
        //TODO finish bookMoveOver flag and merge book with minimax

        return null;

    }
}
