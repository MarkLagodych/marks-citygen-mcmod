package org.markscitygen.lib.tensorfield;

import org.markscitygen.OutVec2;
import org.markscitygen.lib.Vec2Math;

/// Math utilities for Symmetric Traceless 2D Tensors.
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
public final class STTensor2Math {
    private STTensor2Math() {}

    /// Constructs a tensor from a primary direction vector.
    public static void fromMajorEigenvector(double vectorX, double vectorY, OutVec2 tensorXY) {
        var l = Vec2Math.length(vectorX, vectorY);
        var cosAngle = vectorX / l;
        var sinAngle = vectorY / l;
        tensorXY.x = l * (2 * cosAngle * cosAngle - 1); // cos(2⋅angle)⋅length
        tensorXY.y = l * (2 * cosAngle * sinAngle); // sin(2⋅angle)⋅length
    }

    public static void fromMajorEigenvectorPolar(double angle, double length, OutVec2 tensorXY) {
        tensorXY.x = length * Math.cos(2 * angle);
        tensorXY.y = length * Math.sin(2 * angle);
    }

    public static void getMajorEigenvector(double tensorX, double tensorY, OutVec2 eigenvec) {
        var l = Vec2Math.length(tensorX, tensorY);
        var halfCosAngle = 0.5 * tensorX / l;
        var newX = Math.sqrt(0.5 + halfCosAngle);
        var newY = Math.sqrt(0.5 - halfCosAngle);
        newX = Math.copySign(newX, tensorY);

        eigenvec.x = l * newX;
        eigenvec.y = l * newY;
    }
}
