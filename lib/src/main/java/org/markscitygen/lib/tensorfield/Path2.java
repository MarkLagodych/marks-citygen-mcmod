package org.markscitygen.lib.tensorfield;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

public class Path2 {
    // Point coordinates stored as a SoA for better performance
    DoubleArrayList xs = new DoubleArrayList();
    DoubleArrayList ys = new DoubleArrayList();

    public int size() {
        return xs.size();
    }

    public void add(double x, double y) {
        xs.add(x);
        ys.add(y);
    }

    public double getX(int index) {
        return xs.getDouble(index);
    }

    public double getY(int index) {
        return ys.getDouble(index);
    }
}
