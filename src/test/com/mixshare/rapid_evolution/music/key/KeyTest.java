package test.com.mixshare.rapid_evolution.music.key;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.music.key.Key;

public class KeyTest extends RE3TestCase {

    private static Logger log = Logger.getLogger(KeyTest.class);
	
    // adapted from the old mixshare server key class test
    public void testKeyClassFunctionality() {
        if (log.isDebugEnabled()) log.debug("testKeyClassFunctionality(): testing key class functionality");
        try {
            // for each key
            
            // test minor: Am
            Key key = Key.getKey("Am");
            if (!key.getKeyCode().toString(true).equals("08A")) fail("incorrect key code, is=" + key.getKeyCode().toString(true));
            if (!key.getKeyCode().toString().equals("08A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Am")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Am")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 0.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            key = Key.getKey("08A");
            if (!key.getKeyCode().toString(true).equals("08A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("08A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Am")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Am")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 0.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            // test minor: A#m            
            key = Key.getKey("A#m");
            if (!key.getKeyCode().toString(true).equals("03A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("03A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Bbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("A#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 1.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("Bbm");
            if (!key.getKeyCode().toString(true).equals("03A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("03A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Bbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("A#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 1.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            key = Key.getKey("03A");
            if (!key.getKeyCode().toString(true).equals("03A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("03A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Bbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("A#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 1.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect"); 

            // test minor: Bm
            key = Key.getKey("Bm");
            if (!key.getKeyCode().toString(true).equals("10A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("10A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Bm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Bm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 2.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            key = Key.getKey("10A");
            if (!key.getKeyCode().toString(true).equals("10A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("10A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Bm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Bm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 2.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");   

            // test minor: Cm
            key = Key.getKey("Cm");
            if (!key.getKeyCode().toString(true).equals("05A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("05A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Cm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Cm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 3.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            key = Key.getKey("05A");
            if (!key.getKeyCode().toString(true).equals("05A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("05A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Cm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Cm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 3.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            // test minor: C#m            
            key = Key.getKey("C#m");
            if (!key.getKeyCode().toString(true).equals("12A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("12A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Dbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("C#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 4.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("Dbm");
            if (!key.getKeyCode().toString(true).equals("12A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("12A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Dbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("C#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 4.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("12A");
            if (!key.getKeyCode().toString(true).equals("12A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("12A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Dbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("C#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 4.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");

            // test minor: Dm
            key = Key.getKey("Dm");
            if (!key.getKeyCode().toString(true).equals("07A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("07A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Dm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Dm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 5.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            key = Key.getKey("07A");
            if (!key.getKeyCode().toString(true).equals("07A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("07A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Dm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Dm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 5.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            // test minor: D#m            
            key = Key.getKey("D#m");
            if (!key.getKeyCode().toString(true).equals("02A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("02A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Ebm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("D#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 6.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("Ebm");
            if (!key.getKeyCode().toString(true).equals("02A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("02A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Ebm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("D#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 6.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("02A");
            if (!key.getKeyCode().toString(true).equals("02A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("02A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Ebm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("D#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 6.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");

            // test minor: Em
            key = Key.getKey("Em");
            if (!key.getKeyCode().toString(true).equals("09A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("09A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Em")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Em")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 7.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            key = Key.getKey("09A");
            if (!key.getKeyCode().toString(true).equals("09A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("09A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Em")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Em")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 7.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            // test minor: Fm
            key = Key.getKey("Fm");
            if (!key.getKeyCode().toString(true).equals("04A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("04A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Fm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Fm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 8.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            key = Key.getKey("04A");
            if (!key.getKeyCode().toString(true).equals("04A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("04A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Fm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Fm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 8.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            // test minor: F#m            
            key = Key.getKey("F#m");
            if (!key.getKeyCode().toString(true).equals("11A")) fail("incorrect key code, is=" + key.getKeyCode().toString(true));
            if (!key.getKeyCode().toString().equals("11A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Gbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("F#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 9.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("Gbm");
            if (!key.getKeyCode().toString(true).equals("11A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("11A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Gbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("F#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 9.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("11A");
            if (!key.getKeyCode().toString(true).equals("11A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("11A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Gbm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("F#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 9.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");
            
            // test minor: Gm
            key = Key.getKey("Gm");
            if (!key.getKeyCode().toString(true).equals("06A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("06A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Gm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Gm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            key = Key.getKey("06A");
            if (!key.getKeyCode().toString(true).equals("06A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("06A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Gm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("Gm")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");           

            // test minor: G#m            
            key = Key.getKey("G#m");
            if (!key.getKeyCode().toString(true).equals("01A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("01A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Abm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("G#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 11.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("Abm");
            if (!key.getKeyCode().toString(true).equals("01A")) fail("incorrect key code, is=" + key.getKeyCode());
            if (!key.getKeyCode().toString().equals("01A")) fail("incorrect key code, is=" + key.getKeyCode());
            if (!key.getKeyNotationFlat().equals("Abm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("G#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 11.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("01A");
            if (!key.getKeyCode().toString(true).equals("01A")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("01A")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Abm")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("G#m")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 11.0) fail("incorrect key value");
            if (!key.isMinor()) fail("isMinor() is incorrect");            
            
            // test major:
            key = Key.getKey("A");
            if (!key.getKeyCode().toString(true).equals("11B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("11B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("11B")) fail("incorrect key code, is=" + key.getKeyCode());
            if (!key.getKeyNotationFlat().equals("A")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("A")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 0.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("11B");
            if (!key.getKeyCode().toString(true).equals("11B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("11B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("A")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("A")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 0.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            // test major: A#
            key = Key.getKey("A#");
            if (!key.getKeyCode().toString(true).equals("06B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("06B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Bb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("A#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 1.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("Bb");
            if (!key.getKeyCode().toString(true).equals("06B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("06B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Bb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("A#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 1.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("06B");
            if (!key.getKeyCode().toString(true).equals("06B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("06B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Bb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("A#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 1.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            // test major: B
            key = Key.getKey("B");
            if (!key.getKeyCode().toString(true).equals("01B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("01B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("B")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("B")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 2.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("01B");
            if (!key.getKeyCode().toString(true).equals("01B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("01B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("B")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("B")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 2.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            // test major: C
            key = Key.getKey("C");
            if (!key.getKeyCode().toString(true).equals("08B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("08B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("C")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("C")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 3.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("08B");
            if (!key.getKeyCode().toString(true).equals("08B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("08B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("C")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("C")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 3.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
        
            // test major: C#
            key = Key.getKey("C#");
            if (!key.getKeyCode().toString(true).equals("03B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("03B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Db")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("C#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 4.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("Db");
            if (!key.getKeyCode().toString(true).equals("03B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("03B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Db")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("C#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 4.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("03B");
            if (!key.getKeyCode().toString(true).equals("03B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("03B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Db")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("C#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 4.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            // test major: D
            key = Key.getKey("D");
            if (!key.getKeyCode().toString(true).equals("10B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("10B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("D")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("D")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 5.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("10B");
            if (!key.getKeyCode().toString(true).equals("10B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("10B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("D")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("D")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 5.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            // test major: D#
            key = Key.getKey("D#");
            if (!key.getKeyCode().toString(true).equals("05B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("05B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Eb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("D#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 6.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("Eb");
            if (!key.getKeyCode().toString(true).equals("05B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("05B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Eb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("D#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 6.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("05B");
            if (!key.getKeyCode().toString(true).equals("05B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("05B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Eb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("D#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 6.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            // test major: E
            key = Key.getKey("E");
            if (!key.getKeyCode().toString(true).equals("12B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("12B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("E")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("E")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 7.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("12B");
            if (!key.getKeyCode().toString(true).equals("12B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("12B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("E")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("E")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 7.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            // test major: F
            key = Key.getKey("F");
            if (!key.getKeyCode().toString(true).equals("07B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("07B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("F")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("F")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 8.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("07B");
            if (!key.getKeyCode().toString(true).equals("07B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("07B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("F")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("F")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 8.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            // test major: F#
            key = Key.getKey("F#");
            if (!key.getKeyCode().toString(true).equals("02B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("02B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Gb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("F#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 9.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("Gb");
            if (!key.getKeyCode().toString(true).equals("02B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("02B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Gb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("F#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 9.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("02B");
            if (!key.getKeyCode().toString(true).equals("02B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("02B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Gb")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("F#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 9.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            // test major: G
            key = Key.getKey("G");
            if (!key.getKeyCode().toString(true).equals("09B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("09B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("G")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("G")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("09B");
            if (!key.getKeyCode().toString(true).equals("09B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("09B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("G")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("G")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            // test major: G#
            key = Key.getKey("G#");
            if (!key.getKeyCode().toString(true).equals("04B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("04B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Ab")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("G#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 11.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("Ab");
            if (!key.getKeyCode().toString(true).equals("04B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("04B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Ab")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("G#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 11.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("04B");
            if (!key.getKeyCode().toString(true).equals("04B")) fail("incorrect key code");
            if (!key.getKeyCode().toString().equals("04B")) fail("incorrect key code");
            if (!key.getKeyNotationFlat().equals("Ab")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("G#")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 11.0) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            // test shifts
            key = Key.getKey("G#+50");
            if (!key.getKeyCode().toString(true).equals("04B +50")) fail("incorrect key code: " + key.getKeyCode());
            if (!key.getKeyNotationFlat(true).equals("Ab ionian +50")) fail("incorrect key notation flat, is=" + key.getKeyNotationFlat(true));
            if (!key.getKeyNotationSharp(true).equals("G# ionian +50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != -0.5) fail("incorrect key value, is=" + key.getRootValue());
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("Ab +50");
            if (!key.getKeyCode().toString(true).equals("04B +50")) fail("incorrect key code");
            if (!key.getKeyNotationFlat(true).equals("Ab ionian +50")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp(true).equals("G# ionian +50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != -0.5) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("04B +50cents");
            if (!key.getKeyCode().toString(true).equals("04B +50")) fail("incorrect key code");
            if (!key.getKeyNotationFlat(true).equals("Ab ionian +50")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp(true).equals("G# ionian +50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != -0.5) fail("incorrect key value, is=" + key.getRootValue());
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("G#-50");
            if (!key.getKeyCode().toString(true).equals("04B -50")) fail("incorrect key code");
            if (!key.getKeyNotationFlat(true).equals("Ab ionian -50")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp(true).equals("G# ionian -50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.5) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("Ab -50");
            if (!key.getKeyCode().toString(true).equals("04B -50")) fail("incorrect key code");
            if (!key.getKeyNotationFlat(true).equals("Ab ionian -50")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp(true).equals("G# ionian -50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.5) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");
            
            key = Key.getKey("04B -50cents");
            if (!key.getKeyCode().toString(true).equals("04B -50")) fail("incorrect key code");
            if (!key.getKeyNotationFlat(true).equals("Ab ionian -50")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp(true).equals("G# ionian -50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.5) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            // test misc. things
            key = Key.getKey("    4b    -50   cents    ");
            if (!key.getKeyCode().toString(true).equals("04B -50")) fail("incorrect key code");
            if (!key.getKeyNotationFlat(true).equals("Ab ionian -50")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp(true).equals("G# ionian -50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.5) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("    g  #    -    50   cents    ");
            if (!key.getKeyCode().toString(true).equals("04B -50")) fail("incorrect key code: " + key.getKeyCode());
            if (!key.getKeyNotationFlat(true).equals("Ab ionian -50")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp(true).equals("G# ionian -50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != 10.5) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey("    g  #    +    50   cents    ");
            if (!key.getKeyCode().toString(true).equals("04B +50")) fail("incorrect key code: " + key.getKeyCode());
            if (!key.getKeyNotationFlat(true).equals("Ab ionian +50")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp(true).equals("G# ionian +50")) fail("incorrect key notation sharp");
            if (key.getRootValue() != -0.5f) fail("incorrect key value");
            if (key.isMinor()) fail("isMinor() is incorrect");

            key = Key.getKey(" ");
            if (!key.getKeyCode().toString(true).equals("")) fail("incorrect key code: " + key.getKeyCode());
            if (!key.getKeyNotationFlat().equals("")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("")) fail("incorrect key notation sharp");
            if (key.getRootValue() != Key.NO_KEY.getRootValue()) fail("incorrect key value");

            key = Key.getKey("");
            if (!key.getKeyCode().toString(true).equals("")) fail("incorrect key code: " + key.getKeyCode());
            if (!key.getKeyNotationFlat().equals("")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("")) fail("incorrect key notation sharp");
            if (key.getRootValue() != Key.NO_KEY.getRootValue()) fail("incorrect key value");

            key = Key.getKey("zyx");
            if (!key.getKeyCode().toString(true).equals("")) fail("incorrect key code: " + key.getKeyCode());
            if (!key.getKeyNotationFlat().equals("")) fail("incorrect key notation flat");
            if (!key.getKeyNotationSharp().equals("")) fail("incorrect key notation sharp");
            if (key.getRootValue() != Key.NO_KEY.getRootValue()) fail("incorrect key value");
            
        } catch (Exception e) {
            log.error("testKeyClassFunctionality(): an exception occurred: " + e);
            e.printStackTrace();
            fail("an exception occurred testing key class functionality");
        }
    }
    
}
