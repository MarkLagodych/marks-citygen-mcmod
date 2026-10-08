package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.Vec2;

public final class RectField2 implements TensorField2 {
    private final STTensor2 tensor;

    public RectField2(STTensor2 tensor) {
        this.tensor = tensor;
    }

    @Override
    public STTensor2 getTensorAt(Vec2 point) {
        return new STTensor2(tensor);
    }
}
