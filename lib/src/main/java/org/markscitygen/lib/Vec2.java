package org.markscitygen.lib;

public final class Vec2 {
    public double x;
    public double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(Vec2 other) {
        this.x = other.x;
        this.y = other.y;
    }

    public Vec2() {
        this.x = 0;
        this.y = 0;
    }

    /// @param angle Polar angle in radians (no restriction on range)
    /// @return A unit vector with the given polar angle
    public static Vec2 fromPolarUnit(double angle) {
        return new Vec2(Math.cos(angle), Math.sin(angle));
    }

    /// @param angle Polar angle in radians (no restriction on range)
    /// @param length Non-negative length
    public static Vec2 fromPolar(double angle, double length) {
        if (length < 0) throw new IllegalArgumentException("Length must be >=0");
        return new Vec2(Math.cos(angle) * length, Math.sin(angle) * length);
    }

    public static Vec2 sum(Vec2 a, Vec2 b) {
        return new Vec2(a.x + b.x, a.y + b.y);
    }

    public static Vec2 sum(Vec2 a, double bk, Vec2 b) {
        return new Vec2(a.x + bk * b.x, a.y + bk * b.y);
    }

    public static Vec2 sum(double ak, Vec2 a, double bk, Vec2 b) {
        return new Vec2(ak * a.x + bk * b.x, ak * a.y + bk * b.y);
    }

    public static Vec2 sum(Vec2 a, double bk, Vec2 b, double ck, Vec2 c) {
        return new Vec2(a.x + bk * b.x + ck * c.x, a.y + bk * b.y + ck * c.y);
    }

    public static Vec2 sum(double ak, Vec2 a, double bk, Vec2 b, double ck, Vec2 c) {
        return new Vec2(ak * a.x + bk * b.x + ck * c.x, ak * a.y + bk * b.y + ck * c.y);
    }

    public static Vec2 sum(Vec2 a, double bk, Vec2 b, double ck, Vec2 c, double dk, Vec2 d) {
        return new Vec2(a.x + bk * b.x + ck * c.x + dk * d.x, a.y + bk * b.y + ck * c.y + dk * d.y);
    }

    public static Vec2 sum(
            double ak, Vec2 a, double bk, Vec2 b, double ck, Vec2 c, double dk, Vec2 d) {
        return new Vec2(
                ak * a.x + bk * b.x + ck * c.x + dk * d.x,
                ak * a.y + bk * b.y + ck * c.y + dk * d.y);
    }

    public static Vec2 sum(
            Vec2 a, double bk, Vec2 b, double ck, Vec2 c, double dk, Vec2 d, double ek, Vec2 e) {
        return new Vec2(
                a.x + bk * b.x + ck * c.x + dk * d.x + ek * e.x,
                a.y + bk * b.y + ck * c.y + dk * d.y + ek * e.y);
    }

    public static Vec2 sum(
            double ak,
            Vec2 a,
            double bk,
            Vec2 b,
            double ck,
            Vec2 c,
            double dk,
            Vec2 d,
            double ek,
            Vec2 e) {
        return new Vec2(
                ak * a.x + bk * b.x + ck * c.x + dk * d.x + ek * e.x,
                ak * a.y + bk * b.y + ck * c.y + dk * d.y + ek * e.y);
    }

    public static Vec2 sum(double[] coeffs, Vec2[] vectors) {
        if (coeffs.length != vectors.length) {
            throw new IllegalArgumentException(
                    "Coefficients and vectors must have the same length");
        }

        double x = 0;
        double y = 0;
        for (int i = 0; i < coeffs.length; i++) {
            x += coeffs[i] * vectors[i].x;
            y += coeffs[i] * vectors[i].y;
        }
        return new Vec2(x, y);
    }

    /// This function uses {@link Math#atan2}, which is **very slow**!
    /// @return Polar angle in radians, in the range (-π, π]
    public double polarAngle() {
        return Math.atan2(y, x);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public void add(Vec2 other) {
        this.x += other.x;
        this.y += other.y;
    }

    public void sub(Vec2 other) {
        this.x -= other.x;
        this.y -= other.y;
    }

    public void mul(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
    }

    public void negate() {
        this.x = -this.x;
        this.y = -this.y;
    }

    public double dot(Vec2 other) {
        return this.x * other.x + this.y * other.y;
    }

    public Vec2 offsetWith(Vec2 direction) {
        return new Vec2(this.x + direction.x, this.y + direction.y);
    }

    public Vec2 directionTo(Vec2 otherPoint) {
        return new Vec2(otherPoint.x - this.x, otherPoint.y - this.y);
    }

    public double distanceTo(Vec2 otherPoint) {
        var dx = this.x - otherPoint.x;
        var dy = this.y - otherPoint.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public double distanceSquaredTo(Vec2 otherPoint) {
        var dx = this.x - otherPoint.x;
        var dy = this.y - otherPoint.y;
        return dx * dx + dy * dy;
    }

    /// @return true if `this.x` is in [0, max.x) and `this.y` is in [0, max.y).
    public boolean isInBounds(Vec2 max) {
        return this.x >= 0 && this.x < max.x && this.y >= 0 && this.y < max.y;
    }

    /// @return true if `this.x` is in [min.x, max.x) and `this.y` is in [min.y, max.y).
    public boolean isInBounds(Vec2 min, Vec2 max) {
        return this.x >= min.x && this.x < max.x && this.y >= min.y && this.y < max.y;
    }

    /// Rotates the vector by π/2 radians (90 degrees) counterclockwise.
    public void rotatePiHalf() {
        var tmp = x;
        x = -y;
        y = tmp;
    }

    /// Either negates `this` vector or does nothing.
    /// The resulting angle between `this` and `other` is in [0, π/2].
    public void alignWith(Vec2 other) {
        if (this.dot(other) <= 0) {
            negate();
        }
    }
}
