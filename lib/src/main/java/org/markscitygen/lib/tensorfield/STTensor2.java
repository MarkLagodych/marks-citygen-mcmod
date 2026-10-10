package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.Vec2;

/// Symmetric Traceless 2D Tensor
///
/// Tensor matrix:
/// <table style="border:solid; border-width:0 2px 0 2px; border-radius:0.5em;">
///     <tr><td> X </td><td> Y </td></tr>
///     <tr><td> Y </td><td> -X </td></tr>
/// </table>
///
/// where:
///
/// - X = cos(2⋅angle)⋅magnitude
/// - Y = sin(2⋅angle)⋅magnitude
///
/// An advantage of this representation is that its major and minor eigenvectors are orthogonal,
/// are of the same length, and are fast to compute (no eigenvalue decomposition needed).
///
/// To avoid data duplication, only the X and Y components are stored.
public final class STTensor2 {
    private final Vec2 xy;

    public STTensor2() {
        this.xy = new Vec2();
    }

    private STTensor2(Vec2 xy) {
        this.xy = xy;
    }

    public STTensor2(STTensor2 other) {
        this.xy = new Vec2(other.xy);
    }

    /// Similar to {@link #fromPrimaryDirection}, but takes polar coordinates instead of Cartesians.
    ///
    /// @param angle Any angle in radians
    /// @param length Non-negative length
    public static STTensor2 fromPrimaryDirectionPolar(double angle, double length) {
        return new STTensor2(Vec2.fromPolar(angle * 2, length));
    }

    /// Constructs a tensor from a primary direction vector.
    public static STTensor2 fromPrimaryDirection(Vec2 vector) {
        return fromPrimaryDirectionPolar(vector.polarAngle(), vector.length());
    }

    /// @return The major eigenvector of the tensor
    public Vec2 getPrimaryDirection() {
        // return Vec2.fromPolar(xy.polarAngle() / 2, xy.length());
        var l = xy.length();
        var x = xy.x / l;
        var newX = Math.sqrt(0.5 + 0.5 * x);
        var newY = Math.sqrt(0.5 - 0.5 * x);
        newX = Math.copySign(newX, xy.y);
        return new Vec2(l * newX, l * newY);
    }

    /// @return The minor eigenvector of the tensor
    public Vec2 getSecondaryDirection() {
        // return Vec2.fromPolar(xy.polarAngle() / 2 + Math.PI / 2, xy.length());
        var l = xy.length();
        var x = xy.x / l;
        var newX = Math.sqrt(0.5 + 0.5 * x);
        var newY = Math.sqrt(0.5 - 0.5 * x);
        newX = Math.copySign(newX, xy.y);
        return new Vec2(l * -newY, l * newX);
    }

    public void add(STTensor2 other) {
        this.xy.add(other.xy);
    }

    public void sub(STTensor2 other) {
        this.xy.sub(other.xy);
    }

    public void mul(double scalar) {
        this.xy.mul(scalar);
    }
}
