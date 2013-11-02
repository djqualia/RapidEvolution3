package com.mixshare.rapid_evolution.workflow.maintenance;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.Database;
import com.mixshare.rapid_evolution.data.mined.echonest.song.EchonestSongProfile;
import com.mixshare.rapid_evolution.data.mined.echonest.song.EchonestSongSegment;
import com.mixshare.rapid_evolution.data.profile.search.song.SongProfile;
import com.mixshare.rapid_evolution.data.record.search.song.SongRecord;
import com.mixshare.rapid_evolution.ui.model.column.comparables.SmartString;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class EchonestTimbreInspectionTask extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(EchonestTimbreInspectionTask.class);
	
	public String toString() {
		return "Inspecting echonest tambre values";
	}
    
	public void execute() {
		try {
			float[] minTimbre = new float[12];
			float[] maxTimbre = new float[12];
			for (int i = 0; i < 12; ++i) {
				minTimbre[i] = Integer.MAX_VALUE;
				maxTimbre[i] = Integer.MIN_VALUE;
			}
			for (int songId : Database.getSongIndex().getIds()) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				SongRecord song = Database.getSongIndex().getSongRecord(songId);
				if ((song != null) && (song.hasMinedProfileHeader(DATA_SOURCE_ECHONEST))) {
					SongProfile songProfile = Database.getSongIndex().getSongProfile(songId);
					if (songProfile != null) {
						if (log.isDebugEnabled())
							log.debug("execute(): processing song=" + songProfile);
						EchonestSongProfile echonestProfile = (EchonestSongProfile)songProfile.getMinedProfile(DATA_SOURCE_ECHONEST);
						if (echonestProfile != null) {
							float[] totals = new float[12];
							int count = 0;
							for (EchonestSongSegment segment : echonestProfile.getSegments()) {
								double[] timbre = segment.getTimbre();
								for (int i = 0; i < totals.length; ++i) {
									totals[i] += timbre[i];		
									if (timbre[i] > maxTimbre[i])
										maxTimbre[i] = (float)timbre[i];
									if (timbre[i] < minTimbre[i])
										minTimbre[i] = (float)timbre[i];
								}
								++count;
								//log.debug("execute(): \tsegment timbre length=" + timbre.length + ",values=" + new SmartString(timbre).toString());								
							}
							if (count > 0) {
								for (int i = 0; i < totals.length; ++i)
									totals[i] /= count;
								if (log.isDebugEnabled())
									log.debug("execute(): \ttimbre totals=" + new SmartString(totals).toString());
								count = 0;
								float[] variance = new float[12];
								for (EchonestSongSegment segment : echonestProfile.getSegments()) {
									double[] timbre = segment.getTimbre();
									for (int i = 0; i < totals.length; ++i)
										variance[i] += (timbre[i] - totals[i]) * (timbre[i] - totals[i]);								
									++count;
									//log.debug("execute(): \tsegment timbre length=" + timbre.length + ",values=" + new SmartString(timbre).toString());								
								}
								for (int i = 0; i < variance.length; ++i)
									variance[i] = (float)Math.sqrt(variance[i] / count);
								if (log.isDebugEnabled())
									log.debug("execute(): \ttimbre variance=" + new SmartString(variance).toString());
								song.setTimbre(totals);
								song.setTimbreVariance(variance);
							}
							
						}
					}
				}
			}	
			if (log.isDebugEnabled())
				log.debug("execute(): min timbre values=" + new SmartString(minTimbre).toString());			
			if (log.isDebugEnabled())
				log.debug("execute(): max timbre values=" + new SmartString(maxTimbre).toString());
			Database.setProperty("timbre_inspector_run", Boolean.TRUE);
		} catch (Exception e) {
			log.error("execute(): error", e);
		}
	}

	public boolean isIndefiniteTask() { return true; }
	
}
