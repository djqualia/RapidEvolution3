package com.mixshare.rapid_evolution.data.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.util.io.FileLockManager;

public class Serializer {

    static private Logger log = Logger.getLogger(Serializer.class);

    /**
     * Saves a serializable Object to a location on the file system.
     *
     * @return boolean true if successful
     */
    static public boolean saveData(Object data, String filePath) {
    	boolean result = false;
        try {
        	filePath = StringUtil.checkFullPathLength(filePath);
        	FileLockManager.startFileWrite(filePath);
        	if (filePath != null) {
	        	File file = new File(filePath);
	        	if (!file.isDirectory()) {
		            FileOutputStream fileOut = new FileOutputStream(filePath);
		            ObjectOutputStream streamOut = new ObjectOutputStream(fileOut);
		            streamOut.writeObject(data);
		            streamOut.close();
		            fileOut.close();
		            result = true;
	        	} else {
	        		log.warn("saveData(): attempted to save over directory, filePath=" + filePath + ", data=" + data);
	        	}
        	}
        } catch (Exception e) {
            log.error("saveData(): error Exception", e);
        } finally {
        	FileLockManager.endFileWrite(filePath);
        }
        return result;
    }

    static public boolean saveCompressedDataList(List<Object> dataList, String filePath) {
    	return saveCompressedDataList(dataList, filePath, true);
    }

