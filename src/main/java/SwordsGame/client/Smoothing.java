package SwordsGame.client;

public class Smoothing {
    private final boolean front;
    private final boolean back;
    private final boolean right;
    private final boolean left;

    public Smoothing(boolean[] faces) {
        this.front = faces[0];
        this.back = faces[1];
        this.right = faces[4];
        this.left = faces[5];
    }

    public boolean shouldLowerTopVertex(int index) {
        switch (index) {
            case 0:
                return back || left;
            case 1:
                return front || left;
            case 2:
                return front || right;
            case 3:
                return back || right;
            default:
                return false;
        }
    }
}
