package com.mixshare.rapid_evolution.data.mined;

import com.mixshare.rapid_evolution.data.mined.bbc.BBCAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.billboard.BillboardAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.discogs.DiscogsAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.echonest.EchonestAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.idiomag.IdiomagAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.lastfm.LastfmAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.lyricsfly.LyricsFlyAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.lyricwiki.LyricwikiAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.MusicbrainzAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.yahoo.YahooMusicAPIWrapper;

public class MiningAPIFactory {

	static private BBCAPIWrapper bbcAPI;
	static private BillboardAPIWrapper billboardAPI;
	static private DiscogsAPIWrapper discogsAPI;
	static private EchonestAPIWrapper echonestAPI;
	static private IdiomagAPIWrapper idiomagAPI;
	static private LastfmAPIWrapper lastfmAPI;
	static private LyricsFlyAPIWrapper lyricsflyAPI;
	static private LyricwikiAPIWrapper lyricwikiAPI;
	static private MusicbrainzAPIWrapper musicbrainzAPI;
	static private YahooMusicAPIWrapper yahoomusicAPI;

	static public BBCAPIWrapper getBBCAPI() {
		if (bbcAPI == null)
			bbcAPI = new BBCAPIWrapper();
		return bbcAPI;
	}
	static public BillboardAPIWrapper getBillboardAPI() {
		if (billboardAPI == null)
			billboardAPI = new BillboardAPIWrapper();
		return billboardAPI;
	}
	static public DiscogsAPIWrapper getDiscogsAPI() {
		if (discogsAPI == null)
			discogsAPI = new DiscogsAPIWrapper();
		return discogsAPI;
	}
	static public EchonestAPIWrapper getEchonestAPI() {
		if (echonestAPI == null)
			echonestAPI = new EchonestAPIWrapper();
		return echonestAPI;
	}
	static public IdiomagAPIWrapper getIdiomagAPI() {
		if (idiomagAPI == null)
			 idiomagAPI = new IdiomagAPIWrapper();
		return idiomagAPI;
	}
	static public LastfmAPIWrapper getLastfmAPI() {
		if (lastfmAPI == null)
			lastfmAPI = new LastfmAPIWrapper();
		return lastfmAPI;
	}
	static public LyricsFlyAPIWrapper getLyricsflyAPI() {
		if (lyricsflyAPI == null)
			lyricsflyAPI = new LyricsFlyAPIWrapper();
		return lyricsflyAPI;
	}
	static public LyricwikiAPIWrapper getLyricwikiAPI() {
		if (lyricwikiAPI == null)
			lyricwikiAPI = new LyricwikiAPIWrapper();
		return lyricwikiAPI;
	}
	static public MusicbrainzAPIWrapper getMusicbrainzAPI() {
		if (musicbrainzAPI == null)
			musicbrainzAPI = new MusicbrainzAPIWrapper();
		return musicbrainzAPI;
	}
	static public YahooMusicAPIWrapper getYahoomusicAPI() {
		if (yahoomusicAPI == null)
			yahoomusicAPI = new YahooMusicAPIWrapper();
		return yahoomusicAPI;
	}

}
