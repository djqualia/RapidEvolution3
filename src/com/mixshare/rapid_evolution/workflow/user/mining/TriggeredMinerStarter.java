package com.mixshare.rapid_evolution.workflow.user.mining;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.discogs.artist.DiscogsArtistProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.label.DiscogsLabelProfile;
import com.mixshare.rapid_evolution.data.mined.discogs.release.DiscogsReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.Task;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.mining.bbc.BBCArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.billboard.BillboardArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.billboard.BillboardSongFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.discogs.artist.DiscogsArtistFetchResultTrigger;
import com.mixshare.rapid_evolution.workflow.mining.discogs.artist.DiscogsArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.discogs.artist.DiscogsArtistReleaseFetchStateManager;
import com.mixshare.rapid_evolution.workflow.mining.discogs.label.DiscogsLabelFetchResultTrigger;
import com.mixshare.rapid_evolution.workflow.mining.discogs.label.DiscogsLabelFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.discogs.label.DiscogsLabelReleaseFetchStateManager;
import com.mixshare.rapid_evolution.workflow.mining.discogs.release.DiscogsReleaseProfileFetchResultTrigger;
import com.mixshare.rapid_evolution.workflow.mining.discogs.release.DiscogsReleaseProfileFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.discogs.release.DiscogsReleaseRecommendationsFetchStateManager;
import com.mixshare.rapid_evolution.workflow.mining.echonest.EchonestArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.echonest.EchonestSongFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.idiomag.IdiomagArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.lastfm.LastfmArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.lastfm.LastfmReleaseFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.lastfm.LastfmSongFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.lyricsfly.LyricsflySongFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.lyricwiki.LyricwikiSongFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.musicbrainz.MusicbrainzArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.musicbrainz.MusicbrainzLabelFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.musicbrainz.MusicbrainzReleaseFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.musicbrainz.MusicbrainzSongFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.yahoo.YahooArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.yahoo.YahooSongFetchTask;
import com.mixshare.rapid_evolution.workflow.user.tags.TagReadTask;

public class TriggeredMinerStarter extends CommonTask implements DataConstants {

    static private final long serialVersionUID = 0L;
    static private Logger log = Logger.getLogger(TriggeredMinerStarter.class);

	static private Vector<Task> currentFetchTasks = new Vector<Task>();

	private Vector<SearchProfile> searchProfiles = new Vector<SearchProfile>();

	public TriggeredMinerStarter(SearchProfile searchProfile) {
		searchProfiles.add(searchProfile);
	}
	public TriggeredMinerStarter(SongProfile songProfile) {
		searchProfiles.add(songProfile);
		for (ArtistRecord artist : songProfile.getArtists()) {
			ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(artist.getUniqueId());
			if (artistProfile != null)
				searchProfiles.add(artistProfile);
		}
	}
	public TriggeredMinerStarter(Vector<SearchProfile> searchProfiles) {
		this.searchProfiles = searchProfiles;
	}

	@Override
	public String toString() { return "Triggering data miners for profiles " + searchProfiles; }

