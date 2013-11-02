package com.mixshare.rapid_evolution.workflow.importers.traktor;

import java.io.File;
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
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.submitted.search.song.SubmittedSong;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.bpm.Bpm;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.music.key.Key;
import com.mixshare.rapid_evolution.ui.dialogs.taskprogress.TaskProgressLauncher;
import com.mixshare.rapid_evolution.ui.util.FileNameSelectThread;
import com.mixshare.rapid_evolution.ui.util.Translations;
import com.mixshare.rapid_evolution.util.OSHelper;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;
import com.mixshare.rapid_evolution.workflow.SandmanThread;
import com.trolltech.qt.gui.QApplication;

public class TraktorImportTask extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(TraktorImportTask.class);
    static private final long serialVersionUID = 0L;    	

    private boolean silentMode;
    
    public TraktorImportTask(boolean silentMode) {
    	this.silentMode = silentMode;
    }
    
    public String toString() { return "Importing From Traktor"; }
    
    private String getTraktorNMLLocation() {
    	String result = null;
    	try {
    		if (OSHelper.isWindowsVista() || OSHelper.isWindows7())
    			result = System.getProperty("user.home", ".") + "/Traktor3/collection.nml";
    		else if (OSHelper.isWindowsXP())
    			result = System.getProperty("user.home", ".") + "/My Documents/Traktor3/collection.nml";
    		else if (OSHelper.isMacOS())
    			result = System.getProperty("user.home", ".") + "/Traktor3/collection.nml";
    	} catch (Exception e) {
    		log.error("getTraktorNMLLocation(): error", e);
    	}
    	if (result != null) {
    		File file = new File(result);
    		if (!file.exists())
    			result = null;
    		else
    			log.warn("getTraktorNMLLocation(): identified location did not exist=" + file);
    	} else {
    		log.warn("getTraktorNMLLocation(): no pre-defined location determined");
    	}
    	return result;
    }
    
    public void execute() {
        try {
        	String traktorNMLFilename = getTraktorNMLLocation();
        	if ((traktorNMLFilename == null) && !silentMode) {
        		FileNameSelectThread filenameSelected = new FileNameSelectThread(Translations.get("import_traktor_filter_text"), "(*.nml)");        		
        		QApplication.invokeAndWait(filenameSelected);
        		if (filenameSelected.getFilename() != null) {
        	        traktorNMLFilename = filenameSelected.getFilename();
        	        if (log.isTraceEnabled())
        	        	log.trace("browseFilename(): selected filename=" + traktorNMLFilename);
        	    }        		
        	}
        	if (traktorNMLFilename != null) {    
            	if (!silentMode && RE3Properties.getBoolean("show_progress_bars") && !RE3Properties.getBoolean("server_mode"))
            		QApplication.invokeLater(new TaskProgressLauncher(this));        	            	        	
        		
        		if (log.isDebugEnabled())
        			log.debug("execute(): importing traktor filename=" + traktorNMLFilename);
        		        		
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(new File(traktorNMLFilename).toURI().toString());
				Element elem = document.getDocumentElement();
				int nml_version = Integer.parseInt(elem.getAttribute("VERSION"));
				if (log.isDebugEnabled()) 
					log.debug("execute(): nml version=" + nml_version);
				if (nml_version < 12) {
					NodeList root_nodelist = elem.getChildNodes();
					int songcount = 0;
					for (int root_iter = 0; root_iter < root_nodelist.getLength(); ++root_iter) {
						Node root_node = root_nodelist.item(root_iter);
						if (root_node.getNodeName().equalsIgnoreCase("?xml")) {
						} else if (root_node.getNodeName().equalsIgnoreCase("PLAYLIST")) {
							NodeList song_nodelist = root_node.getChildNodes();
							for (int songnode_iter = 0; songnode_iter < song_nodelist.getLength(); ++songnode_iter) {
								Node song_node = song_nodelist.item(songnode_iter);
								if (song_node.getNodeName().equalsIgnoreCase("ENTRY")) {
									// a new song has been encountered
									songcount++;
								}
							}
						}
					}
					if (log.isTraceEnabled())
						log.trace("run(): counted " + songcount + " songs");
					int songsprocessed = 0;
					for (int root_iter = 0; root_iter < root_nodelist.getLength(); ++root_iter) {
						Node root_node = root_nodelist.item(root_iter);
						if (root_node.getNodeName().equalsIgnoreCase("?xml")) {
						} else if (root_node.getNodeName().equalsIgnoreCase("PLAYLIST")) {
							NodeList song_nodelist = root_node.getChildNodes();
							for (int songnode_iter = 0; songnode_iter < song_nodelist.getLength(); ++songnode_iter) {
								Node song_node = song_nodelist.item(songnode_iter);
								if (song_node.getNodeName().equalsIgnoreCase("ENTRY")) {
									// a new song has been encountered
									try {
										songsprocessed++;
										if (RapidEvolution3.isTerminated || isCancelled())
											return;
										setProgress(((float)songsprocessed) / songcount);
										
										String artist = getAttributeValue(song_node, "ARTIST");
										String release = null;
										String track = null;
										String totalTracks = null;
										String title = getAttributeValue(song_node, "TITLE");
										String filename = null;
										String genre = null;
										String comments = null;
										Key key = null;
										Duration duration = null;
										Bpm bpm = null;
										float bpmAccuracy = 0.0f;
										
										// assimilate data from key/value pairs
										NodeList song_proplist = song_node.getChildNodes();
										for (int prop_iter = 0; prop_iter < song_proplist.getLength(); ++prop_iter) {
											Node propNode = song_proplist.item(prop_iter);
											if (propNode.getNodeName().equalsIgnoreCase("LOCATION")) {
												filename = getAttributeValue(propNode, "DIR") + getAttributeValue(propNode, "FILE");
												String volume = getAttributeValue( propNode, "VOLUME");
												if ((volume != null) && (volume.length() > 1) && (volume.charAt(1) == ':'))
													filename = volume + filename;												
											} else if (propNode.getNodeName().equalsIgnoreCase("ALBUM")) {
												totalTracks = getAttributeValue(propNode, "OF_TRACKS");
												release = getAttributeValue(propNode, "TITLE");
												track = getAttributeValue(propNode, "TRACK");												
											} else if (propNode.getNodeName().equalsIgnoreCase("INFO")) {
												genre = getAttributeValue(propNode, "GENRE");
												comments = getAttributeValue(propNode, "COMMENT");
												key = Key.getKey(getAttributeValue(propNode, "KEY"));
												try {
													duration = new Duration(Integer.parseInt(getAttributeValue(propNode, "PLAYTIME")) * 1000);
												} catch (Exception e) { }												
											} else if (propNode.getNodeName().equalsIgnoreCase("TEMPO")) {
												try {
													bpm = new Bpm(Float.parseFloat(getAttributeValue(propNode, "BPM")));
												} catch (Exception e) { }
												try {
													bpmAccuracy = Float.parseFloat(getAttributeValue(propNode, "BPM_QUALITY"));
												} catch (Exception e) { }
											} else if (propNode.getNodeName().equalsIgnoreCase("LOUDNESS")) {
											} else if (propNode.getNodeName().equalsIgnoreCase("EXTDATA")) {
											}
										}
										
										if (artist == null)
											artist = "";
										if (release == null)
											release = "";
										if (track == null)
											track = "";
										if (title == null)
											title = "";
										if ((totalTracks != null) && (totalTracks.length() > 0))
											track += "/" + totalTracks;
										
										SubmittedSong newSong = new SubmittedSong(artist, release, track, title, "");
										newSong.setSongFilename(filename);
										if ((genre != null) && (genre.length() > 0)) {
											Vector<DegreeValue> styles = new Vector<DegreeValue>(1);
											styles.add(new DegreeValue(genre, 0.0f, DATA_SOURCE_UNKNOWN));
											newSong.setStyleDegrees(styles);
										}
										newSong.setComments(comments, DATA_SOURCE_UNKNOWN);
										newSong.setKey(key, Key.NO_KEY, (byte)0, DATA_SOURCE_UNKNOWN);
										newSong.setDuration(duration, DATA_SOURCE_UNKNOWN);
										newSong.setBpm(bpm, Bpm.NO_BPM, (byte)bpmAccuracy, DATA_SOURCE_UNKNOWN);

										try {
											Database.addOrUpdate(newSong);
										} catch (Exception e) {
											log.error("execute(): error adding song=" + newSong, e);
										}
										
									} catch (Exception e) {
										log.error("execute(): error", e);
									}
								}
							}
						}
					}
				} else {
					// nml version is >= 12
					NodeList root_nodelist = elem.getChildNodes();
					int songcount = 0;
					for (int root_iter = 0; root_iter < root_nodelist.getLength(); ++root_iter) {
						Node root_node = root_nodelist.item(root_iter);
						if (root_node.getNodeName().equalsIgnoreCase("?xml")) {
						} else if (root_node.getNodeName().equalsIgnoreCase("COLLECTION")) {
							NodeList song_nodelist = root_node.getChildNodes();
							for (int songnode_iter = 0; songnode_iter < song_nodelist.getLength(); ++songnode_iter) {
								Node song_node = song_nodelist.item(songnode_iter);
								if (song_node.getNodeName().equalsIgnoreCase("ENTRY")) {
									// a new song has been encountered
									songcount++;
								}
							}
						}
					}
					if (log.isTraceEnabled())
						log.trace("run(): counted " + songcount + " songs");
					int songsprocessed = 0;
					for (int root_iter = 0; root_iter < root_nodelist
							.getLength(); ++root_iter) {
						Node root_node = root_nodelist.item(root_iter);
						if (root_node.getNodeName().equalsIgnoreCase("?xml")) {
						} else if (root_node.getNodeName().equalsIgnoreCase(
								"COLLECTION")) {
							NodeList song_nodelist = root_node.getChildNodes();
							for (int songnode_iter = 0; songnode_iter < song_nodelist.getLength(); ++songnode_iter) {
								Node song_node = song_nodelist.item(songnode_iter);
								if (song_node.getNodeName().equalsIgnoreCase("ENTRY")) {
									// a new song has been encountered
									try {
										songsprocessed++;
										if (RapidEvolution3.isTerminated || isCancelled())
											return;

										String artist = getAttributeValue(song_node, "ARTIST");
										String release = null;
										String track = null;
										String totalTracks = null;
										String title = getAttributeValue(song_node, "TITLE");
										String filename = null;
										String genre = null;
										String comments = null;
										Key key = null;
										Duration duration = null;
										Bpm bpm = null;
										float bpmAccuracy = 0.0f;
										
										// assimilate data from key/value pairs
										NodeList song_proplist = song_node.getChildNodes();
										for (int prop_iter = 0; prop_iter < song_proplist.getLength(); ++prop_iter) {
											Node propNode = song_proplist.item(prop_iter);
											if (propNode.getNodeName().equalsIgnoreCase("LOCATION")) {
												String dir = getAttributeValue(propNode, "DIR");
												dir = StringUtil.replace(dir, "/:", "/");
												filename = dir + getAttributeValue(propNode,"FILE");
												String volume = getAttributeValue(propNode, "VOLUME");
												if ((volume != null) && (volume.length() > 1) && (volume.charAt(1) == ':'))
													filename = volume + filename;												
											} else if (propNode.getNodeName().equalsIgnoreCase("ALBUM")) {
												totalTracks = getAttributeValue(propNode, "OF_TRACKS");
												release = getAttributeValue(propNode, "TITLE");
												track = getAttributeValue(propNode, "TRACK");
											} else if (propNode.getNodeName().equalsIgnoreCase("INFO")) {
												genre = getAttributeValue(propNode, "GENRE");
												comments = getAttributeValue(propNode, "COMMENT");
												key = Key.getKey(getAttributeValue(propNode, "KEY"));
												try {
													duration = new Duration(Integer.parseInt(getAttributeValue(propNode, "PLAYTIME")) * 1000);
												} catch (Exception e) { }												
											} else if (propNode.getNodeName().equalsIgnoreCase("TEMPO")) {
												try {
													bpm = new Bpm(Float.parseFloat(getAttributeValue(propNode, "BPM")));
												} catch (Exception e) { }
												try {
													bpmAccuracy = Float.parseFloat(getAttributeValue(propNode, "BPM_QUALITY"));
												} catch (Exception e) { }
											} else if (propNode.getNodeName().equalsIgnoreCase("LOUDNESS")) {
											} else if (propNode.getNodeName().equalsIgnoreCase("EXTDATA")) {
											}
										}

										if (artist == null)
											artist = "";
										if (release == null)
											release = "";
										if (track == null)
											track = "";
										if (title == null)
											title = "";
										if ((totalTracks != null) && (totalTracks.length() > 0))
											track += "/" + totalTracks;
										
										SubmittedSong newSong = new SubmittedSong(artist, release, track, title, "");
										newSong.setSongFilename(filename);
										if ((genre != null) && (genre.length() > 0)) {
											Vector<DegreeValue> styles = new Vector<DegreeValue>(1);
											styles.add(new DegreeValue(genre, 0.0f, DATA_SOURCE_UNKNOWN));
											newSong.setStyleDegrees(styles);
										}
										newSong.setComments(comments, DATA_SOURCE_UNKNOWN);
										newSong.setKey(key, Key.NO_KEY, (byte)0, DATA_SOURCE_UNKNOWN);
										newSong.setDuration(duration, DATA_SOURCE_UNKNOWN);
										newSong.setBpm(bpm, Bpm.NO_BPM, (byte)bpmAccuracy, DATA_SOURCE_UNKNOWN);

										try {
											Database.addOrUpdate(newSong);
										} catch (Exception e) {
											log.error("execute(): error adding song=" + newSong, e);
										}
									} catch (Exception e) {
										log.error("execute(): error", e);
									}
								}
							}
						}
					}
				}        		
        		Database.setHasImportedFromTraktor();
        		SandmanThread.wakeUpAllBackgroundTasks();
        	} else {
        		log.warn("execute(): couldn't locate traktor NML");
        	}
        } catch (Exception e) {
            log.error("execute(): error loading database", e);
        }
    }
    
    public int getTaskPriority() { return RE3Properties.getInt("default_task_priority"); }
    
    public Object getResult() { return null; }

    static private String getAttributeValue(Node node, String id) {
        Node attribute_node = node.getAttributes().getNamedItem(id);
        if (attribute_node != null)
            return attribute_node.getNodeValue().trim();        
        return null;
    }
    
}
