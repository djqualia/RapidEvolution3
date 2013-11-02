package com.mixshare.rapid_evolution.audio.tags.readers.mp4;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.audio.tags.readers.BaseTagReader;
import com.mixshare.rapid_evolution.data.util.DegreeValue;
import com.mixshare.rapid_evolution.music.duration.Duration;

public class DefaultMP4TagReader extends BaseTagReader {
    
    private static Logger log = Logger.getLogger(DefaultMP4TagReader.class);
    
    private String filename = null;
    private FileInputStream fis = null;
    private BufferedInputStream bis = null;
    
    private String album = null;
    private String artist = null;
    private String comments = null;
    private double seconds = 0;
    private String genre = null;   
    private String tempo = null;
    private String title = null;
    private String track = null;
    private String year = null;

    public DefaultMP4TagReader(String filename) {
        try {
            if (log.isDebugEnabled()) log.debug("DefaultMP4TagReader(): filename=" + filename);
            this.filename = filename;
            fis = new FileInputStream(filename);
            bis = new BufferedInputStream(fis);
            Parse();
            bis.close();
            fis.close();
            if (log.isDebugEnabled()) log.debug("DefaultMP4TagReader(): done parsing file");
        } catch (Exception e) {
            log.error("DefaultMP4TagReader(): error Exception", e);
        }
    }
    public boolean isFileSupported() {
        return ((album != null) ||
                (artist != null) ||
                (comments != null) ||
                (genre != null) ||
                (tempo != null) ||
                (title != null) ||
                (track != null) ||
                (year != null) ||
                (seconds != 0));
    }    
    
    public String getAlbum() { return album; }
    public String getArtist() { return artist; }
    public Float getBpmStart() {
        if (tempo != null)
            return new Float(Float.parseFloat(tempo));
        return null;
    }
    public String getComments() { return comments; }
    public String getFilename() { return filename; }
    public String getGenre() { return genre; }
    public Vector<DegreeValue> getStyles() {
    	Vector<DegreeValue> result = new Vector<DegreeValue>();
    	result.add(new DegreeValue(genre, 1.0f, DATA_SOURCE_FILE_TAGS));
    	return result;
    }
    public String getTime() {
        if (seconds != 0.0) 
            return new Duration((int)seconds).getDurationAsString();
        return null;
    }
    public double getTimeInSeconds() {
        return seconds;
    }
    public String getTitle() { return title; }
    public String getTrack() { return track; }
    public String getYear() { return year; }
    
    private void Parse() throws IOException, Exception {
        byte[] buffer = new byte[4];
        
        bis.mark(Integer.MAX_VALUE);
        
        bis.read(buffer, 0, 4);
        bis.read(buffer, 0, 4);
        
        if(buffer[0] != (byte)'f' || buffer[1] != (byte)'t' || buffer[2] != (byte)'y' || buffer[3] != (byte)'p') {
            String msg = "file does not appear to contain a valid MP4 header, filename=" + filename;
            log.error("Parse(): " + msg);
            throw new Exception(msg);
        }
        
        bis.reset();

        Position pos = new Position();
        int level = 0;
        long len = new File(filename).length();
        
        ParseContainer(pos, len, level, bis);        
    }

    private class Position {
        private long pos = 0;
        public Position() { }
        public void increment(long value) { pos += value; }
        public long getValue() { return pos; }
        public String toString() { return String.valueOf(pos); }
    }
    
    int toInt(byte[] bytes, int offset) {
        // little endian
	    int accum = 0;
	    int count = offset;
	    for ( int shiftBy=0; shiftBy<32; shiftBy+=8 ) {
	       accum |= ( bytes[count++] & 0xff ) << shiftBy;
	    }
	    return accum;
    }
    
    private int toInt16(byte[] bytes, int offset) {
        // little endian
        int low = bytes[offset] & 0xff;
        int high = bytes[offset + 1];
        return( high << 8 | low );
    }            
    
    private void ParseMvhd(Position pos, long len, long size, BufferedInputStream br) throws IOException {
        if (log.isTraceEnabled()) log.trace("ParseMvhd(): pos=" + pos + ", len=" + len + ", size=" + size);        
        byte[] bytes = new byte[(int)size];
        br.read(bytes);        
        int scale = toInt(new byte[] { bytes[15], bytes[14], bytes[13], bytes[12]}, 0);
        if (log.isTraceEnabled()) log.trace("ParseMvhd(): scale=" + scale);
        int duration = toInt(new byte[] { bytes[19], bytes[18], bytes[17], bytes[16]}, 0);
        if (log.isTraceEnabled()) log.trace("ParseMvhd(): duration=" + duration);
        seconds = (double)duration / scale;
        if (log.isTraceEnabled()) log.trace("ParseMvhd(): seconds=" + seconds);
        pos.increment(size);
    }

