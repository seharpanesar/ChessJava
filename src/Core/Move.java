package Core;

public class Move implements Comparable<Move> {
    private final int beforeSquare;
    private final int afterSquare;
    private final Piece piece;

    private boolean castlingFlag;
    private boolean enpassantFlag;
    private boolean pawnPromotionFlag;
    private boolean captureFlag;

    private Piece capturedPiece;
    private char promotionPiece;

    private int moveScore = 0; // helps optimize alphabeta pruning. check MoveOrdering class

    public Move(Piece piece, int afterSquare) {
        this.beforeSquare = piece.getSquareNum();
        this.afterSquare = afterSquare;
        this.piece = piece;
    }

    public int getBeforeSquare() {
        return beforeSquare;
    }

    public int getAfterSquare() {
        return afterSquare;
    }

    public Piece getPiece() {
        return piece;
    }

    public boolean castlingFlag() {
        return castlingFlag;
    }

    public void setCastlingFlag(boolean castlingFlag) {
        this.castlingFlag = castlingFlag;
    }

    public boolean enpassantFlag() {
        return enpassantFlag;
    }

    public void setEnpassantFlag(boolean enpassantFlag) {
        this.enpassantFlag = enpassantFlag;
    }

    public boolean pawnPromotionFlag() {
        return pawnPromotionFlag;
    }

    public void setPawnPromotionFlag(boolean pawnPromotionFlag) {
        this.pawnPromotionFlag = pawnPromotionFlag;
    }

    public char getPromotionPiece() {
        return promotionPiece;
    }

    public void setPromotionPiece(char promotionPiece) {
        this.promotionPiece = promotionPiece;
    }

    @Override
    public String toString() {
        String beforeAlg = LegalMoves.squareNumToAlgebraic(beforeSquare);
        String afterAlg = LegalMoves.squareNumToAlgebraic(afterSquare);

        return beforeAlg + afterAlg;
    }

    public boolean captureFlag() {
        return captureFlag;
    }

    public void setCaptureFlag(boolean captureFlag) {
        this.captureFlag = captureFlag;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(Piece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }

    @Override
    public int compareTo(Move move) {
        return move.moveScore - moveScore;
    }

    public void setMoveScore(int moveScore) {
        this.moveScore = moveScore;
    }
}
