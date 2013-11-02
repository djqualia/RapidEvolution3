package test.com.mixshare.rapid_evolution.data.index.filter.playlist;

import org.apache.log4j.Logger;

import test.RE3TestCase;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.filter.playlist.PlaylistIndex;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedCategoryPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedOrderedPlaylist;
import com.mixshare.rapid_evolution.util.io.LineReader;
import com.mixshare.rapid_evolution.util.io.LineReaderFactory;
import com.mixshare.rapid_evolution.util.io.LineWriter;
import com.mixshare.rapid_evolution.util.io.LineWriterFactory;

public class PlaylistIndexTest extends RE3TestCase {

    static private Logger log = Logger.getLogger(PlaylistIndexTest.class);    
	
	public void testCustomSerialization() {
		try {
			Database.getPlaylistIndex().add(new SubmittedCategoryPlaylist("test category"));
			Database.getPlaylistIndex().add(new SubmittedDynamicPlaylist("test dynamic"));
			Database.getPlaylistIndex().add(new SubmittedOrderedPlaylist("test ordered"));
			LineWriter writer = LineWriterFactory.getLineWriter("data/junit/temp/playlistindex.jso");			
			Database.getPlaylistIndex().write(writer);
			writer.close();
			LineReader lineReader = LineReaderFactory.getLineReader("data/junit/temp/playlistindex.jso");
			new PlaylistIndex(lineReader);
			lineReader.close();
		} catch (Exception e) {
			log.error("testCustomSerialization(): error", e);
			fail(e.getMessage());	
		}
	}
	
}
