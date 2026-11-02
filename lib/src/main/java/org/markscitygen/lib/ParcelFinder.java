package org.markscitygen.lib;

import java.util.ArrayList;
import org.markscitygen.lib.core.RasterPath2;
import org.markscitygen.lib.core.Vec2;

public final class ParcelFinder {
    ArrayList<RasterPath2> roads;

    final RoadIntersectionFinder intersectionFinder;

    public ParcelFinder(ArrayList<RasterPath2> roads, Vec2 bounds) {
        this.roads = roads;
        this.intersectionFinder =
                new RoadIntersectionFinder((int) Math.ceil(bounds.x), (int) Math.ceil(bounds.y));
    }

    public ArrayList<ArrayList<RoadIntersectionFinder.DirectedCrossing>> findAllCrossings() {
        var allCrossings = new ArrayList<ArrayList<RoadIntersectionFinder.DirectedCrossing>>();

        for (int pathId = 0; pathId < roads.size(); pathId++) {
            var path = roads.get(pathId);
            intersectionFinder.add(path, pathId);
        }

        for (int pathId = 0; pathId < roads.size(); pathId++) {
            var roadCrossings = intersectionFinder.findUniqueDirectedCrossings(roads, pathId);

            allCrossings.add(roadCrossings);
        }

        for (int pathId = 0; pathId < roads.size(); pathId++) {
            var roadCrossings = allCrossings.get(pathId);

            for (var crossing : roadCrossings) {
                var intersectedRoadCrossings = allCrossings.get(crossing.intersectedPathId());
                intersectedRoadCrossings.add(crossing.reverse(pathId));
            }
        }

        for (var roadCrossings : allCrossings) {
            roadCrossings.sort(
                    (crossing1, crossing2) -> {
                        return Integer.compare(crossing1.pointId(), crossing2.pointId());
                    });
        }

        return allCrossings;
    }
}
