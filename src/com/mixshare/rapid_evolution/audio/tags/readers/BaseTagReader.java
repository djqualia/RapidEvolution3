package com.mixshare.rapid_evolution.audio.tags.readers;

import java.util.Vector;

import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.duration.Duration;
import com.mixshare.rapid_evolution.video.util.VideoUtil;


public abstract class BaseTagReader extends AbstractTagReader implements DataConstants {
        
    public boolean isFileSupported() { return false; }    
    public String getAlbum() { return null; }
    public String getAlbumCoverFilename() { return null; }
    public String getArtist() { return null; }
    public Integer getBeatIntensity() { return null; }    
    public Integer getBpmAccuracy() { return null; }
    public Float getBpmStart() { return null; }
    public Float getBpmEnd() { return null; }
    public String getCatalogId() { return null; }    
    public String getComments() { return null; }
    public String getContentGroupDescription() { return null; }
    public String getContentType() { return null; }
    public String getEncodedBy() { return null; }
    public String getFilename() { return null; }    
    public String getFileType() { return null; }
    public String getGenre() { return null; }
    public Integer getKeyAccuracy() { return null; }
    public String getKeyStart() { return null; }
    public String getKeyEnd() { return null; }
    public String getLanguages() { return null; }
    public String getLyrics() { return null; }    
    public String getPublisher() { return null; }
    public Integer getRating() { return null; }
    public String getRemix() { return null; }
    public Float getReplayGain() { return null; }
    public Integer getSizeInBytes() { return null; }
    public Vector<DegreeValue> getStyles() { return null; }
    public Vector<DegreeValue> getTags() { return null; }
    public String getTime() {
    	double seconds = 0.0;
    	if (AudioUtil.isSupportedAudioFileType(getFilename()))
    		seconds = AudioUtil.getDuration(getFilename()).getDurationInSeconds();
    	else if (VideoUtil.isSupportedVideoFileType(getFilename()))
    		seconds = VideoUtil.getDuration(getFilename()).getDurationInSeconds();
        if (seconds != 0.0) {
            return new Duration((int)Math.max(seconds * 1000, 1000.0)).getDurationAsString();
        }
        return "";        
    }
    public String getTimeSignature() { return null; }
    public String getTitle() { return null; }
    public String getTrack() { return null; }
    public Integer getTotalTracks() { return null; }
    public String getUserField(String fieldName) { return null; } 
    public String getYear() { return null; }
    
}
