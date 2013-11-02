package com.mixshare.rapid_evolution.workflow.mining.yahoo;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.mined.yahoo.artist.YahooArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class YahooArtistFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(YahooArtistFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private ArtistProfile artistProfile;
	private YahooArtistProfile yahooProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public YahooArtistFetchTask() { }
	
	public YahooArtistFetchTask(ArtistProfile artistProfile, int taskPriority) {
		this.artistProfile = artistProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_YAHOO);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("yahoo_artist_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((yahooProfile != null) && (yahooProfile.isValid()))
			return yahooProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getYahoomusicAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		artistProfile = (ArtistProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching yahoo artist profile=" + artistProfile);
		try {
			yahooProfile = (YahooArtistProfile)MiningAPIFactory.getYahoomusicAPI().getArtistProfile(artistProfile);		
			if ((yahooProfile != null) && yahooProfile.isValid()) {
				
				if (!artistProfile.isExternalItem()) {			
					// similar artists
					for (String artistName : yahooProfile.getSimilarArtistNames()) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						float similarity = yahooProfile.getSimilarityWith(artistName);
						if (similarity >= RE3Properties.getFloat("yahoo_min_artist_similarity_to_fetch")) {																											
							ArtistIdentifier artistId = new ArtistIdentifier(artistName);
							if (!Database.getArtistIndex().doesExist(artistId)) {
								try {
									SubmittedArtist submittedArtist = new SubmittedArtist(artistName);
									submittedArtist.setExternalItem(true);
									Database.getArtistIndex().add(submittedArtist);
								} catch (Exception e) {
									log.error("execute(): error", e);
								}
							}
						}
					}
				}
				
				artistProfile.addMinedProfile(yahooProfile);
				artistProfile.save();
			} else {
				artistProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_YAHOO), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for yahoo");
		}							
	}	
	
	public String toString() { return "YahooArtistFetchTask()=" + artistProfile; }
	
}
