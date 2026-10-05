package org.markscitygen.lib.tensorfield;

import org.ejml.data.DMatrix2;
import org.ejml.dense.fixed.CommonOps_DDF2;

public class ConstDirectionMap implements DirectionMap {
    private DMatrix2 primaryDirection;
    private DMatrix2 secondaryDirection;

    public ConstDirectionMap(double primaryAngle, double magnitude) {
        if (magnitude < 0) throw new IllegalArgumentException("Magnitude must be non-negative");

        if (primaryAngle < 0 || primaryAngle >= 2 * Math.PI)
            throw new IllegalArgumentException("Angle must be in the range [0, 2π)");

        primaryDirection = new DMatrix2(Math.cos(primaryAngle), Math.sin(primaryAngle));

        secondaryDirection =
                new DMatrix2(
                        Math.cos(primaryAngle + Math.PI / 2), Math.sin(primaryAngle + Math.PI / 2));

        CommonOps_DDF2.scale(magnitude, primaryDirection);
        CommonOps_DDF2.scale(magnitude, secondaryDirection);
    }

    public DMatrix2 getPrimaryDirection(DMatrix2 point) {
        return primaryDirection;
    }

    public DMatrix2 getSecondaryDirection(DMatrix2 point) {
        return secondaryDirection;
    }
}
