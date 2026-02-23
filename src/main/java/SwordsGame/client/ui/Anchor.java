package SwordsGame.client.ui;

public class Anchor {
    public enum TypeX { LEFT, CENTER, RIGHT }
    public enum TypeY { TOP, CENTER, BOTTOM }

    public final TypeX tx;
    public final TypeY ty;
    public final float x, y;

    public Anchor(TypeX tx, TypeY ty, float x, float y) {
        this.tx = tx;
        this.ty = ty;
        this.x = x;
        this.y = y;
    }

    public static Anchor screenPoint(int screenW, int screenH, TypeX ax, TypeY ay, float offsetX, float offsetY) {
        float baseX = ax == TypeX.LEFT ? 0f : ax == TypeX.CENTER ? screenW / 2f : screenW;
        float baseY = ay == TypeY.TOP ? 0f : ay == TypeY.CENTER ? screenH / 2f : screenH;
        return new Anchor(ax, ay, (float) Math.floor(baseX + offsetX), (float) Math.floor(baseY + offsetY));
    }

    public static Anchor pivotPoint(Anchor pivot, TypeX ax, TypeY ay, float offsetX, float offsetY) {
        if (pivot == null) {
            return new Anchor(ax, ay, (float) Math.floor(offsetX), (float) Math.floor(offsetY));
        }
        return new Anchor(ax, ay, (float) Math.floor(pivot.x + offsetX), (float) Math.floor(pivot.y + offsetY));
    }

    public static final TypeX LEFT = TypeX.LEFT;
    public static final TypeX RIGHT = TypeX.RIGHT;
    public static final TypeX CENTER = TypeX.CENTER;

    public static final TypeY TOP = TypeY.TOP;
    public static final TypeY BOTTOM = TypeY.BOTTOM;
    public static final TypeY CENTER_Y = TypeY.CENTER;
}
