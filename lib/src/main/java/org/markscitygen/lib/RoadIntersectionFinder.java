package org.markscitygen.lib;

import java.util.ArrayList;
import org.markscitygen.lib.core.Path2;
import org.markscitygen.lib.core.Vec2;

final class RoadIntersectionFinder {
    private static final int CELL_PADDING = 1;

    private static final double CROSSING_DISTANCE_TO_CELL_SIZE = 1. / 4;

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

    public void add(int roadId) {
        add(roadId, 0);
    }

    public void add(int roadId, int minSegmentId) {
        var startSegmentId = minSegmentId;
        var road = roads.get(roadId);

        for (int currentSegmentId = minSegmentId;
                currentSegmentId < road.segmentCount();
                currentSegmentId++) {
            var col0 = getCol(road.getX(currentSegmentId));
            var row0 = getRow(road.getY(currentSegmentId));
            var col1 = getCol(road.getX(currentSegmentId + 1));
            var row1 = getRow(road.getY(currentSegmentId + 1));

            var sameCell = col0 == col1 && row0 == row1;
            var isLast = currentSegmentId == road.segmentCount() - 1;

            if (sameCell && !isLast) {
                continue;
            }

            addRecordToCell(roadId, startSegmentId, currentSegmentId, row0, col0);
            startSegmentId = currentSegmentId;

            if (!sameCell) {
                var minRow = Math.min(row0, row1);
                var maxRow = Math.max(row0, row1);
                var minCol = Math.min(col0, col1);
                var maxCol = Math.max(col0, col1);

                for (int row = minRow; row <= maxRow; row++) {
                    for (int col = minCol; col <= maxCol; col++) {
                        if ((row == row0 && col == col0) || (row == row1 && col == col1)) {
                            continue;
                        }

                        addRecordToCell(roadId, currentSegmentId, currentSegmentId, row, col);
                    }
                }

                if (isLast) {
                    addRecordToCell(roadId, currentSegmentId, currentSegmentId, row1, col1);
                }
            }
        }
    }

    void addRecordToCell(int roadId, int firstSegmentId, int lastSegmentId, int row, int col) {
        var cell = cells[row][col];

        if (cell == null) {
            cell = new Cell(new ArrayList<>());
            cells[row][col] = cell;
        }

        cell.records.add(new CellRecord(roadId, firstSegmentId, lastSegmentId));
    }

    public void findAllIntersections(int roadId, ArrayList<Intersection> outIntersections) {
        var road = roads.get(roadId);

        for (int segmentId = 0; segmentId < road.segmentCount(); segmentId++) {
            var p0 = road.getPoint(segmentId);
            var p1 = road.getPoint(segmentId + 1);

            findIntersections(roadId, segmentId, p0, p1, outIntersections);
        }
    }

    public void findIntersections(
            int roadId, int segmentId, Vec2 p0, Vec2 p1, ArrayList<Intersection> outIntersections) {
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

                findIntersectionsInCell(cell, roadId, segmentId, p0, p1, outIntersections);
            }
        }
    }

    void findIntersectionsInCell(
            Cell cell,
            int roadId,
            int segmentId,
            Vec2 p0,
            Vec2 p1,
            ArrayList<Intersection> outIntersections) {

        for (var record : cell.records) {

            if (record.roadId == roadId) {
                continue;
            }

            var road = roads.get(record.roadId);

            for (int i = record.firstSegmentId; i <= record.lastSegmentId; i++) {
                var q0 = road.getPoint(i);
                var q1 = road.getPoint(i + 1);

                var p = Vec2.getIntersectionPoint(p0, p1, q0, q1);

                if (p == null) {
                    continue;
                }

                outIntersections.add(new Intersection(roadId, segmentId, record.roadId, i, p));
            }
        }
    }

    static record CellRecord(int roadId, int firstSegmentId, int lastSegmentId) {}

    static record Cell(ArrayList<CellRecord> records) {}

    public static record Intersection(
            int roadId,
            int segmentId,
            int intersectedPathId,
            int intersectedSegmentId,
            Vec2 point) {

        public Intersection reverse() {
            return new Intersection(
                    intersectedPathId, intersectedSegmentId, roadId, segmentId, point);
        }
    }
}
