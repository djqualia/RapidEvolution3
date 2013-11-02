package com.mixshare.rapid_evolution.music.key.spiral;

public class Point {

    private double x;
    private double y;
    private double z;
    
    public Point() { }
    
    public Point(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    
    public Point getWeightedPoint(double weight) {
        return new Point(x * weight, y * weight, z * weight);
    }
    
    public void add(Point point) {
        x += point.getX();
        y += point.getY();
        z += point.getZ();
    }
    
    public double getDistanceTo(Point point) {
        return Math.sqrt((point.getX() - getX()) * (point.getX() - getX()) +
                (point.getY() - getY()) * (point.getY() - getY()) +
                (point.getZ() - getZ()) * (point.getZ() - getZ()));
    }

    // more computationally efficient...
    public double getDistanceToSquared(Point point) {
        return (point.getX() - getX()) * (point.getX() - getX()) +
                (point.getY() - getY()) * (point.getY() - getY()) +
                (point.getZ() - getZ()) * (point.getZ() - getZ());
    }
    
}
