package org.markscitygen.lib.tensorfield;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.ArrayList;
import org.markscitygen.OutVec2;
import org.markscitygen.lib.Vec2Math;

public final class SumField2 implements TensorField2 {
    private final TensorField2[] fields;
    private final double[] centersX;
    private final double[] centersY;
    private final double[] decayConsts;

    public SumField2(Builder builder) {
        this.fields = builder.fields.toArray(TensorField2[]::new);
        this.centersX = builder.centersX.toDoubleArray();
        this.centersY = builder.centersY.toDoubleArray();
        this.decayConsts = builder.decayConsts.toDoubleArray();
    }

    public static class Builder {
        private final ArrayList<TensorField2> fields = new ArrayList<>();
        private final DoubleArrayList centersX = new DoubleArrayList();
        private final DoubleArrayList centersY = new DoubleArrayList();
        private final DoubleArrayList decayConsts = new DoubleArrayList();

        public Builder add(TensorField2 field, double centerX, double centerY, double decayConst) {
            fields.add(field);
            centersX.add(centerX);
            centersY.add(centerY);
            decayConsts.add(decayConst);
            return this;
        }

        public SumField2 build() {
            return new SumField2(this);
        }
    }

    @Override
    public void getTensorAt(double x, double y, OutVec2 tensor) {
        var resultX = 0.0;
        var resultY = 0.0;

        for (int i = 0; i < fields.length; i++) {
            fields[i].getTensorAt(x, y, tensor);

            var d = Vec2Math.distanceSquared(x, y, centersX[i], centersY[i]);
            var k = Math.exp(-decayConsts[i] * d);

            resultX += k * tensor.x;
            resultY += k * tensor.y;
        }

        tensor.x = resultX;
        tensor.y = resultY;
    }
}
