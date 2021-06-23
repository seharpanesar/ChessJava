package Core;

public class Piece {
    private char pieceRep;
    private int squareNum;
    private final boolean isWhite;

    public Piece(char pieceRep, int squareNum) {
        this.pieceRep = pieceRep;
        this.squareNum = squareNum;
        isWhite = (pieceRep >= 'A' && pieceRep <= 'Z');
    }

    // this constructor is used for the removal of captured pieces. Only the squareNum is significant bc that alone can determine which piece to remove.
    public Piece(int squareNum) {
        this.squareNum = squareNum;
        this.pieceRep = '?';
        isWhite = true;
    }

    // only the square num and color have to be equal for a piece to be equal. This simplifies the removal of captured piece

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return squareNum == piece.squareNum;
    }

    public char getPieceRep() {
        return pieceRep;
    }

    public void setPieceRep(char pieceRep) {
        this.pieceRep = pieceRep;
    }

    public int getSquareNum() {
        return squareNum;
    }

    public void setSquareNum(int squareNum) {
        this.squareNum = squareNum;
    }

    public boolean isWhite() {
        return isWhite;
    }
}
