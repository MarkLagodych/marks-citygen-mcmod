package org.markscitygen.lib.tensorfield;

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
        private final ArrayList<Double> radiuses = new ArrayList<>();

        private double coefAtRadius = 0.0001;

        /// When specifying the radius for a subfield, it is expected that at that radius
        /// far from the center of the subfield, the coefficient of the subfield is equal to this
        /// constant.
        ///
        /// This is typically a very small number. Default is 0.0001.
        public Builder coefAtRadius(double coefAtRadius) {
            if (coefAtRadius <= 0 || coefAtRadius >= 1) {
                throw new IllegalArgumentException(
                        "tensorCoefAtRadius must be in the range (0, 1)");
            }

            this.coefAtRadius = coefAtRadius;
            return this;
        }

        /// @param field The subfield to add.
        /// @param center The point where to apply the field and where it is the strongest.
        /// @param radius If +infinity, the subfield is not decayed with distance.
        public Builder add(TensorField2 field, Vec2 center, double radius) {
            fields.add(field);
            centers.add(center);
            radiuses.add(radius);
            return this;
        }

        public SumField2 build() {
            return new SumField2(
                    fields.toArray(TensorField2[]::new),
                    centers.toArray(Vec2[]::new),
                    radiuses.stream().mapToDouble(this::getDecayConst).toArray());
        }

        private double getDecayConst(double radius) {
            return -Math.log(coefAtRadius) / (radius * radius);
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
