package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.OutVec2;

public final class RadialField2 implements TensorField2 {
    private final double centerX;
    private final double centerY;

    public RadialField2(double centerX, double centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    @Override
    public void getTensorAt(double x, double y, OutVec2 outTensor) {
        // Now (x,y) stores the direction to the center
        x -= centerX;
        y -= centerY;
        STTensor2Math.fromMajorEigenvector(x, y, outTensor);
    }
}
