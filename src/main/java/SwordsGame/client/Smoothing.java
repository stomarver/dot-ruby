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
        return switch (index) {
            case 0 -> !front && !left && (back || right);
            case 1 -> !back && !left && (front || right);
            case 2 -> !back && !right && (front || left);
            case 3 -> !front && !right && (back || left);
            default -> false;
        };
    }
}
