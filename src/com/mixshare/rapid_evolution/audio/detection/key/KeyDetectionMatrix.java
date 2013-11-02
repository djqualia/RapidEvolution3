package com.mixshare.rapid_evolution.audio.detection.key;

import com.mixshare.rapid_evolution.RE3Properties;

public class KeyDetectionMatrix {

	////////////
	// FIELDS //
	////////////
	
    private double startscale = RE3Properties.getDouble("key_detector_matrix_start_scale");
    private int maxoctaves = RE3Properties.getInt("key_detector_matrix_max_octaves");
    private int shifts = RE3Properties.getInt("key_detector_matrix_shifts");
    private int maxFrequency;
    private double[][][][] vmatrix;

    /////////////////
    // CONSTRUCTOR //
    /////////////////
    
    public KeyDetectionMatrix(int maxFrequency) {
        this.maxFrequency = maxFrequency;
        double waveletwidth = RE3Properties.getDouble("key_detector_matrix_wavelet_width");
        int numpoints = KeyDetector.KEY_DETECTOR_ANALYZE_CHUNK_SIZE;
        double timeinterval = ((double)KeyDetector.KEY_DETECTOR_ANALYZE_CHUNK_SIZE) / maxFrequency;
        double coeff1 = timeinterval / numpoints;
        double coeff2 = 1.0 / waveletwidth;
        double[] basefrequency = new double[12];
        basefrequency[0] = 55.0; // a
        basefrequency[1] = 58.27046875; // a#
        basefrequency[2] = 61.73546875; // b
        basefrequency[3] = 65.40640625; // c
        basefrequency[4] = 69.295625; // c#
        basefrequency[5] = 73.41625; // d
        basefrequency[6] = 77.78125; // d#
        basefrequency[7] = 82.406875; // e
        basefrequency[8] = 87.3071875; // f
        basefrequency[9] = 92.49875; // f#
        basefrequency[10] = 97.99875; // g
        basefrequency[11] = 103.82625; // g#
        double[] frequencyparam = new double[12];
        for (int p = 0; p < 12; ++p) {
            basefrequency[p] *= startscale;
            frequencyparam[p] = basefrequency[p] * waveletwidth;
        }
        vmatrix = new double[maxoctaves][1][KeyDetector.KEY_DETECTOR_ANALYZE_CHUNK_SIZE][12];
        for (int s = 0; s < maxoctaves; ++s) {
            double st = Math.pow(2, -s);
            int n = 0;
            for (double ks = 0.5; ks <= 0.5; ks += 0.3) {
                double k = ks * numpoints;
                for (int m = 0; m < KeyDetector.KEY_DETECTOR_ANALYZE_CHUNK_SIZE; ++m) {
                    double x = (((double) m) - k) / st * coeff1;
                    double v1 = x / waveletwidth;
                    for (int z = 0; z < 12; ++z)
                        vmatrix[s][n][m][z] = coeff2
                                * Math.exp(-Math.PI * v1 * v1)
                                * Math.cos(2.0 * Math.PI * frequencyparam[z]
                                        * v1) * coeff1 / Math.sqrt(st);
                }
                ++n;
            }
        }
    }
    
    /////////////
    // GETTERS //
    /////////////
    
    public int getMaxOctaves() { return maxoctaves; }
    
    public double getValue(int p, int ks, int m, int z) { return vmatrix[p][ks][m][z]; }
    
    public int getShifts() { return shifts; }
    
}
