package org.markscitygen.lib.core;

public final class Rasterizer2 {
    /// Rasterizes a line from (x0, y0) to (x1, y1) and writes the coordinates of the points.
    /// Both the start and end points are included.
    ///
    /// @param outCoords The output array of point coordinates.
    /// It is filled with (x0, y0), (x1, y1), (x2, y2), ... until the end of the line is reached
    /// or the array is full.
    /// The length of the array must be even.
    /// Unused elements are left untouched.
    ///
    /// @return The number of points written to the output array.
    public static int rasterize(int x0, int y0, int x1, int y1, int[] outCoords) {
        // Bresenham's line algorithm
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int i = 0;

        while (i + 2 < outCoords.length) {
            outCoords[i++] = x0;
            outCoords[i++] = y0;

            if (x0 == x1 && y0 == y1) {
                break;
            }

            int err2 = 2 * err;
            if (err2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (err2 < dx) {
                err += dx;
                y0 += sy;
            }
        }

        return i / 2;
    }
}
