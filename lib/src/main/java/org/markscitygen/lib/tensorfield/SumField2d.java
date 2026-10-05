package org.markscitygen.lib.tensorfield;

import java.util.ArrayList;
import org.ejml.data.DMatrix2;
import org.ejml.dense.fixed.CommonOps_DDF2;

public final class SumField2d implements TensorField2d {
    private final TensorField2d[] fields;
    private final DMatrix2[] centers;
    private final double[] weights;

    public SumField2d(TensorField2d[] fields, DMatrix2[] centers, double[] weights) {
        this.fields = fields;
        this.centers = centers;
        this.weights = weights;
    }

    public static class Builder {
        private final ArrayList<TensorField2d> fields = new ArrayList<>();
        private final ArrayList<DMatrix2> centers = new ArrayList<>();
        private final ArrayList<Double> weights = new ArrayList<>();

        public Builder add(TensorField2d field, DMatrix2 center, double weight) {
            fields.add(field);
            centers.add(center);
            weights.add(weight);
            return this;
        }

        public SumField2d build() {
            return new SumField2d(
                    fields.toArray(TensorField2d[]::new),
                    centers.toArray(DMatrix2[]::new),
                    weights.stream().mapToDouble(x -> x).toArray());
        }
    }

    @Override
    public STTensor2d getTensorAt(DMatrix2 point) {
        var result = new STTensor2d();

        for (int i = 0; i < fields.length; i++) {
            var tensor = fields[i].getTensorAt(point);

            // tensor *= exp(- weight ⋅ ‖point-center‖²)
            var direction = new DMatrix2();
            CommonOps_DDF2.subtract(point, centers[i], direction);
            var normSquared = CommonOps_DDF2.dot(direction, direction);
            var k = Math.exp(-weights[i] * normSquared);
            CommonOps_DDF2.scale(k, tensor, tensor);

            CommonOps_DDF2.addEquals(result, tensor);
        }

        return result;
    }
}
