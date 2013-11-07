package test.com.mixshare.rapid_evolution.data.identifier.search.release;

import java.util.Vector;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;

public class ReleaseIdentifierTest extends RE3TestCase {

	private ReleaseIdentifier getId1() {
		Vector<String> artists1 = new Vector<String>();
		artists1.add("Boards of Canada");
		return new ReleaseIdentifier(artists1, "Twoism");				
	}
	
	private ReleaseIdentifier getId2() {
		Vector<String> artists2 = new Vector<String>();
		artists2.add("Squarepusher");
		artists2.add("Aphex Twin");
		return new ReleaseIdentifier(artists2, "Some Collab");		
	}

	private ReleaseIdentifier getId3() {
		Vector<String> artists2 = new Vector<String>();
		artists2.add("squarepusher");
		artists2.add("aphex twin");
		return new ReleaseIdentifier(artists2, "some collab");		
	}
	
	public void testGetUniqueId() {
		
		if (!getId1().getUniqueId().equals("release/Twoism/artist/1"))		
			fail("getUniqueId() failed, is=" + getId1().getUniqueId());
		
		if (!getId2().getUniqueId().equals("release/Some Collab/artist/2/artist/3"))		
			fail("getUniqueId() failed");
		
	}
	
	public void testParseIdentifier() {
		
		getId1().getUniqueId(); // will trigger artist creation
		getId2().getUniqueId(); // will trigger artist creation
		
		ReleaseIdentifier id1 = ReleaseIdentifier.parseIdentifier("release/Twoism/artist/1");
		if (!id1.getReleaseTitle().equals("Twoism"))
			fail("parseIdentifier() failed, incorrect album");
		if (!id1.getArtistDescription().equals("Boards of Canada"))
			fail("parseIdentifier() failed, incorrect artist description, is=" + id1.getArtistDescription());
		
		ReleaseIdentifier id2 = ReleaseIdentifier.parseIdentifier("release/Some Collab/artist/2/artist/3");
		if (!id2.getReleaseTitle().equals("Some Collab"))
			fail("parseIdentifier() failed, incorrect album");
		if (!id2.getArtistDescription().equals("Aphex Twin, Squarepusher"))
			fail("parseIdentifier() failed, incorrect artist description, is=" + id2.getArtistDescription());		
		
	}
	
	public void testToString() {
		
		if (!getId1().toString().equals("Boards of Canada - Twoism"))
			fail("toString() failed");

		if (!getId2().toString().equals("Aphex Twin, Squarepusher - Some Collab"))
			fail("toString() failed");
		
	}
	
	public void testEquals() {
				
		if (!getId2().equals(getId3()))
			fail("equals() failed");
		
		if (getId2().equals(getId1()))
			fail("equals() failed");
		
	}

	public void testHashCode() {
		
		if (getId2().hashCode() != getId3().hashCode())
			fail("hashCode() failed");
		
		if (getId2().hashCode() == getId1().hashCode())
			fail("hashCode() failed");
		
	}
}
