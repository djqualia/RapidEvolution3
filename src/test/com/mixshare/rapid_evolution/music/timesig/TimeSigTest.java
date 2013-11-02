package test.com.mixshare.rapid_evolution.music.timesig;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.music.timesig.TimeSig;

public class TimeSigTest extends RE3TestCase {

	public void testIsCompatible() {
		TimeSig sig1 = TimeSig.getTimeSig(4,4);
		TimeSig sig2 = TimeSig.getTimeSig(4,4);
		if (!sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");

		sig1 = TimeSig.getTimeSig(3,4);
		sig2 = TimeSig.getTimeSig(3,4);
		if (!sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");

		sig1 = TimeSig.getTimeSig(4,4);
		sig2 = TimeSig.getTimeSig(8,4);
		if (!sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");
		
		sig1 = TimeSig.getTimeSig(4,4);
		sig2 = TimeSig.getTimeSig(8,8);
		if (!sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");
		
		sig1 = TimeSig.getTimeSig(3,4);
		sig2 = TimeSig.getTimeSig(6,8);
		if (!sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");
	
		// not compatible
		sig1 = TimeSig.getTimeSig(4,4);
		sig2 = TimeSig.getTimeSig(3,4);
		if (sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");

		sig1 = TimeSig.getTimeSig(4,4);
		sig2 = TimeSig.getTimeSig(6,8);
		if (sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");
		
		sig1 = TimeSig.getTimeSig(4,4);
		sig2 = TimeSig.getTimeSig(7,8);
		if (sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");
		
		sig1 = TimeSig.getTimeSig(8,8);
		sig2 = TimeSig.getTimeSig(3,8);
		if (sig1.isCompatibleWith(sig2))
			fail("isCompatible failed");				
		
	}
	
}
