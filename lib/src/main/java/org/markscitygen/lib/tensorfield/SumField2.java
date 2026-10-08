package org.markscitygen.lib.tensorfield;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.ArrayList;
import org.markscitygen.lib.Vec2;

public final class SumField2 implements TensorField2 {
    private final TensorField2[] fields;
    private final Vec2[] centers;
    private final double[] decayConsts;

    public SumField2(TensorField2[] fields, Vec2[] centers, double[] decayConsts) {
        this.fields = fields;
        this.centers = centers;
        this.decayConsts = decayConsts;
    }

    public static class Builder {
        private final ArrayList<TensorField2> fields = new ArrayList<>();
        private final ArrayList<Vec2> centers = new ArrayList<>();
        private final DoubleArrayList decayConsts = new DoubleArrayList();

        public Builder add(TensorField2 field, Vec2 center, double decayConst) {
            fields.add(field);
            centers.add(center);
            decayConsts.add(decayConst);
            return this;
        }

        public SumField2 build() {
            return new SumField2(
                    fields.toArray(TensorField2[]::new),
                    centers.toArray(Vec2[]::new),
                    decayConsts.toDoubleArray());
        }
    }

    @Override
    public STTensor2 getTensorAt(Vec2 point) {
        var result = new STTensor2();

        for (int i = 0; i < fields.length; i++) {
            var tensor = fields[i].getTensorAt(point);
            tensor.mul(Math.exp(-decayConsts[i] * point.distanceSquaredTo(centers[i])));
            result.add(tensor);
        }

        return result;
    }
}
