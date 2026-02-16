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
        boolean[] lowered = new boolean[4];
        lowered[0] = !front && !right && (back || left);
        lowered[1] = !back && !right && (front || left);
        lowered[2] = !back && !left && (front || right);
        lowered[3] = !front && !left && (back || right);

        int loweredCount = 0;
        int loweredIndex = -1;
        for (int i = 0; i < lowered.length; i++) {
            if (lowered[i]) {
                loweredCount++;
                loweredIndex = i;
            }
        }

        if (loweredCount == 1) {
            return index == loweredIndex
                    || index == nextIndex(loweredIndex)
                    || index == prevIndex(loweredIndex);
        }

        return lowered[index];
    }

    private int nextIndex(int index) {
        return (index + 1) & 3;
    }

    private int prevIndex(int index) {
        return (index + 3) & 3;
    }
}
