package com.mixshare.rapid_evolution.workflow.importers.itunes;

import java.io.File;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.tags.TagManager;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.profile.Profile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.data.submitted.filter.playlist.SubmittedDynamicPlaylist;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.rating.Rating;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.ui.util.FileNameSelectThread;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.trolltech.qt.gui.QApplication;

public class ITunesImportTask extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(ITunesImportTask.class);
    static private final long serialVersionUID = 0L;    	

    private boolean silentMode;
    
    public ITunesImportTask(boolean silentMode) {
    	this.silentMode = silentMode;
    }
    
    public String toString() { return "Importing From iTunes"; }
    
    private String getITunesXMLLocation() {
    	String result = null;
    	try {
    		if (OSHelper.isWindowsVista() || OSHelper.isWindows7())
    			result = System.getProperty("user.home", ".") + "/Music/iTunes/iTunes Music Library.xml";
    		else if (OSHelper.isWindowsXP())
    			result = System.getProperty("user.home", ".") + "/My Documents/My Music/iTunes/iTunes Music Library.xml";
    		else if (OSHelper.isMacOS())
    			result = System.getProperty("user.home", ".") + "/Music/iTunes/iTunes Music Library.xml";
    	} catch (Exception e) {
    		log.error("getITunesXMLLocation(): error", e);
    	}
    	if (result != null) {
    		File file = new File(result);
    		if (!file.exists())
    			result = null;
    		else
    			log.warn("getITunesXMLLocation(): identified location did not exist=" + file);
    	} else {
    		log.warn("getITunesXMLLocation(): no pre-defined location determined");
    	}
    	return result;
    }
    
    public void execute() {
        try {
        	String iTunesXMLFilename = getITunesXMLLocation();
        	if ((iTunesXMLFilename == null) && !silentMode) {
        		FileNameSelectThread filenameSelected = new FileNameSelectThread(Translations.get("import_itunes_filter_text"), "(*.xml)");        		
        		QApplication.invokeAndWait(filenameSelected);
        		if (filenameSelected.getFilename() != null) {
        	        iTunesXMLFilename = filenameSelected.getFilename();
        	        if (log.isTraceEnabled())
        	        	log.trace("browseFilename(): selected filename=" + iTunesXMLFilename);
        	    }        		
        	}
        	if (iTunesXMLFilename != null) {        		
            	if (RE3Properties.getBoolean("show_progress_bars"))
            		QApplication.invokeLater(new TaskProgressLauncher(this));
        		if (log.isDebugEnabled())
        			log.debug("execute(): importing itunes filename=" + iTunesXMLFilename);        		
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();                
                Document document = builder.parse(new File(iTunesXMLFilename).toURI().toString());
                Element elem = document.getDocumentElement();
                NodeList root_nodelist = elem.getChildNodes();
                int songCount = 0;
                for (int root_iter = 0; root_iter < root_nodelist.getLength(); ++root_iter) {
                    Node root_node = root_nodelist.item(root_iter);
                    if (root_node.getNodeName().equalsIgnoreCase("?xml")) {
                    } else if (root_node.getNodeName().equalsIgnoreCase("dict")) {
                        NodeList l1_nodelist = root_node.getChildNodes();
                        for (int l1_iter = 0; l1_iter < l1_nodelist.getLength(); ++l1_iter) {
                            Node l1_node = l1_nodelist.item(l1_iter);
                            if (l1_node.getNodeName().equalsIgnoreCase("dict")) {
                                NodeList song_nodelist = l1_node.getChildNodes();
                                for (int songnode_iter = 0; songnode_iter < song_nodelist.getLength(); ++songnode_iter) {
                                    Node song_node = song_nodelist.item(songnode_iter);
                                    if (song_node.getNodeName().equalsIgnoreCase("dict")) {
                                        // a new song has been encountered
                                    	songCount++;
                                    }
                                }
                            }
                        }
                    }
                }
                int songsprocessed = 0;
                boolean onPlaylistKey = false;
                HashMap<String, Integer> trackIdMap = new HashMap<String, Integer>();      	      
                for (int root_iter = 0; root_iter < root_nodelist.getLength(); ++root_iter) {
                    Node root_node = root_nodelist.item(root_iter);
                    if (root_node.getNodeName().equalsIgnoreCase("?xml")) {
                    } else if (root_node.getNodeName().equalsIgnoreCase("dict")) {
                        NodeList l1_nodelist = root_node.getChildNodes();
                        for (int l1_iter = 0; l1_iter < l1_nodelist.getLength(); ++l1_iter) {
                            Node l1_node = l1_nodelist.item(l1_iter);
                            if (l1_node.getNodeName().equalsIgnoreCase("dict")) {
                                NodeList song_nodelist = l1_node.getChildNodes();
                                for (int songnode_iter = 0; songnode_iter < song_nodelist.getLength(); ++songnode_iter) {
                                    Node song_node = song_nodelist.item(songnode_iter);
                                    if (song_node.getNodeName().equalsIgnoreCase("dict")) {
                                        // a new song has been encountered
                                        songsprocessed++;
                                        setProgress(((float)songsprocessed) / songCount);
                                        if (RapidEvolution3.isTerminated || isCancelled()) 
                                        	return;
                                        try {
                                        	String artist = "";
                                        	String album = "";
                                        	String track = "";
                                        	String title = "";
                                        	String remix = "";
                                        	String iTunesID = null;
                                        	int playCount = 0;
                                        	String filename = null;
                                        	Vector<DegreeValue> styles = new Vector<DegreeValue>();
                                        	Duration duration = null;
                                        	float startBpm = 0.0f;
                                        	String comments = null;
    	                                    Rating rating = null;
    	                                    // assimilate data from key/value pairs
    	                                    String key = null;
    	                                    String value = null;
    	                                    NodeList song_proplist = song_node.getChildNodes();
    	                                    for (int prop_iter = 0; prop_iter < song_proplist.getLength(); ++prop_iter) {
    	                                        Node propNode = song_proplist.item(prop_iter);
    	                                        if (propNode.getNodeName().equalsIgnoreCase("key")) {
    	                                            if (propNode.getLastChild() != null) {
    	                                                key = propNode.getLastChild().getNodeValue().toLowerCase();
    	                                            }
    	                                        } else {
    	                                            if (propNode.getLastChild() != null) {
    	                                                value = propNode.getLastChild().getNodeValue();
    	                                                try {
    	                                                    value = URLDecoder.decode(value, "UTF-8");
    	                                                } catch (Exception e) { }
    	                                                // new key/value pair found
    	                                                if (key.equals("name")) title = value.trim();
    	                                                else if (key.equals("artist")) artist = value.trim();
    	                                                else if (key.equals("album")) album = value.trim();
    	                                                else if (key.equals("track id")) {
    	                                                	iTunesID = value;
    	                                                } else if (key.equals("track number")) {
    	                                                	if (value.length() == 1)
    	                                                		track = "0" + value;
    	                                                	else 
    	                                                		track = value.trim();
    	                                                } else if (key.equals("disc number")) {
    	                                                	if (value.length() == 1)
    	                                                		track = "0" + value;
    	                                                	else 
    	                                                		track = value.trim();
    	                                                } else if (key.equals("play count")) {
    	                                                	try { 
    	                                                		playCount = Integer.parseInt(value.trim());
    	                                                	} catch (Exception e) { }
    	                                                } else if (key.equals("location")) {
    	                                                	filename = value;
    	                                                	String prefix = "file://localhost/";
    	                                                	if (OSHelper.isWindows()) {    	                                                		
    	                                                		if (filename.toLowerCase().startsWith(prefix))
    	                                                			filename = filename.substring(prefix.length());    	                                                        	                                                     
    	                                                		while (filename.startsWith("/"))
    	                                                			filename = filename.substring(1);
    	                                                	} else {
    	                                                		if (filename.toLowerCase().startsWith(prefix)) 
    	                                                			filename = filename.substring(prefix.length() - 1);
    	                                                	}
    	                                                	if (filename.toLowerCase().endsWith("\\"))
    	                                                		filename = filename.substring(0, filename.length() - 1);    	                                                	
    	                                                	if (filename.toLowerCase().endsWith("/"))
    	                                                		filename = filename.substring(0, filename.length() - 1);    	                                                	
    	                                                } else if (key.equals("genre")) {
    	                                                	if (value.length() > 0)
    	                                                		styles.add(new DegreeValue(value, 1.0f, DATA_SOURCE_UNKNOWN));
    	                                                } else if (key.equals("total time")) {
    	                                                	try {
    	                                                		duration = new Duration(Integer.parseInt(value));    	                                                		
    	                                                	} catch (Exception e) { }
    	                                                } else if (key.equals("bpm")) {
    	                                                    try {
    	                                                    	startBpm = Float.parseFloat(value);
    	                                                    } catch (Exception e) { }
    	                                                } else if (key.equals("comments")) {
    	                                                    comments = value;
    	                                                } else if (key.equals("rating")) {
    	                                                	rating = Rating.getRating(Integer.parseInt(value));
    	                                                }
    	                                            }                                              
    	                                        }
    	                                    }
    	                                    if (title.toLowerCase().startsWith(artist.toLowerCase())) {
    	                                    	title = title.substring(artist.length()).trim();
    	                                    	if (title.startsWith("-"))
    	                                    		title = title.substring(1).trim();
        	                                    if (title.toLowerCase().startsWith(album.toLowerCase())) {
        	                                    	title = title.substring(album.length()).trim();
        	                                    	if (title.startsWith("-"))
        	                                    		title = title.substring(1).trim();    	                                    	
        	                                    }
        	                                    if (title.toLowerCase().startsWith("[" + track.toLowerCase()) || title.toLowerCase().startsWith("(" + track.toLowerCase())) {
        	                                    	title = title.substring(1 + track.length()).trim();
        	                                    	if (title.endsWith("]") || title.endsWith(")")) {
        	                                    		track = title.substring(0, title.length() - 1).trim();
        	                                    		title = "";
        	                                    	} else if (title.indexOf("]") > 0) {
        	                                    		int index = title.indexOf("]");
        	                                    		track = title.substring(0, index).trim();;
        	                                    		title = title.substring(index + 1).trim();
        	                                    	} else if (title.indexOf(")") > 0) {
        	                                    		int index = title.indexOf(")");
        	                                    		track = title.substring(0, index).trim();;
        	                                    		title = title.substring(index + 1).trim();
        	                                    	}
        	                                    }
    	                                    }
    	                                    File file = new File(filename);
    	                                    if (file.exists()) {
    	                                    	SubmittedSong submittedSong = null;
    	                                    	SongRecord existingSong = Database.getSongIndex().getSongRecord(filename);    	                                    	 
    	                                    	if (existingSong == null) {
    	                                    		submittedSong = TagManager.readTags(filename);
    	                                    	} else {
    	                                    		SongProfile existingProfile = Database.getSongIndex().getSongProfile(existingSong.getUniqueId());
    	                                    		if (existingProfile != null)
    	                                    			submittedSong = new SubmittedSong(existingProfile);
    	                                    	}
	    	                                    if (submittedSong == null) {
	    	                                    	submittedSong = new SubmittedSong(artist, album, track, title, remix);
	    	                                    	submittedSong.setSongFilename(filename);
	    	                                    } else {
	    	                                    	if (existingSong == null)
	    	                                    		submittedSong.setIdentifier(artist, album, track, title, remix);
	    	                                    }    	                                    	
	    	                                    submittedSong.setITunesID(iTunesID);   
	    	                                    
	    	                                    submittedSong.addStyleDegrees(styles);
	    	                                    submittedSong.setPlayCount(playCount);
	    	                                    submittedSong.setDuration(duration, DATA_SOURCE_UNKNOWN);
	    	                                    if (startBpm != 0.0f)
	    	                                    	submittedSong.setBpm(new Bpm(startBpm), Bpm.NO_BPM, (byte)0, DATA_SOURCE_UNKNOWN);
	    	                                    submittedSong.setComments(comments, DATA_SOURCE_UNKNOWN);
	    	                                    if ((rating != null) && rating.isValid())
	    	                                    	submittedSong.setRating(rating, DATA_SOURCE_UNKNOWN);
	    	                                    if (submittedSong.getIdentifier().isValid()) {
	    	                                    	Profile profile = Database.getSongIndex().addOrUpdate(submittedSong);
	    	                                    	trackIdMap.put(iTunesID, profile.getUniqueId());
	    	                                    }
    	                                    }
    	                                    
                                        } catch (Exception e) {
                                        	log.error("run(): error during import", e);
                                        }

                                    }
                                }
                                
                            } else if (l1_node.getNodeName().equalsIgnoreCase("key")) {
                            	if ("Playlists".equalsIgnoreCase(l1_node.getTextContent())) {
                            		onPlaylistKey = true; 
                            	}                            
                            } else if (l1_node.getNodeName().equalsIgnoreCase("array")) {
                                if (onPlaylistKey) {
                                    NodeList l2_nodelist = l1_node.getChildNodes();
                                    for (int l2_iter = 0; l2_iter < l2_nodelist.getLength(); ++l2_iter) {
                                        Node l2_node = l2_nodelist.item(l2_iter);
                                        if (l2_node.getNodeName().equalsIgnoreCase("dict")) {
                                            String playlistName = null;
                                            Vector<Integer> songIds = new Vector<Integer>();
                                            NodeList key_nodelist = l2_node.getChildNodes();
                                            for (int keynode_iter = 0; keynode_iter < key_nodelist.getLength(); ++keynode_iter) {
                                                Node key_node = key_nodelist.item(keynode_iter);
                                                if (key_node.getNodeName().equalsIgnoreCase("key")) {
                                                    if (key_node.getTextContent().equals("Name")) {
                                                        playlistName = key_nodelist.item(++keynode_iter).getTextContent();
                                                    }
                                                } else if (key_node.getNodeName().equalsIgnoreCase("array")) {
                                                    NodeList trackNodes = key_node.getChildNodes();
                                                    for (int t = 0; t < trackNodes.getLength(); ++t) {
                                                        Node trackNode = trackNodes.item(t);
                                                        if (trackNode.getNodeName().equals("dict")) {
                                                            NodeList trackAttributes = trackNode.getChildNodes();
                                                            for (int a = 0; a < trackAttributes.getLength(); ++a) {
                                                                Node trackAttr = trackAttributes.item(a);
                                                                if (trackAttr.getNodeName().equalsIgnoreCase("key") && trackAttr.getTextContent().equalsIgnoreCase("Track ID")) {
                                                                	Integer songId = trackIdMap.get(trackAttributes.item(++a).getTextContent());
                                                                	if (songId != null)
                                                                		songIds.add(songId);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (RE3Properties.getBoolean("import_itunes_playlists")) {
	                                            if (log.isTraceEnabled())
	                                                log.trace("run(): playlist name=" + playlistName + ", songIds=" + songIds);
	                                            if (songIds.size() > 0) {
	                                            	SubmittedDynamicPlaylist submittedPlaylist = new SubmittedDynamicPlaylist(playlistName);
	                                        		for (int songId : songIds)
	                                        			submittedPlaylist.addSongId(songId);
	                                        		Database.getPlaylistIndex().addOrUpdate(submittedPlaylist);
	                                            }
                                            }
                                        }
                                    }
                                }                                
                            }
                        }
                    }
                }        		
        		
        		Database.setHasImportedFromITunes();
        		SandmanThread.wakeUpAllBackgroundTasks();
        	} else {
        		log.warn("execute(): couldn't locate iTunes XML");
        	}
        } catch (Exception e) {
            log.error("execute(): error loading database", e);
        } finally {
        	if (RE3Properties.getBoolean("import_songs_from_itunes_automatically") && RE3Properties.getBoolean("server_mode"))
        		SandmanThread.putBackgroundTaskToSleep(this, 1000 * 60 * 60 * 4);
        }
    }
    
    public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
    
    public Object getResult() { return null; }

}
