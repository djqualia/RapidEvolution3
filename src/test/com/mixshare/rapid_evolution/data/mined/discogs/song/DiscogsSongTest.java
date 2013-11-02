package test.com.mixshare.rapid_evolution.data.mined.discogs.song;

import java.util.Vector;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.mined.discogs.song.DiscogsSong;

public class DiscogsSongTest extends RE3TestCase {

	public void testEquals() {
		Vector<String> artists1 = new Vector<String>();
		artists1.add("Artist1");
		Vector<String> remixers1 = new Vector<String>();
		remixers1.add("Remix1");
		DiscogsSong track1 = new DiscogsSong("A1", "Title", "4:21", artists1, remixers1);
		DiscogsSong track1b = new DiscogsSong("A1", "Title", "4:21", artists1, remixers1);

		Vector<String> artists2 = new Vector<String>();
		artists1.add("Artist1");
		Vector<String> remixers2 = new Vector<String>();
		remixers1.add("Remix1");
		DiscogsSong track2 = new DiscogsSong("A2", "Title", "4:21", artists2, remixers2);		

		Vector<String> artists3 = new Vector<String>();
		Vector<String> remixers3 = new Vector<String>();
		DiscogsSong track3 = new DiscogsSong("A1", "Title", "4:21", artists3, remixers3);
		
		if (!track1.equals(track1b))
			fail("duplicate tracks not equal");

		if (track1.equals(track2))
			fail("tracks not duplicate");

		if (track1.equals(track3))
			fail("tracks not duplicate");
		
		if (track2.equals(track3))
			fail("tracks not duplicate");
	}
	
}
