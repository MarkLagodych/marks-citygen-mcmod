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
    /// @param length Non-negative length
    public static Vec2 fromPolar(double angle, double length) {
        if (length < 0) throw new IllegalArgumentException("Length must be >=0");
        return new Vec2(Math.cos(angle) * length, Math.sin(angle) * length);
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

    public double dot(Vec2 other) {
        return this.x * other.x + this.y * other.y;
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
}
