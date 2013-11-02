package com.mixshare.rapid_evolution.workflow.importers.re3;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.filter.playlist.PlaylistIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.style.StyleIdentifier;
import com.mixshare.rapid_evolution.data.identifier.filter.tag.TagIdentifier;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.CategoryPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.DynamicPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.OrderedPlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.playlist.PlaylistProfile;
import com.mixshare.rapid_evolution.data.profile.filter.style.StyleProfile;
import com.mixshare.rapid_evolution.data.profile.filter.tag.TagProfile;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.filter.playlist.PlaylistRecord;
import com.mixshare.rapid_evolution.data.record.filter.style.StyleRecord;
import com.mixshare.rapid_evolution.data.record.filter.tag.TagRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedCategoryPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedOrderedPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedPlaylist;
import com.mixshare.rapid_evolution.data.submitted.filter.style.SubmittedStyle;
import com.mixshare.rapid_evolution.data.submitted.filter.tag.SubmittedTag;
import com.mixshare.rapid_evolution.data.submitted.search.artist.SubmittedArtist;
import com.mixshare.rapid_evolution.data.submitted.search.label.SubmittedLabel;
import com.mixshare.rapid_evolution.data.submitted.search.release.SubmittedRelease;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.data.util.io.XMLSerializer;
import com.mixshare.rapid_evolution.ui.UIProperties;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.trolltech.qt.gui.QApplication;

