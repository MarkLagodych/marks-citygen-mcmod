package org.markscitygen.lib;

import org.markscitygen.lib.core.RasterPath2;

final class RoadIntersectionFinder {
    /// This many cells are added to each side of the bounding box to avoid edge cases
    /// where a point is near the edge of the bounding box.
    private static final int CELL_PADDING = 1;

    private final int rows;
    private final int cols;

    private final int[] cells;

    public static record Intersection(
            int pointX, int pointY, int pointIndex, int intersectedPathId) {}

    public RoadIntersectionFinder(int width, int height) {
        this.rows = height + 2 * CELL_PADDING;
        this.cols = width + 2 * CELL_PADDING;
        this.cells = new int[rows * cols];
    }

    private int cellIndex(int x, int y) {
        return (y + CELL_PADDING) * cols + (x + CELL_PADDING);
    }

    private int getCell(int x, int y) {
        return cells[cellIndex(x, y)];
    }

    private int setCell(int x, int y, int value) {
        return cells[cellIndex(x, y)] = value;
    }

    public void add(RasterPath2 path, int pathId) {
        var cellValue = pathId + 1;

        for (int i = 0; i < path.size(); i++) {
            var x = path.getX(i);
            var y = path.getY(i);

            setCell(x, y, cellValue);
        }
    }

    /// @return null if no intersection found
    private Intersection getIntersectionNear(int pointX, int pointY, int pointIndex, int pathId) {

        for (int x = pointX - 1; x <= pointX + 1; x++) {
            for (int y = pointY - 1; y <= pointY + 1; y++) {
                var cellValue = getCell(x, y);

                if (cellValue == pathId + 1) {
                    continue;
                }

                if (cellValue != 0) {
                    return new Intersection(x, y, pointIndex, cellValue - 1);
                }
            }
        }

        return null;
    }

    /// @return null if no intersection found
    public Intersection findFirstIntersection(RasterPath2 path, int startIndex, int pathId) {
        for (int i = startIndex; i < path.size(); i++) {
            var x = path.getX(i);
            var y = path.getY(i);

            var intersection = getIntersectionNear(x, y, i, pathId);

            if (intersection != null) {
                return intersection;
            }
        }

        return null;
    }

    /// @return null if no intersection found
    public Intersection findLastIntersection(RasterPath2 path, int pathId) {
        for (int i = path.size() - 1; i >= 0; i--) {
            var x = path.getX(i);
            var y = path.getY(i);

            var intersection = getIntersectionNear(x, y, i, pathId);

            if (intersection != null) {
                return intersection;
            }
        }

        return null;
    }
}
