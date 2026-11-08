package org.markscitygen.lib;

import java.util.ArrayList;
import org.markscitygen.lib.core.Path2;
import org.markscitygen.lib.core.Vec2;

public final class RoadIntersectionFinder {
    private static final int CELL_PADDING = 1;

    // TODO Benchmark what coefficient is the best for performance.
    private static final double CROSSING_DISTANCE_TO_CELL_SIZE = 1. / 2;

    final ArrayList<Path2> roads;
    final Cell[][] cells;
    final double coordToCellIndex;

    public RoadIntersectionFinder(ArrayList<Path2> roads, Vec2 bounds, double crossingDistance) {
        this.roads = roads;

        var cellSize = crossingDistance * CROSSING_DISTANCE_TO_CELL_SIZE;
        this.coordToCellIndex = 1.0 / cellSize;

        var rows = (int) Math.ceil(bounds.y * coordToCellIndex) + 2 * CELL_PADDING;
        var cols = (int) Math.ceil(bounds.x * coordToCellIndex) + 2 * CELL_PADDING;
        this.cells = new Cell[rows][cols];
    }

    int getRow(double y) {
        return (int) Math.floor(y * coordToCellIndex) + CELL_PADDING;
    }

    int getCol(double x) {
        return (int) Math.floor(x * coordToCellIndex) + CELL_PADDING;
    }

    public void add(Path2 road, int pathId) {
        var startSegmentId = 0;

        for (int i = 0; i < road.segmentCount(); i++) {
            var col0 = getCol(road.getX(i));
            var row0 = getRow(road.getY(i));
            var col1 = getCol(road.getX(i + 1));
            var row1 = getRow(road.getY(i + 1));

            var sameCell = col0 == col1 && row0 == row1;
            var isLast = i == road.segmentCount() - 1;

            if (sameCell && !isLast) {
                continue;
            }

            add(pathId, startSegmentId, i, row0, col0);

            if (!sameCell && isLast) {
                add(pathId, i, i, row1, col1);
            }

            startSegmentId = i;
        }
    }

    void add(int pathId, int firstSegmentId, int lastSegmentId, int row, int col) {
        var cell = cells[row][col];

        if (cell == null) {
            cell = new Cell(new ArrayList<>());
            cells[row][col] = cell;
        }

        cell.records.add(new CellRecord(pathId, firstSegmentId, lastSegmentId));
    }

    public void findIntersections(
            int pathId, Vec2 p0, Vec2 p1, ArrayList<Intersection> outIntersections) {
        var row0 = getRow(p0.y);
        var col0 = getCol(p0.x);
        var row1 = getRow(p1.y);
        var col1 = getCol(p1.x);

        var minRow = Math.min(row0, row1);
        var maxRow = Math.max(row0, row1);
        var minCol = Math.min(col0, col1);
        var maxCol = Math.max(col0, col1);

        for (int row = minRow; row <= maxRow; row++) {
            for (int col = minCol; col <= maxCol; col++) {
                var cell = cells[row][col];

                if (cell == null) {
                    continue;
                }

                findIntersections(cell, pathId, p0, p1, outIntersections);
            }
        }
    }

    void findIntersections(
            Cell cell, int pathId, Vec2 p0, Vec2 p1, ArrayList<Intersection> outIntersections) {
        for (var record : cell.records) {
            if (record.pathId == pathId) {
                continue;
            }

            var road = roads.get(record.pathId);

            for (int i = record.firstSegmentId; i <= record.lastSegmentId; i++) {
                var q0 = road.getPoint(i);
                var q1 = road.getPoint(i + 1);

                var p = Vec2.getIntersectionPoint(p0, p1, q0, q1);

                if (p == null) {
                    continue;
                }

                outIntersections.add(new Intersection(record.pathId, i, p));
            }
        }
    }

    static record CellRecord(int pathId, int firstSegmentId, int lastSegmentId) {}

    static record Cell(ArrayList<CellRecord> records) {}

    public static record Intersection(int pathId, int segmentId, Vec2 point) {}
}
