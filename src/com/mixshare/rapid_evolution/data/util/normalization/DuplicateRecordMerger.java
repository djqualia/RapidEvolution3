package com.mixshare.rapid_evolution.data.util.normalization;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.search.spell.LevensteinDistance;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.Index;
import com.mixshare.rapid_evolution.data.index.event.IndexChangeListener;
import com.mixshare.rapid_evolution.data.index.search.artist.ArtistIndex;
import com.mixshare.rapid_evolution.data.index.search.label.LabelIndex;
import com.mixshare.rapid_evolution.data.index.search.release.ReleaseIndex;
import com.mixshare.rapid_evolution.data.index.search.song.SongIndex;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.record.Record;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchEncoder;
import com.mixshare.rapid_evolution.data.submitted.SubmittedProfile;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.user.MergeProfilesTask;

public class DuplicateRecordMerger implements IndexChangeListener {

    static private Logger log = Logger.getLogger(DuplicateRecordMerger.class);

	static public boolean MERGE_RECORDS_IMMEDIATELY = false;
    static private final LevensteinDistance levDist = new LevensteinDistance();

	static private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMM dd");

	static public boolean areSongsEqual(SongRecord song1, SongRecord song2) {
		try {
			String artist1 = SearchEncoder.unifyString(song1.getArtistsDescription());
			String artist2 = SearchEncoder.unifyString(song2.getArtistsDescription());
			if ((artist1.length() == 0) || (artist2.length() == 0))
				return false;
			String title1 = SearchEncoder.unifyString(song1.getSongDescription());
			String title2 = SearchEncoder.unifyString(song2.getSongDescription());
			if ((title1.length() == 0) || (title2.length() == 0))
				return false;
			String song1Encoded = artist1 + " " + title1;
			String song2Encoded = artist2 + " " + title2;
			return song1Encoded.equals(song2Encoded);
		} catch (Exception e) {
			log.error("areSongsEqual(): error, song1=" + song1 + ", song2=" + song2, e);
		}
		return false;
	}
	static public boolean areReleasesEqual(ReleaseRecord release1, ReleaseRecord release2) {
		try {
			String artist1 = SearchEncoder.unifyString(release1.getArtistsDescription());
			String artist2 = SearchEncoder.unifyString(release2.getArtistsDescription());
			if ((artist1.length() == 0) || (artist2.length() == 0))
				return false;
			String title1 = SearchEncoder.unifyRelease(release1.getReleaseTitle());
			String title2 = SearchEncoder.unifyRelease(release2.getReleaseTitle());
			if ((title1.length() == 0) || (title2.length() == 0))
				return false;
			String release1Encoded = artist1 + " " + title1;
			String release2Encoded = artist2 + " " + title2;
			return release1Encoded.equals(release2Encoded);
		} catch (Exception e) {
			log.error("areReleasesEqual(): error, release1=" + release1 + ", release2=" + release2, e);
		}
		return false;
	}
	static public boolean areLabelsEqual(LabelRecord label1, LabelRecord label2) {
		String label1Encoded = SearchEncoder.unifyString(label1.getLabelName());
		String label2Encoded = SearchEncoder.unifyString(label2.getLabelName());
		return (label1Encoded.length() > 0) && (label2Encoded.length() > 0) && label1Encoded.equals(label2Encoded);
	}
	static public boolean areArtistsEqual(ArtistRecord artist1, ArtistRecord artist2) {
		String artist1Encoded = SearchEncoder.unifyString(artist1.getArtistName());
		String artist2Encoded = SearchEncoder.unifyString(artist2.getArtistName());
		return (artist1Encoded.length() > 0) && (artist2Encoded.length() > 0) && artist1Encoded.equals(artist2Encoded);
	}

	static private boolean isValidArtist(ArtistRecord artist) {
		String artistName = artist.getArtistName();
		if (artistName.equalsIgnoreCase("Various"))
			return false;
		if (artistName.equalsIgnoreCase("Various Artists"))
			return false;
		if (artistName.equalsIgnoreCase("TBD"))
			return false;

		return true;
	}

