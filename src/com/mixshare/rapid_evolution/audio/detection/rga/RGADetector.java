package com.mixshare.rapid_evolution.audio.detection.rga;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.AudioBuffer;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoder;
import com.mixshare.rapid_evolution.audio.codecs.AudioDecoderFactory;
import com.mixshare.rapid_evolution.util.io.FileLockManager;
import com.mixshare.rapid_evolution.workflow.Task;

/**
 * This class detects the replay gain (RGAD) value of a song.  This value
 * measures the difference in perceived loudness with a reference level.
 * This value is used by some media players to achieve equal loudness
 * between different songs.
 * 
 * More information can be found here:
 * http://replaygain.hydrogenaudio.org/rms_energy.html
 */
public class RGADetector {

	static private Logger log = Logger.getLogger(RGADetector.class);
    
	static private int blockSize = 50; // in milliseconds
    
    static private double GAIN_NOT_ENOUGH_SAMPLES = -24601.0;
    static private int GAIN_ANALYSIS_ERROR = 0;
    static private int GAIN_ANALYSIS_OK = 1;

    static private int INIT_GAIN_ANALYSIS_ERROR = 0;
    static private int INIT_GAIN_ANALYSIS_OK = 1;   
    
    static private int YULE_ORDER = 10;    
    static private int BUTTER_ORDER = 2;
    static private double RMS_PERCENTILE = 0.95;           // percentile which is louder than the proposed level
    static private double MAX_SAMP_FREQ = 48000.0;          // maximum allowed sample frequency [Hz]
    static private double RMS_WINDOW_TIME = 0.050;          // Time slice size [s]
    static private int STEPS_per_dB = 100;                  // Table entries per dB
    static private int MAX_dB = 120;                        // Table entries for 0...MAX_dB (normal max. values are 70...80 dB)

    static private int MAX_ORDER = (BUTTER_ORDER > YULE_ORDER ? BUTTER_ORDER : YULE_ORDER);
    static private int MAX_SAMPLES_PER_WINDOW = (int)(MAX_SAMP_FREQ * RMS_WINDOW_TIME);      // max. Samples per Time slice // (size_t)
    static private double PINK_REF = 64.82;                 //298640883795                              // calibration value

    // for each filter:
    // [0] 48 kHz, [1] 44.1 kHz, [2] 32 kHz, [3] 24 kHz, [4] 22050 Hz, [5] 16 kHz, [6] 12 kHz, [7] is 11025 Hz, [8] 8 kHz

