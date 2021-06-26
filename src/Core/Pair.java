package Core;

public class Pair {
    private int i;
    private int j;

    public Pair(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public boolean equals(int i, int j) {
        return i == this.i &&
                j == this.j;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }
}
