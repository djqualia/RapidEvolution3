package test.com.mixshare.rapid_evolution.data.util;

import java.util.Vector;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.util.DegreeValue;

public class DegreeValueTest extends RE3TestCase {
	
	public void testDegreeValueSort() {
		DegreeValue value1 = new DegreeValue("1", 1.0f, (byte)0);
		DegreeValue value2 = new DegreeValue("3", 0.0f, (byte)0);
		DegreeValue value3 = new DegreeValue("2", 0.5f, (byte)0);
		Vector<DegreeValue> set = new Vector<DegreeValue>();
		set.add(value1);
		set.add(value2);
		set.add(value3);
		java.util.Collections.sort(set);
		if (set.get(0).getPercentage() != 1.0f)
			fail("sorted backwards");
		if (set.get(1).getPercentage() != 0.5f)
			fail("sorted incorrecgtly");
		if (set.get(2).getPercentage() != 0.0f)
			fail("sorted backwards");
	}

}
