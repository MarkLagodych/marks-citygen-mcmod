package org.markscitygen.lib.tensorfield;

import org.ejml.data.DMatrix2;

public interface TensorField2d {
    STTensor2d getTensorAt(DMatrix2 point);
}