    static double[][] ABYule = {
        {0.03857599435200, -3.84664617118067, -0.02160367184185,  7.81501653005538, -0.00123395316851,-11.34170355132042, -0.00009291677959, 13.05504219327545, -0.01655260341619,-12.28759895145294,  0.02161526843274,  9.48293806319790, -0.02074045215285, -5.87257861775999,  0.00594298065125,  2.75465861874613,  0.00306428023191, -0.86984376593551,  0.00012025322027,  0.13919314567432,  0.00288463683916 },
        {0.05418656406430, -3.47845948550071, -0.02911007808948,  6.36317777566148, -0.00848709379851, -8.54751527471874, -0.00851165645469,  9.47693607801280, -0.00834990904936, -8.81498681370155,  0.02245293253339,  6.85401540936998, -0.02596338512915, -4.39470996079559,  0.01624864962975,  2.19611684890774, -0.00240879051584, -0.75104302451432,  0.00674613682247,  0.13149317958808, -0.00187763777362 },
        {0.15457299681924, -2.37898834973084, -0.09331049056315,  2.84868151156327, -0.06247880153653, -2.64577170229825,  0.02163541888798,  2.23697657451713, -0.05588393329856, -1.67148153367602,  0.04781476674921,  1.00595954808547,  0.00222312597743, -0.45953458054983,  0.03174092540049,  0.16378164858596, -0.01390589421898, -0.05032077717131,  0.00651420667831,  0.02347897407020, -0.00881362733839 },
        {0.30296907319327, -1.61273165137247, -0.22613988682123,  1.07977492259970, -0.08587323730772, -0.25656257754070,  0.03282930172664, -0.16276719120440, -0.00915702933434, -0.22638893773906, -0.02364141202522,  0.39120800788284, -0.00584456039913, -0.22138138954925,  0.06276101321749,  0.04500235387352, -0.00000828086748,  0.02005851806501,  0.00205861885564,  0.00302439095741, -0.02950134983287 },
        {0.33642304856132, -1.49858979367799, -0.25572241425570,  0.87350271418188, -0.11828570177555,  0.12205022308084,  0.11921148675203, -0.80774944671438, -0.07834489609479,  0.47854794562326, -0.00469977914380, -0.12453458140019, -0.00589500224440, -0.04067510197014,  0.05724228140351,  0.08333755284107,  0.00832043980773, -0.04237348025746, -0.01635381384540,  0.02977207319925, -0.01760176568150 },
        {0.44915256608450, -0.62820619233671, -0.14351757464547,  0.29661783706366, -0.22784394429749, -0.37256372942400, -0.01419140100551,  0.00213767857124,  0.04078262797139, -0.42029820170918, -0.12398163381748,  0.22199650564824,  0.04097565135648,  0.00613424350682,  0.10478503600251,  0.06747620744683, -0.01863887810927,  0.05784820375801, -0.03193428438915,  0.03222754072173,  0.00541907748707 },
        {0.56619470757641, -1.04800335126349, -0.75464456939302,  0.29156311971249,  0.16242137742230, -0.26806001042947,  0.16744243493672,  0.00819999645858, -0.18901604199609,  0.45054734505008,  0.30931782841830, -0.33032403314006, -0.27562961986224,  0.06739368333110,  0.00647310677246, -0.04784254229033,  0.08647503780351,  0.01639907836189, -0.03788984554840,  0.01807364323573, -0.00588215443421 },
        {0.58100494960553, -0.51035327095184, -0.53174909058578, -0.31863563325245, -0.14289799034253, -0.20256413484477,  0.17520704835522,  0.14728154134330,  0.02377945217615,  0.38952639978999,  0.15558449135573, -0.23313271880868, -0.25344790059353, -0.05246019024463,  0.01628462406333, -0.02505961724053,  0.06920467763959,  0.02442357316099, -0.03721611395801,  0.01818801111503, -0.00749618797172 },
        {0.53648789255105, -0.25049871956020, -0.42163034350696, -0.43193942311114, -0.00275953611929, -0.03424681017675,  0.04267842219415, -0.04678328784242, -0.10214864179676,  0.26408300200955,  0.14590772289388,  0.15113130533216, -0.02459864859345, -0.17556493366449, -0.11202315195388, -0.18823009262115, -0.04060034127000,  0.05477720428674,  0.04788665548180,  0.04704409688120, -0.02217936801134 }
    };

    static double[][] ABButter = {
        {0.98621192462708, -1.97223372919527, -1.97242384925416,  0.97261396931306,  0.98621192462708 },
        {0.98500175787242, -1.96977855582618, -1.97000351574484,  0.97022847566350,  0.98500175787242 },
        {0.97938932735214, -1.95835380975398, -1.95877865470428,  0.95920349965459,  0.97938932735214 },
        {0.97531843204928, -1.95002759149878, -1.95063686409857,  0.95124613669835,  0.97531843204928 },
        {0.97316523498161, -1.94561023566527, -1.94633046996323,  0.94705070426118,  0.97316523498161 },
        {0.96454515552826, -1.92783286977036, -1.92909031105652,  0.93034775234268,  0.96454515552826 },
        {0.96009142950541, -1.91858953033784, -1.92018285901082,  0.92177618768381,  0.96009142950541 },
        {0.95856916599601, -1.91542108074780, -1.91713833199203,  0.91885558323625,  0.95856916599601 },
        {0.94597685600279, -1.88903307939452, -1.89195371200558,  0.89487434461664,  0.94597685600279 }
    };	
    
