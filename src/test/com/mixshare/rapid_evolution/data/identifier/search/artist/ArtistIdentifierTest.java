package test.com.mixshare.rapid_evolution.data.identifier.search.artist;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;

public class ArtistIdentifierTest extends RE3TestCase {

	public void testGetUniqueId() {
		ArtistIdentifier id = new ArtistIdentifier("Aphex Twin");
		if (!id.getUniqueId().equals("artist/Aphex Twin"))
			fail("getUniqueId() failed, is=" + id.getUniqueId());
	}
	
	public void testParseIdentifier() {
		
		if (!ArtistIdentifier.parseIdentifier("artist/Aphex Twin").getName().equals("Aphex Twin"))
			fail("parseIdentifier() failed");
		
	}
	
	public void testToString() {
		
		if (!new ArtistIdentifier("Aphex Twin").toString().equals("Aphex Twin"))
			fail("toString() failed");
		
	}
	
	public void testEquals() {
		
		ArtistIdentifier id1 = new ArtistIdentifier("Aphex Twin");
		ArtistIdentifier id2 = new ArtistIdentifier("aphex twin");
		ArtistIdentifier id3 = new ArtistIdentifier("boards of canada");
		
		if (!id1.equals(id2))
			fail("equals() failed");
		
		if (id1.equals(id3))
			fail("equals() failed");
		
	}

	public void testHashCode() {
		
		ArtistIdentifier id1 = new ArtistIdentifier("Aphex Twin");
		ArtistIdentifier id2 = new ArtistIdentifier("aphex twin");
		ArtistIdentifier id3 = new ArtistIdentifier("boards of canada");
		
		if (id1.hashCode() != id2.hashCode())
			fail("hashCode() failed");
		
		if (id1.hashCode() == id3.hashCode())
			fail("hashCode() failed");
		
	}
	
}
