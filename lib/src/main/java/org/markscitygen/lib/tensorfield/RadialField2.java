package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.Vec2;

public final class RadialField2 implements TensorField2 {
    private final Vec2 center;

    public RadialField2(Vec2 center) {
        this.center = center;
    }

    @Override
    public STTensor2 getTensorAt(Vec2 point) {
        return STTensor2.fromMajorEigenvector(point.directionTo(center));
    }
}
