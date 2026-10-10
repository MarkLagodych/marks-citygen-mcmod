package org.markscitygen.lib;

public final class Vec2Math {
    private Vec2Math() {}

    public static double length(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    public static double lengthSquared(double x, double y) {
        return x * x + y * y;
    }

    public static double dot(double x1, double y1, double x2, double y2) {
        return x1 * x2 + y1 * y2;
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        var dx = x1 - x2;
        var dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double distanceSquared(double x1, double y1, double x2, double y2) {
        var dx = x1 - x2;
        var dy = y1 - y2;
        return dx * dx + dy * dy;
    }

    /// @return true if `x` is in [0, width) and `y` is in [0, height).
    public static boolean isInBounds(double x, double y, double width, double height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /// @return True if the angle between the two vectors is in [0, π/2].
    /// False otherwise, in this case negating one of the vectors will make the angle in [0, π/2].
    public static boolean isAligned(double x1, double y1, double x2, double y2) {
        return dot(x1, y1, x2, y2) >= 0;
    }
}
