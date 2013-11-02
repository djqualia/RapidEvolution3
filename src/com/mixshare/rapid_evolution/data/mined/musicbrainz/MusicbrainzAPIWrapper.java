package com.mixshare.rapid_evolution.data.mined.musicbrainz;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.shared.JenaException;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.identifier.search.release.ReleaseIdentifier;
import com.mixshare.rapid_evolution.data.mined.CommonMiningAPIWrapper;
import com.mixshare.rapid_evolution.data.mined.MinedProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.artist.MusicbrainzArtistProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.label.MusicbrainzLabelProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.release.MusicbrainzReleaseProfile;
import com.mixshare.rapid_evolution.data.mined.musicbrainz.song.MusicbrainzSongProfile;
import com.mixshare.rapid_evolution.data.mined.util.WebHelper;
import com.mixshare.rapid_evolution.data.profile.search.artist.ArtistProfile;
import com.mixshare.rapid_evolution.data.profile.search.label.LabelProfile;
import com.mixshare.rapid_evolution.data.profile.search.release.ReleaseProfile;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord;

/**
 * http://musicbrainz.org/doc/XMLWebService
 */
public class MusicbrainzAPIWrapper extends CommonMiningAPIWrapper {

    static private Logger log = Logger.getLogger(MusicbrainzAPIWrapper.class);
    static private final long serialVersionUID = 0L;
	    
	public byte getDataSource() { return DATA_SOURCE_MUSICBRAINZ; }
	
	public MinedProfile fetchArtistProfile(ArtistProfile artistProfile) {
		if (artistProfile.getMbId() != null)
			return new MusicbrainzArtistProfile(artistProfile.getMbId(), true);
		return new MusicbrainzArtistProfile(artistProfile.getArtistName());
	}
	public MinedProfile fetchLabelProfile(LabelProfile labelProfile) { return new MusicbrainzLabelProfile(labelProfile.getLabelName()); }
	public MinedProfile fetchReleaseProfile(ReleaseProfile releaseProfile) { return new MusicbrainzReleaseProfile(releaseProfile.getArtistsDescription(), releaseProfile.getReleaseTitle()); }
	public MinedProfile fetchSongProfile(SongProfile songProfile) { return new MusicbrainzSongProfile(songProfile.getArtistsDescription(), songProfile.getSongDescription()); }	
		
	public String getLocalArtistName(String artistMbid) {
		for (int artistId : Database.getArtistIndex().getIds()) {
			ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(artistId);
			if ((artistRecord != null) && (artistRecord.getMbId() != null)) {								
				if (artistMbid.equals(artistRecord.getMbId())) 
					return artistRecord.getArtistName();
			}
		}
		return null;
	}
	
