package com.mixshare.rapid_evolution.ui.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.artist.ArtistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.label.LabelIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.identifier.search.song.SongIdentifier;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.record.search.SearchRecord;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;
import com.mixshare.rapid_evolution.data.record.search.label.LabelRecord;
import com.mixshare.rapid_evolution.data.record.search.release.ReleaseRecord;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.search.SearchResult;
import com.mixshare.rapid_evolution.data.util.io.Serializer;
import com.mixshare.rapid_evolution.ui.model.filter.FilterHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.playlist.PlaylistHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.style.StyleHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.filter.tag.TagHierarchyInstance;
import com.mixshare.rapid_evolution.ui.model.profile.trail.TrailInstance;
import com.mixshare.rapid_evolution.ui.model.tree.TreeHierarchyInstance;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QUrl;

public class DragDropUtil {
	
	static private Logger log = Logger.getLogger(DragDropUtil.class);
	
	///////////////////
	// STATIC FIELDS //
	///////////////////
	
	// filename list (of songs), compatible with OS
	static public final String MIME_TYPE_FILENAME_LIST = "text/uri-list";
	static public final String MIME_TYPE_TRAIL_INSTANCES_IDENTIFIER_LIST = "application/x-re3trailinstances";
	// search identifiers
	static public final String MIME_TYPE_ARTIST_IDENTIFIER_LIST = "application/x-artistidentifierlist";
	static public final String MIME_TYPE_LABEL_IDENTIFIER_LIST = "application/x-labelidentifierlist";
	static public final String MIME_TYPE_RELEASE_IDENTIFIER_LIST = "application/x-releaseidentifierlist";
	static public final String MIME_TYPE_SONG_IDENTIFIER_LIST = "application/x-songidentifierlist";
	// filter identifiers
	static public final String MIME_TYPE_STYLE_IDENTIFIER_LIST = "application/x-styleidentifierlist";
	static public final String MIME_TYPE_TAG_IDENTIFIER_LIST = "application/x-tagidentifierlist";
	static public final String MIME_TYPE_PLAYLIST_IDENTIFIER_LIST = "application/x-playlistidentifierlist";
	// filter instance identifiers
	static public final String MIME_TYPE_STYLE_INSTANCE_LIST = "application/x-styleinstanceidentifierlist";
	static public final String MIME_TYPE_TAG_INSTANCE_LIST = "application/x-taginstanceidentifierlist";
	static public final String MIME_TYPE_PLAYLIST_INSTANCE_LIST = "application/x-playlistinstanceidentifierlist";
	
	
	/////////////
	// METHODS //
	/////////////	
	