    static public boolean saveCompressedDataList(List<Object> dataList, String filePath, boolean tryGcWorkaround) {
    	boolean result = false;
        try {
        	filePath = StringUtil.checkFullPathLength(filePath);
        	FileLockManager.startFileWrite(filePath);
        	File file = new File(filePath);
        	if (!file.isDirectory()) {
	            FileOutputStream fileOut = new FileOutputStream(filePath);
	            GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut);
	            ObjectOutputStream streamOut = new ObjectOutputStream(gzipOut);
	            streamOut.writeObject(dataList.size());
	            for (Object data : dataList) {
	            	streamOut.writeObject(data);
	            }
	            streamOut.close();
	            fileOut.close();
	            result = true;
        	} else {
        		log.warn("saveCompressedData(): attempted to save over directory, filePath=" + filePath + ", dataList=" + dataList);
        	}
        } catch (FileNotFoundException fnfe) {
        	if (tryGcWorkaround) {
            	if (log.isDebugEnabled())
            		log.debug("saveCompressedData(): got file not found exception, trying again after GC...");
        		// attempted workaround from reading online, make sure there are no open file handles and try again
        		System.gc();
        		return saveCompressedData(dataList, filePath, false);
        	} else {
        		log.error("saveCompressedData(): file not found exception saving filePath=" + filePath);
        	}
        } catch (Exception e) {
            log.error("saveCompressedData(): error Exception", e);
        } finally {
        	FileLockManager.endFileWrite(filePath);
        }
        return result;
    }

    /**
     * Saves a serializable Object to a location on the file system, uzing gzip compression.
     *
     * @return boolean true if successful
     */
    static public boolean saveCompressedData(Object data, String filePath) { return saveCompressedData(data, filePath, true); }
    static public boolean saveCompressedData(Object data, String filePath, boolean tryGcWorkaround) {
    	boolean result = false;
        try {
        	filePath = StringUtil.checkFullPathLength(filePath);
        	FileLockManager.startFileWrite(filePath);
        	File file = new File(filePath);
        	if (!file.isDirectory()) {
	            FileOutputStream fileOut = new FileOutputStream(filePath);
	            GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut);
	            ObjectOutputStream streamOut = new ObjectOutputStream(gzipOut);
	            streamOut.writeObject(data);
	            streamOut.close();
	            fileOut.close();
	            result = true;
        	} else {
        		log.warn("saveCompressedData(): attempted to save over directory, filePath=" + filePath + ", data=" + data);
        	}
        } catch (FileNotFoundException fnfe) {
        	if (tryGcWorkaround) {
            	if (log.isDebugEnabled())
            		log.debug("saveCompressedData(): got file not found exception, trying again after GC...");
        		// attempted workaround from reading online, make sure there are no open file handles and try again
        		System.gc();
        		return saveCompressedData(data, filePath, false);
        	} else {
        		log.error("saveCompressedData(): file not found exception saving filePath=" + filePath);
        	}
        } catch (Exception e) {
            log.error("saveCompressedData(): error Exception", e);
        } finally {
        	FileLockManager.endFileWrite(filePath);
        }
        return result;
    }

    /**
     * Reads a serialized Object from a location on the file system.
     *
     * @return Object if read successfully, null otherwise
     */
    static public Object readData(String filePath) {
    	Object result = null;
        try {
        	filePath = StringUtil.checkFullPathLength(filePath);
        	FileLockManager.startFileRead(filePath);
        	if (isValid(filePath)) {
	            FileInputStream fileIn = new FileInputStream(filePath);
	            ObjectInputStream in = new DecompressibleInputStream(fileIn);
	            Object data = in.readObject();
	            in.close();
	            fileIn.close();
	            result = data;
        	}
        } catch (java.io.FileNotFoundException fnfe) {
        	if (log.isTraceEnabled())
        		log.trace("readData(): file not found exception, filePath=" + filePath);
        } catch (java.io.IOException io) {
        	if (log.isTraceEnabled())
        		log.trace("readData(): io exception, filePath=" + filePath + ", error=" + io);
        } catch (Exception e) {
            log.error("readData(): error Exception, filePath=" + filePath, e);
        } finally {
        	FileLockManager.endFileRead(filePath);
        }
        return result;
    }

    static public List<Object> readCompressedDataList(String filePath) {
    	List<Object> result = new ArrayList<Object>();;
        try {
        	filePath = StringUtil.checkFullPathLength(filePath);
        	FileLockManager.startFileRead(filePath);
        	if (isValid(filePath)) {
	            FileInputStream fileIn = new FileInputStream(filePath);
	            GZIPInputStream gzipIn = new GZIPInputStream(fileIn);
	            ObjectInputStream in = new DecompressibleInputStream(gzipIn);
	            int numObjects = (Integer) in.readObject();
	            for (int i = 0; i < numObjects; ++i) {
	            	try {
	            		result.add(in.readObject());
	            	} catch (ClassNotFoundException e) {
	            		// Had to do this after ripping out some classes... wonder if we can't just continue?
	            		result.add(new Object());
	            	}
	            }
	            in.close();
	            fileIn.close();
        	}
        } catch (java.io.FileNotFoundException fnfe) {
        	if (log.isTraceEnabled())
        		log.trace("readCompressedDataList(): file not found exception, filePath=" + filePath);
        } catch (java.io.IOException io) {
        	if (log.isTraceEnabled())
        		log.trace("readCompressedDataList(): io exception, filePath=" + filePath + ", error=" + io);
        } catch (Exception e) {
            log.error("readCompressedDataList(): error Exception, filePath=" + filePath, e);
        } finally {
        	FileLockManager.endFileRead(filePath);
        }
        return result;
    }

    /**
     * Reads a gzip compressed, serialized Object from a location on the file system.
     *
     * @return Object if read successfully, null otherwise
     */
    static public Object readCompressedData(String filePath) {
    	Object result = null;
        try {
        	filePath = StringUtil.checkFullPathLength(filePath);
        	FileLockManager.startFileRead(filePath);
        	if (isValid(filePath)) {
	            FileInputStream fileIn = new FileInputStream(filePath);
	            GZIPInputStream gzipIn = new GZIPInputStream(fileIn);
	            ObjectInputStream in = new DecompressibleInputStream(gzipIn);
	            Object data = in.readObject();
	            in.close();
	            fileIn.close();
	            result = data;
        	}
        } catch (java.io.FileNotFoundException fnfe) {
        	if (log.isTraceEnabled())
        		log.trace("readCompressedData(): file not found exception, filePath=" + filePath);
        } catch (java.io.IOException io) {
        	if (log.isDebugEnabled())
        		log.debug("readCompressedData(): io exception, filePath=" + filePath + ", error=" + io);
        } catch (Exception e) {
        	if (log.isDebugEnabled())
        		log.debug("readCompressedData(): error Exception, filePath=" + filePath, e);
        } finally {
        	FileLockManager.endFileRead(filePath);
        }
        return result;
    }

    /**
     * Serializes an object to a compressed byte array.
     */
    static public byte[] encodeCompressedBytes(Object object) {
    	byte[] result = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
            ObjectOutputStream oos = new ObjectOutputStream(gzipOut);
            oos.writeObject(object);
            oos.close();
            gzipOut.close();
            result = baos.toByteArray();
            baos.close();
        } catch (Exception e) {
            log.error("encodeCompressedBytes(): error Exception", e);
        }
        return result;
    }

    /**
     * De-serializes an object from a compressed byte array.
     */
    static public Object decodeCompressedBytes(byte[] byteArray) {
    	Object result = null;
    	try {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteArray);
            GZIPInputStream gzipIn = new GZIPInputStream(byteIn);
            ObjectInputStream in = new DecompressibleInputStream(gzipIn);
            result = in.readObject();
            in.close();
            byteIn.close();
    	} catch (Exception e) {
    		log.error("decodeCompressedBytes(): error Exception", e);
    	}
    	return result;
    }

    /**
     * Create a "deep copy" of an Object using serialization... this is a lazy way
     * rather than implementing clone in all the inherited objects...
     *
     * Note: test before using
     */
    public static final Object clone(Object src) {
    	try {
    		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    		java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
    		oos.writeObject(src);
    		oos.flush();
    		oos.close();
    		java.io.ByteArrayInputStream bais = new
    		java.io.ByteArrayInputStream(baos.toByteArray());
    		java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
    		Object dest = ois.readObject();
    		ois.close();
    		return dest;
    	} catch (Throwable th) {
    		log.error("clone(): could not clone src=" + src);
    		return null;
    	}
    }

    ////////////////////
    // HELPER METHODS //
    ////////////////////

    static private boolean isValid(String filePath) {
    	if (filePath == null)
    		return false;
    	File file = new File(filePath);
    	if (file.getName().equalsIgnoreCase("con")) // windows didn't like a particular file with that name once, weird...
    		return false;
    	return true;
    }

}
