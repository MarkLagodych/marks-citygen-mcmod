package org.markscitygen.lib.tensorfield;

import org.ejml.data.DMatrix2;

public class RadialDirectionMap implements DirectionMap {
    private DMatrix2 center;

    public RadialDirectionMap(DMatrix2 center) {
        this.center = center;
    }
}
