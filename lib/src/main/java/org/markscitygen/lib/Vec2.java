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

    /// @return A random vector with `x` in [0, bounds.x) and `y` in [0, bounds.y).
    /// The half-open intervals are consistent with {@link Math#random()}.
    public static Vec2 randomBounded(Vec2 bounds) {
        return new Vec2(Math.random() * bounds.x, Math.random() * bounds.y);
    }

    public static Vec2 randomUnit() {
        var angle = Math.random() * 2 * Math.PI;
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

    /// @return Polar angle in radians, in the range (-π, π]
    public double polarAngle() {
        return Math.atan2(y, x);
    }

    public double length() {
        return Math.hypot(x, y);
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
        return Math.hypot(this.x - otherPoint.x, this.y - otherPoint.y);
    }

    public double distanceSquaredTo(Vec2 otherPoint) {
        var dx = this.x - otherPoint.x;
        var dy = this.y - otherPoint.y;
        return dx * dx + dy * dy;
    }

    /// @return true if `this.x` is in [0, bounds.x) and `this.y` is in [0, bounds.y).
    public boolean isInBounds(Vec2 bounds) {
        return this.x >= 0 && this.x < bounds.x && this.y >= 0 && this.y < bounds.y;
    }

    /// Either negates `this` vector or does nothing.
    /// The resulting angle between `this` and `other` is in [0, π/2].
    public void alignWith(Vec2 other) {
        if (this.dot(other) <= 0) {
            negate();
        }
    }
}
