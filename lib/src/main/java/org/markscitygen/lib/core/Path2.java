package org.markscitygen.lib.core;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

/// A 2D path represented as a sequence of points.
///
/// A path is logically devided into segments, where the i-th segment is the straight line
/// connecting the i-th point and the (i+1)-th point.
/// Segment indexes go from 0 to {@link #size()} - 1.
///
/// Calling functions that operate on segments with an invalid index or when the path
/// has less than 2 points will throw an {@link IndexOutOfBoundsException}.
public class Path2 {
    // Point coordinates stored as a Structure of Arrays for better performance
    DoubleArrayList xs = new DoubleArrayList();
    DoubleArrayList ys = new DoubleArrayList();

    public Path2() {}

    public Path2(Path2 other) {
        xs.addAll(other.xs);
        ys.addAll(other.ys);
    }

    public Path2(Vec2... points) {
        for (var point : points) {
            add(point);
        }
    }

    public int size() {
        return xs.size();
    }

    public void size(int size) {
        xs.size(size);
        ys.size(size);
    }

    public int segmentCount() {
        if (size() == 0) return 0;
        return size() - 1;
    }

    public double getX(int index) {
        return xs.getDouble(index);
    }

    public double getY(int index) {
        return ys.getDouble(index);
    }

    public Vec2 getPoint(int index) {
        return new Vec2(xs.getDouble(index), ys.getDouble(index));
    }

    public Vec2 getLastPoint() {
        return new Vec2(xs.getDouble(xs.size() - 1), ys.getDouble(ys.size() - 1));
    }

    public void add(Vec2 point) {
        xs.add(point.x);
        ys.add(point.y);
    }

    public void add(Path2 other) {
        xs.addAll(other.xs);
        ys.addAll(other.ys);
    }

    public void close() {
        if (size() > 0) {
            add(getPoint(0));
        }
    }

    public boolean isClosed() {
        if (size() < 2) return false;
        return getX(0) == getX(size() - 1) && getY(0) == getY(size() - 1);
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
        return getDirectionAt(size() - 2);
    }

    public void pop() {
        if (size() == 0) return;
        xs.removeDouble(xs.size() - 1);
        ys.removeDouble(ys.size() - 1);
    }
}
