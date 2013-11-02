package com.mixshare.rapid_evolution.audio.tags.readers.mp4;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.movies.Movie;
import quicktime.std.movies.media.UserData;
import quicktime.util.QTHandle;
import quicktime.util.QTUtils;

import com.mixshare.rapid_evolution.audio.codecs.decoders.QTAudioDecoder;
import com.mixshare.rapid_evolution.audio.qt.QTConstants;
import com.mixshare.rapid_evolution.audio.qt.QTSessionCheck;
import com.mixshare.rapid_evolution.audio.qt.QTUtil;
import com.mixshare.rapid_evolution.audio.tags.readers.QTTagReader;
import com.mixshare.rapid_evolution.music.duration.Duration;

public class QTAACTagReader extends QTTagReader implements QTConstants {
  
    private static Logger log = Logger.getLogger(QTAACTagReader.class);
    
    private Movie movie = null;
    private Map tagValues = new HashMap();
    
  public QTAACTagReader(String filename) {
      try {
    	  QTUtil.getQuicktimeLock();
          QTSessionCheck.check();
          QTFile f = new QTFile(filename);
          OpenMovieFile omf = OpenMovieFile.asRead(f);
          movie = Movie.fromFile (omf);
          UserData userData = movie.getUserData( );
          int metaFCC = QTUtils.toOSType("meta");
          QTHandle metaHandle = userData.getData (metaFCC, 1);
          if (log.isDebugEnabled()) log.debug("QTAACTagReader(): found meta");
          byte[] metaBytes = metaHandle.getBytes();
          // locate the "ilst" pseudo-atom, ignoring first 4 bytes
          int ilstFCC = QTUtils.toOSType("ilst");
          PseudoAtomPointer ilst = findPseudoAtom (metaBytes, 4, ilstFCC);
          // iterate over the pseudo-atoms inside the "ilst"
          // building lists of tags and values from which we'll
          // create arrays for the DefaultTableModel constructor
          int off = ilst.offset + 8;
          while (off < metaBytes.length) {
              PseudoAtomPointer atom = findPseudoAtom (metaBytes, off, -1);
              // if we match a type, read everything after byte 24
              // which skips size, type, size, 'data', 8 junk bytes
              byte[  ] valueBytes = new byte [atom.atomSize - 24];
              System.arraycopy (metaBytes,
                                atom.offset+24,
                                valueBytes,
                                0,
                                valueBytes.length);
              String value = new String (valueBytes);
              tagValues.put(new Integer(atom.type), value);
              if (log.isDebugEnabled())
                  log.debug("QTAACTagReader(): id=" + atom.type + ", value=" + value);
              off = atom.offset + atom.atomSize;
          }
      } catch (Exception e) {
          log.error("QTAACTagReader(): error Exception", e);
      } finally {
    	  QTUtil.releaseQuicktimeLock();
      }
  }
  
  public boolean isFileSupported() {
      return ((movie != null) && (tagValues.size() > 0));
  }  
  
  /** find the given type in the byte array, starting at
      the start position.  Returns the offset within the
      byte array that begins this pseudo-atom.  a helper method
      to populateFromMetaAtom( ).
      @param bytes byte array to search
      @param start offset to start at
      @param type type to search for.  if -1, returns first
      atom with a plausible size
   */
  private PseudoAtomPointer findPseudoAtom (byte[] bytes,
                                            int start,
                                            int type) {
      // read size, then type
      // if size is bogus, forget it, increment offset, and try again
      int off = start;
      boolean found = false;
      while ((! found) &&
             (off < bytes.length-8)) {
          // read 32 bits of atom size
          // use BigInteger to convert bytes to long
          // (instead of signed int)
          byte sizeBytes[  ] = new byte[4];
          System.arraycopy (bytes, off, sizeBytes, 0, 4);
          BigInteger atomSizeBI = new BigInteger (sizeBytes);
          long atomSize = atomSizeBI.longValue( );
 
          // don't bother if the size would take us beyond end of
          // array, or is impossibly small
          if ((atomSize > 7) &&
              (off + atomSize <= bytes.length)) {
              byte[  ] typeBytes = new byte[4];
              System.arraycopy (bytes, off+4, typeBytes, 0, 4);
              int aType = QTUtils.toOSType (new String (typeBytes));
 
              if ((type == aType) ||
                  (type == -1))
                  return new PseudoAtomPointer (off, (int) atomSize, aType);
              else
                  off += atomSize;
                                  
          } else {
              System.out.println ("bogus atom size " + atomSize);
              // well, how did this happen?  increment off and try again
              off++;
          }
      } // while
      return null;
  }
 
  /** Inner class to represent atom-like structures inside
      the meta atom, designed to work with the byte array 
      of the meta atom (i.e., just wraps pointers to the 
      beginning of the atom and its computed size and type)
   */
  class PseudoAtomPointer {
      int offset;
      int atomSize;
      int type;
      public PseudoAtomPointer (int o, int s, int t) {
          offset=o;
          atomSize=s;
          type=t;
      }
      
  }
  
  public String getProperty(int tag) {      
      String result = (String)tagValues.get(new Integer(tag));
      if (result == null) result = "";
      return result;
  }  
  
  public String getTime() {
      String time = "";
      if (movie != null) {
          try {
              int seconds = movie.getDuration() / movie.getTimeScale();
              time = new Duration(seconds / 1000).getDurationAsString();
          } catch (Exception e) { }
      }
      if (log.isDebugEnabled()) log.debug("getTime(): time=" + time);
      return time;
  }  
 
}