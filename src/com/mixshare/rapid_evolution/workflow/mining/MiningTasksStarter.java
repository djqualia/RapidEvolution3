package com.mixshare.rapid_evolution.workflow.mining;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.mining.bbc.BBCArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.billboard.BillboardArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.billboard.BillboardSongFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.discogs.artist.DiscogsArtistFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.discogs.artist.DiscogsArtistReleaseFetchStateManager;
import com.mixshare.rapid_evolution.workflow.mining.discogs.label.DiscogsLabelFetchTask;
import com.mixshare.rapid_evolution.workflow.mining.discogs.label.DiscogsLabelReleaseFetchStateManager;
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

public class MiningTasksStarter {

	static public void start() {
		if (RE3Properties.getBoolean("enable_data_miners")) {
			// lastfm
			TaskManager.runBackgroundTask(new MiningStateManager(new LastfmSongFetchTask(), "enable_lastfm_data_miners"));
			TaskManager.runBackgroundTask(new MiningStateManager(new LastfmArtistFetchTask(), "enable_lastfm_data_miners"));
			TaskManager.runBackgroundTask(new MiningStateManager(new LastfmReleaseFetchTask(), "enable_lastfm_data_miners"));

			// discogs
			TaskManager.runBackgroundTask(new MiningStateManager(new DiscogsArtistFetchTask(), "enable_discogs_data_miners"));
			TaskManager.runBackgroundTask(new MiningStateManager(new DiscogsLabelFetchTask(), "enable_discogs_data_miners"));
			TaskManager.runBackgroundTask(new DiscogsArtistReleaseFetchStateManager());
			TaskManager.runBackgroundTask(new DiscogsLabelReleaseFetchStateManager());
			TaskManager.runBackgroundTask(new DiscogsReleaseRecommendationsFetchStateManager());

			// echonest
			TaskManager.runBackgroundTask(new MiningStateManager(new EchonestArtistFetchTask(), "enable_echonest_data_miners"));
			if (RE3Properties.getBoolean("enable_echonest_song_data_miners"))
				TaskManager.runBackgroundTask(new MiningStateManager(new EchonestSongFetchTask(), "enable_echonest_data_miners"));

			// musicbrainz
			TaskManager.runBackgroundTask(new MiningStateManager(new MusicbrainzArtistFetchTask(), "enable_musicbrainz_data_miners"));
			TaskManager.runBackgroundTask(new MiningStateManager(new MusicbrainzLabelFetchTask(), "enable_musicbrainz_data_miners"));
			TaskManager.runBackgroundTask(new MiningStateManager(new MusicbrainzReleaseFetchTask(), "enable_musicbrainz_data_miners"));
			TaskManager.runBackgroundTask(new MiningStateManager(new MusicbrainzSongFetchTask(), "enable_musicbrainz_data_miners"));

			// lyricsfly
			TaskManager.runBackgroundTask(new MiningStateManager(new LyricsflySongFetchTask(), "enable_lyricsfly_data_miners"));

			// lyricwiki
			TaskManager.runBackgroundTask(new MiningStateManager(new LyricwikiSongFetchTask(), "enable_lyricwiki_data_miners"));

			// bbc
			TaskManager.runBackgroundTask(new MiningStateManager(new BBCArtistFetchTask(), "enable_bbc_data_miners"));

			// billboard
			TaskManager.runBackgroundTask(new MiningStateManager(new BillboardArtistFetchTask(), "enable_billboard_data_miners"));
			TaskManager.runBackgroundTask(new MiningStateManager(new BillboardSongFetchTask(), "enable_billboard_data_miners"));

			// idiomag
			TaskManager.runBackgroundTask(new MiningStateManager(new IdiomagArtistFetchTask(), "enable_idiomag_data_miners"));

			// yahoo
			TaskManager.runBackgroundTask(new MiningStateManager(new YahooArtistFetchTask(), "enable_yahoo_data_miners"));
			TaskManager.runBackgroundTask(new MiningStateManager(new YahooSongFetchTask(), "enable_yahoo_data_miners"));
		}
	}

}
