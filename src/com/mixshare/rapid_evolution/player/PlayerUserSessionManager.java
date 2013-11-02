package com.mixshare.rapid_evolution.player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.song.MixoutRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.search.parameters.CommonSearchParameters;
import com.mixshare.rapid_evolution.data.search.parameters.search.song.SongSearchParameters;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.DegreeValueSetAverager;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.model.filter.FilterSelection;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.mining.TriggeredMinerStarter;

public class PlayerUserSessionManager implements DataConstants {

    static private Logger log = Logger.getLogger(PlayerUserSessionManager.class);

	static private class PlayRecord {
		public int songId;
		public boolean played;
		public PlayRecord(int songId) { this.songId = songId; }
		public PlayRecord(int songId, boolean played) { this.songId = songId; this.played = played; }
		@Override
		public String toString() { return "Play record=" + songId; }
	}

	////////////
	// FIELDS //
	////////////

	private final String userName;

	private Vector<PlayRecord> currentPlayRecords;
	private int currentSongIndex;
	private SongRecord lastPlayedSong;

	private final Map<Integer, Long> playedTimes = new HashMap<Integer, Long>(); // keeps track of which songs have been played and when
	private final Map<String, Long> playedSongTitles = new HashMap<String, Long>(); // keeps track of which song TITLEs have been played (to prevent playing remixes of the same song)
	private final Vector<Integer> recentArtistIds = new Vector<Integer>(); // keeps track of which artists have been played

	private Vector<DegreeValue> targetStyles;
	private int[] targetStyleIds;
	private float[] targetStyleDegrees;
	private Vector<DegreeValue> targetTags;
	private int[] targetTagIds;
	private float[] targetTagDegrees;
	private SongProfile targetSong;

	private SongRecord preComputedSong = null;

	//////////////////
	// CONSTRUCTORS //
	//////////////////

	public PlayerUserSessionManager(String userName) {
		this.userName = userName;
	}

	/////////////
	// GETTERS //
	/////////////

	public String getUserName() { return userName; }

	public int getCurrentSongIndex() { return currentSongIndex; }
	public SongRecord getCurrentSong() {
		if ((currentPlayRecords != null) && (currentSongIndex < currentPlayRecords.size()) && (currentSongIndex >= 0))
			return Database.getSongIndex().getSongRecord(currentPlayRecords.get(currentSongIndex).songId);
		return null;
	}

	public SongProfile getCurrentSongProfile() {
		if ((currentPlayRecords != null) && (currentSongIndex < currentPlayRecords.size()) && (currentSongIndex >= 0))
			return Database.getSongIndex().getSongProfile(currentPlayRecords.get(currentSongIndex).songId);
		return null;
	}

	public SongRecord getPreviousSongToPlay() {
		SongRecord result = null;
		--currentSongIndex;
		while ((currentSongIndex >= 0) && (result == null)) {
			int songId = currentPlayRecords.get(currentSongIndex).songId;
			SongRecord song = Database.getSongIndex().getSongRecord(songId);
			if ((song.getSongFilename() != null) && (song.getSongFilename().length() > 0)) {
				File file = new File(song.getSongFilename());
				if (file.exists())
					result = song;
			}
			if (result == null)
				--currentSongIndex;
		}
		if (log.isDebugEnabled())
			log.debug("getPreviousSongToPlay(): result=" + result + ", currentSongIndex=" + currentSongIndex);
		return result;
	}

	public boolean hasPreviousSong() { return (currentSongIndex > 0); }

	public void preComputeNextSongToPlay() {
		if (log.isTraceEnabled())
			log.trace("preComputeNextSongToPlay(): going...");
		preComputedSong = getNextSongToPlay(false, true);
	}

	public int getIndexOf(int songId) {
		if (currentPlayRecords != null) {
			int i = 0;
			for (PlayRecord song : currentPlayRecords) {
				if (song.songId == songId)
					return i;
				++i;
			}
		}
		return -1;
	}

