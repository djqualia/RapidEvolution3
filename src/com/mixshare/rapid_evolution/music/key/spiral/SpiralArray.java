package com.mixshare.rapid_evolution.music.key.spiral;

import java.util.Vector;

public class SpiralArray {

    // 0.3651 ~= sqrt(2/15) <= heightIncrement / spiralRadius <= sqrt(3/15) ~= 0.5345
    // a = heightIncrement^2 / spiralRadius^2
    static private double spiralRadius = 1.0;
    static private double heightIncrement = Math.sqrt(2.0 / 15.0);

    // majorChordRootWeight + majorChordThirdWeight + majorChordFifthWeight = 1.0
    // majorChordRootWeight > majorChordThirdWeight > majorChordFifthWeight
    // 3 < 4 * majorChordRootWeight + 3 * majorChordThirdWeight < 1 / 5a + 5/2
    // 4 * majorChordRootWeight + 3 * majorChordThirdWeight < 1 / 2a + 1/2
    static private double majorChordRootWeight = 0.6025;
    static private double majorChordThirdWeight = 0.2930;
    static private double majorChordFifthWeight = 1.0 - majorChordRootWeight - majorChordThirdWeight;

    // minorChordRootWeight + minorChordThirdWeight + minorChordFifthWeight = 1.0
    // minorChordRootWeight > minorChordThirdWeight > minorChordFifthWeight
    // minorChordRootWeight > 1/3 * minorChordThirdWeight + 1/3
    // (gamma + 1/2) * minorChordRootWeight + minorChordThirdWeight < gamma
    //      where gamma = 1 / 8a + 1/4
    static private double minorChordRootWeight = 0.6011;
    static private double minorChordThirdWeight = 0.2121;
    static private double minorChordFifthWeight = 1.0 - minorChordRootWeight - minorChordThirdWeight;

    // majorKeyRootWeight + majorKeyThirdWeight + majorKeyFifthWeight = 1.0
    // majorKeyRootWeight > majorKeyThirdWeight > majorKeyFifthWeight
    static private double majorKeyRootWeight = 0.6025;
    static private double majorKeyDominantWeight = 0.2930;
    static private double majorKeySubdominantWeight = 1.0 - majorKeyRootWeight - majorKeyDominantWeight;

    // minorKeyRootWeight + minorKeyThirdWeight + minorKeyFifthWeight = 1.0
    // minorKeyRootWeight > minorKeyThirdWeight > minorKeyFifthWeight
    static private double minorKeyRootWeight = 0.6025;
    static private double minorKeyDominantWeight = 0.2930;
    static private double minorKeySubdominantWeight = 1.0 - minorKeyRootWeight - minorKeyDominantWeight;

    // 0 <= minorKeyAlpha, minorKeyBeta <= 1
    static private double minorKeyAlpha = 0.75; // 0.0 for natural, 1.0 for harmonic
    static private double minorKeyBeta = 0.75; // 1.0 for natural & harmonic
    
    
    /**
     * Returns the 3D point representing a pitch in the spiral.  The
     * pitch index is the distance in circle of fifths from C.
     * For example, A is 0, E is 1, B is 2, etc.
     * 
     * @return Point 
     */
    static public Point getPitchPoint(int pitchIndex) {
        return new Point(spiralRadius * Math.sin(pitchIndex * Math.PI / 2.0),
                         spiralRadius * Math.cos(pitchIndex * Math.PI / 2.0),
                         pitchIndex * heightIncrement);
    }
    
    static public Point getMajorChordPoint(int pitchIndex) {
        Point root = getPitchPoint(pitchIndex);
        Point third = getPitchPoint(pitchIndex + 4);
        Point fifth = getPitchPoint(pitchIndex + 1);
        Point chord = new Point();
        chord.add(root.getWeightedPoint(majorChordRootWeight));
        chord.add(third.getWeightedPoint(majorChordThirdWeight));
        chord.add(fifth.getWeightedPoint(majorChordFifthWeight));
        return chord;
    }
    
    static public Point getMinorChordPoint(int pitchIndex) {
        Point root = getPitchPoint(pitchIndex);
        Point third = getPitchPoint(pitchIndex - 3);
        Point fifth = getPitchPoint(pitchIndex + 1);
        Point chord = new Point();
        chord.add(root.getWeightedPoint(minorChordRootWeight));
        chord.add(third.getWeightedPoint(minorChordThirdWeight));
        chord.add(fifth.getWeightedPoint(minorChordFifthWeight));
        return chord;        
    }

    static public Point getDiminishedChordPoint(int pitchIndex) {
        Point root = getPitchPoint(pitchIndex);
        Point third = getPitchPoint(pitchIndex - 3);
        Point fifth = getPitchPoint(pitchIndex - 6);
        Point chord = new Point();
        chord.add(root.getWeightedPoint(minorChordRootWeight));
        chord.add(third.getWeightedPoint(minorChordThirdWeight));
        chord.add(fifth.getWeightedPoint(minorChordFifthWeight));
        return chord;        
    }
    
