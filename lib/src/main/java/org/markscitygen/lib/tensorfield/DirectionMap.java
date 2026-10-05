package org.markscitygen.lib.tensorfield;

import org.ejml.data.DMatrix2;

public interface DirectionMap {
    DMatrix2 getPrimaryDirection(DMatrix2 point);
    DMatrix2 getSecondaryDirection(DMatrix2 point);
}
