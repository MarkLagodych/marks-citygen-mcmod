package org.markscitygen.lib;

import java.util.ArrayList;
import org.markscitygen.lib.core.RasterPath2;
import org.markscitygen.lib.core.Vec2;

public final class ParcelFinder {
    final ArrayList<RasterPath2> roads;

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

    // private ArrayList<Parcel.Segment> findSegments(
    //         ArrayList<ArrayList<RoadIntersectionFinder.DirectedCrossing>> crossings) {
    //     var segments = new ArrayList<Parcel.Segment>();

    //     for (int pathId = 0; pathId < crossings.size(); pathId++) {
    //         var roadCrossings = crossings.get(pathId);

    //         if (roadCrossings.size() < 2) {
    //             // TODO should these roads be considered at all?
    //             // It might be better to remove them.
    //             continue;
    //         }

    //         if (roadCrossings.get(0).pointId() > 0) {
    //             segments.add(new Parcel.Segment(pathId, 0, roadCrossings.get(0).pointId()));
    //         }

    //         if (roadCrossings.get(roadCrossings.size() - 1).pointId()
    //                 < roads.get(pathId).size() - 1) {
    //             segments.add(
    //                     new Parcel.Segment(
    //                             pathId,
    //                             roadCrossings.get(roadCrossings.size() - 1).pointId(),
    //                             roads.get(pathId).size() - 1));
    //         }

    //         for (int i = 0; i < roadCrossings.size(); i++) {
    //             var crossing1 = roadCrossings.get(i);
    //             var crossing2 = roadCrossings.get((i + 1) % roadCrossings.size());

    //             segments.add(new Parcel.Segment(pathId, crossing1.pointId(),
    // crossing2.pointId()));
    //             segments.add(new Parcel.Segment(pathId, crossing2.pointId(),
    // crossing1.pointId()));
    //         }
    //     }

    //     return segments;
    // }

    RoadIntersectionFinder.DirectedCrossing findCrossingAt(
            int pathId,
            int pointId,
            ArrayList<ArrayList<RoadIntersectionFinder.DirectedCrossing>> allCrossings) {
        var roadCrossings = allCrossings.get(pathId);

        for (var c : roadCrossings) {
            if (c.pointId() == pointId) {
                return c;
            }
        }

        throw new RuntimeException(
                "Crossing not found at pathId " + pathId + " with pointId " + pointId);
    }

    RoadIntersectionFinder.DirectedCrossing go(
            boolean right,
            RoadIntersectionFinder.DirectedCrossing crossing,
            ArrayList<ArrayList<RoadIntersectionFinder.DirectedCrossing>> allCrossings) {

        var roadCrossings = allCrossings.get(crossing.intersectedPathId());
        var crossingPointId = crossing.intersectedPointId();

        if (crossing.intersectedPathGoesLeft() ^ right) {
            crossingPointId++;
        } else {
            crossingPointId--;
        }

        if (crossingPointId < 0 || crossingPointId >= roadCrossings.size()) {
            // FIXME
            IO.println(crossingPointId);
            return crossing;
        }

        return roadCrossings.get(crossingPointId);
    }

    public ArrayList<Parcel> findParcels() {
        var crossings = findAllCrossings();

        var randPath = (int) (crossings.size() * Math.random());
        var randCrossing = (int) (crossings.get(randPath).size() * Math.random());

        var startCrossing = crossings.get(randPath).get(randCrossing);

        Parcel parcel = new Parcel();
        RoadIntersectionFinder.DirectedCrossing otherCrossing;
        int i = 0;
        do {
            otherCrossing = go(false, startCrossing, crossings);

            parcel.segments.add(
                    new Parcel.Segment(randPath, startCrossing.pointId(), otherCrossing.pointId()));
        } while (otherCrossing != startCrossing && i++ < 100);

        return new ArrayList<Parcel>(java.util.List.of(parcel));
    }
}
