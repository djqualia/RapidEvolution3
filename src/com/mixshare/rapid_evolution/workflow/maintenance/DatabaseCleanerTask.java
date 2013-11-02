package com.mixshare.rapid_evolution.workflow.maintenance;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.index.filter.FilterIndex;
import com.mixshare.rapid_evolution.data.index.filter.playlist.PlaylistIndex;
import com.mixshare.rapid_evolution.data.profile.filter.FilterProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.FilterRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchEncoder;
import com.mixshare.rapid_evolution.data.util.normalization.DuplicateRecordMerger;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.maintenance.images.OrphanedImagesDeleterTask;

public class DatabaseCleanerTask extends CommonTask {

	static private Logger log = Logger.getLogger(DatabaseCleanerTask.class);
    static private final long serialVersionUID = 0L;

    static public DatabaseCleanerTask instance = null;

    public DatabaseCleanerTask() {
    	this(false);
    }

    public DatabaseCleanerTask(boolean removeExtraExternalsOnly) {
    	instance = this;
		if (!removeExtraExternalsOnly) {
			//TaskManager.runBackgroundTask(new OrphanedMinedProfileRemover()); // Need to fix with FileLimitingProfileIO before running again
			TaskManager.runBackgroundTask(new OrphanedImagesDeleterTask());
		}
    }

	@Override
	public void execute() {
		try {
			if (log.isTraceEnabled())
				log.trace("execute(): removing excess external items...");

			if (RE3Properties.getBoolean("attempt_to_automatically_merge_duplicates")) {
				for (int externalArtistId : Database.getArtistIndex().getIds()) {
					ArtistRecord externalArtist = Database.getArtistIndex().getArtistRecord(externalArtistId);
					if ((externalArtist != null) && (externalArtist.isExternalItem())) {
						boolean foundMatch = false;
						for (int internalArtistId : Database.getArtistIndex().getIds()) {
							if (RapidEvolution3.isTerminated || isCancelled())
								return;
							ArtistRecord internalArtist = Database.getArtistIndex().getArtistRecord(internalArtistId);
							if ((internalArtist != null) && !internalArtist.isExternalItem()) {
								if (DuplicateRecordMerger.areArtistsEqual(internalArtist, externalArtist)) {
									if (log.isDebugEnabled())
										log.debug("execute(): merging duplicate external artist=" + externalArtist + ", with internal artist=" + internalArtist);
									ArtistProfile internalProfile = Database.getArtistIndex().getArtistProfile(internalArtist.getUniqueId());
									ArtistProfile externalProfile = Database.getArtistIndex().getArtistProfile(externalArtist.getUniqueId());
									if ((internalProfile != null) && (externalProfile != null)) {
										Database.mergeProfiles(internalProfile, externalProfile);
										foundMatch = true;
										break;
									}
								}
							}
						}
					}
				}
			}
			int maxExternalArtists = RE3Properties.getInt("max_external_artists");
			if (maxExternalArtists != -1) {
				Vector<SortableExternalItem> externalItems = new Vector<SortableExternalItem>(maxExternalArtists);
				for (int artistId : Database.getArtistIndex().getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					SearchRecord record = Database.getArtistIndex().getSearchRecord(artistId);
					if ((record != null) && record.isExternalItem()) {
						float preference = Database.getUserProfile().computePreference(record);
						externalItems.add(new SortableExternalItem(record, preference));
					}
				}
				java.util.Collections.sort(externalItems);
				int numToRemove = externalItems.size() - maxExternalArtists;
				if (numToRemove > 0) {
					if (log.isTraceEnabled())
						log.trace("execute(): removing " + numToRemove + " external artists");
					while (externalItems.size() > maxExternalArtists) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						SortableExternalItem removed = externalItems.remove(externalItems.size() - 1);
						Database.getArtistIndex().delete(removed.getSearchRecord().getUniqueId());
					}
				}
			}

			if (RE3Properties.getBoolean("attempt_to_automatically_merge_duplicates")) {
				for (int externalLabelId : Database.getLabelIndex().getIds()) {
					LabelRecord externalLabel = Database.getLabelIndex().getLabelRecord(externalLabelId);
					if ((externalLabel != null) && (externalLabel.isExternalItem())) {
						boolean foundMatch = false;
						for (int internalLabelId : Database.getLabelIndex().getIds()) {
							if (RapidEvolution3.isTerminated || isCancelled())
								return;
							LabelRecord internalLabel = Database.getLabelIndex().getLabelRecord(internalLabelId);
							if ((internalLabel != null) && !internalLabel.isExternalItem()) {
								if (DuplicateRecordMerger.areLabelsEqual(internalLabel, externalLabel)) {
									if (log.isDebugEnabled())
										log.debug("execute(): merging duplicate external label=" + externalLabel + ", with internal label=" + internalLabel);
									LabelProfile internalProfile = Database.getLabelIndex().getLabelProfile(internalLabel.getUniqueId());
									LabelProfile externalProfile = Database.getLabelIndex().getLabelProfile(externalLabel.getUniqueId());
									Database.mergeProfiles(internalProfile, externalProfile);
									foundMatch = true;
									break;
								}
							}
						}
					}
				}
			}
			int maxExternalLabels = RE3Properties.getInt("max_external_labels");
			if (maxExternalLabels != -1) {
				Vector<SortableExternalItem> externalItems = new Vector<SortableExternalItem>(maxExternalLabels);
				for (int labelId : Database.getLabelIndex().getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					SearchRecord record = Database.getLabelIndex().getSearchRecord(labelId);
					if ((record != null) && record.isExternalItem()) {
						float preference = Database.getUserProfile().computePreference(record);
						externalItems.add(new SortableExternalItem(record, preference));
					}
				}
				java.util.Collections.sort(externalItems);
				int numToRemove = externalItems.size() - maxExternalLabels;
				if (numToRemove > 0) {
					if (log.isTraceEnabled())
						log.trace("execute(): removing " + numToRemove + " external labels");
					while (externalItems.size() > maxExternalLabels) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						SortableExternalItem removed = externalItems.remove(externalItems.size() - 1);
						Database.getLabelIndex().delete(removed.getSearchRecord().getUniqueId());
					}
				}
			}

