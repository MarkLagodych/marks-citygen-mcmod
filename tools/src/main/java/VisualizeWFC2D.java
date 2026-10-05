/*
This sketch tests Wave Function Collapse (2D).

Controls:
  - 0..9: change current color
  - G: generate new result
*/

import java.util.Arrays;
import org.markscitygen.lib.wfc2d.MapSize;
import org.markscitygen.lib.wfc2d.WFC2D;
import processing.core.PApplet;

public class VisualizeWFC2D extends PApplet {
    final int[] TILE_COLORS = {
        0xff000000, // 0 = black
        0xffff0000, // 1 = red
        0xffff7f00, // 2 = orange
        0xffffff00, // 3 = yellow
        0xff00ff00, // 4 = green
        0xff00ffff, // 5 = cyan
        0xff0000ff, // 6 = blue
        0xff7f007f, // 7 = purple
        0xffff007f, // 8 = pink
        0xffffffff, // 9 = white
    };

    final int BACKGROUND = 0xff808080;

    final int TILE_SIZE = 20;

    final int SAMPLE_X = 10;
    final int SAMPLE_Y = 10;
    final int SAMPLE_WIDTH = 10;
    final int SAMPLE_HEIGHT = 10;

    final int RESULT_X = 300;
    final int RESULT_Y = 10;
    final int RESULT_WIDTH = 20;
    final int RESULT_HEIGHT = 15;

    int currentColorIndex = 9;

    int[][] sampleGrid = new int[SAMPLE_HEIGHT][SAMPLE_WIDTH];
    int[][] resultGrid = new int[RESULT_HEIGHT][RESULT_WIDTH];

    public static void main(String[] args) {
        PApplet.main(VisualizeWFC2D.class, args);
    }

    @Override
    public void settings() {
        size(800, 600);
    }

    @Override
    public void setup() {
        surface.setTitle("Wave Function Collapse 2D");
    }

    @Override
    public void draw() {
        background(BACKGROUND);
        stroke(BACKGROUND);

        if (mousePressed) {
            clickOnGrid(SAMPLE_X, SAMPLE_Y, sampleGrid);
        }

        drawGrid(sampleGrid, SAMPLE_X, SAMPLE_Y);
        drawGrid(resultGrid, RESULT_X, RESULT_Y);
    }

    private void drawGrid(int[][] grid, int startX, int startY) {
        int y = startY;

        for (int[] row : grid) {
            int x = startX;

            for (int tile : row) {
                fill(TILE_COLORS[tile]);
                rect(x, y, TILE_SIZE, TILE_SIZE);

                x += TILE_SIZE;
            }

            y += TILE_SIZE;
        }
    }

    @Override
    public void keyReleased() {
        switch ((Character) key) {
            case Character k when k >= '0' && k <= '9' -> {
                currentColorIndex = key - '0';
            }

            case 'g', 'G' -> {
                try {
                    resultGrid =
                            new WFC2D(sampleGrid)
                                    .generate(new MapSize(RESULT_HEIGHT, RESULT_WIDTH));
                } catch (Exception e) {
                    System.err.println("Failed to generate result: " + e.getMessage());
                }
            }

            case 'c', 'C' -> {
                for (int[] ints : sampleGrid) {
                    Arrays.fill(ints, 0);
                }
            }

            default -> {}
        }
    }

    boolean didHitRow(int startX, int startY, int row) {
        int rowY = startY + row * TILE_SIZE;
        return rowY <= mouseY && mouseY <= rowY + TILE_SIZE;
    }

    boolean didHitColumn(int startX, int startY, int col) {
        int colX = startX + col * TILE_SIZE;
        return colX <= mouseX && mouseX <= colX + TILE_SIZE;
    }

    void clickOnGrid(int startX, int startY, int[][] grid) {
        for (int row = 0; row < grid.length; row++) {
            if (!didHitRow(startX, startY, row)) continue;

            for (int col = 0; col < grid[row].length; col++) {
                if (didHitColumn(startX, startY, col)) {
                    grid[row][col] = currentColorIndex;
                }
            }
        }
    }
}
