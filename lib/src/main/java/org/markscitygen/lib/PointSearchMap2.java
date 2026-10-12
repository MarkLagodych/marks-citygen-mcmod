package org.markscitygen.lib;

public class PointSearchMap2 {
    private static final double RADIUS_TO_WINDOW_SIZE = 1.0 / 5;

    /// This many windows are added to each side of the bounding box to avoid edge cases
    /// where a point is near the edge of the bounding box.
    private static final int WINDOW_PADDING = 4;

    private final double coordToWindowIndex;

    private final int rows;
    private final int cols;
    private final boolean[][] windows;

    public PointSearchMap2(Vec2 bounds, double radius) {
        var windowSize = radius * RADIUS_TO_WINDOW_SIZE;

        this.coordToWindowIndex = 1.0 / windowSize;

        this.rows = (int) Math.ceil(bounds.y * coordToWindowIndex) + 2 * WINDOW_PADDING;
        this.cols = (int) Math.ceil(bounds.x * coordToWindowIndex) + 2 * WINDOW_PADDING;
        this.windows = new boolean[rows][cols];
    }

    public int getRow(double y) {
        return (int) Math.floor(y * coordToWindowIndex) + WINDOW_PADDING;
    }

    public int getCol(double x) {
        return (int) Math.floor(x * coordToWindowIndex) + WINDOW_PADDING;
    }

    public void add(Vec2 point) {
        windows[getRow(point.y)][getCol(point.x)] = true;
    }

    private boolean checkRow(int row, int colFirst, int colLast) {
        for (int col = colFirst; col <= colLast; col++) {
            if (windows[row][col]) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRadius(int row, int col) {
        return checkRow(row - 4, col - 1, col + 1)
                || checkRow(row - 3, col - 2, col + 2)
                || checkRow(row - 2, col - 2, col + 2)
                || checkRow(row - 1, col - 3, col + 3)
                || checkRow(row, col - 3, col + 3)
                || checkRow(row + 1, col - 3, col + 3)
                || checkRow(row + 2, col - 2, col + 2)
                || checkRow(row + 3, col - 2, col + 2)
                || checkRow(row + 4, col - 1, col + 1);
    }

    public boolean anyPointsNear(Vec2 point) {
        return checkRadius(getRow(point.y), getCol(point.x));
    }
}
