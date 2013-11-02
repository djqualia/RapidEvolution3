package test.com.mixshare.rapid_evolution.data.identifier.search.song;

import java.util.Vector;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;

public class SongIdentifierTest extends RE3TestCase {

	private SongIdentifier getId1() {
		Vector<String> artists1 = new Vector<String>();
		artists1.add("Boards of Canada");
		return new SongIdentifier(artists1, SongIdentifier.getSongDescriptionFromTitleAndRemix("Aquarius", "Version 3"));				
	}
	
	private SongIdentifier getId2() {
		Vector<String> artists2 = new Vector<String>();
		artists2.add("Squarepusher");
		artists2.add("Aphex Twin");
		return new SongIdentifier(artists2, SongIdentifier.getSongDescriptionFromTitleAndRemix("Some Song", null));		
	}

	private SongIdentifier getId3() {
		Vector<String> artists2 = new Vector<String>();
		artists2.add("squarepusher");
		artists2.add("aphex twin");
		return new SongIdentifier(artists2, SongIdentifier.getSongDescriptionFromTitleAndRemix("some song", ""));		
	}

	private SongIdentifier getId4() {
		Vector<String> artists1 = new Vector<String>();
		artists1.add("Boards of Canada");
		return new SongIdentifier(artists1, SongIdentifier.getSongDescriptionFromTitleAndRemix("Aquarius (Version 3)", ""));				
	}

	private SongIdentifier getId5() {
		Vector<String> artists1 = new Vector<String>();
		return new SongIdentifier(artists1, SongIdentifier.getSongDescriptionFromTitleAndRemix("description only", ""));				
	}
	
	public void testGetUniqueId() {
		
		if (!getId1().getUniqueId().equals("song/Aquarius (Version 3)/artist/1"))		
			fail("getUniqueId() failed, value=" + getId1().getUniqueId());
		
		if (!getId2().getUniqueId().equals("song/Some Song/artist/2/artist/3"))		
			fail("getUniqueId() failed, value=" + getId2().getUniqueId());

		if (!getId3().getUniqueId().equals("song/some song/artist/2/artist/3"))		
			fail("getUniqueId() failed, value=" + getId3().getUniqueId());

		if (!getId4().getUniqueId().equals("song/Aquarius (Version 3)/artist/1"))		
			fail("getUniqueId() failed, value=" + getId4().getUniqueId());

		if (!getId5().getUniqueId().equals("song/description only"))		
			fail("getUniqueId() failed, value=" + getId5().getUniqueId());
		
	}
	
	public void testParseIdentifier() {
		
		getId1().getUniqueId(); // will trigger artist creation
		getId2().getUniqueId(); // will trigger artist creation
		getId5().getUniqueId();
		
		SongIdentifier id1 = SongIdentifier.parseIdentifier("song/Aquarius (Version 3)/artist/1");
		if (!id1.getSongDescription().equals("Aquarius (Version 3)"))
			fail("parseIdentifier() failed, incorrect title");
		if (!id1.getArtistDescription().equals("Boards of Canada"))
			fail("parseIdentifier() failed, incorrect artist description");
		
		SongIdentifier id2 = SongIdentifier.parseIdentifier("song/Some Song/artist/2/artist/3");
		if (!id2.getSongDescription().equals("Some Song"))
			fail("parseIdentifier() failed, incorrect title");
		if (!id2.getArtistDescription().equals("Aphex Twin, Squarepusher"))
			fail("parseIdentifier() failed, incorrect artist description");		
		
		SongIdentifier id5 = SongIdentifier.parseIdentifier("song/description only");
		if (!id5.getSongDescription().equals("description only"))
			fail("parseIdentifier() failed, incorrect title");		
		
	}
	
	public void testToString() {
		
		if (!getId1().toString().equals("Boards of Canada - Aquarius (Version 3)"))
			fail("toString() failed, is=" + getId1().toString());

		if (!getId4().toString().equals("Boards of Canada - Aquarius (Version 3)"))
			fail("toString() failed, is=" + getId2().toString());
		
		if (!getId2().toString().equals("Aphex Twin, Squarepusher - Some Song"))
			fail("toString() failed, is=" + getId3().toString());

		if (!getId3().toString().equals("Aphex Twin, Squarepusher - some song"))
			fail("toString() failed, is=" + getId3().toString());

		if (!getId5().toString().equals("description only"))
			fail("toString() failed, is=" + getId5().toString());
		
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
