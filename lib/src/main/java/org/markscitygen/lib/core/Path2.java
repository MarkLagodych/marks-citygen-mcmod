package org.markscitygen.lib.core;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/// A 2D path represented as a sequence of points.
///
/// A path is logically divided into segments, where the i-th segment is the straight line
/// connecting the i-th point and the (i+1)-th point.
/// Segment indexes go from 0 to {@link #size()} - 1.
///
/// Calling functions that operate on segments with an invalid index or when the path
/// has less than 2 points will throw an {@link IndexOutOfBoundsException}.
public class Path2 {
    /// Stores sequences of x and y coordinates in the form [x0, y0, x1, y1, x2, y2, ...].
    DoubleArrayList coords = new DoubleArrayList();

    public Path2() {}

    public Path2(Path2 other) {
        this.add(other);
    }

    public Path2(Vec2... points) {
        for (var point : points) {
            add(point);
        }
    }

    public void add(double x, double y) {
        coords.add(x);
        coords.add(y);
    }

    public void add(Vec2 point) {
        add(point.x, point.y);
    }

    public void add(Path2 other) {
        coords.addAll(other.coords);
    }

    public int size() {
        return coords.size() >> 1;
    }

    public void size(int size) {
        coords.size(size << 1);
    }

    public double getX(int index) {
        return coords.elements()[index << 1];
    }

    public double getY(int index) {
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

    public Vec2 getSegmentDirection(int segmentIndex) {
        var x0 = getX(segmentIndex);
        var y0 = getY(segmentIndex);
        var x1 = getX(segmentIndex + 1);
        var y1 = getY(segmentIndex + 1);
        return new Vec2(x1 - x0, y1 - y0);
    }

    public Vec2 getLastSegmentDirection() {
        return getSegmentDirection(segmentCount() - 1);
    }

    public double getSegmentLength(int segmentIndex) {
        var x0 = getX(segmentIndex);
        var y0 = getY(segmentIndex);
        var x1 = getX(segmentIndex + 1);
        var y1 = getY(segmentIndex + 1);
        var dx = x1 - x0;
        var dy = y1 - y0;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void reverse() {
        var elements = coords.elements();
        var size = coords.size();

        for (int i = 0, j = size - 2; i < j; i += 2, j -= 2) {
            var x1 = elements[i];
            var y1 = elements[i + 1];
            var x2 = elements[j];
            var y2 = elements[j + 1];

            elements[i] = x2;
            elements[i + 1] = y2;
            elements[j] = x1;
            elements[j + 1] = y1;
        }
    }
}
