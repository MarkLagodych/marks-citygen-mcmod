package org.markscitygen.lib.core;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/// A rasterized 2D path represented as a sequence of points with integer coordinates.
///
/// A path is logically devided into segments, where the i-th segment is the straight line
/// connecting the i-th point and the (i+1)-th point.
/// Segment indexes go from 0 to {@link #size()} - 1.
///
/// Calling functions that operate on segments with an invalid index or when the path
/// has less than 2 points will throw an {@link IndexOutOfBoundsException}.
public class RasterPath2 {
    /// Stores sequences of x and y coordinates in the form [x0, y0, x1, y1, x2, y2, ...].
    IntArrayList coords = new IntArrayList();

    public RasterPath2() {}

    public RasterPath2(RasterPath2 other) {
        this.add(other);
    }

    public RasterPath2(Vec2... points) {
        for (var point : points) {
            add(point);
        }
    }

    private void addRaw(int x, int y) {
        coords.add(x);
        coords.add(y);
    }

    public int size() {
        return coords.size() >> 1;
    }

    public void size(int size) {
        coords.size(size << 1);
    }

    public int getX(int index) {
        return coords.elements()[index << 1];
    }

    public int getY(int index) {
        return coords.elements()[(index << 1) + 1];
    }

    public int segmentCount() {
        var size = size();
        if (size == 0) return 0;
        return size - 1;
    }

    public Vec2 getPoint(int index) {
        return new Vec2(getX(index), getY(index));
    }

    public Vec2 getLastPoint() {
        return getPoint(size() - 1);
    }

    public void add(int x, int y) {
        var size = size();

        if (size == 0) {
            addRaw(x, y);
            return;
        }

        // Bresenham's line algorithm
        var x0 = getX(size - 1);
        var y0 = getY(size - 1);
        var dx = Math.abs(x - x0);
        var dy = Math.abs(y - y0);
        var sx = x0 < x ? 1 : -1;
        var sy = y0 < y ? 1 : -1;
        var err = dx - dy;

        while (!(x0 == x && y0 == y)) {
            addRaw(x0, y0);

            var err2 = 2 * err;

            if (err2 > -dy) {
                err -= dy;
                x0 += sx;
            }

            if (err2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public void add(Vec2 point) {
        add((int) Math.round(point.x), (int) Math.round(point.y));
    }

    public void add(RasterPath2 other) {
        coords.addAll(other.coords);
    }

    public void close() {
        if (size() > 0) {
            add(getX(0), getY(0));
        }
    }

    public boolean isClosed() {
        var size = size();
        if (size < 2) return false;
        return getX(0) == getX(size - 1) && getY(0) == getY(size - 1);
    }

    public double getSegmentLength(int segmentIndex) {
        var dx = getX(segmentIndex + 1) - getX(segmentIndex);
        var dy = getY(segmentIndex + 1) - getY(segmentIndex);
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Vec2 getDirectionAt(int segmentIndex) {
        var dx = getX(segmentIndex + 1) - getX(segmentIndex);
        var dy = getY(segmentIndex + 1) - getY(segmentIndex);
        return new Vec2(dx, dy);
    }

    public Vec2 getLastSegmentDirection() {
        var size = size();
        if (size < 2) {
            throw new IndexOutOfBoundsException("Path has less than 2 points");
        }
        return getDirectionAt(size - 2);
    }
}
