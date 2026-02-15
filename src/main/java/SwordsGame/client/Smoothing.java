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
            case 0 -> !back && !left && (front || right);
            case 1 -> !front && !left && (back || right);
            case 2 -> !front && !right && (back || left);
            case 3 -> !back && !right && (front || left);
            default -> false;
        };
    }
}
