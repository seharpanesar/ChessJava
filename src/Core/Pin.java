package Core;

import java.util.ArrayList;

public class Pin {
    private final ArrayList<Integer> legalSquares; // this is the ray of squares from pinner (inclusive) to opp king (exclusive)
    private final Piece pinnedPiece;
    private final boolean isDiagonal;

    public Pin(ArrayList<Integer> legalSquares, Piece pinnedPiece) {
        this.legalSquares = legalSquares;
        this.pinnedPiece = pinnedPiece;
        int diagChecker = Math.abs(legalSquares.get(0) - legalSquares.get(1));
        isDiagonal = (diagChecker != 1 && diagChecker != 8);
    }

    public ArrayList<Integer> getLegalSquares() {
        return legalSquares;
    }

    public Piece getPinnedPiece() {
        return pinnedPiece;
    }

    public boolean isDiagonal() {
        return isDiagonal;
    }
}
