import java.util.ArrayList;
import org.markscitygen.lib.ParcelFinder;
import org.markscitygen.lib.RoadGenerator;
import org.markscitygen.lib.RoadIntersectionFinder;
import org.markscitygen.lib.core.RasterPath2;
import org.markscitygen.lib.core.Vec2;
import org.markscitygen.lib.tensorfield.RadialField2;
import org.markscitygen.lib.tensorfield.RectField2;
import org.markscitygen.lib.tensorfield.SumField2;
import processing.core.PApplet;

public class VisualizeParcelDivision extends PApplet {
    public static void main(String[] args) {
        PApplet.main(VisualizeParcelDivision.class, args);
    }

    public VisualizeParcelDivision() {
        genRoads();
    }

    final SumField2 field =
            new SumField2.Builder()
                    .add(new RadialField2(new Vec2(200, 200)), new Vec2(200, 200), 100)
                    .add(new RadialField2(new Vec2(500, 300)), new Vec2(600, 300), 100)
                    .add(new RadialField2(new Vec2(600, 120)), new Vec2(600, 120), 70)
                    .add(new RectField2(Math.PI / 6, 0.5), new Vec2(0, 0), Double.POSITIVE_INFINITY)
                    .add(new RectField2(-Math.PI / 4, 0.5), new Vec2(400, 400), 150)
                    .build();

    ArrayList<RasterPath2> roads;
    ArrayList<ArrayList<RoadIntersectionFinder.DirectedCrossing>> crossings;
    boolean showCrossings = true;

    void genRoads() {
        var gen =
                new RoadGenerator.Builder(field)
                        .bounds(new Vec2(800, 600))
                        .maxPoints(1200)
                        .stepSize(0.5)
                        .minRoadLength(20.0)
                        .maxRoadLength(600)
                        .minGrowLength(0.1)
                        .crossingDistance(30.0)
                        .proximityThreshold(2.0)
                        .build();

        var time1 = System.nanoTime();

        roads = gen.generateRoads(30);
        var parcelFinder = new ParcelFinder(roads);
        crossings = parcelFinder.findAllCrossings();

        var time2 = System.nanoTime();
        System.out.println("Road generation time:\n" + (time2 - time1) * 1e-6 + " ms");
    }

    @Override
    public void settings() {
        size(800, 600);
        smooth(0);
    }

    @Override
    public void setup() {
        surface.setTitle("Parcel division");
        noLoop();
    }

    @Override
    public void draw() {
        background(0xff000000);
        noFill();

        stroke(0xffffffff);
        strokeWeight(1);

        for (var road : roads) {
            var clr = 0xff000000 | (int) (Math.random() * 0xffffff);
            stroke(clr);
            for (int i = 0; i < road.size() - 1; i++) {
                var point = road.getPoint(i);
                point((float) point.x, (float) point.y);
            }
        }

        if (showCrossings) {
            for (int i = 0; i < roads.size(); i++) {
                var road = roads.get(i);
                var roadCrossings = crossings.get(i);

                for (var crossing : roadCrossings) {
                    // var point = road.getPoint(crossing.pointIndex());
                    // noFill();
                    // stroke(0xff00ff00);
                    // point((float) point.x, (float) point.y);

                    var clr = 0xff000000 | (int) (Math.random() * 0xffffff);

                    var intersectedRoad = roads.get(crossing.intersectedPathId());
                    var intersectedPoint = intersectedRoad.getPoint(crossing.intersectedPointId());
                    noFill();
                    stroke(clr);
                    point((float) intersectedPoint.x, (float) intersectedPoint.y);

                    // for (int j = -10; j < 10; j++) {
                    //     var nextPointId = crossing.intersectedPointId() + j;
                    //     if (nextPointId < 0 || nextPointId >= intersectedRoad.size()) {
                    //         continue;
                    //     }

                    //     var nextPoint = intersectedRoad.getPoint(nextPointId);
                    //     stroke(clr);
                    //     point((float) nextPoint.x, (float) nextPoint.y);
                    // }

                    var needsReverse = false;

                    var nearbyPointId = crossing.intersectedPointId() - 10;
                    if (nearbyPointId < 0) {
                        nearbyPointId = crossing.intersectedPointId() + 10;
                        needsReverse = true;
                    }
                    if (nearbyPointId < 0 || nearbyPointId >= intersectedRoad.size()) {
                        continue;
                    }

                    var nearbyPoint = intersectedRoad.getPoint(nearbyPointId);

                    var segmentDirection = intersectedPoint.directionTo(nearbyPoint);

                    // segmentDirection.mul(10.0);
                    if (needsReverse) {
                        segmentDirection.negate();
                    }

                    if (!crossing.intersectedPathGoesLeft()) {
                        segmentDirection.negate();
                    }

                    stroke(0xffff0000);
                    strokeWeight(1);
                    line(
                            (float) intersectedPoint.x,
                            (float) intersectedPoint.y,
                            (float) (intersectedPoint.x + segmentDirection.x),
                            (float) (intersectedPoint.y + segmentDirection.y));
                }
            }
        }
    }

    @Override
    public void keyPressed() {
        if (key == 'g') {
            genRoads();
            redraw();
        }

        if (key == 't') {
            showCrossings = !showCrossings;
            redraw();
        }
    }
}
