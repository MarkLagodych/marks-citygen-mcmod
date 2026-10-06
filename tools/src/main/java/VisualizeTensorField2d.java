import org.ejml.data.DMatrix2;
import org.markscitygen.lib.tensorfield.RadialField2d;
import org.markscitygen.lib.tensorfield.RectField2d;
import org.markscitygen.lib.tensorfield.STTensor2d;
import org.markscitygen.lib.tensorfield.SumField2d;
import processing.core.PApplet;

public class VisualizeTensorField2d extends PApplet {
    public static void main(String[] args) {
        PApplet.main(VisualizeTensorField2d.class, args);
    }

    SumField2d field =
            new SumField2d.Builder()
                    .add(new RadialField2d(new DMatrix2(100, 100)), new DMatrix2(100, 100), 0.0008)
                    .add(new RadialField2d(new DMatrix2(400, 100)), new DMatrix2(500, 100), 0.0008)
                    .add(
                            new RectField2d(STTensor2d.fromPrimaryDirection(new DMatrix2(5, 2))),
                            new DMatrix2(0, 0),
                            0)
                    .build();

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
                var tensor = field.getTensorAt(new DMatrix2(x, y));
                var direction = tensor.getPrimaryDirection();
                line(
                        (float) x,
                        (float) y,
                        (float) Math.round(x + direction.a1),
                        (float) Math.round(y + direction.a2));
                line(
                        (float) x,
                        (float) y,
                        (float) Math.round(x - direction.a1),
                        (float) Math.round(y - direction.a2));
            }
        }
    }
}
