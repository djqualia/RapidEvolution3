package com.mixshare.rapid_evolution.workflow.mining.idiomag;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.idiomag.artist.IdiomagArtistProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class IdiomagArtistFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;    
    static private Logger log = Logger.getLogger(IdiomagArtistFetchTask.class);    
	
    ////////////
    // FIELDS //
    ////////////
    
	private ArtistProfile artistProfile;
	private IdiomagArtistProfile idiomagProfile;
	
	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public IdiomagArtistFetchTask() { }
	
	public IdiomagArtistFetchTask(ArtistProfile artistProfile, int taskPriority) {
		this.artistProfile = artistProfile;
		this.taskPriority = taskPriority;
	}
	
	/////////////
	// GETTERS //
	/////////////
	
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_IDIOMAG);
	}
	
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("idiomag_artist_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}	
	
	public Object getResult() {
		if ((idiomagProfile != null) && (idiomagProfile.isValid()))
			return idiomagProfile;
		return null;
	}
	
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getIdiomagAPI(); }
	
	/////////////
	// METHODS //
	/////////////

	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		artistProfile = (ArtistProfile)profile;
	}
		
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching idiomag artist profile=" + artistProfile);
		try {
			idiomagProfile = (IdiomagArtistProfile)MiningAPIFactory.getIdiomagAPI().getArtistProfile(artistProfile);		
			if ((idiomagProfile != null) && idiomagProfile.isValid()) {			
				
				if (!artistProfile.isExternalItem() && RE3Properties.getBoolean("automatically_add_external_items")) {
					// similar artists
					for (String artistName : idiomagProfile.getSimilarArtistNames()) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						float similarity = idiomagProfile.getSimilarityWith(artistName);
						if (similarity >= RE3Properties.getFloat("idiomag_min_artist_similarity_to_fetch")) {																		
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
				
				artistProfile.addMinedProfile(idiomagProfile);
				artistProfile.save();
			} else {
				artistProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_IDIOMAG), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for idiomag");
		}	
			
	}	
	
	public String toString() { return "IdiomagArtistFetchTask()=" + artistProfile; }
	
}