	public SongRecord getNextSongToPlay(boolean shuffle) { return getNextSongToPlay(shuffle, false); }
	public SongRecord getNextSongToPlay(boolean shuffle, boolean preCompute) {

		SongRecord result = null;
		if (!shuffle && (preComputedSong != null) && !preCompute) {
			if (log.isTraceEnabled())
				log.trace("getNextSongToPlay(): using pre-computed result");
			result = preComputedSong;
			currentPlayRecords.add(new PlayRecord(result.getUniqueId(), true));
			preComputedSong = null;
			++currentSongIndex;
			pickedSong(result);
			return result;
		}

		if (log.isTraceEnabled())
			log.trace("getNextSongToPlay(): started...", new Exception());

		if (!preCompute) {
			// check queue
			currentSongIndex = getNextSongIndex(currentSongIndex, shuffle);
			while ((currentSongIndex < currentPlayRecords.size()) && (result == null)) {
				PlayRecord playRecord = currentPlayRecords.get(currentSongIndex);
				playRecord.played = true;
				int songId = playRecord.songId;
				SongRecord song = Database.getSongIndex().getSongRecord(songId);
				if (song != null) {
					if ((song.getSongFilename() != null) && (song.getSongFilename().length() > 0)) {
						File file = new File(song.getSongFilename());
						if (file.exists())
							result = song;
					}
				} else {
					log.warn("getNextSongToPlay(): song with id is null=" + songId);
				}
				if (result == null)
					currentSongIndex = getNextSongIndex(currentSongIndex, shuffle);
			}
		} else {
			if (hasNextSongQueued(shuffle))
				return result;
		}

		// look for a similar item to the last song
		int minRating = RE3Properties.getInt("media_player_autoplay_minimum_rating");
		boolean includeUnrated = RE3Properties.getBoolean("media_player_autoplay_include_unrated");
		if (result == null) {
			if (lastPlayedSong != null) {
				if (log.isDebugEnabled())
					log.debug("getNextSongToPlay(): searching for best autoplay song, currentSongIndex=" + currentSongIndex);
				float randomness = RE3Properties.getFloat("autoplay_randomness") / 100.0f;
				SongProfile songProfile = Database.getSongIndex().getSongProfile(lastPlayedSong.getUniqueId());
				if (songProfile != null) {
					SongRecord nextSong = null;
					float maxSimilarity = 0.0f;
					if (!shuffle) {
						for (MixoutRecord mixout : songProfile.getMixouts()) {
							if (mixout.getType() == MixoutRecord.TYPE_TRANSITION) {
								SongRecord toSong = mixout.getToSong();
								if (toSong != null) {
									if ((toSong.getUniqueId() != songProfile.getUniqueId()) && !playedTimes.containsKey(toSong.getUniqueId())) { // if it hasn't been played this session yet
										float rating = (mixout.getRatingValue().getRatingNormalized());
										if (targetSong != null)
											rating = (rating + targetSong.getSimilarity(mixout.getToSong())) / 2.0f;
										rating *= (1.0f - randomness * Math.random());
										if (rating > maxSimilarity) {
											maxSimilarity = rating;
											nextSong = toSong;
										}
									}
								}
							}
						}
						if (nextSong != null) {
							if (log.isDebugEnabled())
								log.debug("getNextSongToPlay(): found nextSong from mixouts=" + nextSong);
						}
					}
					if (!(RE3Properties.getBoolean("autoplay_prefer_mixouts_when_available") && (nextSong != null))) {

						SongSearchParameters songSearch = new SongSearchParameters();

						Vector<Integer> excludeSongIds = new Vector<Integer>();
						excludeSongIds.add(lastPlayedSong.getUniqueId());
						for (Integer playedId : playedTimes.keySet())
							excludeSongIds.add(playedId);
						songSearch.setExcludedIds(excludeSongIds);

						songSearch.setInternalItemsOnly(true);
						songSearch.setShowDisabled(false);

						songSearch.setExcludeSongTitles(playedSongTitles.keySet());

						songSearch.setExcludeArtistIds(recentArtistIds);

						songSearch.setValidFilenamesOnly(true);

						Vector<SearchProfile> relativeProfiles = new Vector<SearchProfile>(2);
						relativeProfiles.add(Database.getSongIndex().getSongProfile(lastPlayedSong.getUniqueId()));
						if ((targetSong != null) && !shuffle)
							relativeProfiles.add(targetSong);
						songSearch.initRelativeProfiles(relativeProfiles);

						songSearch.setRandomness(randomness);

						if (minRating > 0)
							songSearch.setMinRating(Rating.getRating(minRating * 20));
						songSearch.setIncludeUnrated(includeUnrated);
						songSearch.setSortType(new byte[] { CommonSearchParameters.SORT_BY_SIMILARITY, CommonSearchParameters.SORT_BY_SCORE });

						if ((targetStyleIds != null) && (targetStyleIds.length > 0)) {
							FilterSelection stylesSelection = new FilterSelection();
							Vector<FilterRecord> filterStyles = new Vector<FilterRecord>();
							for (int i = 0; i < targetStyleIds.length; ++i) {
								StyleRecord styleRecord = Database.getStyleIndex().getStyleRecord(targetStyleIds[i]);
								if (styleRecord != null)
									filterStyles.add(styleRecord);
							}
							stylesSelection.setOptionalFilters(filterStyles);
							songSearch.setStylesSelection(stylesSelection);
						}
						if ((targetTagIds != null) && (targetTagIds.length > 0)) {
							FilterSelection tagsSelection = new FilterSelection();
							Vector<FilterRecord> filterTags = new Vector<FilterRecord>();
							for (int i = 0; i < targetTagIds.length; ++i) {
								TagRecord tagRecord = Database.getTagIndex().getTagRecord(targetTagIds[i]);
								if (tagRecord != null)
									filterTags.add(tagRecord);
							}
							tagsSelection.setOptionalFilters(filterTags);
							songSearch.setTagsSelection(tagsSelection);
						}

						Vector<SearchResult> searchResults = Database.getSongIndex().searchRecords(songSearch, 2);
						for (SearchResult searchResult : searchResults) {
							nextSong = (SongRecord)searchResult.getRecord();
							if (log.isDebugEnabled())
								log.debug("getNextSongToPlay(): nextResult=" + nextSong + ", match=" + searchResult.getScore());
							break;
						}
						if (nextSong == null) {
							if (log.isDebugEnabled())
								log.debug("getNextSongToPlay(): no results found, laxing search criteria");
							// lax up the search critera to see if we can't find something
							songSearch.setStylesSelection(null);
							songSearch.setTagsSelection(null);
							searchResults = Database.getSongIndex().searchRecords(songSearch, 2);
							for (SearchResult searchResult : searchResults) {
								nextSong = (SongRecord)searchResult.getRecord();
								break;
							}
							if (nextSong == null) {
								// lax it up some more
								songSearch.clearExcludedArtistIds();
								searchResults = Database.getSongIndex().searchRecords(songSearch, 2);
								for (SearchResult searchResult : searchResults) {
									nextSong = (SongRecord)searchResult.getRecord();
									break;
								}
							}
						}
					}
					if (nextSong != null) {
						result = nextSong;
						if (!preCompute)
							currentPlayRecords.add(new PlayRecord(result.getUniqueId(), true));
					}
				} else {
					if (log.isDebugEnabled())
						log.debug("getNextSongToPlay(): lastPlayedSong profile could not be loaded, can't search for new autoplay song");
				}
			} else {
				if (log.isDebugEnabled())
					log.debug("getNextSongToPlay(): lastPlayedSong is null, can't search for new autoplay song");
			}
		}
		if ((result != null) && !preCompute)
			pickedSong(result);
		if (log.isDebugEnabled())
			log.debug("getNextSongToPlay(): result=" + result + ", currentSongIndex=" + currentSongIndex);
		return result;
	}

