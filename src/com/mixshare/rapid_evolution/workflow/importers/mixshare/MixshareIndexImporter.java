package com.mixshare.rapid_evolution.workflow.importers.mixshare;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class MixshareIndexImporter extends CommonTask implements DataConstants {

    static private Logger log = Logger.getLogger(MixshareIndexImporter.class);
	
	public void execute() {
		try {
			log.info("execute(): reading mixshare artist index");
			/*
			CommonIndex artistIndex = (CommonIndex)Serializer.readData(RE3Properties.getProperty("mixshare_artist_index_filename"));
			artistIndex.loadSearchRecords(RE3Properties.getProperty("mixshare_artist_index_filename") + ".search");
			log.info("execute(): checking against artists");
			for (int artistId : Database.getArtistIndex().getIds()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				com.mixshare.rapid_evolution.data.record.search.artist.ArtistRecord artistRecord = Database.getArtistIndex().getArtistRecord(artistId);
				if (artistRecord != null) {
					ArtistRecord mixshareArtistRecord = (ArtistRecord)artistIndex.getSearchRecord(new ArtistIdentifier(artistRecord.getArtistName()));
					if (mixshareArtistRecord != null) {
						MixshareArtistProfile mixshareProfile = new MixshareArtistProfile(artistRecord.getArtistName());
						Vector<DegreeValue> styles = new Vector<DegreeValue>(mixshareArtistRecord.getNumStyles());
						for (int s = 0; s < mixshareArtistRecord.getNumStyles(); ++s)
							styles.add(new DegreeValue(artistIndex.getStyleIdTable().getValueFromId(mixshareArtistRecord.getStyleId(s)), mixshareArtistRecord.getStyleDegree(s), DATA_SOURCE_MIXSHARE));
						mixshareProfile.setStyles(styles);
						ArtistProfile artistProfile = Database.getArtistIndex().getArtistProfile(artistId);
						if (artistProfile != null) {
							artistProfile.addMinedProfile(mixshareProfile);
							artistProfile.save();							
							log.info("execute(): artist=" + artistRecord.getArtistName() + ", added styles=" + styles);
						}
					} else {
						log.debug("execute(): no result for artist=" + artistRecord.getArtistName());
					}
				}
			}
			*/
			log.info("execute(): done!");
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}
	
	static public void main(String[] args) {
		try {
			RapidEvolution3.loadLog4J();			
			new MixshareIndexImporter().execute();			
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}
	
}
