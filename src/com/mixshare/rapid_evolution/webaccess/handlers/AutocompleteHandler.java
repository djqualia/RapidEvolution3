package com.mixshare.rapid_evolution.webaccess.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.autocomplete.ArtistAutoCompleter;
import com.mixshare.rapid_evolution.data.search.autocomplete.LabelAutoCompleter;
import com.mixshare.rapid_evolution.data.search.autocomplete.ReleaseTitleAutoCompleter;
import com.mixshare.rapid_evolution.data.search.autocomplete.SongTitleAutoCompleter;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;

public class AutocompleteHandler extends AbstractHandler implements IndexChangeListener {

	static private Logger log = Logger.getLogger(AutocompleteHandler.class);

	static private ArtistAutoCompleter cachedArtistCompleter;
	static private LabelAutoCompleter cachedLabelCompleter;
	static private ReleaseTitleAutoCompleter cachedReleaseCompleter;
	static private SongTitleAutoCompleter cachedSongCompleter;

	public AutocompleteHandler() {
    	Database.getArtistIndex().addIndexChangeListener(this);
    	Database.getLabelIndex().addIndexChangeListener(this);
    	Database.getReleaseIndex().addIndexChangeListener(this);
    	Database.getSongIndex().addIndexChangeListener(this);		
	}
	
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
    	try {
	    	if (request.getPathInfo().startsWith("/autocomplete")) {	
	    		((Request)request).setHandled(true);	    		    		
	    		String query = request.getParameter("q");
	    		if (cachedArtistCompleter == null)
	    			cachedArtistCompleter = new ArtistAutoCompleter();
	    		String match = cachedArtistCompleter.getFirstMatch(query);
	    		if (match.equals("")) {
	    			if (cachedReleaseCompleter == null)
	    				cachedReleaseCompleter = new ReleaseTitleAutoCompleter();
	    			match = cachedReleaseCompleter.getFirstMatch(query);
	    			if (match.equals("")) {
		    			if (cachedSongCompleter == null)
		    				cachedSongCompleter = new SongTitleAutoCompleter();
		    			match = cachedSongCompleter.getFirstMatch(query);
		    			if (match.equals("")) {
    		    			if (cachedLabelCompleter == null)
    		    				cachedLabelCompleter = new LabelAutoCompleter();
    		    			match = cachedLabelCompleter.getFirstMatch(query);		    		    				
		    			}
	    			}
	    		}
	    		response.getWriter().println(match);
	    	}
    	} catch (Exception e) {
    		log.error("handle(): error getting views", e);
    	}
    }

	public void addedRecord(Record record, SubmittedProfile submittedProfile) { invalidateCompleterCache(record); }
	public void updatedRecord(Record record) { invalidateCompleterCache(record); }
	public void removedRecord(Record record) { invalidateCompleterCache(record); }
	
	private void invalidateCompleterCache(Record record) {
		if (record instanceof ArtistRecord)
			cachedArtistCompleter = null;
		else if (record instanceof LabelRecord)
			cachedLabelCompleter = null;
		else if (record instanceof ReleaseRecord)
			cachedReleaseCompleter = null;
		else if (record instanceof SongRecord)
			cachedSongCompleter = null;		
	}

}
