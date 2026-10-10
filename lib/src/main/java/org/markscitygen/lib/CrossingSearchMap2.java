package org.markscitygen.lib;

import java.util.Arrays;
import org.markscitygen.lib.tensorfield.Path2;

public class CrossingSearchMap2 {
    /// The scale factor to get the window size from the crossing distance.
    static final double WINDOW_SCALE = 0.5;

    static final double WINDOW_EMPTY = Double.NEGATIVE_INFINITY;
    static final double WINDOW_FULL = Double.POSITIVE_INFINITY;
    static final double CROSSING_DETECTION_TOLERANCE = 0.2;

    final Vec2 mapOrigin;
    final double windowSize;
    final double[] windows;
    final int rows;
    final int cols;

    public CrossingSearchMap2(Vec2 minBounds, Vec2 maxBounds, double crossingDistance) {
        if (maxBounds.x <= minBounds.x || maxBounds.y <= minBounds.y) {
            throw new IllegalArgumentException("maxBounds must be > minBounds");
        }

        this.mapOrigin = minBounds;
        this.windowSize = crossingDistance * WINDOW_SCALE;

        var mapSize = mapOrigin.directionTo(maxBounds);
        mapSize.mul(1.0 / windowSize);
        this.rows = (int) Math.ceil(mapSize.x);
        this.cols = (int) Math.ceil(mapSize.y);
        this.windows = new double[rows * cols];
        Arrays.fill(windows, WINDOW_EMPTY);
    }

    private int getWindowRow(Vec2 point) {
        return (int) ((point.x - mapOrigin.x) / windowSize);
    }

    private int getWindowCol(Vec2 point) {
        return (int) ((point.y - mapOrigin.y) / windowSize);
    }

    private int getWindowIndex(int row, int col) {
        return row * cols + col;
    }

    private int getWindowIndex(Vec2 point) {
        return getWindowIndex(getWindowRow(point), getWindowCol(point));
    }

    /// @param point The last (current) point of the road segment
    /// @param direction The direction of the road segment at the given point
    public boolean canPlaceRoad(Vec2 point, Vec2 direction) {
        var row = getWindowRow(point);
        var col = getWindowCol(point);

        // -cos(b) ≈ sin(a) => a and b are perpendicular => correct crossing detected
        var compareSin = -direction.polarCos();

        return canPlaceRoadInWindow(row, col, compareSin)
                && canPlaceRoadInWindow(row - 1, col, compareSin)
                && canPlaceRoadInWindow(row + 1, col, compareSin)
                && canPlaceRoadInWindow(row, col - 1, compareSin)
                && canPlaceRoadInWindow(row, col + 1, compareSin)
                && canPlaceRoadInWindow(row - 1, col - 1, compareSin)
                && canPlaceRoadInWindow(row - 1, col + 1, compareSin)
                && canPlaceRoadInWindow(row + 1, col - 1, compareSin)
                && canPlaceRoadInWindow(row + 1, col + 1, compareSin);
    }

    private boolean canPlaceRoadInWindow(int row, int col, double compareSin) {
        // Dummy checks for out-of-bounds windows
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return true; // Skip the check
        }

        var index = getWindowIndex(row, col);

        if (windows[index] == WINDOW_EMPTY) {
            return true;
        }

        if (windows[index] == WINDOW_FULL) {
            return false;
        }

        if (Math.abs(windows[index] - compareSin) < CROSSING_DETECTION_TOLERANCE) {
            return true;
        } else {
            return false;
        }
    }

    public void markRoad(Vec2 point, Vec2 direction) {
        var index = getWindowIndex(point);

        if (windows[index] == WINDOW_FULL) {
            return;
        }

        windows[index] = Math.abs(direction.polarSin());
    }

    public void markRoad(Path2 path) {
        if (path.size() < 2) {
            return;
        }

        var lastPoint = path.get(0);
        for (int i = 1; i < path.size() - 1; i++) {
            var direction = lastPoint.directionTo(path.get(i + 1));
            markRoad(lastPoint, direction);
            lastPoint = path.get(i + 1);
        }
    }
}
