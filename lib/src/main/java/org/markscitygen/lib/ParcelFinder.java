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
                intersectedRoadCrossings.add(crossing.reverse());
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

    int findCrossingAt(
            int pathId,
            int pointId,
            ArrayList<ArrayList<RoadIntersectionFinder.DirectedCrossing>> allCrossings) {
        var roadCrossings = allCrossings.get(pathId);

        for (int i = 0; i < roadCrossings.size(); i++) {
            var c = roadCrossings.get(i);
            if (c.pointId() == pointId) {
                return i;
            }
        }

        throw new RuntimeException(
                "Crossing not found at pathId " + pathId + " with pointId " + pointId);
    }

    DirectionPair go(
            boolean right,
            DirectionPair prevPair,
            ArrayList<ArrayList<RoadIntersectionFinder.DirectedCrossing>> allCrossings) {

        var crossing = prevPair.crossing();
        var roadCrossings = allCrossings.get(crossing.intersectedPathId());
        var crossingPointId = crossing.intersectedPointId();
        var crossingId =
                findCrossingAt(crossing.intersectedPathId(), crossingPointId, allCrossings);

        var goingForward = true;
        if (crossing.intersectedPathGoesLeft() ^ right ^ !prevPair.wentForward()) {
            crossingId++;
        } else {
            crossingId--;
            goingForward = false;
        }

        if (crossingId < 0 || crossingId >= roadCrossings.size()) {
            // FIXME
            IO.println(crossingId);
            // return new DirectionPair(crossing.turnBack().reverse(), !goingForward);
            throw new RuntimeException(
                    "No crossing found at pathId "
                            + crossing.intersectedPathId()
                            + " with pointId "
                            + crossingPointId);
        }

        return new DirectionPair(roadCrossings.get(crossingId), goingForward);
    }

    public ArrayList<Parcel> findParcels() {
        var crossings = findAllCrossings();

        var randPath = (int) (crossings.size() * Math.random());
        var randCrossing = (int) (crossings.get(randPath).size() * Math.random());

        var startCrossing = crossings.get(randPath).get(randCrossing);

        Parcel parcel = new Parcel();

        try {

            var c1_ = go(false, new DirectionPair(startCrossing, true), crossings);
            var c1 = c1_.crossing();
            var c2_ = go(false, c1_, crossings);
            var c2 = c2_.crossing();

            parcel.segments.add(
                    new Parcel.Segment(
                            startCrossing.intersectedPathId(),
                            startCrossing.intersectedPointId(),
                            c1.pointId()));

            parcel.segments.add(
                    new Parcel.Segment(
                            c1.intersectedPathId(), c1.intersectedPointId(), c2.pointId()));

            var c3_ = go(false, c2_, crossings);
            var c3 = c3_.crossing();

            parcel.segments.add(
                    new Parcel.Segment(
                            c2.intersectedPathId(), c2.intersectedPointId(), c3.pointId()));

            var c4_ = go(false, c3_, crossings);
            var c4 = c4_.crossing();

            parcel.segments.add(
                    new Parcel.Segment(
                            c3.intersectedPathId(), c3.intersectedPointId(), c4.pointId()));

            var c5_ = go(false, c4_, crossings);
            var c5 = c5_.crossing();

            parcel.segments.add(
                    new Parcel.Segment(
                            c4.intersectedPathId(), c4.intersectedPointId(), c5.pointId()));

            var c6_ = go(false, c5_, crossings);
            var c6 = c6_.crossing();

            parcel.segments.add(
                    new Parcel.Segment(
                            c5.intersectedPathId(), c5.intersectedPointId(), c6.pointId()));

            var c7_ = go(false, c6_, crossings);
            var c7 = c7_.crossing();

            parcel.segments.add(
                    new Parcel.Segment(
                            c6.intersectedPathId(), c6.intersectedPointId(), c7.pointId()));

            return new ArrayList<Parcel>(java.util.List.of(parcel));
        } catch (RuntimeException e) {
            IO.println("Failed to find parcel: " + e.getMessage());
            return new ArrayList<Parcel>(java.util.List.of(parcel));
        }
    }

    record DirectionPair(RoadIntersectionFinder.DirectedCrossing crossing, boolean wentForward) {}
}