public class RE3DatabaseImport extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(RE3DatabaseImport.class);
    static private final long serialVersionUID = 0L;    	

    private float baseProgress = 0.0f;
    private float progressIncrement = 1.0f / 7.0f;
    private String rootDirectory = null;
    
    public RE3DatabaseImport(String rootDirectory) {
    	this.rootDirectory = rootDirectory;
    }
    
    public String toString() { return "Importing From RE3"; }
    
    public void execute() {
        try {   
        	log.info("execute(): importing directory=" + rootDirectory);
        	
        	if (!RE3Properties.getBoolean("server_mode"))
        		UIProperties.loadFile(rootDirectory + "/ui.properties");
        	RE3Properties.loadUserProperties(rootDirectory + "/user_settings.properties");
        	if (RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
        		QApplication.invokeLater(new TaskProgressLauncher(this));        	            	        	
        	
        	// styles
        	Map<Integer, StyleRecord> styleMap = new HashMap<Integer, StyleRecord>();
        	Map<Integer, StyleRecord> duplicateStyleMap = new HashMap<Integer, StyleRecord>();
        	styleMap.put(1, (StyleRecord)Database.getStyleIndex().getRootRecord());        	
        	File styleDir = new File(rootDirectory + "/profiles/style");
        	File[] styleFiles = styleDir.listFiles();
        	if (styleFiles != null) {
        		int p = 0;
	        	for (File styleFile : styleFiles) {	        		
	        		if (RapidEvolution3.isTerminated || isCancelled())
	        			return;
	        		StyleProfile style = (StyleProfile)XMLSerializer.readData(styleFile.getAbsolutePath());
	        		if (style != null) {
	        			if (log.isDebugEnabled())
	        				log.debug("execute(): adding style=" + style + ", id=" + style.getUniqueId());
	        			styleMap.put(style.getUniqueId(), style.getStyleRecord());
	        			for (int i = 0 ; i < style.getNumDuplicateIds(); ++i)
	        				duplicateStyleMap.put(style.getDuplicateId(i), style.getStyleRecord());
	        			if (!style.getStyleName().equals("***ROOT STYLE***")) {
	        				SubmittedStyle submittedStyle = new SubmittedStyle(style);
	        				submittedStyle.setDoNotAddToHierarchy(true);
	        				Database.addOrUpdate(submittedStyle);
	        			} else {
	        				styleMap.put(style.getUniqueId(), (StyleRecord)Database.getStyleIndex().getRootRecord());
	        			}
	        		}
	        		setProgress(((float)++p) / styleFiles.length + baseProgress);
	        	}
        	}
        	// style hierarchy
			if (log.isDebugEnabled())
				log.debug("execute(): updating style hierarchy...");
            Database.doUpdateFilterAndParentsOnHierarchyChanged = false;
        	for (StyleRecord styleRecord : styleMap.values()) {
        		if (RapidEvolution3.isTerminated || isCancelled())
        			return;
        		if (!duplicateStyleMap.containsKey(styleRecord.getUniqueId())) {
	        		StyleRecord localStyle = Database.getStyleIndex().getStyleRecord(new StyleIdentifier(styleRecord.getStyleName()));
	        		if ((localStyle == null) && (styleRecord.getStyleName().equals("***ROOT STYLE***")))
	        			localStyle = (StyleRecord)Database.getStyleIndex().getRootRecord();
	        		if (localStyle != null) {
	        			if (styleRecord.getParentIds() != null) {
			        		for (int parentId : styleRecord.getParentIds()) {
			        			StyleRecord parentRecord = styleMap.get(parentId);
			        			if (parentRecord == null)
			        				parentRecord = duplicateStyleMap.get(parentId);
			        			if (parentRecord != null) {
			        				StyleRecord localParent = Database.getStyleIndex().getStyleRecord(new StyleIdentifier(parentRecord.getStyleName()));
			        				if (localParent != null) {
			        					if (log.isDebugEnabled())
			        						log.debug("execute(): adding parent relationship, parent=" + localParent + ", child=" + localStyle);
			        					Database.getStyleIndex().addRelationship(localParent, localStyle);
			        				} else if (parentRecord.getStyleName().equals("***ROOT STYLE***")) {
			        					if (log.isDebugEnabled())
			        						log.debug("execute(): adding parent relationship, parent=root, child=" + localStyle);
			        					Database.getStyleIndex().addRelationship(Database.getStyleIndex().getRootRecord(), localStyle);
			        				} else {
			        					log.warn("execute(): couldn't find local parent=" + parentRecord.getStyleName());
			        				}
			        			} else {
			        				log.warn("execute(): non existent parent in map?=" + parentId);
			        			}
			        		}
	        			}
	        			if (styleRecord.getChildIds() != null) {
			        		for (int childId : styleRecord.getChildIds()) {
			        			StyleRecord childRecord = styleMap.get(childId);
			        			if (childRecord == null)
			        				childRecord = duplicateStyleMap.get(childId);
			        			if (childRecord != null) {
			        				StyleRecord localChild = Database.getStyleIndex().getStyleRecord(new StyleIdentifier(childRecord.getStyleName()));
			        				if (localChild != null) {
			        					if (log.isDebugEnabled())
			        						log.debug("execute(): adding child relationship, parent=" + localStyle + ", child=" + localChild);
			        					Database.getStyleIndex().addRelationship(localStyle, localChild);
			        				} else {
			        					log.warn("execute(): couldn't find local child=" + childRecord.getStyleName());
			        				}
			        			} else {
			        				log.warn("execute(): non existent child in map?=" + childId);
			        			}
			        		}
	        			}
	        		} else {
	        			log.warn("execute(): couldn't find local style=" + styleRecord.getStyleName());
	        		}
        		}
        	}
        	if (!RE3Properties.getBoolean("server_mode"))
        		Database.getStyleModelManager().reset();
            Database.doUpdateFilterAndParentsOnHierarchyChanged = true;
            baseProgress += this.progressIncrement;
        	
        	// tags
        	Map<Integer, TagRecord> tagMap = new HashMap<Integer, TagRecord>();
        	Map<Integer, TagRecord> duplicateTagMap = new HashMap<Integer, TagRecord>();
        	tagMap.put(1, (TagRecord)Database.getTagIndex().getRootRecord());        	
        	File tagDir = new File(rootDirectory + "/profiles/tag");
        	File[] tagFiles = tagDir.listFiles();
        	if (tagFiles != null) {
        		int p = 0;
	        	for (File tagFile : tagFiles) {
	        		if (RapidEvolution3.isTerminated || isCancelled())
	        			return;
	        		TagProfile tag = (TagProfile)XMLSerializer.readData(tagFile.getAbsolutePath());
	        		if (tag != null) {
	        			if (log.isDebugEnabled())
	        				log.debug("execute(): adding tag=" + tag);
	        			tagMap.put(tag.getUniqueId(), tag.getTagRecord());
	        			for (int i = 0 ; i < tag.getNumDuplicateIds(); ++i)
	        				duplicateTagMap.put(tag.getDuplicateId(i), tag.getTagRecord());
	        			if (!tag.getTagName().equals("***ROOT TAG***")) {
	        				SubmittedTag submittedTag = new SubmittedTag(tag);
	        				submittedTag.setDoNotAddToHierarchy(true);
	        				Database.addOrUpdate(submittedTag);
	        			} else {
	        				tagMap.put(tag.getUniqueId(), (TagRecord)Database.getTagIndex().getRootRecord());
	        			}
	        		}
	        		setProgress(((float)++p) / tagFiles.length + baseProgress);
	        	}   
	        }
        	// tag hierarchy
			if (log.isDebugEnabled())
				log.debug("execute(): updating tag hierarchy...");
            Database.doUpdateFilterAndParentsOnHierarchyChanged = false;
        	for (TagRecord tagRecord : tagMap.values()) {
        		if (RapidEvolution3.isTerminated || isCancelled())
        			return;
				if (!duplicateTagMap.containsKey(tagRecord.getUniqueId())) {
	        		TagRecord localTag = Database.getTagIndex().getTagRecord(new TagIdentifier(tagRecord.getTagName()));
	        		if ((localTag == null) && (tagRecord.getTagName().equals("***ROOT TAG***")))
	        			localTag = (TagRecord)Database.getTagIndex().getRootRecord();
	        		if (localTag != null) {
	        			if (tagRecord.getParentIds() != null) {
			        		for (int parentId : tagRecord.getParentIds()) {
			        			TagRecord parentRecord = tagMap.get(parentId);
			        			if (parentRecord == null)
			        				parentRecord = duplicateTagMap.get(parentId);
			        			if (parentRecord != null) {
			        				TagRecord localParent = Database.getTagIndex().getTagRecord(new TagIdentifier(parentRecord.getTagName()));
			        				if (localParent != null) {
			        					Database.getTagIndex().addRelationship(localParent, localTag);
			        				} else if (parentRecord.getTagName().equals("***ROOT TAG***")) {
			        					Database.getTagIndex().addRelationship(Database.getTagIndex().getRootRecord(), localTag);
			        				} else {
			        					log.warn("execute(): couldn't find local parent=" + parentRecord.getTagName());
			        				}
			        			} else {
			        				log.warn("execute(): non existent parent in map?=" + parentId);
			        			}
			        		}
	        			}
		        		if (tagRecord.getChildIds() != null) {
			        		for (int childId : tagRecord.getChildIds()) {
			        			TagRecord childRecord = tagMap.get(childId);
			        			if (childRecord == null)
			        				childRecord = duplicateTagMap.get(childId);
			        			if (childRecord != null) {
			        				TagRecord localChild = Database.getTagIndex().getTagRecord(new TagIdentifier(childRecord.getTagName()));
			        				if (localChild != null) {
			        					Database.getTagIndex().addRelationship(localTag, localChild);
			        				} else {
			        					log.warn("execute(): couldn't find local child=" + childRecord.getTagName());
			        				}
			        			} else {
			        				log.warn("execute(): non existent child in map?=" + childId);
			        			}
			        		}
		        		}
	        		} else {
	        			log.warn("execute(): couldn't find local tag=" + tagRecord.getTagName());
	        		}
	        	}
        	}    
        	if (!RE3Properties.getBoolean("server_mode"))
        		Database.getTagModelManager().reset();
            Database.doUpdateFilterAndParentsOnHierarchyChanged = true;
            baseProgress += this.progressIncrement;
        	
        	// playlists
        	Map<Integer, PlaylistRecord> playlistMap = new HashMap<Integer, PlaylistRecord>();
        	Map<Integer, PlaylistRecord> duplicatePlaylistMap = new HashMap<Integer, PlaylistRecord>();
        	playlistMap.put(1, (PlaylistRecord)Database.getPlaylistIndex().getRootRecord());        	
        	File playlistDir = new File(rootDirectory + "/profiles/playlist");
        	File[] playlistFiles = playlistDir.listFiles();
        	if (playlistFiles != null) {
        		int p = 0;
	        	for (File playlistFile : playlistFiles) {
	        		if (RapidEvolution3.isTerminated || isCancelled())
	        			return;
	        		PlaylistProfile playlist = (PlaylistProfile)XMLSerializer.readData(playlistFile.getAbsolutePath());
	        		if (playlist != null) {
	        			if (log.isDebugEnabled())
	        				log.debug("execute(): adding playlist=" + playlist);
	        			playlistMap.put(playlist.getUniqueId(), playlist.getPlaylistRecord());
	        			for (int i = 0 ; i < playlist.getNumDuplicateIds(); ++i)
	        				duplicatePlaylistMap.put(playlist.getDuplicateId(i), playlist.getPlaylistRecord());
	        			if (!playlist.getPlaylistName().equals("***ROOT PLAYLIST***")) {
	            			SubmittedPlaylist submittedPlaylist = null;
	            			if (playlist instanceof OrderedPlaylistProfile)
	            				submittedPlaylist = new SubmittedOrderedPlaylist((OrderedPlaylistProfile)playlist);
	            			else if (playlist instanceof DynamicPlaylistProfile)
	            				submittedPlaylist = new SubmittedDynamicPlaylist((DynamicPlaylistProfile)playlist);
	            			else if (playlist instanceof CategoryPlaylistProfile)
	            				submittedPlaylist = new SubmittedCategoryPlaylist((CategoryPlaylistProfile)playlist);
	            			submittedPlaylist.setDoNotAddToHierarchy(true);
	            			Database.addOrUpdate(submittedPlaylist);
	        			} else {
	        				playlistMap.put(playlist.getUniqueId(), (PlaylistRecord)Database.getPlaylistIndex().getRootRecord());
	        			}
	        		}
	        		setProgress(((float)++p) / playlistFiles.length + baseProgress);
	        	}          
        	}
        	// playlist hierarchy
			if (log.isDebugEnabled())
				log.debug("execute(): updating playlist hierarchy...");
            Database.doUpdateFilterAndParentsOnHierarchyChanged = false;
        	for (PlaylistRecord playlistRecord : playlistMap.values()) {
        		if (RapidEvolution3.isTerminated || isCancelled())
        			return;
        		if (!duplicatePlaylistMap.containsKey(playlistRecord.getUniqueId())) {        		
	        		PlaylistRecord localPlaylist = Database.getPlaylistIndex().getPlaylistRecord(new PlaylistIdentifier(playlistRecord.getPlaylistName()));
	        		if ((localPlaylist == null) && (playlistRecord.getPlaylistName().equals("***ROOT PLAYLIST***")))
	        			localPlaylist = (PlaylistRecord)Database.getPlaylistIndex().getRootRecord();
	        		if (localPlaylist != null) {
	        			if (playlistRecord.getParentIds() != null) {
			        		for (int parentId : playlistRecord.getParentIds()) {
			        			PlaylistRecord parentRecord = playlistMap.get(parentId);
			        			if (parentRecord == null)
			        				parentRecord = duplicatePlaylistMap.get(parentId);
			        			if (parentRecord != null) {
			        				PlaylistRecord localParent = Database.getPlaylistIndex().getPlaylistRecord(new PlaylistIdentifier(parentRecord.getPlaylistName()));
			        				if (localParent != null) {
			        					Database.getPlaylistIndex().addRelationship(localParent, localPlaylist);
			        				} else if (parentRecord.getPlaylistName().equals("***ROOT PLAYLIST***")) {
			        					Database.getPlaylistIndex().addRelationship(Database.getPlaylistIndex().getRootRecord(), localPlaylist);
			        				} else {
			        					log.warn("execute(): couldn't find local parent=" + parentRecord.getPlaylistName());
			        				}
			        			} else {
			        				log.warn("execute(): non existent parent in map?=" + parentId);
			        			}
			        		}
	        			}
	        			if (playlistRecord.getChildIds() != null) {
			        		for (int childId : playlistRecord.getChildIds()) {
			        			PlaylistRecord childRecord = playlistMap.get(childId);
			        			if (childRecord == null)
			        				childRecord = duplicatePlaylistMap.get(childId);
			        			if (childRecord != null) {
			        				PlaylistRecord localChild = Database.getPlaylistIndex().getPlaylistRecord(new PlaylistIdentifier(childRecord.getPlaylistName()));
			        				if (localChild != null) {
			        					Database.getPlaylistIndex().addRelationship(localPlaylist, localChild);
			        				} else {
			        					log.warn("execute(): couldn't find local child=" + childRecord.getPlaylistName());
			        				}
			        			} else {
			        				log.warn("execute(): non existent child in map?=" + childId);
			        			}
			        		}
	        			}
	        		} else {
	        			log.warn("execute(): couldn't find local playlist=" + playlistRecord.getPlaylistName());
	        		}
        		}
        	}    
        	playlistMap.clear();
        	if (!RE3Properties.getBoolean("server_mode"))
        		Database.getPlaylistModelManager().reset();
            Database.doUpdateFilterAndParentsOnHierarchyChanged = true;
            baseProgress += this.progressIncrement;
        	
        	// artists
        	File artistDir = new File(rootDirectory + "/profiles/artist");
        	File[] artistFiles = artistDir.listFiles();
        	if (artistFiles != null) {
        		int p = 0;
	        	for (File artistFile : artistFiles) {
	        		if (RapidEvolution3.isTerminated || isCancelled())
	        			return;
	        		ArtistProfile artist = (ArtistProfile)XMLSerializer.readData(artistFile.getAbsolutePath());
	        		if (artist != null) {
	        			if (log.isDebugEnabled())
	        				log.debug("execute(): adding artist=" + artist);
	        			SubmittedArtist submittedArtist = new SubmittedArtist(artist);
	
	        			Vector<DegreeValue> styleDegrees = new Vector<DegreeValue>();
	        			int[] sourceStyleIds = artist.getArtistRecord().getSourceStyleIds();
	        			float[] sourceStyleDegrees = artist.getArtistRecord().getSourceStyleDegrees();
	        			byte[] sourceStyleSources = artist.getArtistRecord().getSourceStyleSources();
	        			if (sourceStyleIds != null) {
	        				for (int i = 0; i < sourceStyleIds.length; ++i) {
	        					StyleRecord style = styleMap.get(sourceStyleIds[i]);
	        					if (style != null)
	        						styleDegrees.add(new DegreeValue(style.getStyleName(), sourceStyleDegrees[i], sourceStyleSources[i]));
	        				}
	        			}
	        			submittedArtist.setStyleDegrees(styleDegrees);
	
	        			Vector<DegreeValue> tagDegrees = new Vector<DegreeValue>();
	        			int[] sourceTagIds = artist.getArtistRecord().getSourceTagIds();
	        			float[] sourceTagDegrees = artist.getArtistRecord().getSourceTagDegrees();
	        			byte[] sourceTagSources = artist.getArtistRecord().getSourceTagSources();
	        			if (sourceTagIds != null) {
	        				for (int i = 0; i < sourceTagIds.length; ++i) {
	        					TagRecord tag = tagMap.get(sourceTagIds[i]);
	        					if (tag != null)
	        						tagDegrees.add(new DegreeValue(tag.getTagName(), sourceTagDegrees[i], sourceTagSources[i]));
	        				}
	        			}
	        			submittedArtist.setTagDegrees(tagDegrees);
	
	        			try {
		        			if (artist.hasThumbnail()) {
			        			String oldThumbnailImageRelativeFilename = artist.getSearchRecord().getThumbnailImageFilename();
			        			String oldThumbnailImageFilename = rootDirectory + "/" + oldThumbnailImageRelativeFilename;
								BufferedImage image = ImageIO.read(new File(oldThumbnailImageFilename));
								if (image != null) {
		    						String newFilename = artist.getSearchRecord().getThumbnailImageFilename();
									String extension = FileUtil.getExtension(newFilename);
									if ((extension != null) && (extension.length() > 0))
										// replace type to .jpg
										newFilename = newFilename.substring(0, newFilename.length() - extension.length() - 1) + ".jpg";
									
									FileSystemAccess.getFileSystem().saveImage(newFilename, image, false);
									submittedArtist.setThumbnailImage(newFilename, artist.getThumbnailImageFilenameSource());
								} else {
									log.warn("execute(): couldn't read thumbnail file=" + oldThumbnailImageRelativeFilename);
								}
		        			}
	        			} catch (Exception e) {
	        				log.error("execute(): error transferring thumbnail for=" + artist, e);
	        			}
	        			
	        			Vector<Image> newImages = new Vector<Image>();
	        			for (Image oldImage : artist.getImages()) {
	        				try {
	        					if (!oldImage.isDisabled()) {
			        				String oldImageFilename = oldImage.getImageFilename();
			        				String imageFile = rootDirectory + "/" + oldImageFilename;
			        				File file = new File(imageFile);
			        				if (!file.exists()) {
			        					log.warn("execute(): couldn't find image=" + oldImage.getImageFilename());
			        				} else {
			        					BufferedImage image = ImageIO.read(file);
			        					if (image != null) {
			        						String newFilename = oldImage.getImageFilename();
											String extension = FileUtil.getExtension(newFilename);
											if ((extension != null) && (extension.length() > 0))
												// replace type to .jpg
												newFilename = newFilename.substring(0, newFilename.length() - extension.length() - 1) + ".jpg";
			        						FileSystemAccess.getFileSystem().saveImage(newFilename, image, false);
			        						newImages.add(new Image(oldImage.getUrl(), newFilename, oldImage.getDataSource(), oldImage.getDescription()));
			        					} else {
			        						log.warn("execute(): couldn't read image=" + file);
			        					}        					
			        				}
	        					}
	        				} catch (Exception e) {
	        					log.error("execute(): error transferring image=" + oldImage);
	        				}        				
	        			}
	        			submittedArtist.setImages(newImages);
	        			
	        			Database.addOrUpdate(submittedArtist);
	        		}
	        		setProgress(((float)++p) / artistFiles.length + baseProgress);
	        	}
        	}
        	baseProgress += this.progressIncrement;

        	// labels
        	File labelDir = new File(rootDirectory + "/profiles/label");
        	File[] labelFiles = labelDir.listFiles();
        	if (labelFiles != null) {
        		int p = 0;
	        	for (File labelFile : labelFiles) {
	        		if (RapidEvolution3.isTerminated || isCancelled())
	        			return;
	        		LabelProfile label = (LabelProfile)XMLSerializer.readData(labelFile.getAbsolutePath());
	        		if (label != null) {
	        			if (log.isDebugEnabled())
	        				log.debug("execute(): adding label=" + label);
	        			SubmittedLabel submittedLabel = new SubmittedLabel(label);
	
	        			Vector<DegreeValue> styleDegrees = new Vector<DegreeValue>();
	        			int[] sourceStyleIds = label.getLabelRecord().getSourceStyleIds();
	        			float[] sourceStyleDegrees = label.getLabelRecord().getSourceStyleDegrees();
	        			byte[] sourceStyleSources = label.getLabelRecord().getSourceStyleSources();
	        			if (sourceStyleIds != null) {
	        				for (int i = 0; i < sourceStyleIds.length; ++i) {
	        					StyleRecord style = styleMap.get(sourceStyleIds[i]);
	        					if (style != null)
	        						styleDegrees.add(new DegreeValue(style.getStyleName(), sourceStyleDegrees[i], sourceStyleSources[i]));
	        				}
	        			}
	        			submittedLabel.setStyleDegrees(styleDegrees);
	
	        			Vector<DegreeValue> tagDegrees = new Vector<DegreeValue>();
	        			int[] sourceTagIds = label.getLabelRecord().getSourceTagIds();
	        			float[] sourceTagDegrees = label.getLabelRecord().getSourceTagDegrees();
	        			byte[] sourceTagSources = label.getLabelRecord().getSourceTagSources();
	        			if (sourceTagIds != null) {
	        				for (int i = 0; i < sourceTagIds.length; ++i) {
	        					TagRecord tag = tagMap.get(sourceTagIds[i]);
	        					if (tag != null)
	        						tagDegrees.add(new DegreeValue(tag.getTagName(), sourceTagDegrees[i], sourceTagSources[i]));
	        				}
	        			}
	        			submittedLabel.setTagDegrees(tagDegrees);
	        			
	        			try {
		        			if (label.hasThumbnail()) {
			        			String oldThumbnailImageRelativeFilename = label.getSearchRecord().getThumbnailImageFilename();
			        			String oldThumbnailImageFilename = rootDirectory + "/" + oldThumbnailImageRelativeFilename;
								BufferedImage image = ImageIO.read(new File(oldThumbnailImageFilename));
								if (image != null) {
		    						String newFilename = label.getSearchRecord().getThumbnailImageFilename();
									String extension = FileUtil.getExtension(newFilename);
									if ((extension != null) && (extension.length() > 0))
										// replace type to .jpg
										newFilename = newFilename.substring(0, newFilename.length() - extension.length() - 1) + ".jpg";
									
									FileSystemAccess.getFileSystem().saveImage(newFilename, image, false);
									submittedLabel.setThumbnailImage(newFilename, label.getThumbnailImageFilenameSource());
								} else {
									log.warn("execute(): couldn't read thumbnail file=" + oldThumbnailImageRelativeFilename);
								}
		        			}
	        			} catch (Exception e) {
	        				log.error("execute(): error transferring thumbnail for=" + label, e);
	        			}
	        			
	        			Vector<Image> newImages = new Vector<Image>();
	        			for (Image oldImage : label.getImages()) {
	        				try {
	        					if (!oldImage.isDisabled()) {
			        				String oldImageFilename = oldImage.getImageFilename();
			        				String imageFile = rootDirectory + "/" + oldImageFilename;
			        				File file = new File(imageFile);
			        				if (!file.exists()) {
			        					log.warn("execute(): couldn't find image=" + oldImage.getImageFilename());
			        				} else {
			        					BufferedImage image = ImageIO.read(file);
			        					if (image != null) {
			        						String newFilename = oldImage.getImageFilename();
											String extension = FileUtil.getExtension(newFilename);
											if ((extension != null) && (extension.length() > 0))
												// replace type to .jpg
												newFilename = newFilename.substring(0, newFilename.length() - extension.length() - 1) + ".jpg";
			        						FileSystemAccess.getFileSystem().saveImage(newFilename, image, false);
			        						newImages.add(new Image(oldImage.getUrl(), newFilename, oldImage.getDataSource(), oldImage.getDescription()));
			        					} else {
			        						log.warn("execute(): couldn't read image=" + file);
			        					}        					
			        				}
	        					}
	        				} catch (Exception e) {
	        					log.error("execute(): error transferring image=" + oldImage);
	        				}        				
	        			}
	        			submittedLabel.setImages(newImages);
	        			
	        			Database.addOrUpdate(submittedLabel);
	        		}
	        		setProgress(((float)++p) / labelFiles.length + baseProgress);
	        	}
        	}
        	baseProgress += this.progressIncrement;
        	
        	// releases
        	File releaseDir = new File(rootDirectory + "/profiles/release");
        	File[] releaseFiles = releaseDir.listFiles();
        	if (releaseFiles != null) {
        		int p = 0;
	        	for (File releaseFile : releaseFiles) {
	        		if (RapidEvolution3.isTerminated || isCancelled())
	        			return;
	        		ReleaseProfile release = (ReleaseProfile)XMLSerializer.readData(releaseFile.getAbsolutePath());
	        		if (release != null) {
	        			if (log.isDebugEnabled())
	        				log.debug("execute(): adding release=" + release);
	        			SubmittedRelease submittedRelease = new SubmittedRelease(release);
	
	        			Vector<DegreeValue> styleDegrees = new Vector<DegreeValue>();
	        			int[] sourceStyleIds = release.getReleaseRecord().getSourceStyleIds();
	        			float[] sourceStyleDegrees = release.getReleaseRecord().getSourceStyleDegrees();
	        			byte[] sourceStyleSources = release.getReleaseRecord().getSourceStyleSources();
	        			if (sourceStyleIds != null) {
	        				for (int i = 0; i < sourceStyleIds.length; ++i) {
	        					StyleRecord style = styleMap.get(sourceStyleIds[i]);
	        					if (style != null)
	        						styleDegrees.add(new DegreeValue(style.getStyleName(), sourceStyleDegrees[i], sourceStyleSources[i]));
	        				}
	        			}
	        			submittedRelease.setStyleDegrees(styleDegrees);
	
	        			Vector<DegreeValue> tagDegrees = new Vector<DegreeValue>();
	        			int[] sourceTagIds = release.getReleaseRecord().getSourceTagIds();
	        			float[] sourceTagDegrees = release.getReleaseRecord().getSourceTagDegrees();
	        			byte[] sourceTagSources = release.getReleaseRecord().getSourceTagSources();
	        			if (sourceTagIds != null) {
	        				for (int i = 0; i < sourceTagIds.length; ++i) {
	        					TagRecord tag = tagMap.get(sourceTagIds[i]);
	        					if (tag != null)
	        						tagDegrees.add(new DegreeValue(tag.getTagName(), sourceTagDegrees[i], sourceTagSources[i]));
	        				}
	        			}
	        			submittedRelease.setTagDegrees(tagDegrees);
	        			
	        			try {
		        			if (release.hasThumbnail()) {
			        			String oldThumbnailImageRelativeFilename = release.getSearchRecord().getThumbnailImageFilename();
			        			String oldThumbnailImageFilename = rootDirectory + "/" + oldThumbnailImageRelativeFilename;
								BufferedImage image = ImageIO.read(new File(oldThumbnailImageFilename));
								if (image != null) {
		    						String newFilename = release.getSearchRecord().getThumbnailImageFilename();
									String extension = FileUtil.getExtension(newFilename);
									if ((extension != null) && (extension.length() > 0))
										// replace type to .jpg
										newFilename = newFilename.substring(0, newFilename.length() - extension.length() - 1) + ".jpg";
									
									FileSystemAccess.getFileSystem().saveImage(newFilename, image, false);
									submittedRelease.setThumbnailImage(newFilename, release.getThumbnailImageFilenameSource());
								} else {
									log.warn("execute(): couldn't read thumbnail file=" + oldThumbnailImageRelativeFilename);
								}
		        			}
	        			} catch (Exception e) {
	        				log.error("execute(): error transferring thumbnail for=" + release, e);
	        			}
	        			
	        			Vector<Image> newImages = new Vector<Image>();
	        			for (Image oldImage : release.getImages()) {
	        				try {
	        					if (!oldImage.isDisabled()) {
			        				String oldImageFilename = oldImage.getImageFilename();
			        				String imageFile = rootDirectory + "/" + oldImageFilename;
			        				File file = new File(imageFile);
			        				if (!file.exists()) {
			        					log.warn("execute(): couldn't find image=" + oldImage.getImageFilename());
			        				} else {
			        					BufferedImage image = ImageIO.read(file);
			        					if (image != null) {
			        						String newFilename = oldImage.getImageFilename();
											String extension = FileUtil.getExtension(newFilename);
											if ((extension != null) && (extension.length() > 0))
												// replace type to .jpg
												newFilename = newFilename.substring(0, newFilename.length() - extension.length() - 1) + ".jpg";
			        						FileSystemAccess.getFileSystem().saveImage(newFilename, image, false);
			        						newImages.add(new Image(oldImage.getUrl(), newFilename, oldImage.getDataSource(), oldImage.getDescription()));
			        					} else {
			        						log.warn("execute(): couldn't read image=" + file);
			        					}        					
			        				}
	        					}
	        				} catch (Exception e) {
	        					log.error("execute(): error transferring image=" + oldImage);
	        				}        				
	        			}
	        			submittedRelease.setImages(newImages);
	        			
	        			Database.addOrUpdate(submittedRelease);
	        		}
	        		setProgress(((float)++p) / releaseFiles.length + baseProgress);
	        	}
        	}
        	baseProgress += this.progressIncrement;

        	// songs
        	File songDir = new File(rootDirectory + "/profiles/song");
        	File[] songFiles = songDir.listFiles();
        	if (songFiles != null) {
        		int p = 0;
	        	for (File songFile : songFiles) {
	        		if (RapidEvolution3.isTerminated || isCancelled())
	        			return;
	        		SongProfile song = (SongProfile)XMLSerializer.readData(songFile.getAbsolutePath());
	        		if (song != null) {
	        			if (log.isDebugEnabled())
	        				log.debug("execute(): adding song=" + song);
	        			SubmittedSong submittedSong = new SubmittedSong(song);
	
	        			Vector<DegreeValue> styleDegrees = new Vector<DegreeValue>();
	        			int[] sourceStyleIds = song.getSongRecord().getSourceStyleIds();
	        			float[] sourceStyleDegrees = song.getSongRecord().getSourceStyleDegrees();
	        			byte[] sourceStyleSources = song.getSongRecord().getSourceStyleSources();
	        			if (sourceStyleIds != null) {
	        				for (int i = 0; i < sourceStyleIds.length; ++i) {
	        					StyleRecord style = styleMap.get(sourceStyleIds[i]);
	        					if (style != null)
	        						styleDegrees.add(new DegreeValue(style.getStyleName(), sourceStyleDegrees[i], sourceStyleSources[i]));
	        				}
	        			}
	        			submittedSong.setStyleDegrees(styleDegrees);
	
	        			Vector<DegreeValue> tagDegrees = new Vector<DegreeValue>();
	        			int[] sourceTagIds = song.getSongRecord().getSourceTagIds();
	        			float[] sourceTagDegrees = song.getSongRecord().getSourceTagDegrees();
	        			byte[] sourceTagSources = song.getSongRecord().getSourceTagSources();
	        			if (sourceTagIds != null) {
	        				for (int i = 0; i < sourceTagIds.length; ++i) {
	        					TagRecord tag = tagMap.get(sourceTagIds[i]);
	        					if (tag != null)
	        						tagDegrees.add(new DegreeValue(tag.getTagName(), sourceTagDegrees[i], sourceTagSources[i]));
	        				}
	        			}
	        			submittedSong.setTagDegrees(tagDegrees);
	
	        			try {
		        			if (song.hasThumbnail()) {
			        			String oldThumbnailImageRelativeFilename = song.getSearchRecord().getThumbnailImageFilename();
			        			String oldThumbnailImageFilename = rootDirectory + "/" + oldThumbnailImageRelativeFilename;
								BufferedImage image = ImageIO.read(new File(oldThumbnailImageFilename));
								if (image != null) {
		    						String newFilename = song.getSearchRecord().getThumbnailImageFilename();
									String extension = FileUtil.getExtension(newFilename);
									if ((extension != null) && (extension.length() > 0))
										// replace type to .jpg
										newFilename = newFilename.substring(0, newFilename.length() - extension.length() - 1) + ".jpg";
									
									FileSystemAccess.getFileSystem().saveImage(newFilename, image, false);
									submittedSong.setThumbnailImage(newFilename, song.getThumbnailImageFilenameSource());
								} else {
									log.warn("execute(): couldn't read thumbnail file=" + oldThumbnailImageRelativeFilename);
								}
		        			}
	        			} catch (Exception e) {
	        				log.error("execute(): error transferring thumbnail for=" + song, e);
	        			}
	        			
	        			Vector<Image> newImages = new Vector<Image>();
	        			for (Image oldImage : song.getImages()) {
	        				try {
	        					if (!oldImage.isDisabled()) {
			        				String oldImageFilename = oldImage.getImageFilename();
			        				String imageFile = rootDirectory + "/" + oldImageFilename;
			        				File file = new File(imageFile);
			        				if (!file.exists()) {
			        					log.warn("execute(): couldn't find image=" + oldImage.getImageFilename());
			        				} else {
			        					BufferedImage image = ImageIO.read(file);
			        					if (image != null) {
			        						String newFilename = oldImage.getImageFilename();
											String extension = FileUtil.getExtension(newFilename);
											if ((extension != null) && (extension.length() > 0))
												// replace type to .jpg
												newFilename = newFilename.substring(0, newFilename.length() - extension.length() - 1) + ".jpg";
			        						FileSystemAccess.getFileSystem().saveImage(newFilename, image, false);
			        						newImages.add(new Image(oldImage.getUrl(), newFilename, oldImage.getDataSource(), oldImage.getDescription()));
			        					} else {
			        						log.warn("execute(): couldn't read image=" + file);
			        					}        					
			        				}
	        					}
	        				} catch (Exception e) {
	        					log.error("execute(): error transferring image=" + oldImage);
	        				}        				
	        			}
	        			submittedSong.setImages(newImages);
	        			
	        			Database.addOrUpdate(submittedSong);
	        		}
	        		setProgress(((float)++p) / songFiles.length + baseProgress);
	        	}
        	}
        	baseProgress += this.progressIncrement;
        	
        } catch (Exception e) {
            log.error("execute(): error loading database", e);
        }
    	log.info("execute(): done!");
    }
    
    public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
    
    public Object getResult() { return null; }

}