	private void pickedSong(SongRecord result) {
		lastPlayedSong = result;
		playedTimes.put(result.getUniqueId(), System.currentTimeMillis());
		playedSongTitles.put(SongSearchParameters.getLogicalSongKey(result), System.currentTimeMillis());
		for (int artistId : result.getArtistIds())
			recentArtistIds.add(artistId);
		while (recentArtistIds.size() > RE3Properties.getInt("minimum_songs_between_artist_repeat"))
			recentArtistIds.remove(0);
		result.incrementPlayCount(1, true);
		SongProfile profile = Database.getSongIndex().getSongProfile(result.getUniqueId());
		if (RE3Properties.getBoolean("enable_triggered_data_miners") && !RE3Properties.getBoolean("server_mode"))
			TaskManager.runForegroundTask(new TriggeredMinerStarter(profile));
		if (log.isDebugEnabled())
			log.debug("pickedSong(): result=" + result + ", currentSongIndex=" + currentSongIndex + ", currentPlayRecords=" + currentPlayRecords);
	}

	/////////////
	// SETTERS //
	/////////////

	public void setCurrentIndex(int index) {
		currentSongIndex = index;
		preComputedSong = null;
	}

	/////////////
	// METHODS //
	/////////////

	public void init(Vector<Integer> songIds) {
		try {
			preComputedSong = null;
			currentSongIndex = -1;
			currentPlayRecords = new Vector<PlayRecord>();
			for (int songId : songIds)
				currentPlayRecords.add(new PlayRecord(songId));

			if (!RE3Properties.getBoolean("autoplay_avoid_repeats_all_session")) {
				playedTimes.clear();
				playedSongTitles.clear();
				recentArtistIds.clear();
			}

			lastPlayedSong =  Database.getSongIndex().getSongRecord(songIds.get(0));
			targetSong = Database.getSongIndex().getSongProfile(songIds.get(0));
			playedTimes.put(lastPlayedSong.getUniqueId(), null);

			if (songIds.size() > 1) {
				// compute style/tag targets to keep auto-play on track
				DegreeValueSetAverager styleAverager = new DegreeValueSetAverager();
				DegreeValueSetAverager tagAverager = new DegreeValueSetAverager();
				for (int songId : songIds) {
					SongRecord song = Database.getSongIndex().getSongRecord(songId);
					if (song != null) {
						Vector<DegreeValue> styleDegrees = song.getActualStyleDegreeValues();
						Vector<DegreeValue> tagDegrees = song.getActualTagDegreeValues();
						if (styleDegrees.size() > 0)
							styleAverager.addDegreeValueSet(styleDegrees, 1.0f);
						if (tagDegrees.size() > 0)
							tagAverager.addDegreeValueSet(tagDegrees, 1.0f);
						for (ArtistRecord artist :song.getArtists()) {
							styleDegrees = artist.getActualStyleDegreeValues();
							tagDegrees = artist.getActualTagDegreeValues();
							if (styleDegrees.size() > 0)
								styleAverager.addDegreeValueSet(styleDegrees, 1.0f);
							if (tagDegrees.size() > 0)
								tagAverager.addDegreeValueSet(tagDegrees, 1.0f);
						}
					}
				}
				targetStyles = styleAverager.getDegrees();
				targetTags = tagAverager.getDegrees();
				targetStyleIds = new int[targetStyles.size()];
				targetStyleDegrees = new float[targetStyles.size()];
				int s = 0;
				for (DegreeValue degree : targetStyles) {
					targetStyleIds[s] = Database.getStyleIndex().getStyleRecord(new StyleIdentifier(degree.getName())).getUniqueId();
					targetStyleDegrees[s] = degree.getPercentage();
					++s;
				}
				targetTagIds = new int[targetTags.size()];
				targetTagDegrees = new float[targetTags.size()];
				int t = 0;
				for (DegreeValue degree : targetTags) {
					targetTagIds[t] = Database.getTagIndex().getTagRecord(new TagIdentifier(degree.getName())).getUniqueId();
					targetTagDegrees[t] = degree.getPercentage();
					++t;
				}
			} else {
				targetStyleIds = null;
				targetStyleDegrees = null;
				targetTagIds = null;
				targetTagDegrees = null;
			}

		} catch (Exception e) {
			log.error("init(): error", e);
		}
	}