    static public Point getMajorKeyPoint(int pitchIndex) {
        Point root = getMajorChordPoint(pitchIndex);
        Point dominant = getMajorChordPoint(pitchIndex + 1);
        Point subdominant = getMajorChordPoint(pitchIndex - 1);
        Point key = new Point();
        key.add(root.getWeightedPoint(majorKeyRootWeight));
        key.add(dominant.getWeightedPoint(majorKeyDominantWeight));
        key.add(subdominant.getWeightedPoint(majorKeySubdominantWeight));
        return key;        
    }

    static public Point getMinorKeyPoint(int pitchIndex) {
        return getMinorKeyPoint(pitchIndex, minorKeyAlpha, minorKeyBeta);
    }
    static public Point getMinorKeyPoint(int pitchIndex, double alpha, double beta) {
        Point root = getMinorChordPoint(pitchIndex);
        Point dominant = new Point();
        dominant.add(getMinorChordPoint(pitchIndex + 1).getWeightedPoint(1.0 - alpha));
        dominant.add(getMajorChordPoint(pitchIndex + 1).getWeightedPoint(alpha));
        Point subdominant = new Point();
        subdominant.add(getMinorChordPoint(pitchIndex - 1).getWeightedPoint(beta));
        subdominant.add(getMajorChordPoint(pitchIndex - 1).getWeightedPoint(1.0 - beta));
        Point key = new Point();
        key.add(root.getWeightedPoint(minorKeyRootWeight));
        key.add(dominant.getWeightedPoint(minorKeyDominantWeight));
        key.add(subdominant.getWeightedPoint(minorKeySubdominantWeight));
        return key;        
    }
    
    static public Point getPhrygianKeyPoint(int pitchIndex) {
        Point root = getMinorChordPoint(pitchIndex);
        Point dominant = getDiminishedChordPoint(pitchIndex + 1);
        Point subdominant = getMinorChordPoint(pitchIndex - 1);
        Point key = new Point();
        key.add(root.getWeightedPoint(minorKeyRootWeight));
        key.add(dominant.getWeightedPoint(minorKeyDominantWeight));
        key.add(subdominant.getWeightedPoint(minorKeySubdominantWeight));
        return key;        
    }    
    
    static public Point getMixolydianKeyPoint(int pitchIndex) {
        Point root = getMajorChordPoint(pitchIndex);
        Point dominant = getMinorChordPoint(pitchIndex + 1);
        Point subdominant = getMajorChordPoint(pitchIndex - 1);
        Point key = new Point();
        key.add(root.getWeightedPoint(majorKeyRootWeight));
        key.add(dominant.getWeightedPoint(majorKeyDominantWeight));
        key.add(subdominant.getWeightedPoint(majorKeySubdominantWeight));
        return key;        
    }        

    static public Point getLydianKeyPoint(int pitchIndex) {
        Point root = getMajorChordPoint(pitchIndex);
        Point dominant = getMinorChordPoint(pitchIndex + 1);
        Point subdominant = getDiminishedChordPoint(pitchIndex + 6);
        Point key = new Point();
        key.add(root.getWeightedPoint(majorKeyRootWeight));
        key.add(dominant.getWeightedPoint(majorKeyDominantWeight));
        key.add(subdominant.getWeightedPoint(majorKeySubdominantWeight));
        return key;        
    }        
    
    static private Vector<Point> notePoints = new Vector<Point>();
    static {
        for (int i = 0; i < 36; ++i) {  
            Point point = getPitchPoint(i);
            notePoints.add(point);
        }            
    }
        
    static public Point getCenterPoint(double[] noteStrengths) {
        double max = 0.0;
        int max_index = 0;
        double total = 0.0;
        for (int i = 0; i < 12; ++i) {
            if (noteStrengths[i] > max) {
                max = noteStrengths[i];
                max_index = i;
            }
            total += noteStrengths[i];
        }
        if (total == 0.0) return null;
        for (int i = 0; i < 12; ++i) {
            noteStrengths[i] /= total;
        }
        Point centerPoint = new Point();
        Point firstPoint = (Point)notePoints.get(12 + max_index);
        centerPoint.add(firstPoint.getWeightedPoint(noteStrengths[max_index]));
        for (int i = 0; i < 12; ++i) {
            if (i != max_index) {
                Point point1 = (Point)notePoints.get(12 + i);
                Point point2 = (Point)notePoints.get(i);
                Point point3 = (Point)notePoints.get(24 + i);
                double distance1 = point1.getDistanceToSquared(firstPoint);
                double distance2 = point2.getDistanceToSquared(firstPoint);
                double distance3 = point3.getDistanceToSquared(firstPoint);
                if ((distance2 < distance1) && (distance2 < distance3)) {
                    centerPoint.add(point2.getWeightedPoint(noteStrengths[i]));
                } else if ((distance3 < distance1) && (distance3 < distance2)) {
                    centerPoint.add(point3.getWeightedPoint(noteStrengths[i]));                        
                } else {
                    centerPoint.add(point1.getWeightedPoint(noteStrengths[i]));
                }
                
            }
        }
        return centerPoint;
    }
}
