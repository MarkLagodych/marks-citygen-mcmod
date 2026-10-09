package org.markscitygen.lib.tensorfield;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.markscitygen.lib.Vec2;

public class Path2 {
    // Point coordinates stored as a SoA for better performance
    DoubleArrayList xs = new DoubleArrayList();
    DoubleArrayList ys = new DoubleArrayList();

    public Path2() {}

    public void add(Vec2 point) {
        xs.add(point.x);
        ys.add(point.y);
    }

    public int size() {
        return xs.size();
    }

    public Vec2 get(int index) {
        return new Vec2(xs.getDouble(index), ys.getDouble(index));
    }

    public Vec2 getLast() {
        return new Vec2(xs.getDouble(xs.size() - 1), ys.getDouble(ys.size() - 1));
    }
}