	static public QMimeData getMimeDataForSearchRecords(Vector<SearchRecord> searchRecords) {
		QMimeData mimeData = new QMimeData();
		try {
			List<QUrl> urls = new ArrayList<QUrl>();
			Vector<ArtistIdentifier> artistIds = new Vector<ArtistIdentifier>();
			Vector<LabelIdentifier> labelIds = new Vector<LabelIdentifier>();
			Vector<ReleaseIdentifier> releaseIds = new Vector<ReleaseIdentifier>();
			Vector<SongIdentifier> songIds = new Vector<SongIdentifier>();			
			for (SearchRecord searchRecord : searchRecords) {
				if (searchRecord instanceof ArtistRecord) {
					ArtistRecord artist = (ArtistRecord)searchRecord;
					Vector<SongRecord> songRecords = artist.getSongs();
					for (SongRecord song : songRecords) {
						String filename = song.getSongFilename();
						if ((filename != null) && (filename.length() > 0)) 
							urls.add(QUrl.fromLocalFile(filename));						
					}	
					artistIds.add(artist.getArtistIdentifier());
				} else if (searchRecord instanceof LabelRecord) {
					LabelRecord label = (LabelRecord)searchRecord;
					Vector<SongRecord> songRecords = label.getSongs();
					for (SongRecord song : songRecords) {
						String filename = song.getSongFilename();
						if ((filename != null) && (filename.length() > 0))
							urls.add(QUrl.fromLocalFile(filename));
					}	
					labelIds.add(label.getLabelIdentifier());
				} else if (searchRecord instanceof ReleaseRecord) {
					ReleaseRecord release = (ReleaseRecord)searchRecord;
					Vector<SongRecord> songRecords = release.getSongs();
					for (SongRecord song : songRecords) {
						String filename = song.getSongFilename();
						if ((filename != null) && (filename.length() > 0))
							urls.add(QUrl.fromLocalFile(filename));
					}	
					releaseIds.add(release.getReleaseIdentifier());
				} else if (searchRecord instanceof SongRecord) {
					SongRecord song = (SongRecord)searchRecord;
					String filename = song.getSongFilename();
					if ((filename != null) && (filename.length() > 0)) {
						File file = new File(filename);
						if (file.exists()) {
							urls.add(QUrl.fromLocalFile(filename));
						}
						/*
						// TEST CODE:
						String fileUrl = FileUtil.getFilenameAsURL(filename);
						fileUrl = "C:/test.mp3";
						if (log.isTraceEnabled())
							log.trace("getMimeDataForSearchRecords(): song=" + song + ", fileUrl=" + fileUrl);
						urls.add(new QUrl(fileUrl));
						*/
					}
					songIds.add((SongIdentifier)song.getIdentifier());
				}
			}
			mimeData.setUrls(urls);		
			if (artistIds.size() > 0) {
				QByteArray artistIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(artistIds));			
				mimeData.setData(MIME_TYPE_ARTIST_IDENTIFIER_LIST, artistIdsBytes);				
			}
			if (labelIds.size() > 0) {
				QByteArray labelIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(labelIds));			
				mimeData.setData(MIME_TYPE_LABEL_IDENTIFIER_LIST, labelIdsBytes);				
			}
			if (releaseIds.size() > 0) {
				QByteArray releaseIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(releaseIds));			
				mimeData.setData(MIME_TYPE_RELEASE_IDENTIFIER_LIST, releaseIdsBytes);				
			}
			if (songIds.size() > 0) {
				QByteArray songIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(songIds));			
				mimeData.setData(MIME_TYPE_SONG_IDENTIFIER_LIST, songIdsBytes);
			}			
		} catch (Exception e) {
			log.error("getMimeData(): error", e);
		}
		return mimeData;		
	}
	
	static public QMimeData getMimeDataForTrailInstances(Vector<TrailInstance> trailInstances) {
		QMimeData mimeData = new QMimeData();
		try {
			List<QUrl> urls = new ArrayList<QUrl>();
			Vector<ArtistIdentifier> artistIds = new Vector<ArtistIdentifier>();
			Vector<LabelIdentifier> labelIds = new Vector<LabelIdentifier>();
			Vector<ReleaseIdentifier> releaseIds = new Vector<ReleaseIdentifier>();
			Vector<SongIdentifier> songIds = new Vector<SongIdentifier>();			
			for (TrailInstance trailInstance : trailInstances) {
				SearchRecord searchRecord = (SearchRecord)trailInstance.getRecord();
				if (searchRecord instanceof ArtistRecord) {
					ArtistRecord artist = (ArtistRecord)searchRecord;
					artistIds.add(artist.getArtistIdentifier());
				} else if (searchRecord instanceof LabelRecord) {
					LabelRecord label = (LabelRecord)searchRecord;
					labelIds.add(label.getLabelIdentifier());
				} else if (searchRecord instanceof ReleaseRecord) {
					ReleaseRecord release = (ReleaseRecord)searchRecord;
					releaseIds.add(release.getReleaseIdentifier());
				} else if (searchRecord instanceof SongRecord) {
					SongRecord song = (SongRecord)searchRecord;
					String filename = song.getSongFilename();
					if ((filename != null) && (filename.length() > 0)) {
						File file = new File(filename);
						if (file.exists()) {
							urls.add(QUrl.fromLocalFile(filename));
						}
					}
					songIds.add((SongIdentifier)song.getIdentifier());
				}
			}
			mimeData.setUrls(urls);		
			if (artistIds.size() > 0) {
				QByteArray artistIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(artistIds));			
				mimeData.setData(MIME_TYPE_ARTIST_IDENTIFIER_LIST, artistIdsBytes);				
			}
			if (labelIds.size() > 0) {
				QByteArray labelIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(labelIds));			
				mimeData.setData(MIME_TYPE_LABEL_IDENTIFIER_LIST, labelIdsBytes);				
			}
			if (releaseIds.size() > 0) {
				QByteArray releaseIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(releaseIds));			
				mimeData.setData(MIME_TYPE_RELEASE_IDENTIFIER_LIST, releaseIdsBytes);				
			}
			if (songIds.size() > 0) {
				QByteArray songIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(songIds));			
				mimeData.setData(MIME_TYPE_SONG_IDENTIFIER_LIST, songIdsBytes);
			}			
			Vector<TrailInstance> globalIds = new Vector<TrailInstance>();
			for (TrailInstance trailInstance : trailInstances)			
				globalIds.add(trailInstance);
			QByteArray idBytes = new QByteArray(Serializer.encodeCompressedBytes(globalIds));			
			mimeData.setData(MIME_TYPE_TRAIL_INSTANCES_IDENTIFIER_LIST, idBytes); // this list preserves ordering of different elements, for trail view
		} catch (Exception e) {
			log.error("getMimeData(): error", e);
		}
		return mimeData;		
	}	
	
	static public QMimeData getMimeDataForFilterInstances(Vector<FilterHierarchyInstance> filterInstances) {
		QMimeData mimeData = new QMimeData();
		try {
			List<QUrl> urls = new ArrayList<QUrl>();
			Map<SongRecord, Object> songMap = new HashMap<SongRecord, Object>();
			Vector<StyleIdentifier> styleIds = new Vector<StyleIdentifier>();
			Vector<TagIdentifier> tagIds = new Vector<TagIdentifier>();
			Vector<PlaylistIdentifier> playlistIds = new Vector<PlaylistIdentifier>();
			Vector<StyleHierarchyInstance> styleInstances = new Vector<StyleHierarchyInstance>();
			Vector<TagHierarchyInstance> tagInstances = new Vector<TagHierarchyInstance>();
			Vector<PlaylistHierarchyInstance> playlistInstances = new Vector<PlaylistHierarchyInstance>();			
			for (TreeHierarchyInstance filterInstance : filterInstances) {
				if (filterInstance instanceof StyleHierarchyInstance) {
					StyleHierarchyInstance styleInstance = (StyleHierarchyInstance)filterInstance;
					StyleRecord style = (StyleRecord)styleInstance.getRecord();
					if (!styleIds.contains(style.getStyleIdentifier()))
						styleIds.add(style.getStyleIdentifier());
					styleInstances.add(styleInstance);
					Vector<SearchResult> matchedSongs = Database.getSongIndex().getSearchRecords(style);
					for (SearchResult matchedSong : matchedSongs)
						songMap.put((SongRecord)matchedSong.getRecord(), null);
				} else if (filterInstance instanceof TagHierarchyInstance) {
					TagHierarchyInstance tagInstance = (TagHierarchyInstance)filterInstance;
					TagRecord tag = (TagRecord)tagInstance.getRecord();
					if (!tagIds.contains(tag.getTagIdentifier()))
						tagIds.add(tag.getTagIdentifier());
					tagInstances.add(tagInstance);		
					Vector<SearchResult> matchedSongs = Database.getSongIndex().getSearchRecords(tag);
					for (SearchResult matchedSong : matchedSongs)
						songMap.put((SongRecord)matchedSong.getRecord(), null);					
				} else if (filterInstance instanceof PlaylistHierarchyInstance) {
					PlaylistHierarchyInstance playlistInstance = (PlaylistHierarchyInstance)filterInstance;
					PlaylistRecord playlist = (PlaylistRecord)playlistInstance.getRecord();
					if (!playlistIds.contains(playlist.getPlaylistIdentifier()))
						playlistIds.add(playlist.getPlaylistIdentifier());
					playlistInstances.add(playlistInstance);
					Vector<SearchResult> matchedSongs = Database.getSongIndex().getSearchRecords(playlist);
					for (SearchResult matchedSong : matchedSongs)
						songMap.put((SongRecord)matchedSong.getRecord(), null);					
				}
			}
			Iterator<SongRecord> iter = songMap.keySet().iterator();
			while (iter.hasNext()) {
				SongRecord song = iter.next();
				String filename = song.getSongFilename();
				if ((filename != null) && (filename.length() > 0))
					urls.add(QUrl.fromLocalFile(filename));				
			}			
			mimeData.setUrls(urls);
			if (styleIds.size() > 0) {
				QByteArray styleIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(styleIds));			
				mimeData.setData(MIME_TYPE_STYLE_IDENTIFIER_LIST, styleIdsBytes);				
				QByteArray styleInstancesBytes = new QByteArray(Serializer.encodeCompressedBytes(styleInstances));			
				mimeData.setData(MIME_TYPE_STYLE_INSTANCE_LIST, styleInstancesBytes);				
			}
			if (tagIds.size() > 0) {
				QByteArray tagIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(tagIds));			
				mimeData.setData(MIME_TYPE_TAG_IDENTIFIER_LIST, tagIdsBytes);				
				QByteArray tagInstancesBytes = new QByteArray(Serializer.encodeCompressedBytes(tagInstances));			
				mimeData.setData(MIME_TYPE_TAG_INSTANCE_LIST, tagInstancesBytes);				
			}
			if (playlistIds.size() > 0) {
				QByteArray playlistIdsBytes = new QByteArray(Serializer.encodeCompressedBytes(playlistIds));			
				mimeData.setData(MIME_TYPE_PLAYLIST_IDENTIFIER_LIST, playlistIdsBytes);				
				QByteArray playlistInstancesBytes = new QByteArray(Serializer.encodeCompressedBytes(playlistInstances));			
				mimeData.setData(MIME_TYPE_PLAYLIST_INSTANCE_LIST, playlistInstancesBytes);				
			}			
		} catch (Exception e) {
			log.error("getMimeData(): error", e);
		}
		return mimeData;		
	}
	
	static public Vector<ArtistRecord> getArtists(QMimeData mimeData) {
		Vector<ArtistRecord> result = new Vector<ArtistRecord>();
		try {
			QByteArray artistIdsBytes = mimeData.data(MIME_TYPE_ARTIST_IDENTIFIER_LIST);
			if ((artistIdsBytes != null) && (artistIdsBytes.size() > 0)) {
				Vector<ArtistIdentifier> artistIds = (Vector<ArtistIdentifier>)Serializer.decodeCompressedBytes(artistIdsBytes.toByteArray());
				for (ArtistIdentifier artistId : artistIds) {
					ArtistRecord artist = (ArtistRecord)Database.getRecord(artistId);
					result.add(artist);
				}
			}
		} catch (Exception e) {
			log.error("getArtists(): error", e);
		}
		return result;
	}
	
	static public Vector<LabelRecord> getLabels(QMimeData mimeData) {
		Vector<LabelRecord> result = new Vector<LabelRecord>();
		try {
			QByteArray labelIdsBytes = mimeData.data(MIME_TYPE_LABEL_IDENTIFIER_LIST);
			if ((labelIdsBytes != null) && (labelIdsBytes.size() > 0)) {
				Vector<LabelIdentifier> labelIds = (Vector<LabelIdentifier>)Serializer.decodeCompressedBytes(labelIdsBytes.toByteArray());
				for (LabelIdentifier labelId : labelIds) {
					LabelRecord label = (LabelRecord)Database.getRecord(labelId);
					result.add(label);
				}
			}
		} catch (Exception e) {
			log.error("getLabels(): error", e);
		}
		return result;
	}	
	
	static public Vector<ReleaseRecord> getReleases(QMimeData mimeData) {
		Vector<ReleaseRecord> result = new Vector<ReleaseRecord>();
		try {
			QByteArray releaseIdsBytes = mimeData.data(MIME_TYPE_RELEASE_IDENTIFIER_LIST);
			if ((releaseIdsBytes != null) && (releaseIdsBytes.size() > 0)) {
				Vector<ReleaseIdentifier> releaseIds = (Vector<ReleaseIdentifier>)Serializer.decodeCompressedBytes(releaseIdsBytes.toByteArray());
				for (ReleaseIdentifier releaseId : releaseIds) {
					ReleaseRecord release = (ReleaseRecord)Database.getRecord(releaseId);
					result.add(release);
				}
			}
		} catch (Exception e) {
			log.error("getReleases(): error", e);
		}
		return result;
	}	
	
	static public Vector<SongRecord> getSongs(QMimeData mimeData) {
		Vector<SongRecord> result = new Vector<SongRecord>();
		try {
			QByteArray songIdsBytes = mimeData.data(MIME_TYPE_SONG_IDENTIFIER_LIST);
			if ((songIdsBytes != null) && (songIdsBytes.size() > 0)) {
				Vector<SongIdentifier> songIds = (Vector<SongIdentifier>)Serializer.decodeCompressedBytes(songIdsBytes.toByteArray());
				for (SongIdentifier songId : songIds) {
					SongRecord song = (SongRecord)Database.getRecord(songId);
					result.add(song);
				}
			}
		} catch (Exception e) {
			log.error("getSongs(): error", e);
		}
		return result;
	}
	
	static public Vector<TrailInstance> getTrailInstances(QMimeData mimeData) {
		Vector<TrailInstance> result = new Vector<TrailInstance>();
		try {
			QByteArray idBytes = mimeData.data(MIME_TYPE_TRAIL_INSTANCES_IDENTIFIER_LIST);
			if ((idBytes != null) && (idBytes.size() > 0))
				result = (Vector<TrailInstance>)Serializer.decodeCompressedBytes(idBytes.toByteArray());			
		} catch (Exception e) {
			log.error("getSearchRecords(): error", e);
		}
		return result;
	}	
	
	static public Vector<StyleRecord> getStyles(QMimeData mimeData) {
		Vector<StyleRecord> result = new Vector<StyleRecord>();
		try {
			QByteArray styleIdsBytes = mimeData.data(MIME_TYPE_STYLE_IDENTIFIER_LIST);
			if ((styleIdsBytes != null) && (styleIdsBytes.size() > 0)) {
				Vector<StyleIdentifier> styleIds = (Vector<StyleIdentifier>)Serializer.decodeCompressedBytes(styleIdsBytes.toByteArray());
				for (StyleIdentifier styleId : styleIds) {
					StyleRecord style = (StyleRecord)Database.getRecord(styleId);
					result.add(style);
				}
			}
		} catch (Exception e) {
			log.error("getStyles(): error", e);
		}
		return result;
	}		
	
	static public Vector<TagRecord> getTags(QMimeData mimeData) {
		Vector<TagRecord> result = new Vector<TagRecord>();
		try {
			QByteArray tagIdsBytes = mimeData.data(MIME_TYPE_TAG_IDENTIFIER_LIST);
			if ((tagIdsBytes != null) && (tagIdsBytes.size() > 0)) {
				Vector<TagIdentifier> tagIds = (Vector<TagIdentifier>)Serializer.decodeCompressedBytes(tagIdsBytes.toByteArray());
				for (TagIdentifier tagId : tagIds) {
					TagRecord tag = (TagRecord)Database.getRecord(tagId);
					result.add(tag);
				}
			}
		} catch (Exception e) {
			log.error("getTags(): error", e);
		}
		return result;
	}	
	
	static public Vector<PlaylistRecord> getPlaylists(QMimeData mimeData) {
		Vector<PlaylistRecord> result = new Vector<PlaylistRecord>();
		try {
			QByteArray playlistIdsBytes = mimeData.data(MIME_TYPE_PLAYLIST_IDENTIFIER_LIST);
			if ((playlistIdsBytes != null) && (playlistIdsBytes.size() > 0)) {
				Vector<PlaylistIdentifier> playlistIds = (Vector<PlaylistIdentifier>)Serializer.decodeCompressedBytes(playlistIdsBytes.toByteArray());
				for (PlaylistIdentifier playlistId : playlistIds) {
					PlaylistRecord playlist = (PlaylistRecord)Database.getRecord(playlistId);
					result.add(playlist);
				}
			}
		} catch (Exception e) {
			log.error("getPlaylists(): error", e);
		}
		return result;
	}		
	
	static public Vector<TreeHierarchyInstance> getTreeInstances(QMimeData mimeData) {
		Vector<TreeHierarchyInstance> result = new Vector<TreeHierarchyInstance>();
		try {
			QByteArray styleInstanceBytes = mimeData.data(MIME_TYPE_STYLE_INSTANCE_LIST);
			if ((styleInstanceBytes != null) && (styleInstanceBytes.size() > 0)) {
				Vector<StyleHierarchyInstance> styleInstances = (Vector<StyleHierarchyInstance>)Serializer.decodeCompressedBytes(styleInstanceBytes.toByteArray());
				for (StyleHierarchyInstance styleInstance : styleInstances)
					result.add(Database.getStyleModelManager().getActualTreeHierarchyInstance(styleInstance));					
			}
			QByteArray tagInstanceBytes = mimeData.data(MIME_TYPE_TAG_INSTANCE_LIST);
			if ((tagInstanceBytes != null) && (tagInstanceBytes.size() > 0)) {
				Vector<TagHierarchyInstance> tagInstances = (Vector<TagHierarchyInstance>)Serializer.decodeCompressedBytes(tagInstanceBytes.toByteArray());
				for (TagHierarchyInstance tagInstance : tagInstances)
					result.add(Database.getTagModelManager().getActualTreeHierarchyInstance(tagInstance));
			}
			QByteArray playlistInstanceBytes = mimeData.data(MIME_TYPE_PLAYLIST_INSTANCE_LIST);
			if ((playlistInstanceBytes != null) && (playlistInstanceBytes.size() > 0)) {
				Vector<PlaylistHierarchyInstance> playlistInstances = (Vector<PlaylistHierarchyInstance>)Serializer.decodeCompressedBytes(playlistInstanceBytes.toByteArray());
				for (PlaylistHierarchyInstance playlistInstance : playlistInstances)
					result.add(Database.getPlaylistModelManager().getActualTreeHierarchyInstance(playlistInstance));					
			}			
		} catch (Exception e) {
			log.error("getStyles(): error", e);
		}
		return result;
	}		
	
	static public boolean containsSearchRecords(QMimeData mimeData) {
		for (String format : mimeData.formats()) {
			if (format.equals(MIME_TYPE_ARTIST_IDENTIFIER_LIST))
				return true;
			if (format.equals(MIME_TYPE_LABEL_IDENTIFIER_LIST))
				return true;
			if (format.equals(MIME_TYPE_RELEASE_IDENTIFIER_LIST))
				return true;
			if (format.equals(MIME_TYPE_SONG_IDENTIFIER_LIST))
				return true;
		}
		return false;
	}
	
	static public boolean containsFilterRecords(QMimeData mimeData) {
		for (String format : mimeData.formats()) {
			if (format.equals(MIME_TYPE_STYLE_IDENTIFIER_LIST))
				return true;
			if (format.equals(MIME_TYPE_TAG_IDENTIFIER_LIST))
				return true;
			if (format.equals(MIME_TYPE_PLAYLIST_IDENTIFIER_LIST))
				return true;
		}
		return false;		
	}

}
