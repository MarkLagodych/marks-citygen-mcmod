package org.markscitygen.lib;

import org.markscitygen.lib.core.Vec2;

final class RoadSeed {
    final Vec2 point;
    final boolean isMinorLine;
    final Vec2 growDirection;

    public static RoadSeed randomInBounds(Vec2 bounds) {
        return new RoadSeed(new Vec2(Math.random() * bounds.x, Math.random() * bounds.y));
    }

    RoadSeed(Vec2 point) {
        this(point, false);
    }

    RoadSeed(Vec2 point, boolean isMinorLine) {
        this(point, isMinorLine, Vec2.fromPolarUnit(Math.random() * 2 * Math.PI));
    }

    RoadSeed(Vec2 point, boolean isMinorLine, Vec2 growDirection) {
        this.point = point;
        this.isMinorLine = isMinorLine;
        this.growDirection = growDirection;
    }
}
