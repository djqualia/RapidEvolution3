package com.mixshare.rapid_evolution.audio.util.cddb;


/*************************************************************************
 *  % java CDDB
 *  970ada0c
 * 
 *  Note: Pearl Jam's album Vs. has N = 12 tracks. The first track
 *  starts at frames[0] =  150, the second at frames[1] = 14672,
 *  the twelfth at frames[11] = 185792, and the disc ends at
 *  frames[N] = 208500. Its disc id is 970ADA0C.
 *
 *  The disc id is a 32-bit integer, which we represent using 8
 *  hex digits XXYYYYZZ. 
 *
 *     - XX is the checksum. The checksum is computed as follows:
 *       for each starting frame[i], we convert it to seconds by
 *       dividing by the frame rate 75; then we sum up the decimal
 *       digits. E.g., if frame[i] = 7500600, this corresponds to
 *       100008 seconds whose digit sum is 1 + 8 = 9.
 *       XX is the total sum of all of these digit sums mod 255.
 *     - YYYY is the length of the album tracks in seconds. It is 
 *       computed as (frames[N] - frames[0]) / 75 and output in hex.
 *     - ZZ is the number of tracks N expressed in hex.
 *
 *************************************************************************/
public class CDDB {

	// return sum of decimal digits in n
	static int sumOfDigits(int n) {
		int sum = 0;
		while (n > 0) {
			sum = sum + (n % 10);
			n = n / 10;
		}
		return sum;
	}

	/*
	static public void main(String[] args) {
		int FRAMES_PER_SECOND = 75;
		int[] frames = { 150, 14672, 27367, 45030, 60545, 76707, 103645,
						116430, 137730, 156887, 171577, 185792, 208500 } ;
		int N = 12;
		int totalLength = (frames[N] - frames[0]) / FRAMES_PER_SECOND;
		int checkSum = 0;
		int s = 2;

		for (int i = 0; i < N; i++)
			checkSum += sumOfDigits(frames[i] / FRAMES_PER_SECOND);

		int XX = checkSum % 255;
		int YYYY = totalLength;
		int ZZ = N;

		// XXYYYYZZ
		int discID = ((XX << 24) | (YYYY << 8) | N);
		System.out.println(Integer.toHexString(discID));
	}
	*/

}