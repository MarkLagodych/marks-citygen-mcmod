package org.markscitygen.lib.tensorfield;

import java.util.ArrayList;
import org.ejml.data.DMatrix2;
import org.ejml.dense.fixed.CommonOps_DDF2;

public final class SumField2d implements TensorField2d {
    private final TensorField2d[] fields;
    private final DMatrix2[] centers;
    private final double[] decayConsts;

    public SumField2d(TensorField2d[] fields, DMatrix2[] centers, double[] decayConsts) {
        this.fields = fields;
        this.centers = centers;
        this.decayConsts = decayConsts;
    }

    public static class Builder {
        private final ArrayList<TensorField2d> fields = new ArrayList<>();
        private final ArrayList<DMatrix2> centers = new ArrayList<>();
        private final ArrayList<Double> decayConsts = new ArrayList<>();

        public Builder add(TensorField2d field, DMatrix2 center, double decayConst) {
            fields.add(field);
            centers.add(center);
            decayConsts.add(decayConst);
            return this;
        }

        public SumField2d build() {
            return new SumField2d(
                    fields.toArray(TensorField2d[]::new),
                    centers.toArray(DMatrix2[]::new),
                    decayConsts.stream().mapToDouble(x -> x).toArray());
        }
    }

    @Override
    public STTensor2d getTensorAt(DMatrix2 point) {
        var result = new STTensor2d();

        for (int i = 0; i < fields.length; i++) {
            var tensor = fields[i].getTensorAt(point);

            // tensor *= exp(- decayConst ⋅ ‖point-center‖²)
            var direction = new DMatrix2();
            CommonOps_DDF2.subtract(point, centers[i], direction);
            var normSquared = CommonOps_DDF2.dot(direction, direction);
            var k = Math.exp(-decayConsts[i] * normSquared);
            CommonOps_DDF2.scale(k, tensor, tensor);

            CommonOps_DDF2.addEquals(result, tensor);
        }

        return result;
    }
}
