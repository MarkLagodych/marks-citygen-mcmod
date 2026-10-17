package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.Path2;
import org.markscitygen.lib.Vec2;

/// Searches for nearby points in a constant radius in 2D space.
///
/// This is designed to be fast in trade for accuracy.
/// The whole map is divided into a grid of cells with 1/5 the radius size.
/// Each cell is a boolean indicating if there is a point in that cell.
public class RoadDensityChecker {
    private static final double RADIUS_TO_CELL_SIZE = 1.0 / 5;

    /// This many cells are added to each side of the bounding box to avoid edge cases
    /// where a point is near the edge of the bounding box.
    private static final int CELL_PADDING = 4;

    private final double coordToCellIndex;

    private final int rows;
    private final int cols;
    private final boolean[][] cells;

    public RoadDensityChecker(Vec2 bounds, double radius) {
        var cellSize = radius * RADIUS_TO_CELL_SIZE;

        this.coordToCellIndex = 1.0 / cellSize;

        this.rows = (int) Math.ceil(bounds.y * coordToCellIndex) + 2 * CELL_PADDING;
        this.cols = (int) Math.ceil(bounds.x * coordToCellIndex) + 2 * CELL_PADDING;
        this.cells = new boolean[rows][cols];
    }

    private int getRow(double y) {
        return (int) Math.floor(y * coordToCellIndex) + CELL_PADDING;
    }

    private int getCol(double x) {
        return (int) Math.floor(x * coordToCellIndex) + CELL_PADDING;
    }

    public void add(Path2 road) {
        for (int i = 0; i < road.size(); i++) {
            var x = road.getX(i);
            var y = road.getY(i);

            var row = getRow(y);
            var col = getCol(x);

            cells[row][col] = true;
        }
    }

    private boolean checkTooDenseInRow(int row, int colFirst, int colLast) {
        for (int col = colFirst; col <= colLast; col++) {
            if (cells[row][col]) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTooDenseInRadius(int row, int col) {
        return checkTooDenseInRow(row - 4, col - 1, col + 1)
                || checkTooDenseInRow(row - 3, col - 2, col + 2)
                || checkTooDenseInRow(row - 2, col - 2, col + 2)
                || checkTooDenseInRow(row - 1, col - 3, col + 3)
                || checkTooDenseInRow(row, col - 3, col + 3)
                || checkTooDenseInRow(row + 1, col - 3, col + 3)
                || checkTooDenseInRow(row + 2, col - 2, col + 2)
                || checkTooDenseInRow(row + 3, col - 2, col + 2)
                || checkTooDenseInRow(row + 4, col - 1, col + 1);
    }

    /// @return True if the road net is too dense near the given point, false otherwise.
    public boolean checkTooDenseNear(Vec2 point) {
        return checkTooDenseInRadius(getRow(point.y), getCol(point.x));
    }
}
