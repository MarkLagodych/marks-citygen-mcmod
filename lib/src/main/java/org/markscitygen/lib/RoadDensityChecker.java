package org.markscitygen.lib;

import org.markscitygen.lib.core.Path2;
import org.markscitygen.lib.core.Rasterizer2;
import org.markscitygen.lib.core.Vec2;

/// Searches for nearby roads in a constant radius in 2D space.
///
/// This is designed to be fast in trade for accuracy.
/// The whole map is divided into a grid of cells with a side of 1/5 the radius size.
/// Each cell is a boolean indicating if there is a point in that cell.
class RoadDensityChecker {
    private static final double RADIUS_TO_CELL_SIZE = 1.0 / 5;

    /// This many cells are added to each side of the bounding box to avoid edge cases
    /// where a point is near the edge of the bounding box.
    private static final int CELL_PADDING = 4;

    private final double coordToCellIndex;

    private final boolean[][] cells;

    public RoadDensityChecker(Vec2 bounds, double radius) {
        var cellSize = radius * RADIUS_TO_CELL_SIZE;

        this.coordToCellIndex = 1.0 / cellSize;

        var rows = (int) Math.ceil(bounds.y * coordToCellIndex) + 2 * CELL_PADDING;
        var cols = (int) Math.ceil(bounds.x * coordToCellIndex) + 2 * CELL_PADDING;
        this.cells = new boolean[rows][cols];
    }

    private int getRow(double y) {
        return (int) Math.floor(y * coordToCellIndex) + CELL_PADDING;
    }

    private int getCol(double x) {
        return (int) Math.floor(x * coordToCellIndex) + CELL_PADDING;
    }

    /// Add a road to the density checker.
    public void add(Path2 road) {
        var coords = new int[2 * 5]; // 5 point coordinates (x, y)

        for (int i = 0; i < road.size() - 1; i++) {
            var x0 = getCol(road.getX(i));
            var y0 = getRow(road.getY(i));
            var x1 = getCol(road.getX(i + 1));
            var y1 = getRow(road.getY(i + 1));

            int numPoints = Rasterizer2.rasterize(x0, y0, x1, y1, coords);

            for (int j = 0; j < numPoints * 2; j += 2) {
                var x = coords[j];
                var y = coords[j + 1];
                cells[y][x] = true;
            }
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
