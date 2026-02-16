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
        boolean corner0 = !front && !right && (back || left);
        boolean corner1 = !back && !right && (front || left);
        boolean corner2 = !back && !left && (front || right);
        boolean corner3 = !front && !left && (back || right);

        if (corner0) {
            return index == 0 || index == 1 || index == 3;
        }
        if (corner1) {
            return index == 0 || index == 1 || index == 2;
        }
        if (corner2) {
            return index == 1 || index == 2 || index == 3;
        }
        if (corner3) {
            return index == 0 || index == 2 || index == 3;
        }

        return false;
    }
}
