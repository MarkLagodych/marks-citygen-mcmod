package org.markscitygen.lib;

import java.util.ArrayList;
import org.markscitygen.lib.core.RasterPath2;

public final class ParcelFinder {
    ArrayList<RasterPath2> roads;

    RoadIntersectionFinder intersectionFinder = new RoadIntersectionFinder(800, 600);

    public ParcelFinder(ArrayList<RasterPath2> roads) {
        this.roads = roads;
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
