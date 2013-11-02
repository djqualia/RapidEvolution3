package com.mixshare.rapid_evolution.workflow.mining.lastfm;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfileHeader;
import com.mixshare.rapid_evolution.data.mined.MiningAPIFactory;
import com.mixshare.rapid_evolution.data.mined.lastfm.artist.LastfmArtistProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.release.LastfmReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.lastfm.song.LastfmSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.MiningLimitReachedException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.workflow.TaskResultListener;
import com.mixshare.rapid_evolution.workflow.mining.MiningTask;

public class LastfmArtistFetchTask extends MiningTask {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(LastfmArtistFetchTask.class);

    ////////////
    // FIELDS //
    ////////////

	private ArtistProfile artistProfile;
	private LastfmArtistProfile lastfmProfile;

	/////////////////
	// CONSTRUCTOR //
	/////////////////

	public LastfmArtistFetchTask() { }

	public LastfmArtistFetchTask(ArtistProfile artistProfile, int taskPriority) {
		this.artistProfile = artistProfile;
		this.taskPriority = taskPriority;
	}

	/////////////
	// GETTERS //
	/////////////

	@Override
	public MinedProfileHeader getMinedProfileHeader() {
		return new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_LASTFM);
	}

	@Override
	public long getMinimumTimeBetweenQueries() {
		return RE3Properties.getLong("lastfm_artist_minimum_query_interval_days") * 1000 * 60 * 60 * 24;
	}

	@Override
	public Object getResult() {
		if ((lastfmProfile != null) && (lastfmProfile.isValid()))
			return lastfmProfile;
		return null;
	}

	@Override
	public CommonMiningAPIWrapper getAPIWrapper() { return MiningAPIFactory.getLastfmAPI(); }

	/////////////
	// METHODS //
	/////////////

	@Override
	public void init(TaskResultListener resultListener, SearchProfile profile) {
		setTaskResultListener(resultListener);
		artistProfile = (ArtistProfile)profile;
	}

	@Override
	public void execute() {
		if (log.isDebugEnabled())
			log.debug("execute(): fetching lastfm artist profile=" + artistProfile);
		try {
			lastfmProfile = (LastfmArtistProfile)MiningAPIFactory.getLastfmAPI().getArtistProfile(artistProfile);
			if ((lastfmProfile != null) && (lastfmProfile.isValid())) {

				if (!artistProfile.isExternalItem() && RE3Properties.getBoolean("automatically_add_external_items")) {
					// similar artists
					for (String artistName : lastfmProfile.getSimilarArtistNames()) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						float similarity = lastfmProfile.getSimilarityWith(artistName);
						if (similarity >= RE3Properties.getFloat("lastfm_min_artist_similarity_to_fetch")) {
							ArtistIdentifier artistId = new ArtistIdentifier(artistName);
							if (!Database.getArtistIndex().doesExist(artistId)) {
								try {
									SubmittedArtist submittedArtist = new SubmittedArtist(artistName);
									submittedArtist.setExternalItem(true);
									Database.getArtistIndex().add(submittedArtist);
								} catch (Exception e) {
									log.error("execute(): error adding external similar artist", e);
								}
							}
						}
					}

					// releases
					for (LastfmReleaseProfile release : lastfmProfile.getReleases()) {
						if (release.getArtistDescription().equalsIgnoreCase(artistProfile.getLastfmArtistName())) {
							if (RapidEvolution3.isTerminated || isCancelled())
								return;
							SubmittedRelease submittedRelease = new SubmittedRelease(release.getArtistDescription(), release.getReleaseTitle());
							submittedRelease.setExternalItem(true);
							submittedRelease.addMinedProfile(release);
							try {
								Database.getReleaseIndex().addOrUpdate(submittedRelease);
							} catch (Exception e) {
								log.error("execute(): error adding/updating external release=" + submittedRelease, e);
							}
						}
					}

					// songs
					for (LastfmSongProfile song : lastfmProfile.getSongs()) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						SubmittedSong submittedSong = new SubmittedSong(song.getArtistName(), song.getReleaseName(), song.getReleaseTrack(), song.getSongTitle(), "");
						submittedSong.setExternalItem(true);
						submittedSong.addMinedProfile(song);
						try {
							Database.getSongIndex().addOrUpdate(submittedSong);
						} catch (Exception e) {
							log.error("execute(): error adding/updating external song=" + submittedSong, e);
						}
					}
				}

				// lastfm profile
				artistProfile.addMinedProfile(lastfmProfile);
				artistProfile.save();

			} else {
				artistProfile.addMinedProfileHeader(new MinedProfileHeader(DATA_TYPE_ARTISTS, DATA_SOURCE_LASTFM), System.currentTimeMillis() - getMinimumTimeBetweenQueries() + RE3Properties.getInt("data_mining_error_delay_minutes") * 1000 * 60);
			}
		} catch (MiningLimitReachedException mlre) {
			log.warn("execute(): mining limit reached for lastfm");
		}
	}

	@Override
	public String toString() { return "LastfmArtistFetchTask()=" + artistProfile; }

}