    private void ParseData(Position pos, long len, int level, BufferedInputStream br, String dataatom) throws IOException {
        byte[] buffer = new byte[4];
        pos.increment(br.read(buffer, 0, 4));
        
        long size = toInt(new byte [] { buffer[3], buffer[2], buffer[1], buffer[0] }, 0);
        pos.increment(br.read(buffer, 0, 4));
        pos.increment(br.read(buffer, 0, 4));
        
        long type = toInt(new byte [] { buffer[3], buffer[2], buffer[1], buffer[0] }, 0);
        size -= 16;
        type &= 255;
        pos.increment(br.read(buffer, 0, 4));
        
        byte [] data = new byte[(int)size];
        pos.increment(br.read(data, 0, (int)size));
        
        switch((int)type) {
            case 0:               
            case 21:
                int [] intvals = new int[(int)size / 2];
                for(int i = 0; i < size / 2; i++) {
                    intvals[i] = toInt16(new byte[] { 
                      data[1 + (i * 2)], 
                      data[0 + (i * 2)] 
                    }, 0);
                }
                
                if (dataatom.equals("GNRE")) {
                    genre = GENRE_MAP[intvals[0]];
                    if (log.isTraceEnabled()) log.trace("ParseData(): genre=" + genre);
                } else if (dataatom.equals("TRKN")) {
                    track = String.valueOf(intvals[1]);
                    if (log.isTraceEnabled()) log.trace("ParseData(): track=" + track);
                } else if (dataatom.equals("TMPO")) {
                    tempo = String.valueOf(intvals[0]);
                    if (log.isTraceEnabled()) log.trace("ParseData(): tempo=" + tempo);
                } else {
                    if (log.isTraceEnabled()) log.trace("ParseData(): {0} data atom=" + dataatom);
                }
                
                break;
            case 1:
                String value = new String(data);
                if (dataatom.equals("GEN")) {
                    if (log.isTraceEnabled()) log.trace("ParseData(): genre=" + value);
                    genre = value;
                } else if (dataatom.equals("NAM")) {
                    if (log.isTraceEnabled()) log.trace("ParseData(): title=" + value);
                    title = value;
                } else if (dataatom.equals("ART")) {
                    if (log.isTraceEnabled()) log.trace("ParseData(): artist=" + value);
                    artist = value;
                } else if (dataatom.equals("ALB")) {
                    if (log.isTraceEnabled()) log.trace("ParseData(): album=" + value);
                    album = value;
                } else if (dataatom.equals("DAY")) {
                    if (log.isTraceEnabled()) log.trace("ParseData(): year=" + value);
                    year = value;
                } else if (dataatom.equals("CMT")) {
                    if (log.isTraceEnabled()) log.trace("ParseData(): comment=" + value);
                    comments = value;
                } else {
                    if (log.isTraceEnabled()) log.trace("ParseData(): value=" + value);
                }
                break;
            case 2:
                // other byte data
                break;
            default:
                if (log.isTraceEnabled()) log.trace("ParseData(): non standard type=" + type + ", dataatom=" + dataatom);
                // non-standard data
                break;
        }
    }
    
