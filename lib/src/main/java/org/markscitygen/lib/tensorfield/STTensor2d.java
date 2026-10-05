package org.markscitygen.lib.tensorfield;

import org.ejml.data.DMatrix2;

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
public final class STTensor2d extends DMatrix2 {
    public STTensor2d(double x, double y) {
        super(x, y);
    }

    public STTensor2d(DMatrix2 xy) {
        super(xy);
    }

    /// Similar to {@link #fromPrimaryDirection}, but takes polar coordinates instead of Cartesians.
    public static STTensor2d fromPolar(double angle, double magnitude) {
        if (magnitude < 0) throw new IllegalArgumentException("Magnitude must be >=0");

        if (angle < 0 || angle >= 2 * Math.PI)
            throw new IllegalArgumentException("Angle must be in the range [0, 2π)");

        var x = Math.cos(2 * angle) * magnitude;
        var y = Math.sin(2 * angle) * magnitude;
        return new STTensor2d(x, y);
    }

    /// Constructs a tensor from a primary direction vector.
    public static STTensor2d fromPrimaryDirection(DMatrix2 vector) {
        var angle = Math.atan2(vector.a2, vector.a1);
        var magnitude = Math.hypot(vector.a1, vector.a2);
        return fromPolar(angle, magnitude);
    }

    /// @return The major eigenvector of the tensor
    public DMatrix2 getPrimaryDirection() {
        var angle = 0.5 * Math.atan2(a2, a1);
        var length = Math.hypot(a1, a2);
        return new DMatrix2(length * Math.cos(angle), length * Math.sin(angle));
    }

    /// @return The secondary eigenvector of the tensor
    public DMatrix2 getSecondaryDirection() {
        var angle = 0.5 * Math.atan2(a2, a1) + Math.PI / 2;
        var length = Math.hypot(a1, a2);
        return new DMatrix2(length * Math.cos(angle), length * Math.sin(angle));
    }
}