	/////////////////////
	// PRIVATE METHODS //
	/////////////////////

	private boolean hasNextSongQueued(boolean shuffle) {
		if (shuffle) {
			int numOptions = 0;
			for (int i = 0; i < currentPlayRecords.size(); ++i) {
				PlayRecord playRecord = currentPlayRecords.get(i);
				if (!playRecord.played)
					++numOptions;
			}
			return (numOptions != 0);
		} else {
			return (currentSongIndex + 1 < currentPlayRecords.size());
		}
	}

	private int getNextSongIndex(int currentSongIndex, boolean shuffle) {
		if (shuffle) {
			int numOptions = 0;
			for (int i = 0; i < currentPlayRecords.size(); ++i) {
				PlayRecord playRecord = currentPlayRecords.get(i);
				if (!playRecord.played)
					++numOptions;
			}
			if (numOptions == 0) {
				currentSongIndex = currentPlayRecords.size();
			} else {
				int random = (int)(Math.random() * 1000000);
				while (random >= numOptions)
					random -= numOptions;
				numOptions = 0;
				for (int i = 0; i < currentPlayRecords.size(); ++i) {
					PlayRecord playRecord = currentPlayRecords.get(i);
					if (!playRecord.played) {
						if (random == numOptions) {
							currentSongIndex = i;
							break;
						}
						++numOptions;
					}
				}
			}
		} else {
			++currentSongIndex;
		}
		return currentSongIndex;
	}

}
