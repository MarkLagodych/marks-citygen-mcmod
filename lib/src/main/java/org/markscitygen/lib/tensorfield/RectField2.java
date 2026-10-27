package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.core.Vec2;

public final class RectField2 implements TensorField2 {
    private final STTensor2 tensor;

    public RectField2(STTensor2 tensor) {
        this.tensor = tensor;
    }

    public RectField2(double angle, double magnitude) {
        this(STTensor2.fromMajorEigenvector(Vec2.fromPolar(angle, magnitude)));
    }

    @Override
    public STTensor2 getTensorAt(Vec2 point) {
        return new STTensor2(tensor);
    }
}
