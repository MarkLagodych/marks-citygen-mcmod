import java.util.ArrayList;
import org.markscitygen.lib.OutVec2;
import org.markscitygen.lib.tensorfield.Path2;
import org.markscitygen.lib.tensorfield.RadialField2;
import org.markscitygen.lib.tensorfield.RectField2;
import org.markscitygen.lib.tensorfield.RoadGenerator;
import org.markscitygen.lib.tensorfield.STTensor2Math;
import org.markscitygen.lib.tensorfield.SumField2;
import processing.core.PApplet;

public class VisualizeRoadGen extends PApplet {
    public static void main(String[] args) {
        PApplet.main(VisualizeRoadGen.class, args);
    }

    static final SumField2 field = makeField();

    static SumField2 makeField() {
        var b =
                new SumField2.Builder()
                        .add(new RadialField2(100, 100), 100, 100, 0.0008)
                        .add(new RadialField2(400, 100), 500, 100, 0.0008)
                        .add(new RadialField2(300, 300), 300, 300, 0.003);

        var t = new OutVec2();
        STTensor2Math.fromMajorEigenvectorPolar(Math.PI / 6, 0.5, t);
        b.add(new RectField2(t.x, t.y), 0, 0, 0);

        STTensor2Math.fromMajorEigenvectorPolar(Math.PI / 3, 0.5, t);
        b.add(new RectField2(t.x, t.y), 500, 400, 0.0008);

        return b.build();
    }

    ArrayList<Path2> roads = genRoads();

    static ArrayList<Path2> genRoads() {
        var gen =
                new RoadGenerator.Builder(field)
                        .bounds(800, 600)
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
                var pointX = road.getX(i);
                var pointY = road.getY(i);
                var nextPointX = road.getX(i + 1);
                var nextPointY = road.getY(i + 1);
                line((float) pointX, (float) pointY, (float) nextPointX, (float) nextPointY);
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