    static void filterYule (double[] input, int inputIndex, double[] output, int outputIndex, int nSamples, double[] kernel) {
         try {
             while (nSamples > 0) {
                 output[outputIndex] =  
                    input [inputIndex]  * kernel[0]
                  - output[outputIndex-1] * kernel[1]
                  + input [inputIndex-1] * kernel[2]
                  - output[outputIndex-2] * kernel[3]
                  + input [inputIndex-2] * kernel[4]
                  - output[outputIndex-3] * kernel[5]
                  + input [inputIndex-3] * kernel[6]
                  - output[outputIndex-4] * kernel[7]
                  + input [inputIndex-4] * kernel[8]
                  - output[outputIndex-5] * kernel[9]
                  + input [inputIndex-5] * kernel[10]
                  - output[outputIndex-6] * kernel[11]
                  + input [inputIndex-6] * kernel[12]
                  - output[outputIndex-7] * kernel[13]
                  + input [inputIndex-7] * kernel[14]
                  - output[outputIndex-8] * kernel[15]
                  + input [inputIndex-8] * kernel[16]
                  - output[outputIndex-9] * kernel[17]
                  + input [inputIndex-9] * kernel[18]
                  - output[outputIndex-10]* kernel[19]
                  + input [inputIndex-10]* kernel[20];
                 ++outputIndex;
                 ++inputIndex;
                 --nSamples;
             }
         } catch (java.lang.ArrayIndexOutOfBoundsException aob) {
             log.error("filterYule(): error, inputIndex=" + inputIndex + ", outputIndex=" + outputIndex, aob);             
             throw aob;
         }
     }

     static void filterButter (double[] input, int inputIndex, double[] output, int outputIndex, int nSamples, double[] kernel) {   
         while (nSamples > 0) {
             output[outputIndex] =  
                input [inputIndex]  * kernel[0]
              - output[outputIndex-1] * kernel[1]
              + input [inputIndex-1] * kernel[2]
              - output[outputIndex-2] * kernel[3]
              + input [inputIndex-2] * kernel[4];
             ++outputIndex;
             ++inputIndex;
             --nSamples;
         }
     }    
    
     ////////////
     // FIELDS //
     ////////////
    
     private Task task;
     String filename;
     double[] linprebuf = new double[MAX_ORDER * 2];
     int linpre;                                          // left input samples, with pre-buffer
     double[] lstepbuf = new double[MAX_SAMPLES_PER_WINDOW + MAX_ORDER];
     int lstep;                                           // left "first step" (i.e. post first filter) samples
     double[] loutbuf = new double[MAX_SAMPLES_PER_WINDOW + MAX_ORDER];
     int lout;                                            // left "out" (i.e. post second filter) samples
     double[] rinprebuf = new double[MAX_ORDER * 2];
     int rinpre;                                          // right input samples ...
     double[] rstepbuf = new double[MAX_SAMPLES_PER_WINDOW + MAX_ORDER];
     int rstep;
     double[] routbuf = new double[MAX_SAMPLES_PER_WINDOW + MAX_ORDER];
     int rout;
     int sampleWindow;                                    // number of samples required to reach number of milliseconds required for RMS window
     int totsamp;
     double lsum;
     double rsum;
     int freqindex;
     int first;
     int[] A = new int[(int)(STEPS_per_dB * MAX_dB)];
     int[] B = new int[(int)(STEPS_per_dB * MAX_dB)];

     /////////////////
     // CONSTRUCTOR //
     /////////////////
	
     public RGADetector(String filename, Task task) {
    	 this.filename = filename;
    	 this.task = task;
     }
    
