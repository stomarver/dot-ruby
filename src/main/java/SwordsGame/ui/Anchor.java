package SwordsGame.ui;

public class Anchor { // Просто public class, без static
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

    // Константы для удобного доступа
    public static final TypeX LEFT = TypeX.LEFT;
    public static final TypeX RIGHT = TypeX.RIGHT;
    public static final TypeX CENTER = TypeX.CENTER;

    public static final TypeY TOP = TypeY.TOP;
    public static final TypeY BOTTOM = TypeY.BOTTOM;
    public static final TypeY CENTER_Y = TypeY.CENTER;
}