	public String getArtistMbId(String artistName) {
		try {
			getRateController().startQuery();
			StringBuffer url = new StringBuffer("http://musicbrainz.org/ws/1/artist/?type=xml&name=");
            String encodedArtistName = URLEncoder.encode(artistName, "UTF-8");
            url.append(encodedArtistName);			            
			Document doc = WebHelper.getDocumentResponseFromURL(url.toString());
			if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();	            
	            NodeList artistNodes = (NodeList)xPath.evaluate("//metadata/artist-list/artist[@score='100']", doc, XPathConstants.NODESET);
	            Vector<String> possibleMbids = new Vector<String>();
	            for (int i = 0; i < artistNodes.getLength(); ++i) {
	            	Node artistNode = artistNodes.item(i);
	            	possibleMbids.add(artistNode.getAttributes().getNamedItem("id").getTextContent());
	            }
	            if (possibleMbids.size() == 1)
	            	return possibleMbids.get(0);
	            else if (possibleMbids.size() > 1) {
					Vector<Integer> releaseMatches = new Vector<Integer>(possibleMbids.size());
					int i = 0;
					for (String artistMbid : possibleMbids) {						
						Set<ReleaseIdentifier> releaseIds = getReleaseIdentifiers(artistMbid);
						int releaseCount = 0;
						for (ReleaseIdentifier releaseId : releaseIds) {
							if (Database.getReleaseIndex().doesExist(releaseId))
								++releaseCount;
						}						
						releaseMatches.add(releaseCount);
						++i;
					}
					int maxIndex = -1;
					for (i = 0; i < releaseMatches.size(); ++i)
						if ((maxIndex == -1) || (releaseMatches.get(i) > releaseMatches.get(maxIndex)))
							maxIndex = i;    			
					if (releaseMatches.get(maxIndex) > 0)
						return possibleMbids.get(maxIndex);					
	            }
			}
			
			/*
			 * This code seems to be malfunctioning, I think the API is obsolete...
			MusicBrainzImpl server = new MusicBrainzImpl();
			getRateController().startQuery();
			Model results = server.findArtistByName(artistName, MusicbrainzArtistProfile.MUSICBRAINZ_ARTIST_QUERY_DEPTH, 10);
			if (results != null) {
				List<Artist> artists = BeanPopulator.getArtists(results);
				Vector<Artist> possibleMatches = new Vector<Artist>();
				for (Artist artist : artists) {
					if (artist.getName().equalsIgnoreCase(artistName))
						possibleMatches.add(artist);    			
				}
				if (possibleMatches.size() == 1) {
					return possibleMatches.get(0).getId();
				} else if (possibleMatches.size() > 1) {
					Vector<Integer> releaseMatches = new Vector<Integer>(possibleMatches.size());
					int i = 0;
					for (Artist artist : possibleMatches) {
						int releaseCount = 0;
						List<Album> albums = artist.getAlbums();
						for (Album album : albums) {
							ReleaseIdentifier releaseId = album.isCompilation() ? new ReleaseIdentifier(album.getName()) : new ReleaseIdentifier(album.getArtist().getName(), album.getName());
							if (Database.getReleaseIndex().doesExist(releaseId))
								++releaseCount;
						}
						releaseMatches.add(releaseCount);
						++i;
					}
					int maxIndex = -1;
					for (i = 0; i < releaseMatches.size(); ++i)
						if ((maxIndex == -1) || (releaseMatches.get(i) > releaseMatches.get(maxIndex)))
							maxIndex = i;    			
					if (releaseMatches.get(maxIndex) > 0)
						return possibleMatches.get(maxIndex).getId();
				}
			} else {
				if (log.isDebugEnabled())
					log.debug("getArtistMbId(): results are null");
			}
			*/
		} catch (com.ldodds.musicbrainz.MusicBrainzException mbe) {
			if (log.isDebugEnabled())
				log.debug("getArtistMbId(): music brainz exception=" + mbe);						
		} catch (java.io.IOException ie) {
			if (log.isDebugEnabled())
				log.debug("getArtistMbId(): IO exception=" + ie);			
		} catch (JenaException je) {
			if (log.isDebugEnabled())
				log.debug("getArtistMbId(): jena exception=" + je);
		} catch (Exception e) {
			log.error("getArtistMbId(): error", e);
		}
		return null;
	}
	
	// used to disambiguate artists with the same name
	private Set<ReleaseIdentifier> getReleaseIdentifiers(String artistMbid) {
		Map<ReleaseIdentifier, Object> releaseIdMap = new HashMap<ReleaseIdentifier, Object>(); // using a map prevents duplicate ReleaseIdentifiers from being returned
		try {			
			getRateController().startQuery();			
			StringBuffer url = new StringBuffer("http://musicbrainz.org/ws/1/artist/");
			url.append(artistMbid);
			url.append("?type=xml&inc=release-rels");
			Document doc = WebHelper.getDocumentResponseFromURL(url.toString());
			if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();
	            NodeList releaseNodes = (NodeList)xPath.evaluate("//metadata/artist/relation-list/relation/release", doc, XPathConstants.NODESET);
	            for (int i = 0; i < releaseNodes.getLength(); ++i) {
	            	Node releaseNode = releaseNodes.item(i);
	            	String releaseTitle = (String)xPath.evaluate("./title/text()", releaseNode, XPathConstants.STRING);	            	
	            	NodeList artistNodes = (NodeList)xPath.evaluate("./artist", releaseNode, XPathConstants.NODESET);
	            	if (artistNodes.getLength() == 1) {
	            		// regular release
	            		String artistName = (String)xPath.evaluate("./artist/name/text()", releaseNode, XPathConstants.STRING);
	            		releaseIdMap.put(new ReleaseIdentifier(artistName, releaseTitle), null);
	            	} else {
	            		// compilation?
	            		releaseIdMap.put(new ReleaseIdentifier(releaseTitle), null);
	            	}
	            }
			}			 			
 		} catch (Exception e) {
 			log.error("getReleaseIdentifiers(): error", e);
 		}
 		return releaseIdMap.keySet();
	}
	
	public String getLabelMbid(String labelName) {
		String result = null;
		try {			
			StringBuffer labelSearchURL = new StringBuffer();
			labelSearchURL.append("http://musicbrainz.org/ws/1/label/?type=xml&name=");
            String encodedLabelName = URLEncoder.encode(labelName, "UTF-8");
            labelSearchURL.append(encodedLabelName);			            
            getRateController().startQuery();
			if (log.isTraceEnabled())
				log.trace("getLabelMbid(): search for label with url=" +  labelSearchURL.toString());
			Document doc = WebHelper.getDocumentResponseFromURL(labelSearchURL.toString());
			if (doc != null) {
	            XPathFactory factory = XPathFactory.newInstance();
	            XPath xPath = factory.newXPath();
	            
	            Vector<String> labelIds = new Vector<String>();
	            Vector<String> labelNames = new Vector<String>();
	            
	            // ids
	            NodeList labelIdNodes = (NodeList)xPath.evaluate("//metadata/label-list/label", doc, XPathConstants.NODESET);
	            for (int i = 0; i < labelIdNodes.getLength(); i++) 
	            	labelIds.add(labelIdNodes.item(i).getAttributes().getNamedItem("id").getTextContent());
	            // names
	            NodeList labelNameNodes = (NodeList)xPath.evaluate("//metadata/label-list/label/name", doc, XPathConstants.NODESET);
	            for (int i = 0; i < labelNameNodes.getLength(); i++) 
	            	labelNames.add(labelNameNodes.item(i).getTextContent());
	            
	            int i = 0;
	            for (String resultLabelName : labelNames) {
	            	if (resultLabelName.equalsIgnoreCase(labelName)) {
	            		result = labelIds.get(i);
	            		break;
	            	}
	            	++i;
	            }	            
			}    										
		} catch (Exception e) {
			log.error("getLabelMbid(): error", e);
		}		
		return result;
	}
	
 }
