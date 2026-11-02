package org.markscitygen.lib;

import java.util.ArrayList;
import java.util.HashMap;
import org.markscitygen.lib.core.RasterPath2;

public final class ParcelFinder {
    ArrayList<RasterPath2> roads;

    RoadIntersectionFinder intersectionFinder = new RoadIntersectionFinder(800, 600);

    public ParcelFinder(ArrayList<RasterPath2> roads) {
        this.roads = roads;
    }

    public HashMap<Integer, ArrayList<RoadIntersectionFinder.DirectedCrossing>> findAllCrossings() {
        var crossings = new HashMap<Integer, ArrayList<RoadIntersectionFinder.DirectedCrossing>>();

        for (int i = 0; i < roads.size(); i++) {
            var path = roads.get(i);
            intersectionFinder.add(path, i);
        }

        for (int i = 0; i < roads.size(); i++) {
            var roadCrossings = intersectionFinder.findDirectedCrossings(roads, i);

            crossings.put(i, roadCrossings);
        }

        return crossings;
    }
}