	@Override
	public void execute() {
		try {
			if (!RE3Properties.getBoolean("enable_triggered_data_miners"))
				return;
			if (!RE3Properties.getBoolean("enable_data_miners"))
				return;
			if (searchProfiles.size() == 0)
				return;
			for (Task task : currentFetchTasks)
				TaskManager.removeBackgroundTask(task);
			currentFetchTasks.clear();
			for (SearchProfile searchProfile : searchProfiles) {
				if (searchProfile instanceof SongProfile) {
					SongProfile song = (SongProfile)searchProfile;
					if (!song.hasMinedProfile(DATA_SOURCE_LASTFM) && RE3Properties.getBoolean("enable_lastfm_data_miners"))
						currentFetchTasks.add(new LastfmSongFetchTask(song, RE3Properties.getInt("lastfm_mining_task_priority") + 5));
					if (!song.hasMinedProfile(DATA_SOURCE_ECHONEST) && RE3Properties.getBoolean("enable_echonest_data_miners"))
						currentFetchTasks.add(new EchonestSongFetchTask(song, RE3Properties.getInt("echonest_mining_task_priority") + 5));
					if (!song.hasMinedProfile(DATA_SOURCE_MUSICBRAINZ) && RE3Properties.getBoolean("enable_musicbrainz_data_miners"))
						currentFetchTasks.add(new MusicbrainzSongFetchTask(song, RE3Properties.getInt("musicbrainz_mining_task_priority") + 5));
					if (!song.hasMinedProfile(DATA_SOURCE_BILLBOARD) && RE3Properties.getBoolean("enable_billboard_data_miners"))
						currentFetchTasks.add(new BillboardSongFetchTask(song, RE3Properties.getInt("billboard_mining_task_priority") + 5));
					if (!song.hasMinedProfile(DATA_SOURCE_YAHOO) && RE3Properties.getBoolean("enable_yahoo_data_miners"))
						currentFetchTasks.add(new YahooSongFetchTask(song, RE3Properties.getInt("yahoo_mining_task_priority") + 5));
					if (!song.hasMinedProfile(DATA_SOURCE_LYRICSFLY) && RE3Properties.getBoolean("enable_lyricsfly_data_miners"))
						currentFetchTasks.add(new LyricsflySongFetchTask(song, RE3Properties.getInt("lyricsfly_mining_task_priority") + 5));
					if (!song.hasMinedProfile(DATA_SOURCE_LYRICWIKI) && RE3Properties.getBoolean("enable_lyricwiki_data_miners"))
						currentFetchTasks.add(new LyricwikiSongFetchTask(song, RE3Properties.getInt("lyricwiki_mining_task_priority") + 5));
					Vector<SongRecord> songs = new Vector<SongRecord>(1);
					songs.add(song.getSongRecord());
					if (RE3Properties.getBoolean("read_tags_automatically_when_opening_song_profiles"))
						currentFetchTasks.add(new TagReadTask(songs, RE3Properties.getInt("default_task_priority") + 6, false));
				} else if (searchProfile instanceof ArtistProfile) {
					ArtistProfile artist = (ArtistProfile)searchProfile;
					if (!artist.hasMinedProfile(DATA_SOURCE_LASTFM) && RE3Properties.getBoolean("enable_lastfm_data_miners"))
						currentFetchTasks.add(new LastfmArtistFetchTask(artist, RE3Properties.getInt("lastfm_mining_task_priority") + 5));
					if (!artist.hasMinedProfile(DATA_SOURCE_DISCOGS) && RE3Properties.getBoolean("enable_discogs_data_miners"))
						currentFetchTasks.add(new DiscogsArtistFetchTask(artist, RE3Properties.getInt("discogs_mining_task_priority") + 5, new DiscogsArtistFetchResultTrigger(artist)));
					DiscogsArtistProfile discogsArtistProfile = (DiscogsArtistProfile)artist.getMinedProfile(DATA_SOURCE_DISCOGS);
					if (discogsArtistProfile != null)
						if (discogsArtistProfile.getLastFetchedReleaseIds() == 0)
							currentFetchTasks.add(new DiscogsArtistReleaseFetchStateManager(artist, discogsArtistProfile, RE3Properties.getInt("discogs_mining_task_priority") + 5));
					if (!artist.hasMinedProfile(DATA_SOURCE_ECHONEST) && RE3Properties.getBoolean("enable_echonest_data_miners"))
						currentFetchTasks.add(new EchonestArtistFetchTask(artist, RE3Properties.getInt("echonest_mining_task_priority") + 5));
					if (!artist.hasMinedProfile(DATA_SOURCE_IDIOMAG) && RE3Properties.getBoolean("enable_idiomag_data_miners"))
						currentFetchTasks.add(new IdiomagArtistFetchTask(artist, RE3Properties.getInt("idiomag_mining_task_priority") + 5));
					if (!artist.hasMinedProfile(DATA_SOURCE_MUSICBRAINZ) && RE3Properties.getBoolean("enable_musicbrainz_data_miners"))
						currentFetchTasks.add(new MusicbrainzArtistFetchTask(artist, RE3Properties.getInt("musicbrainz_mining_task_priority") + 5));
					if (!artist.hasMinedProfile(DATA_SOURCE_BILLBOARD) && RE3Properties.getBoolean("enable_billboard_data_miners"))
						currentFetchTasks.add(new BillboardArtistFetchTask(artist, RE3Properties.getInt("billboard_mining_task_priority") + 5));
					if (!artist.hasMinedProfile(DATA_SOURCE_BBC) && RE3Properties.getBoolean("enable_bbc_data_miners"))
						currentFetchTasks.add(new BBCArtistFetchTask(artist, RE3Properties.getInt("bbc_mining_task_priority") + 5));
					if (!artist.hasMinedProfile(DATA_SOURCE_YAHOO) && RE3Properties.getBoolean("enable_yahoo_data_miners"))
						currentFetchTasks.add(new YahooArtistFetchTask(artist, RE3Properties.getInt("yahoo_mining_task_priority") + 5));
				} else if (searchProfile instanceof LabelProfile) {
					LabelProfile label = (LabelProfile)searchProfile;
					if (!label.hasMinedProfileHeader(DATA_SOURCE_DISCOGS) && RE3Properties.getBoolean("enable_discogs_data_miners"))
						currentFetchTasks.add(new DiscogsLabelFetchTask(label, RE3Properties.getInt("discogs_mining_task_priority") + 5, new DiscogsLabelFetchResultTrigger(label)));
					DiscogsLabelProfile discogsLabelProfile = (DiscogsLabelProfile)label.getMinedProfile(DATA_SOURCE_DISCOGS);
					if (discogsLabelProfile != null)
						if (discogsLabelProfile.getLastFetchedReleaseIds() == 0)
							currentFetchTasks.add(new DiscogsLabelReleaseFetchStateManager(label, discogsLabelProfile, RE3Properties.getInt("discogs_mining_task_priority") + 4));
					if (!label.hasMinedProfileHeader(DATA_SOURCE_MUSICBRAINZ) && RE3Properties.getBoolean("enable_musicbrainz_data_miners"))
						currentFetchTasks.add(new MusicbrainzLabelFetchTask(label, RE3Properties.getInt("musicbrainz_mining_task_priority") + 5));
				} else if (searchProfile instanceof ReleaseProfile) {
					ReleaseProfile release = (ReleaseProfile)searchProfile;
					if (!release.hasMinedProfileHeader(DATA_SOURCE_DISCOGS) && RE3Properties.getBoolean("enable_discogs_data_miners"))
						currentFetchTasks.add(new DiscogsReleaseProfileFetchTask(release, new DiscogsReleaseProfileFetchResultTrigger(release), RE3Properties.getInt("discogs_mining_task_priority") + 5));
					if (!release.hasMinedProfileHeader(DATA_SOURCE_LASTFM) && RE3Properties.getBoolean("enable_lastfm_data_miners"))
						currentFetchTasks.add(new LastfmReleaseFetchTask(release, RE3Properties.getInt("lastfm_mining_task_priority") + 5));
					if (!release.hasMinedProfileHeader(DATA_SOURCE_MUSICBRAINZ) && RE3Properties.getBoolean("enable_musicbrainz_data_miners"))
						currentFetchTasks.add(new MusicbrainzReleaseFetchTask(release, RE3Properties.getInt("musicbrainz_mining_task_priority") + 5));
					DiscogsReleaseProfile discogsReleaseProfile = (DiscogsReleaseProfile)release.getMinedProfile(DATA_SOURCE_DISCOGS);
					if (discogsReleaseProfile != null)
						if (discogsReleaseProfile.getLastFetchedRecommendedReleases() == 0)
							currentFetchTasks.add(new DiscogsReleaseRecommendationsFetchStateManager(release, discogsReleaseProfile, RE3Properties.getInt("discogs_mining_task_priority") + 4));
				}
			}
			for (Task task : currentFetchTasks)
				TaskManager.runBackgroundTask(task);
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}

	@Override
	public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }

	@Override
	public boolean isIndefiniteTask() { return true; }

}

