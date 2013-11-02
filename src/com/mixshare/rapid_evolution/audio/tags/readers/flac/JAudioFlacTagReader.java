package com.mixshare.rapid_evolution.audio.tags.readers.flac;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.flac.metadatablock.MetadataBlockDataPicture;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey;
import org.jaudiotagger.tag.vorbiscomment.util.Base64Coder;

import com.mixshare.rapid_evolution.audio.tags.readers.ogg.JAudioOggTagReader;
import com.mixshare.rapid_evolution.audio.tags.util.AlbumCoverUtil;

public class JAudioFlacTagReader extends JAudioOggTagReader {
    
    private static Logger log = Logger.getLogger(JAudioFlacTagReader.class);
        
    private FlacTag flacTag = null;
    
    public JAudioFlacTagReader(String filename) {
    	super();
        try {
            this.filename = filename;
            if (log.isDebugEnabled())
            	log.debug("JAudioFlacTagReader(): filename=" + filename);            
            audiofile = AudioFileIO.read(new File(filename));            
            if (audiofile != null) {
            	flacTag = (FlacTag)audiofile.getTag();
            	tag = flacTag.getVorbisCommentTag();
                if (log.isDebugEnabled()) log.debug("JAudioFlacTagReader(): flacTag=" + flacTag);
            }
        } catch (Exception e) {
            log.error("JAudioFlacTagReader(): error Exception", e);
        }
    }   
    
    public String getAlbumCoverFilename() {
    	String albumcover_filename = null;
    	try {
    		if (flacTag.getImages().size() > 0) {
    			MetadataBlockDataPicture image = flacTag.getImages().get(0);
    			BufferedImage bi = ImageIO.read(ImageIO.createImageInputStream(new ByteArrayInputStream(image.getImageData())));
    			albumcover_filename = AlbumCoverUtil.saveAlbumCover(this, bi);
    		}
    	} catch (Exception e) {
    		log.error("getAlbumCoverFilename(): error", e);
    	}
    	return albumcover_filename;
    }
    
}