     public RGAData detectRGA() {
		 if (log.isTraceEnabled())
			 log.trace("detectRGA(): filename=" + filename);
    	 RGAData result = null;
    	 AudioDecoder decoder = null;
    	 try {
    		 FileLockManager.startFileRead(filename);
    		 decoder = AudioDecoderFactory.getAudioDecoder(filename);
    		 if (decoder != null) {
    			 if (InitGainAnalysis((int)decoder.getSampleRate()) == INIT_GAIN_ANALYSIS_OK) {
    				 double frameRate = decoder.getFrameRate();
    				 int chunkSize = (int)((double)blockSize / 1000.0 * frameRate);
    				 boolean done = false;
    				 boolean aborted = false;
    				 while (!done) {
    					 long frames_read = decoder.readFrames(chunkSize);
    					 if (frames_read == 0) {
    						 done = true;
    					 } else {
    						 AudioBuffer readBuffer = decoder.getAudioBuffer();
    						 
    						 int analyzeResult;
    						 if (readBuffer.getNumChannels() == 1) {
    							 analyzeResult = AnalyzeSamples(readBuffer.getSampleData(0), 0, null, 0, (int)frames_read, 1);
    						 } else {
    							 analyzeResult = AnalyzeSamples(readBuffer.getSampleData(0), 0, readBuffer.getSampleData(1), 0, (int)frames_read, 2);
    						 }
    						 if (analyzeResult != GAIN_ANALYSIS_OK)
    							 log.warn("detectRGA(): analyze samples not okay");    
    						 
    					 }
    					 if (task != null) {
    						 task.setProgress((float)(decoder.getSecondsRead() / decoder.getTotalSeconds()));
    					 }
    					 if (RapidEvolution3.isTerminated || ((task != null) && task.isCancelled())) {
    						 aborted = true;
    						 done = true;
    					 }                    
    				 }                
    				 if (!aborted) {
    					 double gain = GetTitleGain();
    					 if (gain != GAIN_NOT_ENOUGH_SAMPLES)
    						 result = new RGAData(gain);
    				 }
    			 } else {
    				 log.error("detectRGA(): error in InitGainAnalysis");
    			 }
    		 }            
    	 } catch (Exception e) {
    		 log.error("detectRGA(): error", e);
    	 } finally {
    		 if (decoder != null)
    			 decoder.close();
    		 FileLockManager.endFileRead(filename);
    	 }
    	 if (log.isDebugEnabled())
    		 log.debug("detectRGA(): result=" + result);
    	 return result;
     }
     
    private void processSegment(AudioBuffer buffer) {
        double total = 0.0;            
        for (int c = 0; c < buffer.getNumChannels(); ++c) {
            double[] sampleData = buffer.getSampleData(c);
            for (int s = 0; s < sampleData.length; ++s) {
                double squaredValue = sampleData[s] * sampleData[s];
                total += squaredValue;
            }
        }
        double average = total / (buffer.getNumChannels() * buffer.getSampleData(0).length);
        double rms = Math.sqrt(average);            
        if (log.isTraceEnabled())
            log.trace("processSegment(): rms=" + rms);
    }


    /**
     * Returns a INIT_GAIN_ANALYSIS_OK if successful, INIT_GAIN_ANALYSIS_ERROR if not
     */
    int ResetSampleFrequency(int samplefreq) {

        // zero out initial values
        for (int i = 0; i < MAX_ORDER; i++ )
            linprebuf[i] = lstepbuf[i] = loutbuf[i] = rinprebuf[i] = rstepbuf[i] = routbuf[i] = 0.;

        switch ( (int)(samplefreq) ) {
            case 48000: freqindex = 0; break;
            case 44100: freqindex = 1; break;
            case 32000: freqindex = 2; break;
            case 24000: freqindex = 3; break;
            case 22050: freqindex = 4; break;
            case 16000: freqindex = 5; break;
            case 12000: freqindex = 6; break;
            case 11025: freqindex = 7; break;
            case  8000: freqindex = 8; break;
            default:    return INIT_GAIN_ANALYSIS_ERROR;
        }

        sampleWindow = (int) Math.ceil (samplefreq * RMS_WINDOW_TIME);

        lsum         = 0.;
        rsum         = 0.;
        totsamp      = 0;

        for (int i = 0; i < A.length; ++i)
            A[i] = 0;

        return INIT_GAIN_ANALYSIS_OK;
    }

    int InitGainAnalysis ( int samplefreq ) {
        
        if (ResetSampleFrequency(samplefreq) != INIT_GAIN_ANALYSIS_OK)
            return INIT_GAIN_ANALYSIS_ERROR;        

        linpre       = MAX_ORDER;
        rinpre       = MAX_ORDER;
        lstep        = MAX_ORDER;
        rstep        = MAX_ORDER;
        lout         = MAX_ORDER;
        rout         = MAX_ORDER;

        for (int i = 0; i < B.length; ++i)
            B[i] = 0;

        return INIT_GAIN_ANALYSIS_OK;
    }

