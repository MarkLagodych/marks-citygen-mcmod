import java.util.ArrayList;
import org.markscitygen.lib.Vec2;
import org.markscitygen.lib.tensorfield.Path2;
import org.markscitygen.lib.tensorfield.RadialField2;
import org.markscitygen.lib.tensorfield.RectField2;
import org.markscitygen.lib.tensorfield.RoadGenerator;
import org.markscitygen.lib.tensorfield.STTensor2;
import org.markscitygen.lib.tensorfield.SumField2;
import processing.core.PApplet;

public class VisualizeRoadGen extends PApplet {
    public static void main(String[] args) {
        PApplet.main(VisualizeRoadGen.class, args);
    }

    static final SumField2 field =
            new SumField2.Builder()
                    .add(new RadialField2(new Vec2(100, 100)), new Vec2(100, 100), 0.0008)
                    .add(new RadialField2(new Vec2(400, 100)), new Vec2(500, 100), 0.0008)
                    .add(new RadialField2(new Vec2(300, 300)), new Vec2(300, 300), 0.003)
                    .add(
                            new RectField2(STTensor2.fromPrimaryDirectionPolar(Math.PI / 6, 0.5)),
                            new Vec2(0, 0),
                            0)
                    .add(
                            new RectField2(STTensor2.fromPrimaryDirectionPolar(Math.PI / 3, 0.5)),
                            new Vec2(500, 400),
                            0.0008)
                    .build();

    ArrayList<Path2> roads = genRoads();

    static ArrayList<Path2> genRoads() {
        var gen =
                new RoadGenerator.Builder(field)
                        .bounds(new Vec2(800, 600))
                        .maxLength(600)
                        .maxSteps(1200)
                        .stepSize(0.5)
                        .build();

        var time1 = System.nanoTime();

        gen.generateRoads(100);

        var time2 = System.nanoTime();
        System.out.println("Road generation time:\n" + (time2 - time1) * 1e-6 + " ms");

        return gen.getGeneratedRoads();
    }

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void setup() {
        surface.setTitle("Road gen");
        noLoop();
    }

    @Override
    public void draw() {
        background(0xff000000);
        fill(0xffffffff);
        stroke(0xffffffff);
        strokeWeight(2);

        for (var road : roads) {
            for (int i = 0; i < road.size() - 1; i++) {
                var point = road.get(i);
                var nextPoint = road.get(i + 1);
                line((float) point.x, (float) point.y, (float) nextPoint.x, (float) nextPoint.y);
            }
        }
    }

    @Override
    public void keyPressed() {
        if (key == 'g') {
            roads = genRoads();
            redraw();
        }
    }
}
