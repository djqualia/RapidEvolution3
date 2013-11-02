package test.com.mixshare.rapid_evolution.data.identifier.search.label;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;

public class LabelIdentifierTest extends RE3TestCase {

	public void testGetUniqueId() {
		
		if (!new LabelIdentifier("Border Community").getUniqueId().equals("label/Border Community"))		
			fail("getUniqueId() failed");
				
	}
	
	public void testParseIdentifier() {
		
		if (!LabelIdentifier.parseIdentifier("label/Border Community").getName().equals("Border Community"))
			fail("parseIdentifier() failed");
		
	}
	
	public void testToString() {
		
		if (!new LabelIdentifier("Border Community").toString().equals("Border Community"))
			fail("toString() failed");
		
	}
	
	public void testEquals() {
		
		LabelIdentifier id1 = new LabelIdentifier("Border Community");
		LabelIdentifier id2 = new LabelIdentifier("border community");
		LabelIdentifier id3 = new LabelIdentifier("rephlex");
		
		if (!id1.equals(id2))
			fail("equals() failed");
		
		if (id1.equals(id3))
			fail("equals() failed");
		
	}

	public void testHashCode() {
		
		LabelIdentifier id1 = new LabelIdentifier("Border Community");
		LabelIdentifier id2 = new LabelIdentifier("border community");
		LabelIdentifier id3 = new LabelIdentifier("rephlex");
		
		if (id1.hashCode() != id2.hashCode())
			fail("hashCode() failed");
		
		if (id1.hashCode() == id3.hashCode())
			fail("hashCode() failed");
		
	}
	
}
