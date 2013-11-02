package com.mixshare.rapid_evolution.audio.tags.writers.flac;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

import com.mixshare.rapid_evolution.audio.tags.writers.ogg.JAudioOggTagWriter;
import com.mixshare.rapid_evolution.util.OSHelper;

public class JAudioFlacTagWriter extends JAudioOggTagWriter {

    private static Logger log = Logger.getLogger(JAudioFlacTagWriter.class);
	
    private FlacTag flacTag = null;    
    
    public JAudioFlacTagWriter(String filename, int mode) {
        try {
            this.filename = filename;
            if (log.isDebugEnabled()) log.debug("JAudioFlacTagWriter(): filename=" + filename);            
            audiofile = AudioFileIO.read(new File(filename));
            if (audiofile != null) {
            	flacTag = (FlacTag)audiofile.getTag();
                tag = (VorbisCommentTag)flacTag.getVorbisCommentTag();
                if (log.isDebugEnabled()) log.debug("JAudioFlacTagWriter(): tag=" + tag);
            }
        } catch (Exception e) {
            log.error("JAudioFlacTagWriter(): error Exception", e);
        }
    }	
    
    public void setAlbumCover(String filename, String album) {
        try {
            if (flacTag != null) {            	
            	RandomAccessFile imageFile = new RandomAccessFile(new File(OSHelper.getWorkingDirectory() + "/" + filename), "r");
            	byte[] imagedata = new byte[(int) imageFile.length()];
            	imageFile.read(imagedata);
            	BufferedImage bi = ImageIO.read(new File(filename));
            	flacTag.setField(flacTag.createArtworkField(bi,
            	                     PictureTypes.DEFAULT_ID,
            	                     ImageFormats.getMimeTypeForBinarySignature(imagedata),
            	                     album,
            	                     24,
            	                     0));
            }
        } catch (Exception e) {
            log.error("setAlbumCover(): error Exception", e);
        }
    }        
	
}