	static private void addTokenized(String value, Vector<String> tokens) {
		StringTokenizer tokenizer = new StringTokenizer(value, " ");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			if (token.length() > 0)
				tokens.add(token);
		}
	}

	static private String removeCommonTerms(String eventName) {
		eventName = StringUtil.replace(eventName, "headliner", "");
		eventName = StringUtil.replace(eventName, "with", "");
		eventName = StringUtil.replace(eventName, "and", "");
		eventName = StringUtil.replace(eventName, "the", "");
		return eventName;
	}

	static private String removeDateTimeInfo(String eventName) {
		// months
		eventName = StringUtil.replace(eventName, "january", "");
		eventName = StringUtil.replace(eventName, "february", "");
		eventName = StringUtil.replace(eventName, "march", "");
		eventName = StringUtil.replace(eventName, "april", "");
		eventName = StringUtil.replace(eventName, "may", "");
		eventName = StringUtil.replace(eventName, "june", "");
		eventName = StringUtil.replace(eventName, "july", "");
		eventName = StringUtil.replace(eventName, "august", "");
		eventName = StringUtil.replace(eventName, "september", "");
		eventName = StringUtil.replace(eventName, "october", "");
		eventName = StringUtil.replace(eventName, "november", "");
		eventName = StringUtil.replace(eventName, "december", "");
		// month abbreviations
		eventName = StringUtil.replace(eventName, "jan ", "");
		eventName = StringUtil.replace(eventName, "feb ", "");
		eventName = StringUtil.replace(eventName, "mar ", "");
		eventName = StringUtil.replace(eventName, "apr ", "");
		eventName = StringUtil.replace(eventName, "may ", "");
		eventName = StringUtil.replace(eventName, "jun ", "");
		eventName = StringUtil.replace(eventName, "jul ", "");
		eventName = StringUtil.replace(eventName, "aug ", "");
		eventName = StringUtil.replace(eventName, "sep ", "");
		eventName = StringUtil.replace(eventName, "oct ", "");
		eventName = StringUtil.replace(eventName, "nov ", "");
		eventName = StringUtil.replace(eventName, "dec ", "");

		// days
		eventName = StringUtil.replace(eventName, "monday", "");
		eventName = StringUtil.replace(eventName, "tuesday", "");
		eventName = StringUtil.replace(eventName, "wednesday", "");
		eventName = StringUtil.replace(eventName, "thursday", "");
		eventName = StringUtil.replace(eventName, "friday", "");
		eventName = StringUtil.replace(eventName, "saturday", "");
		eventName = StringUtil.replace(eventName, "sunday", "");
		// day abbreviations
		eventName = StringUtil.replace(eventName, "mon ", "");
		eventName = StringUtil.replace(eventName, "tue ", "");
		eventName = StringUtil.replace(eventName, "wed ", "");
		eventName = StringUtil.replace(eventName, "thu ", "");
		eventName = StringUtil.replace(eventName, "fri ", "");
		eventName = StringUtil.replace(eventName, "sat ", "");
		eventName = StringUtil.replace(eventName, "sun ", "");

		// years
		for (int y = 2011; y < 2011 + 10; ++y)
			eventName = StringUtil.replace(eventName, String.valueOf(y), "");

		return eventName;
	}

	private final Index index;

	public DuplicateRecordMerger(Index index) {
		this.index = index;
	}

	@Override
	public void addedRecord(Record newRecord, SubmittedProfile submittedProfile) {
		if (!RE3Properties.getBoolean("attempt_to_automatically_merge_duplicates"))
			return;
		if (index instanceof SongIndex) {
			SongRecord newSong = (SongRecord)newRecord;
			String newRecordEncoded = SearchEncoder.unifyString(newSong.getArtistsDescription()) + " " + SearchEncoder.unifyString(newSong.getSongDescription());
			Profile newProfile = index.getProfile(newRecord.getUniqueId());
			for (int id : index.getIds()) {
				SongRecord existingSong = (SongRecord)index.getRecord(id);
				if ((existingSong != null) && (existingSong.getUniqueId() != newRecord.getUniqueId())) {
					String existingRecordEncoded = SearchEncoder.unifyString(existingSong.getArtistsDescription()) + " " + SearchEncoder.unifyString(existingSong.getSongDescription());
					if (existingRecordEncoded.equals(newRecordEncoded)) {
						Profile existingProfile = index.getProfile(existingSong.getUniqueId());
						if (existingProfile != null)
							if (MERGE_RECORDS_IMMEDIATELY)
								Database.mergeProfiles(existingProfile, newProfile);
							else
								TaskManager.runBackgroundTask(new MergeProfilesTask(existingProfile, newProfile));
					}
				}
			}
		} else if (index instanceof ReleaseIndex) {
			ReleaseRecord newRelease = (ReleaseRecord)newRecord;
			String newRecordEncoded = SearchEncoder.unifyString(newRelease.getArtistsDescription()) + " " + SearchEncoder.unifyString(newRelease.getReleaseTitle());
			Profile newProfile = index.getProfile(newRecord.getUniqueId());
			for (int id : index.getIds()) {
				ReleaseRecord existingRelease = (ReleaseRecord)index.getRecord(id);
				if ((existingRelease != null) && (existingRelease.getUniqueId() != newRecord.getUniqueId())) {
					String existingRecordEncoded = SearchEncoder.unifyString(existingRelease.getArtistsDescription()) + " " + SearchEncoder.unifyString(existingRelease.getReleaseTitle());
					if (existingRecordEncoded.equals(newRecordEncoded)) {
						Profile existingProfile = index.getProfile(existingRelease.getUniqueId());
						if (existingProfile != null)
							if (MERGE_RECORDS_IMMEDIATELY)
								Database.mergeProfiles(existingProfile, newProfile);
							else
								TaskManager.runBackgroundTask(new MergeProfilesTask(existingProfile, newProfile));
					}
				}
			}
		} else {
			Profile newProfile = index.getProfile(newRecord.getUniqueId());
			for (int id : index.getIds()) {
				Record existingRecord = index.getRecord(id);
				if ((existingRecord != null) && (existingRecord.getUniqueId() != newRecord.getUniqueId())) {
					boolean equals = false;
					if (index instanceof ArtistIndex)
						equals = areArtistsEqual((ArtistRecord)newRecord, (ArtistRecord)existingRecord);
					else if (index instanceof LabelIndex)
						equals = areLabelsEqual((LabelRecord)newRecord, (LabelRecord)existingRecord);
					else if (index instanceof ReleaseIndex)
						equals = areReleasesEqual((ReleaseRecord)newRecord, (ReleaseRecord)existingRecord);
					else if (index instanceof SongIndex)
						equals = areSongsEqual((SongRecord)newRecord, (SongRecord)existingRecord);
					if (equals) {
						Profile existingProfile = index.getProfile(existingRecord.getUniqueId());
						if (existingProfile != null) {
							if (MERGE_RECORDS_IMMEDIATELY)
								Database.mergeProfiles(existingProfile, newProfile);
							else
								TaskManager.runBackgroundTask(new MergeProfilesTask(existingProfile, newProfile));
						}
					}
				}
			}
		}
	}

	@Override
	public void removedRecord(Record record) { }
	@Override
	public void updatedRecord(Record record) { }

}
