package com.mixshare.rapid_evolution.audio.dsp;

public final class FastFourierTransform {

    private int n;
    private int nu;

    public FastFourierTransform() { }

    public final float[] doFFT(float af[]) {
        n = af.length;
        double d = Math.log(n) / Math.log(2D);
        if ((double)(float)(int)d - d != 0.0D)
            return af;
        
        nu = (int)d;
        int i = n / 2;
        int j = nu - 1;
        float af1[] = new float[n];
        float af2[] = new float[n];
        float af3[] = new float[i];
        for(int k = 0; k < n; k++) {
            af1[k] = af[k];
            af2[k] = 0.0F;
        }

        int l = 0;
        for(int j1 = 1; j1 <= nu; j1++) {
            for(; l < n; l += i) {
                for(int l1 = 1; l1 <= i; l1++) {
                    float f4 = bitrev(l >> j);
                    float f5 = (6.283185F * f4) / (float)n;
                    float f6 = (float)Math.cos(f5);
                    float f7 = (float)Math.sin(f5);
                    float f = af1[l + i] * f6 + af2[l + i] * f7;
                    float f2 = af2[l + i] * f6 - af1[l + i] * f7;
                    af1[l + i] = af1[l] - f;
                    af2[l + i] = af2[l] - f2;
                    af1[l] += f;
                    af2[l] += f2;
                    l++;
                }
            }
            l = 0;
            j--;
            i /= 2;
        }

        for(int i1 = 0; i1 < n; i1++) {
            int k1 = bitrev(i1);
            if(k1 > i1) {
                float f1 = af1[i1];
                float f3 = af2[i1];
                af1[i1] = af1[k1];
                af2[i1] = af2[k1];
                af1[k1] = f1;
                af2[k1] = f3;
            }
        }

        af3[0] = (float)Math.sqrt(af1[0] * af1[0] + af2[0] * af2[0]) / (float)n;
        for(int i2 = 1; i2 < n / 2; i2++)
            af3[i2] = (2.0F * (float)Math.sqrt(af1[i2] * af1[i2] + af2[i2] * af2[i2])) / (float)n;

        return af3;
    }

    private int bitrev(int i) {
        int k = i;
        int l = 0;
        for(int i1 = 1; i1 <= nu; i1++) {
            int j = k / 2;
            l = (2 * l + k) - 2 * j;
            k = j;
        }
        return l;
    }
    
}
