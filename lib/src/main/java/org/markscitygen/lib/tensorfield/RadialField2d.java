package org.markscitygen.lib.tensorfield;

import org.ejml.data.DMatrix2;
import org.ejml.dense.fixed.CommonOps_DDF2;

public final class RadialField2d implements TensorField2d {
    private DMatrix2 center;

    public RadialField2d(DMatrix2 center) {
        this.center = center;
    }

    @Override
    public STTensor2d getTensorAt(DMatrix2 point) {
        var direction = new DMatrix2();
        CommonOps_DDF2.subtract(point, center, direction);

        return STTensor2d.fromPrimaryDirection(direction);
    }
}
