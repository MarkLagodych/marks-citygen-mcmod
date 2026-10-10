import org.markscitygen.OutVec2;
import org.markscitygen.lib.tensorfield.RadialField2;
import org.markscitygen.lib.tensorfield.RectField2;
import org.markscitygen.lib.tensorfield.STTensor2Math;
import org.markscitygen.lib.tensorfield.SumField2;
import processing.core.PApplet;

public class VisualizeTensorField2d extends PApplet {
    public static void main(String[] args) {
        PApplet.main(VisualizeTensorField2d.class, args);
    }

    static final double ANGLE_STEP = Math.PI / 16;

    double angle = 0;

    SumField2 field = makeField(angle);

    static SumField2 makeField(double angle) {
        var t = new OutVec2();
        STTensor2Math.fromMajorEigenvectorPolar(angle, 5, t);

        return new SumField2.Builder()
                .add(new RadialField2(100, 100), 100, 100, 0.0008)
                .add(new RadialField2(400, 100), 500, 100, 0.0008)
                .add(new RectField2(t.x, t.y), 0, 0, 0)
                .build();
    }

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void setup() {
        surface.setTitle("Tensor Field 2D");
    }

    @Override
    public void draw() {
        background(0xff000000);
        fill(0xffffffff);
        stroke(0xffffffff);
        strokeWeight(2);

        var tensor = new OutVec2();
        var dir = new OutVec2();

        for (int x = 0; x < width; x += 20) {
            for (int y = 0; y < height; y += 20) {
                field.getTensorAt(x, y, tensor);
                STTensor2Math.getMajorEigenvector(tensor.x, tensor.y, dir);

                line(
                        (float) x,
                        (float) y,
                        (float) Math.round(x + dir.x),
                        (float) Math.round(y + dir.y));
                line(
                        (float) x,
                        (float) y,
                        (float) Math.round(x - dir.x),
                        (float) Math.round(y - dir.y));
            }
        }
    }

    @Override
    public void keyPressed() {
        if (keyCode == UP) {
            angle += ANGLE_STEP;
            field = makeField(angle);
        } else if (keyCode == DOWN) {
            angle -= ANGLE_STEP;
            field = makeField(angle);
        }
    }
}
