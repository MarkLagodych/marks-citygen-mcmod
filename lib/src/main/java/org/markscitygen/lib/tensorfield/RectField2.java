package org.markscitygen.lib.tensorfield;

import org.markscitygen.OutVec2;

public final class RectField2 implements TensorField2 {
    private final double tensorX;
    private final double tensorY;

    public RectField2(double tensorX, double tensorY) {
        this.tensorX = tensorX;
        this.tensorY = tensorY;
    }

    public static RectField2 fromPrimaryDirection(double vectorX, double vectorY) {
        var tensor = new OutVec2();
        STTensor2Math.fromMajorEigenvector(vectorX, vectorY, tensor);
        return new RectField2(tensor.x, tensor.y);
    }

    @Override
    public void getTensorAt(double x, double y, OutVec2 outTensor) {
        outTensor.x = tensorX;
        outTensor.y = tensorY;
    }
}
