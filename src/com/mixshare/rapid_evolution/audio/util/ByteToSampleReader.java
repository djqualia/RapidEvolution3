package com.mixshare.rapid_evolution.audio.util;

import org.tritonus.share.sampled.TConversionTool;

public class ByteToSampleReader {
	
    public ByteToSampleReader() { }

    public static double getSampleFromBytesNormalized(byte[] buffer, int offset, int bytespersample, boolean bigendian) {
        double val = getSampleFromBytes(buffer, offset, bytespersample, bigendian);
        if (bytespersample == 1) val /= (128.0);
        else if (bytespersample == 2) val /= (32768.0);
        else if (bytespersample == 3) val /= (8388606.0);
        else if (bytespersample == 4) val /= (2147483648.0);
        return val;
    }

    public static double getSampleFromBytesFactored(byte[] buffer, int offset, int bytespersample, boolean bigendian, double factor) {
        double val = getSampleFromBytes(buffer, offset, bytespersample, bigendian);
        if (bytespersample == 1) val /= (128.0 * factor);
        else if (bytespersample == 2) val /= (32768.0 * factor);
        else if (bytespersample == 3) val /= (8388606.0 * factor);
        else if (bytespersample == 4) val /= (2147483648.0 * factor);
        return val;
    }

    public static double getSampleFromBytes(byte[] buffer, int offset, int bytespersample, boolean bigendian) {
        if (bytespersample == 1) {
            if (bigendian) {
                double sample = ( (buffer[offset] & 0xFF) - 128);
                return sample;
            } else {
                double sample = buffer[offset];
                return sample;
            }
        } else if (bytespersample == 2) {
            if (bigendian) {
                double sample = (  (buffer[offset + 0] << 8) | (buffer[offset + 1] & 0xFF) );
                return sample;
            } else {
                double sample = (  (buffer[offset + 0] & 0xFF) | (buffer[offset + 1] << 8) );
                return sample;
            }
        } else if (bytespersample == 3) {
            if (bigendian) {
                double sample = (   (buffer[offset + 0] << 16) | ((buffer[offset + 1] & 0xFF) << 8) |  (buffer[offset + 2] & 0xFF) );
                return sample;
            } else {
                double sample = (   (buffer[offset + 0] & 0xFF) | ((buffer[offset + 1] & 0xFF) << 8) |  (buffer[offset + 2] << 16) );
                return sample;
            }
        } else if (bytespersample == 4) {
            if (bigendian) {
                double sample = (   (buffer[offset + 0] << 24) | ((buffer[offset + 1] & 0xFF) << 16) | ((buffer[offset + 2] & 0xFF) << 8) |  (buffer[offset + 3] & 0xFF) );
                return sample;
            } else {
                double sample = (   (buffer[offset + 0] & 0xFF) | ((buffer[offset + 1] & 0xFF) << 8) | ((buffer[offset + 2] & 0xFF) << 16) |  (buffer[offset + 3] << 24) );
                return sample;
            }
        }
        return 0.0;
    }

    public static void setBytesFromSample(double sample, byte[] buffer, int offset, int bytespersample, boolean bigendian) {
        if (bytespersample == 1) {
            byte val = (byte)sample;
            buffer[offset] = val;
        } else if (bytespersample == 2) {
            int val = (int)sample;
            TConversionTool.intToBytes16(val, buffer, offset, bigendian);
        } else if (bytespersample == 3) {
            int val = (int)sample;
            TConversionTool.intToBytes24(val, buffer, offset, bigendian);
        } else if (bytespersample == 4) {
            int val = (int)sample;
            TConversionTool.intToBytes32(val, buffer, offset, bigendian);
        }
    }

    public static void setBytesFromNormalizedSample(double sample, byte[] buffer, int offset, int bytespersample, boolean bigendian) {
        if (bytespersample == 1) sample *= 127.0;
        else if (bytespersample == 2) sample *= 32767.0;
        else if (bytespersample == 3) sample *= 8388605.0;
        else if (bytespersample == 4) sample *= 2147483647.0;
        setBytesFromSample(sample, buffer, offset, bytespersample, bigendian);
    }
}
