package org.markscitygen.lib;

import org.markscitygen.lib.core.Vec2;

/// @param priority Lower priority = grows later. Higher priority = grows sooner.
record RoadSeed(Vec2 point, boolean isMinorLine, Vec2 growDirection, int priority)
        implements Comparable<RoadSeed> {

    public static RoadSeed randomInBounds(Vec2 bounds) {
        return new RoadSeed(new Vec2(Math.random() * bounds.x, Math.random() * bounds.y));
    }

    RoadSeed(Vec2 point) {
        this(point, false, 0);
    }

    RoadSeed(Vec2 point, boolean isMinorLine, int priority) {
        this(point, isMinorLine, Vec2.fromPolarUnit(Math.random() * 2 * Math.PI), priority);
    }

    @Override
    public int compareTo(RoadSeed other) {
        return Integer.compare(other.priority, this.priority); // Reverse! Not a natural order.
    }
}
