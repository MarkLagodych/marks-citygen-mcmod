package org.markscitygen.lib;

/// Searches for nearby points in a constant radius in 2D space.
///
/// This is designed to be fast in trade for accuracy.
/// The whole map is divided into a grid of cells with 1/5 the radius size.
/// Each cell is a boolean indicating if there is a point in that cell.
public class NearbyPointSearchMap2 {
    private static final double RADIUS_TO_CELL_SIZE = 1.0 / 5;

    /// This many cells are added to each side of the bounding box to avoid edge cases
    /// where a point is near the edge of the bounding box.
    private static final int CELL_PADDING = 4;

    private final double coordToCellIndex;

    private final int rows;
    private final int cols;
    private final boolean[][] cells;

    public NearbyPointSearchMap2(Vec2 bounds, double radius) {
        var cellSize = radius * RADIUS_TO_CELL_SIZE;

        this.coordToCellIndex = 1.0 / cellSize;

        this.rows = (int) Math.ceil(bounds.y * coordToCellIndex) + 2 * CELL_PADDING;
        this.cols = (int) Math.ceil(bounds.x * coordToCellIndex) + 2 * CELL_PADDING;
        this.cells = new boolean[rows][cols];
    }

    public int getRow(double y) {
        return (int) Math.floor(y * coordToCellIndex) + CELL_PADDING;
    }

    public int getCol(double x) {
        return (int) Math.floor(x * coordToCellIndex) + CELL_PADDING;
    }

    public void add(Vec2 point) {
        cells[getRow(point.y)][getCol(point.x)] = true;
    }

    public void add(Path2 path) {
        for (int i = 0; i < path.size(); i++) {
            add(path.get(i));
        }
    }

    private boolean checkRow(int row, int colFirst, int colLast) {
        for (int col = colFirst; col <= colLast; col++) {
            if (cells[row][col]) {
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
