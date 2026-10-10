package org.markscitygen.lib.tensorfield;

import org.markscitygen.OutVec2;

public interface TensorField2 {
    void getTensorAt(double x, double y, OutVec2 outTensor);
}
