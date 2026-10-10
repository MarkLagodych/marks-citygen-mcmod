import org.markscitygen.lib.Vec2;
import org.markscitygen.lib.tensorfield.RadialField2;
import org.markscitygen.lib.tensorfield.RectField2;
import org.markscitygen.lib.tensorfield.STTensor2;
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
        return new SumField2.Builder()
                .add(new RadialField2(new Vec2(100, 100)), new Vec2(100, 100), 0.0008)
                .add(new RadialField2(new Vec2(400, 100)), new Vec2(500, 100), 0.0008)
                .add(
                        new RectField2(STTensor2.fromPrimaryDirection(Vec2.fromPolar(angle, 5))),
                        new Vec2(0, 0),
                        0)
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

        for (int x = 0; x < width; x += 20) {
            for (int y = 0; y < height; y += 20) {
                var tensor = field.getTensorAt(new Vec2(x, y));
                var direction = tensor.getPrimaryDirection();
                line(
                        (float) x,
                        (float) y,
                        (float) Math.round(x + direction.x),
                        (float) Math.round(y + direction.y));
                line(
                        (float) x,
                        (float) y,
                        (float) Math.round(x - direction.x),
                        (float) Math.round(y - direction.y));
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
