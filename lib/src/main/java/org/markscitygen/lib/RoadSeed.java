package org.markscitygen.lib;

import org.markscitygen.lib.core.Vec2;

final class RoadSeed {
    final Vec2 point;
    final boolean isMinorLine;
    final Vec2 growDirection;
    final int pathId;

    public static RoadSeed randomInBounds(Vec2 bounds, int pathId) {
        return new RoadSeed(new Vec2(Math.random() * bounds.x, Math.random() * bounds.y), pathId);
    }

    RoadSeed(Vec2 point, int pathId) {
        this(point, false, pathId);
    }

    RoadSeed(Vec2 point, boolean isMinorLine, int pathId) {
        this(point, isMinorLine, Vec2.fromPolarUnit(Math.random() * 2 * Math.PI), pathId);
    }

    RoadSeed(Vec2 point, boolean isMinorLine, Vec2 growDirection, int pathId) {
        this.point = point;
        this.isMinorLine = isMinorLine;
        this.growDirection = growDirection;
        this.pathId = pathId;
    }
}
