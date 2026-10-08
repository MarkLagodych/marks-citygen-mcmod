package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.Vec2;

public interface TensorField2 {
    STTensor2 getTensorAt(Vec2 point);
}