    private void memcpy(double[] destination, int destinationIndex, double[] source, int sourceIndex, int size) {
        for (int i = 0; i < size; ++i)
            destination[destinationIndex + i] = source[sourceIndex + i];        
    }
    
    private void memmove(double[] destination, int destinationIndex, double[] source, int sourceIndex, int size) {
        double[] temp = new double[size];
        for (int i = 0; i < size; ++i)
            temp[i] = source[sourceIndex + i];        
        for (int i = 0; i < size; ++i)
            destination[destinationIndex + i] = temp[i];        
    }
    
    private double sqr(double val) {
        return val * val;
    }
        
    int AnalyzeSamples (double[] left_samples, int leftIndex, double[] right_samples, int rightIndex, int num_samples, int num_channels) {

        int curleft;
        int curright;
        int batchsamples;
        int cursamples;
        int cursamplepos;
        int i;

        if ( num_samples == 0 )
            return GAIN_ANALYSIS_OK;

        cursamplepos = 0;
        batchsamples = num_samples;

        switch (num_channels) {
            case 1: right_samples = left_samples;
            case 2: break;
            default: return GAIN_ANALYSIS_ERROR;
        }

        memcpy ( linprebuf, MAX_ORDER, left_samples , leftIndex, Math.min(num_samples, MAX_ORDER) );
        memcpy ( rinprebuf, MAX_ORDER, right_samples, rightIndex, Math.min(num_samples, MAX_ORDER) );

        while ( batchsamples > 0 ) {            
            cursamples = Math.min(sampleWindow - totsamp, batchsamples);
            if ( cursamplepos < MAX_ORDER ) {
                curleft = linpre + cursamplepos;
                curright = rinpre + cursamplepos;
                if (cursamples > MAX_ORDER - cursamplepos )
                    cursamples = MAX_ORDER - cursamplepos;
                filterYule ( linprebuf, curleft , lstepbuf, lstep + totsamp, cursamples, ABYule[freqindex]);
                filterYule ( rinprebuf, curright, rstepbuf, rstep + totsamp, cursamples, ABYule[freqindex]);
            }
            else {
                curleft  = leftIndex  + cursamplepos;
                curright = rightIndex + cursamplepos;
                filterYule ( left_samples, curleft , lstepbuf, lstep + totsamp, cursamples, ABYule[freqindex]);
                filterYule ( right_samples, curright, rstepbuf, rstep + totsamp, cursamples, ABYule[freqindex]);
            }


            filterButter ( lstepbuf, lstep + totsamp, loutbuf, lout + totsamp, cursamples, ABButter[freqindex]);
            filterButter ( rstepbuf, rstep + totsamp, routbuf, rout + totsamp, cursamples, ABButter[freqindex]);

            curleft = lout + totsamp;                   // Get the squared values
            curright = rout + totsamp;

            i = cursamples % 16;
            while (i > 0) {
            	lsum += sqr(loutbuf[curleft++]);
                rsum += sqr(routbuf[curright++]);
                --i;
            }
            i = cursamples / 16;
            while (i > 0) {   
                lsum += sqr(loutbuf[curleft+0])
                      + sqr(loutbuf[curleft+1])
                      + sqr(loutbuf[curleft+2])
                      + sqr(loutbuf[curleft+3])
                      + sqr(loutbuf[curleft+4])
                      + sqr(loutbuf[curleft+5])
                      + sqr(loutbuf[curleft+6])
                      + sqr(loutbuf[curleft+7])
                      + sqr(loutbuf[curleft+8])
                      + sqr(loutbuf[curleft+9])
                      + sqr(loutbuf[curleft+10])
                      + sqr(loutbuf[curleft+11])
                      + sqr(loutbuf[curleft+12])
                      + sqr(loutbuf[curleft+13])
                      + sqr(loutbuf[curleft+14])
                      + sqr(loutbuf[curleft+15]);
                curleft += 16;
                rsum += sqr(routbuf[curright+0])
                      + sqr(routbuf[curright+1])
                      + sqr(routbuf[curright+2])
                      + sqr(routbuf[curright+3])
                      + sqr(routbuf[curright+4])
                      + sqr(routbuf[curright+5])
                      + sqr(routbuf[curright+6])
                      + sqr(routbuf[curright+7])
                      + sqr(routbuf[curright+8])
                      + sqr(routbuf[curright+9])
                      + sqr(routbuf[curright+10])
                      + sqr(routbuf[curright+11])
                      + sqr(routbuf[curright+12])
                      + sqr(routbuf[curright+13])
                      + sqr(routbuf[curright+14])
                      + sqr(routbuf[curright+15]);
                curright += 16;
                --i;
            }

            batchsamples -= cursamples;
            cursamplepos += cursamples;
            totsamp      += cursamples;
            if ( totsamp == sampleWindow ) {  // Get the Root Mean Square (RMS) for this set of samples
                double  val  = STEPS_per_dB * 10.0 * Math.log10 ( (lsum+rsum) / totsamp * 0.5 + 1.e-37 );
                int     ival = (int) val;
                if ( ival <                     0 ) ival = 0;
                if ( ival >= A.length ) ival = A.length - 1;;
                A [ival]++;
                lsum = rsum = 0.;
                memmove ( loutbuf , 0, loutbuf, totsamp, MAX_ORDER );
                memmove ( routbuf , 0, routbuf, totsamp, MAX_ORDER );
                memmove ( lstepbuf, 0, lstepbuf, totsamp, MAX_ORDER );
                memmove ( rstepbuf, 0, rstepbuf, totsamp, MAX_ORDER );
                totsamp = 0;
            }
            if ( totsamp > sampleWindow )   // somehow I really screwed up: Error in programming! Contact author about totsamp > sampleWindow
                return GAIN_ANALYSIS_ERROR;
        }
        if ( num_samples < MAX_ORDER ) {
            memmove ( linprebuf, 0, linprebuf, num_samples, MAX_ORDER-num_samples );
            memmove ( rinprebuf, 0, rinprebuf, num_samples, MAX_ORDER-num_samples );
            memcpy  ( linprebuf, MAX_ORDER - num_samples, left_samples, leftIndex, num_samples );
            memcpy  ( rinprebuf, MAX_ORDER - num_samples, right_samples, rightIndex, num_samples );
        } else {
            memcpy  ( linprebuf, 0, left_samples, leftIndex + num_samples - MAX_ORDER, MAX_ORDER );
            memcpy  ( rinprebuf, 0, right_samples, rightIndex + num_samples - MAX_ORDER, MAX_ORDER );
        }

        return GAIN_ANALYSIS_OK;
    }


    static double analyzeResult ( int[] Array, int len ) {
        int elems = 0;
        int upper;
        int i;

        for ( i = 0; i < len; i++ )
            elems += Array[i];
        if ( elems == 0 )
            return GAIN_NOT_ENOUGH_SAMPLES;

        upper = (int) Math.ceil (elems * (1.0 - RMS_PERCENTILE));
        for ( i = len; i-- > 0; ) {
            if ( (upper -= Array[i]) <= 0 )
                break;
        }

        return (double) ((double)PINK_REF - (double)i / (double)STEPS_per_dB);
    }


    double GetTitleGain () {
        double  retval;
        int    i;

        retval = analyzeResult ( A, A.length);

        for ( i = 0; i < A.length; i++) {
            B[i] += A[i];
            A[i]  = 0;
        }

        for ( i = 0; i < MAX_ORDER; i++ )
            linprebuf[i] = lstepbuf[i] = loutbuf[i] = rinprebuf[i] = rstepbuf[i] = routbuf[i] = 0.f;

        totsamp = 0;
        lsum = rsum = 0.;
        return retval;
    }

    double GetAlbumGain ( ) {
        return analyzeResult (B, B.length);
    }

}
