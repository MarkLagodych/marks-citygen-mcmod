package org.markscitygen.lib.tensorfield;

import org.ejml.data.DMatrix2;

public final class RectField2d implements TensorField2d {
    private final STTensor2d tensor;

    public RectField2d (STTensor2d tensor) {
        this.tensor = tensor;
    }

    @Override
    public STTensor2d getTensorAt(DMatrix2 _point) {
        return tensor;
    }
}
