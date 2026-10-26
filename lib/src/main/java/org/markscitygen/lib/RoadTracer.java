package org.markscitygen.lib;

import org.markscitygen.lib.core.RasterPath2;
import org.markscitygen.lib.core.Vec2;
import org.markscitygen.lib.tensorfield.FieldIntegrator2;

final class RoadTracer {
    final FieldIntegrator2 fieldIntegrator;

    final double minGrowLength;

    final boolean isMinorLine;
    final Vec2 seedPoint;
    final int pathId;

    final RasterPath2 path;

    Vec2 prevPoint;
    Vec2 prevGrowDirection;

    Vec2 point;
    Vec2 growDirection;

    double growLength;

    RoadTracer(RoadSeed seed, FieldIntegrator2 fieldIntegrator, double minGrowLength) {
        this.fieldIntegrator = fieldIntegrator;
        this.seedPoint = seed.point;
        this.isMinorLine = seed.isMinorLine;
        this.pathId = seed.pathId;
        this.minGrowLength = minGrowLength;
        this.path = new RasterPath2(seed.point);
        this.prevPoint = seed.point;
        this.prevGrowDirection = seed.growDirection;
    }

    void acceptPoint() {
        path.add(point);
        prevPoint = point;
        prevGrowDirection = growDirection;
    }

    /// @return null if the tracer hit a degenerate point in the tensor field
    /// and thus cannot grow the road anymore
    Vec2 nextPoint() {
        point = fieldIntegrator.getNextPoint(prevPoint, isMinorLine, prevGrowDirection);
        growDirection = prevPoint.directionTo(point);
        growLength = growDirection.length();

        if (growLength < minGrowLength) {
            // Hit a degenerate point in the tensor field, cannot grow anymore
            return null;
        }

        return point;
    }
}
