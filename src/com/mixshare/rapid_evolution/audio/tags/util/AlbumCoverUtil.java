package com.mixshare.rapid_evolution.audio.tags.util;

import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.tags.TagReader;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;

public class AlbumCoverUtil {
    
    private static Logger log = Logger.getLogger(AlbumCoverUtil.class);
    
    public static String saveAlbumCover(TagReader tag_reader, BufferedImage image) {
        String filename = null;
        try {
            if (image != null) {
                filename = "" + tag_reader.getArtist() + tag_reader.getAlbum();
                if (filename.equals(""))
                	filename = "unknown";
                filename = StringUtil.cleanString(filename);
                filename = StringUtil.makeValidFilename(filename);                
                FileSystemAccess.getFileSystem().saveImage("/data/albumcovers/" + filename + ".jpg", image, false);                
            }
        } catch (Exception e) {
            log.error("saveAlbumCover(): error Exception", e);
            filename = null;
        }
        return filename;
    }
    
    public static String saveAlbumCover(TagReader tag_reader, String description, String mime_type, byte[] data) {
    	if (data == null)
    		return null;
        String filename = null;
        try {
            if (log.isTraceEnabled())
            	log.trace("saveAlbumCover(): description=" + description + ", mime_type=" + mime_type);            
            String name = null;
            String artist = tag_reader.getArtist();
            String album = tag_reader.getAlbum();
            if ((artist != null) && !artist.equals("") && (album != null) && !album.equals("")) {
                name = artist + "_" + album;
            } else if ((description != null) && !description.equals("")){
                name = description;
            } else {
                name = FileUtil.getFilenameMinusDirectory(tag_reader.getFilename());
                name = name.substring(0, name.indexOf(FileUtil.getExtension(tag_reader.getFilename()))) + "_albumcover";
            }
            if (log.isTraceEnabled()) log.trace("saveAlbumCover(): name=" + name);
            if (mime_type.length() > 0) {
                int index = mime_type.indexOf("/");
                String extension = null;
                if (index >= 0)
                    extension = mime_type.substring(index + 1, mime_type.length());
                else
                    extension = mime_type;
                filename = FileSystemAccess.getFileSystem().saveData("/data/albumcovers/" + StringUtil.makeValidFilename(name) + "." + extension, data, false);
            }            
        } catch (Exception e) {
            log.error("saveAlbumCover(): error Exception", e);
        }
        return filename;
    }
    
}
