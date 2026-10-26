package org.markscitygen.lib;

import org.markscitygen.lib.core.RasterPath2;
import org.markscitygen.lib.core.Vec2;
import org.markscitygen.lib.tensorfield.FieldIntegrator2;

final class RoadTracer {
    FieldIntegrator2 fieldIntegrator;

    boolean isMinorLine;
    Vec2 seedPoint;

    RasterPath2 path;

    Vec2 prevPoint;
    Vec2 prevGrowDirection;

    Vec2 point;
    Vec2 growDirection;

    RoadTracer(FieldIntegrator2 fieldIntegrator, RoadSeed seed) {
        this.fieldIntegrator = fieldIntegrator;
        this.seedPoint = seed.point;
        this.isMinorLine = seed.isMinorLine;
        this.path = new RasterPath2(seed.point);
        this.prevPoint = seed.point;
        this.prevGrowDirection = seed.growDirection;
    }

    void acceptPoint() {
        path.add(point);
        prevPoint = point;
        prevGrowDirection = growDirection;
    }

    void nextPoint() {
        point = fieldIntegrator.getNextPoint(prevPoint, isMinorLine, prevGrowDirection);
        growDirection = prevPoint.directionTo(point);
    }
}