    private void ParseContainer(Position pos, long len, int level, BufferedInputStream br) throws IOException, Exception
    {
        byte [] buffer = new byte[4];
        int stuck_count = 0;
        
        level++;
        
        while(pos.getValue() < len) {
            pos.increment(br.read(buffer, 0, 4));
            long size = toInt(new byte[] { buffer[3], buffer[2], buffer[1], buffer[0] }, 0);
            pos.increment(br.read(buffer, 0, 4));
            
            String id = new String(buffer);
            
            if(size == 1) {
                if (log.isTraceEnabled()) log.trace("ParseContainer(): reading extended size...");
                pos.increment(br.read(buffer, 0, 4));
                long hi = toInt(new byte[] { buffer[3], buffer[2], buffer[1], buffer[0] }, 0);
                pos.increment(br.read(buffer, 0, 4));
                long lo = toInt(new byte[] { buffer[3], buffer[2], buffer[1], buffer[0] }, 0);
                size = hi * (2 ^ 32) + lo - 16;
            } else {
                size -= 8;
            }
            
            if (log.isTraceEnabled()) log.trace("ParseContainer(): size=" + size + ", id=" + id);
            
            if(size <= 0) {
                if(size != 0 && level != 1) {
                    throw new Exception("MP4 Container Parsing error");
                }
            }
            
            // deal with potentially corrupt files: hack, maybe we shouldn't even care
            if(size == -8 && level == 1) {
                if(stuck_count++ >= 100) {
                    throw new Exception(
                        "MP4 Container Parsing Error: Stuck at position {0}" + pos.getValue());
                }
            }

            id = id.toUpperCase();
            if (id.endsWith("NAM")) {
                ParseData(pos, len, level, br, "NAM");                
            } else if (id.endsWith("ART")) {
                ParseData(pos, len, level, br, "ART");                
            } else if (id.endsWith("ALB")) {
                ParseData(pos, len, level, br, "ALB");
            } else if (id.endsWith("DAY")) {
                ParseData(pos, len, level, br, "DAY");
            } else if (id.endsWith("CMT")) {
                ParseData(pos, len, level, br, "CMT");
            } else if (id.endsWith("GEN")) {
                ParseData(pos, len, level, br, "GEN");
            } else if (id.equals("GNRE")) {
                ParseData(pos, len, level, br, "GNRE");
            } else if (id.equals("TMPO")) {
                ParseData(pos, len, level, br, "TMPO");
            } else if (id.equals("TRKN")) {
                ParseData(pos, len, level, br, "TRKN");
            } else if (id.equals("META")) {
                long bytes_skipped = br.skip(4);
                while (bytes_skipped < 4) {
                    long new_bytes_skipped = br.skip(4 - bytes_skipped);
                    if (new_bytes_skipped <= 0) {
                        throw new Exception(
                                "Error skipping bytes, pos=" + pos);
                    }
                    bytes_skipped += new_bytes_skipped;
                }
                pos.increment(4);
                ParseContainer(pos, len, level, br);
            } else if (id.equals("MOOV") || id.equals("ILST") || id.equals("MDIA") || id.equals("MNIF") || id.equals("MINF") || id.equals("STBL") || id.equals("TRAK") || id.equals("UDTA")) {
                ParseContainer(pos, len, level, br);                
            } else if (id.equals("MVHD")) {
                ParseMvhd (pos, len, size, br);
            } else {
                long skipped = br.skip(size);
                while (skipped < size) {
                    long new_skipped = br.skip(size - skipped);
                    if (new_skipped <= 0) {
                        throw new Exception(
                                "Error skipping bytes, pos=" + pos);                        
                    }
                    skipped += new_skipped;
                }
                pos.increment(size);
            }
        }
    }
    
    static String[] GENRE_MAP = new String[] {
        "N/A", "Blues", "Classic Rock", "Country", "Dance", "Disco",
        "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies",
        "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno",
        "Industrial", "Alternative", "Ska", "Death Metal", "Pranks",
        "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal",
        "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental",
        "Acid", "House", "Game", "Sound Clip", "Gospel", "Noise",
        "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative",
        "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic",
        "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk",
        "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta",
        "Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American",
        "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes",
        "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka",
        "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk",
        "Folk/Rock", "National Folk", "Swing", "Fast-Fusion", "Bebob",
        "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
        "Gothic Rock", "Progressive Rock", "Psychedelic Rock",
        "Symphonic Rock", "Slow Rock", "Big Band", "Chorus",
        "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
        "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass",
        "Primus", "Porn Groove", "Satire", "Slow Jam", "Club", "Tango",
        "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul",
        "Freestyle", "Duet", "Punk Rock", "Drum Solo", "A capella",
        "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club House",
        "Hardcore", "Terror", "Indie", "BritPop", "NegerPunk",
        "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal",
        "Black Metal", "Crossover", "Contemporary C", "Christian Rock",
        "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "SynthPop"
    };
    
    private String[] DataAtoms = {
        "AART",
        "AKID",
        "ALB",
        "APID",
        "ATID",
        "ART",
        "CMT",
        "CNID",
        "CPIL",
        "CPRT",
        "DAY",
        "DISK",
        "GEID",
        "GEN",
        "GNRE",
        "GRP",
        "NAM",
        "PLID",
        "RTNG",
        "TMPO",
        "TOO",
        "TRKN",
        "WRT"
    };

}
