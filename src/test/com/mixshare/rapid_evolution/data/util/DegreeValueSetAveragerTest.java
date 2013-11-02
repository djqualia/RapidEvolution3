package test.com.mixshare.rapid_evolution.data.util;

import java.util.Vector;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;

public class DegreeValueSetAveragerTest extends RE3TestCase {

    private static Logger log = Logger.getLogger(DegreeValueSetAveragerTest.class);
	
	public void testDegreeValueSetAverager() {
		
		DegreeValueSetAverager averager = new DegreeValueSetAverager();
		
		Vector<DegreeValue> set1 = getVector(new DegreeValue[] {
				new DegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Downtempo", 0.5f, DATA_SOURCE_UNKNOWN)
		});

		Vector<DegreeValue> set2 = getVector(new DegreeValue[] {
				new DegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("IDM", 0.75f, DATA_SOURCE_UNKNOWN)
		});
		
		Vector<DegreeValue> set3 = getVector(new DegreeValue[] {
				new DegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Downtempo", 0.75f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Electronica", 1.0f, DATA_SOURCE_UNKNOWN)				
		});
		
		averager.addDegreeValueSet(set1, 0.5f);
		averager.addDegreeValueSet(set2, 0.3f);
		averager.addDegreeValueSet(set3, 0.2f);
		
		Vector<DegreeValue> result = getVector(new DegreeValue[] {
				new DegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Downtempo", 0.4f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("IDM", 0.225f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Electronica", 0.2f, DATA_SOURCE_UNKNOWN)
		});		
		
		if (!areCollectionsEqual(result, averager.getDegrees(), true))
			fail("unexpected results for DegreeValueSetAverager=" + averager.getDegrees());
		
	}
	
	public void testDegreeValueSetAverager2() {
		
		DegreeValueSetAverager averager = new DegreeValueSetAverager();
		
		Vector<DegreeValue> set1 = getVector(new DegreeValue[] {
				new DegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Downtempo", 1.0f, DATA_SOURCE_UNKNOWN)
		});

		Vector<DegreeValue> set2 = getVector(new DegreeValue[] {
				new DegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("IDM", 1.0f, DATA_SOURCE_UNKNOWN)
		});
		
		Vector<DegreeValue> set3 = getVector(new DegreeValue[] {
				new DegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Downtempo", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Electronica", 1.0f, DATA_SOURCE_UNKNOWN)				
		});
		
		averager.addDegreeValueSet(set1, 1.0f);
		averager.addDegreeValueSet(set2, 1.0f);
		averager.addDegreeValueSet(set3, 1.0f);
		
		Vector<DegreeValue> result = getVector(new DegreeValue[] {
				new DegreeValue("Ambient", 1.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Downtempo", 2.0f / 3.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("Electronica", 1.0f / 3.0f, DATA_SOURCE_UNKNOWN),
				new DegreeValue("IDM", 1.0f / 3.0f, DATA_SOURCE_UNKNOWN)
		});		
		
		if (!areCollectionsEqual(result, averager.getDegrees(), true))
			fail("unexpected results for DegreeValueSetAverager=" + averager.getDegrees());
		
	}		
	
}
