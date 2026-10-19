package org.markscitygen.lib;

import org.markscitygen.lib.core.Path2;
import org.markscitygen.lib.core.Vec2;
import org.markscitygen.lib.tensorfield.FieldIntegrator2;

final class BasicRoadTracer {
    private final FieldIntegrator2 fieldIntegrator;
    private final double minGrowLength;

    Path2 road;
    boolean isMinorLine;

    double growLength;
    double totalLength;

    Vec2 growDirection;
    Vec2 prevGrowDirection;

    Vec2 point;
    Vec2 prevPoint;
    Vec2 seedPoint;

    BasicRoadTracer(FieldIntegrator2 fieldIntegrator, double minGrowLength) {
        this.fieldIntegrator = fieldIntegrator;
        this.minGrowLength = minGrowLength;
    }

    void seed(boolean isMinorLine, Vec2 seedGrowDirection, Vec2 seedPoint) {
        this.road = new Path2(seedPoint);
        this.isMinorLine = isMinorLine;
        this.totalLength = 0.0;
        this.prevGrowDirection = seedGrowDirection;
        this.prevPoint = seedPoint;
        this.seedPoint = seedPoint;
    }

    /// @return True if the next point was successfully generated,
    /// false if the road cannot grow any further and should stop.
    boolean generateNextPoint() {
        point = fieldIntegrator.getNextPoint(prevPoint, isMinorLine, prevGrowDirection);

        growDirection = prevPoint.directionTo(point);

        growLength = growDirection.length();

        if (growLength < minGrowLength) {
            // Hit a degenerate point in the tensor field. Cannot continue growing the road at all.
            return false;
        }

        road.add(point);
        prevPoint = point;
        prevGrowDirection = growDirection;
        totalLength += growLength;

        return true;
    }
}
