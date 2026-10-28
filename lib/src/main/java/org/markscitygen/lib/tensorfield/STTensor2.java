package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.core.Vec2;

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
/// To get the minor eigenvector, simply rotate the major eigenvector by π/2
/// using {@link Vec2#rotateLeft()}.
public final class STTensor2 {
    /// To avoid data duplication, only the X and Y components are stored.
    private final Vec2 xy;

    public STTensor2() {
        this.xy = new Vec2();
    }

    private STTensor2(double x, double y) {
        this.xy = new Vec2(x, y);
    }

    public STTensor2(STTensor2 other) {
        this.xy = new Vec2(other.xy);
    }

    /// Constructs a tensor from its major (primary) direction.
    public static STTensor2 fromMajorEigenvector(Vec2 vector) {
        var l = vector.length();
        if (l < Vec2.LENGTH_EPSILON) {
            return new STTensor2(0, 0);
        }

        // cos(a)
        var cosA = vector.x / l;
        // sin(a)
        var sinA = vector.y / l;

        // cos(2a) = 2⋅cos²(a) - 1
        var cos2A = 2 * cosA * cosA - 1;
        // sin(2a) = 2⋅cos(a)⋅sin(a)
        var sin2A = 2 * cosA * sinA;

        return new STTensor2(l * cos2A, l * sin2A);
    }

    /// Gets the major (primary) direction of the tensor.
    public Vec2 getMajorEigenvector() {
        var l = xy.length();
        if (l < Vec2.LENGTH_EPSILON) {
            return new Vec2(0, 0);
        }

        // cos(2a) / 2
        var halfCos2A = 0.5 * xy.x / l;

        // cos(a) = ±√((1 + cos(2a)) / 2)
        var cosA = Math.sqrt(0.5 + halfCos2A);
        // sin(a) = ±√((1 - cos(2a)) / 2)
        var sinA = Math.sqrt(0.5 - halfCos2A);
        // Resolve sign ambiguity of sqrt
        cosA = Math.copySign(cosA, xy.y);
        // The sign of sin(a) does not need to be resolved.
        // It would only lead to vector negation, but not change of direction.

        return new Vec2(l * cosA, l * sinA);
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