			if (RE3Properties.getBoolean("attempt_to_automatically_merge_duplicates")) {
				for (int externalReleaseId : Database.getReleaseIndex().getIds()) {
					ReleaseRecord externalRelease = Database.getReleaseIndex().getReleaseRecord(externalReleaseId);
					if ((externalRelease != null) && (externalRelease.isExternalItem())) {
						boolean foundMatch = false;
						for (int internalReleaseId : Database.getReleaseIndex().getIds()) {
							if (RapidEvolution3.isTerminated || isCancelled())
								return;
							ReleaseRecord internalRelease = Database.getReleaseIndex().getReleaseRecord(internalReleaseId);
							if ((internalRelease != null) && !internalRelease.isExternalItem()) {
								if (DuplicateRecordMerger.areReleasesEqual(internalRelease, externalRelease)) {
									if (log.isDebugEnabled())
										log.debug("execute(): merging duplicate external release=" + externalRelease + ", with internal release=" + internalRelease);
									ReleaseProfile internalProfile = Database.getReleaseIndex().getReleaseProfile(internalRelease.getUniqueId());
									ReleaseProfile externalProfile = Database.getReleaseIndex().getReleaseProfile(externalRelease.getUniqueId());
									Database.mergeProfiles(internalProfile, externalProfile);
									foundMatch = true;
									break;
								}
							}
						}
					}
				}
			}
			int maxExternalReleases = RE3Properties.getInt("max_external_releases");
			if (maxExternalReleases != -1) {
				Vector<SortableExternalItem> externalItems = new Vector<SortableExternalItem>(maxExternalReleases);
				for (int releaseId : Database.getReleaseIndex().getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					SearchRecord record = Database.getReleaseIndex().getSearchRecord(releaseId);
					if ((record != null) && record.isExternalItem()) {
						float preference = Database.getUserProfile().computePreference(record);
						externalItems.add(new SortableExternalItem(record, preference));
					}
				}
				java.util.Collections.sort(externalItems);
				int numToRemove = externalItems.size() - maxExternalReleases;
				if (numToRemove > 0) {
					if (log.isTraceEnabled())
						log.trace("execute(): removing " + numToRemove + " external releases");
					while (externalItems.size() > maxExternalReleases) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						SortableExternalItem removed = externalItems.remove(externalItems.size() - 1);
						Database.getReleaseIndex().delete(removed.getSearchRecord().getUniqueId());
					}
				}
			}

			if (RE3Properties.getBoolean("attempt_to_automatically_merge_duplicates")) {
				for (int externalSongId : Database.getSongIndex().getIds()) {
					SongRecord externalSong = Database.getSongIndex().getSongRecord(externalSongId);
					if ((externalSong != null) && (externalSong.isExternalItem())) {
						boolean foundMatch = false;
						for (int internalSongId : Database.getSongIndex().getIds()) {
							if (RapidEvolution3.isTerminated || isCancelled())
								return;
							SongRecord internalSong = Database.getSongIndex().getSongRecord(internalSongId);
							if ((internalSong != null) && !internalSong.isExternalItem()) {
								if (DuplicateRecordMerger.areSongsEqual(internalSong, externalSong)) {
									if (log.isDebugEnabled())
										log.debug("execute(): merging duplicate external song=" + externalSong + ", with internal song=" + internalSong);
									SongProfile internalProfile = Database.getSongIndex().getSongProfile(internalSong.getUniqueId());
									SongProfile externalProfile = Database.getSongIndex().getSongProfile(externalSong.getUniqueId());
									Database.mergeProfiles(internalProfile, externalProfile);
									foundMatch = true;
									break;
								}
							}
						}
					}
				}
			}
			int maxExternalSongs = RE3Properties.getInt("max_external_songs");
			if (maxExternalSongs != -1) {
				Vector<SortableExternalItem> externalItems = new Vector<SortableExternalItem>(maxExternalSongs);
				for (int songId : Database.getSongIndex().getIds()) {
					if (RapidEvolution3.isTerminated || isCancelled())
						return;
					SearchRecord record = Database.getSongIndex().getSearchRecord(songId);
					if ((record != null) && record.isExternalItem()) {
						float preference = Database.getUserProfile().computePreference(record);
						externalItems.add(new SortableExternalItem(record, preference));
					}
				}
				java.util.Collections.sort(externalItems);
				int numToRemove = externalItems.size() - maxExternalSongs;
				if (numToRemove > 0) {
					if (log.isTraceEnabled())
						log.trace("execute(): removing " + numToRemove + " external songs");
					while (externalItems.size() > maxExternalSongs) {
						if (RapidEvolution3.isTerminated || isCancelled())
							return;
						SortableExternalItem removed = externalItems.remove(externalItems.size() - 1);
						Database.getSongIndex().delete(removed.getSearchRecord().getUniqueId());
					}
				}
			}

			// clean filters
			if (RE3Properties.getBoolean("attempt_to_automatically_merge_duplicates")) {
				for (FilterIndex filterIndex : Database.getFilterIndexes()) {
					if (!(filterIndex instanceof PlaylistIndex)) {
						for (int id : filterIndex.getIds()) {
							FilterRecord filter = filterIndex.getFilterRecord(id);
							if (filter != null) {
								String encodedFilter = SearchEncoder.unifyString(filter.toString());
								for (int dupId : filterIndex.getIds()) {
									if (RapidEvolution3.isTerminated || isCancelled())
										return;
									if (dupId != id) {
										FilterRecord dupFilter = filterIndex.getFilterRecord(dupId);
										if (dupFilter != null) {
											String encodedDupFilter = SearchEncoder.unifyString(dupFilter.toString());
											if (encodedFilter.equals(encodedDupFilter)) {
												FilterProfile filterProfile = (FilterProfile)filterIndex.getProfile(id);
												FilterProfile dupProfile = (FilterProfile)filterIndex.getProfile(dupId);

												if ((filterProfile != null && (dupProfile != null))) {
													// some logic to decide which should be primary....
													FilterProfile primaryFilter = null;
													FilterProfile duplicateFilter = null;

													boolean isFirstRootOnlyParent = false;
													if ((filterProfile.getParentRecords().length == 1) && (filterProfile.getParentRecords()[0].isRoot()))
														isFirstRootOnlyParent = true;
													boolean isSecondRootOnlyParent = false;
													if ((dupProfile.getParentRecords().length == 1) && (dupProfile.getParentRecords()[0].isRoot()))
														isSecondRootOnlyParent = true;
													if (!isFirstRootOnlyParent && isSecondRootOnlyParent) {
														primaryFilter = filterProfile;
														duplicateFilter = dupProfile;
													} else if (isFirstRootOnlyParent && !isSecondRootOnlyParent) {
														primaryFilter = dupProfile;
														duplicateFilter = filterProfile;
													}
													if (primaryFilter == null) {
														if (filterProfile.getChildRecords().length > dupProfile.getChildRecords().length) {
															primaryFilter = filterProfile;
															duplicateFilter = dupProfile;
														} else if (filterProfile.getChildRecords().length < dupProfile.getChildRecords().length) {
															primaryFilter = dupProfile;
															duplicateFilter = filterProfile;
														}
														if (primaryFilter == null) {
															int size1 = filter.getNumArtistRecordsCached() + filter.getNumLabelRecordsCached() + filter.getNumReleaseRecordsCached() + filter.getNumSongRecordsCached();
															int size2 = dupFilter.getNumArtistRecordsCached() + dupFilter.getNumLabelRecordsCached() + dupFilter.getNumReleaseRecordsCached() + dupFilter.getNumSongRecordsCached();
															if (size1 > size2) {
																primaryFilter = filterProfile;
																duplicateFilter = dupProfile;
															} else if (size2 > size1) {
																primaryFilter = dupProfile;
																duplicateFilter = filterProfile;
															}
															if (primaryFilter == null) {
																if (id < dupId) {
																	primaryFilter = filterProfile;
																	duplicateFilter = dupProfile;
																} else { // dupId < id
																	primaryFilter = dupProfile;
																	duplicateFilter = filterProfile;
																}
															}
														}
													}
													if (log.isDebugEnabled())
														log.debug("execute(): merging filter=" + primaryFilter + ", with duplicateFilter=" + duplicateFilter);
													Database.mergeProfiles(primaryFilter, duplicateFilter);
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			log.error("execute(): error", e);
		} finally {
			instance = null;
		}
	}

	static protected class SortableExternalItem implements Comparable<SortableExternalItem> {
		private final SearchRecord searchRecord;
		private final float preference;
		public SortableExternalItem(SearchRecord searchRecord, float preference) {
			this.searchRecord = searchRecord;
			this.preference = preference;
		}
		public SearchRecord getSearchRecord() { return searchRecord; }
		@Override
		public int compareTo(SortableExternalItem item) {
			if (preference > item.preference)
				return -1;
			if (preference < item.preference)
				return 1;
			return 0;
		}
	}

	@Override
	public boolean isIndefiniteTask() { return true; }

	@Override
	public String toString() {
		return "Cleaning database";
	}

}
