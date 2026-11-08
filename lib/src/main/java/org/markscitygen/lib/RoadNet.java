package org.markscitygen.lib;

import org.markscitygen.lib.core.Path2;

public record RoadNet(Path2[] roads, RoadConnections[] connections) {

    public static record RoadConnections(
            RoadTip startLeft, RoadTip startRight, RoadTip endLeft, RoadTip endRight) {

        public RoadTip turnRight(boolean isRoadEnd) {
            return isRoadEnd ? endRight : startLeft;
        }
    }

    public static record RoadTip(int roadId, boolean isRoadEnd) {}
}
