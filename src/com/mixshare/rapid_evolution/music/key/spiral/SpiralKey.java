package com.mixshare.rapid_evolution.music.key.spiral;

import java.util.Vector;

public class SpiralKey {
    
    private boolean major;
    private String type;
    private Vector<Point> points;
    private double[] minDistances = new double[12];
    
    public SpiralKey(boolean major, String type, Vector<Point> points) {
        this.major = major;
        this.type = type;
        this.points = points;
    }
    
    public boolean isMajor() { return major; }
    public String getType() { return type; }
    
    public void getKeyProbabilities(Point centerEffectPoint, double[] result) {
        for (int i = 0; i < 12; ++i) {
           Point point1 = (Point)points.get(i);
           Point point2 = (Point)points.get(i + 12);
           Point point3 = (Point)points.get(i + 24);
           double distance1 = point1.getDistanceToSquared(centerEffectPoint);
           double distance2 = point2.getDistanceToSquared(centerEffectPoint);
           double distance3 = point3.getDistanceToSquared(centerEffectPoint);
           minDistances[i] = Math.sqrt(Math.min(distance1, Math.min(distance2, distance3)));           
       }
       for (int i = 0; i < 12; ++i)
           result[i] -= minDistances[i];       
    }        
    
}
