package com.mixshare.rapid_evolution.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.audio.util.AudioUtil;
import com.mixshare.rapid_evolution.video.util.VideoUtil;
import com.mixshare.rapid_evolution.workflow.TaskManager;
import com.mixshare.rapid_evolution.workflow.importers.filesystem.ImportFilesTask;

public class FileUtil {

    static private Logger log = Logger.getLogger(FileUtil.class);
	
	static public void recurseFileTree(String directory, Vector<String> results) {
    	File dir = new File(directory);
    	if ((dir != null) && (dir.listFiles() != null)) {
    		File files[] = new File[dir.listFiles().length];
    		files = dir.listFiles();
    		files = sortfiles(files);
    		for (int i = 0; i < files.length; ++i) {
    			if (!files[i].isDirectory()) {    				
					results.add(files[i].getAbsolutePath());
    			} else {
    				recurseFileTree(files[i].getAbsolutePath(), results);
    			}
    		}
        }
    }
    
	static public File[] sortfiles(File[] files) {
    	String[] strArray = new String[files.length];
    	for (int s = 0; s < strArray.length; ++s)
    		strArray[s] = files[s].getAbsolutePath();    	
    	java.util.Arrays.sort(strArray, String.CASE_INSENSITIVE_ORDER);
    	for (int s = 0; s < strArray.length; ++s)
    		files[s] = new File(strArray[s]);
    	return files;
    }    
	
    static public String getFilenameAsURL(String fileName) {
    	return "file:" + new File(fileName).toURI().getPath();
    }
    
    static public String getDirectoryFromFilename(String filename) {
        if ((filename == null) || filename.equals(""))
        	return null;
        File efile = new File(filename);
        if (efile.isDirectory())
        	return filename;
        String name = efile.getName();
        String path = efile.getPath();
        int pos = path.indexOf(name);
        return StringUtil.replace(path.substring(0, pos), "\\", "/");          
    }    

    static public String getFilenameMinusDirectory(String filename) {
        if ((filename == null) || filename.equals(""))
        	return null;
        File efile = new File(filename);
        return efile.getName();
    }

    static public String getFilenameMinusDirectoryMinusExtension(String filename) {
    	filename = getFilenameMinusDirectory(filename);
        if ((filename == null) || filename.equals(""))
        	return null;
        String extension = getExtension(filename);
        if ((extension != null) && (extension.length() > 0))
        	filename = filename.substring(0, filename.length() - extension.length() - 1);
        return filename;
    }
    
    static public String getExtension(File f) { return getExtension(f.getAbsolutePath()); }
    static public String getExtension(String s) {
    	if (s == null)
    		return "";
        String ext = "";
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1)
        	ext = s.substring(i+1).toLowerCase();
        return ext;
    }    
    
    public static void copy(String fromFileName, String toFileName) throws IOException {
    	
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		FileUtils.copyFile(fromFile, toFile);
		
		/*
		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite()) 
				throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
		}

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[32768]; //[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) { }
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {  }
		}
		*/
	}
    
    static public String unify(String filename) {
    	if (filename == null)
    		return "";
    	File file = new File(filename);
    	try {
    		filename = file.getCanonicalPath();
    	} catch (IOException io) { }
    	filename = StringUtil.replace(filename, "\\\\", "/");
    	filename = StringUtil.replace(filename, "\\", "/");
    	return filename;
    }
    
    static public String correctPathSeperators(String filename) {
        String seperator = getFileSeperator();
        if (seperator.equals("/"))
            return StringUtil.replace(filename, "\\", "/");        
        else if (seperator.equals("\\"))
            return StringUtil.replace(filename, "/", "\\");
        return filename;
    }
 
    static public String getFileSeperator() {
        return System.getProperty("file.separator");
    }
 
	static public String stripWorkingDirectory(String originalFilename) {
		try {
			String workingDirectory = OSHelper.getWorkingDirectory().getCanonicalPath();
			String filename = new File(originalFilename).getCanonicalPath();
			if (filename.startsWith(workingDirectory)) {
				filename = filename.substring(workingDirectory.length() + 1);
				return StringUtil.replace(filename, "\\", "/");
			}			
		} catch (Exception e) {	}
		return originalFilename;
	}    
	
    static public List<String> getSupportedFileFilters() {
    	List<String> result = new ArrayList<String>();
    	result.add("ALL supported files " + getSupportedFileDescriptions());
    	for (String filter : AudioUtil.getAudioFilters())
    		result.add(filter);
    	for (String filter : VideoUtil.getVideoFilters())
    		result.add(filter);
    	return result;
    }
    
    static public String getSupportedFileDescriptions() {
    	StringBuffer result = new StringBuffer();
    	result.append("(");
    	for (String extension : AudioUtil.getSupportedAudioFileExtensions()) {
    		if (result.length() > 1)
    			result.append(" ");
    		result.append("*");
    		result.append(extension);
    	}
    	for (String extension : VideoUtil.getSupportedVideoFileExtensions()) {
    		if (result.length() > 1)
    			result.append(" ");
    		result.append("*");
    		result.append(extension);
    	}
    	result.append(")");
    	return result.toString();    	
    }

	public static int pid() {
		String id = ManagementFactory.getRuntimeMXBean().getName();
		String[] ids = id.split("@");
		return Integer.parseInt(ids[0]);
	}
    
    static public void logOpenFileHandles() {
    	try {
    		int pid = pid();
    		Runtime runtime = Runtime.getRuntime();
    		Process process = runtime.exec("lsof -p " + pid);
    		InputStream is = process.getInputStream();
    		InputStreamReader isr = new InputStreamReader(is);
    		BufferedReader br = new BufferedReader(isr);
    		String line;
    		while ((line = br.readLine()) != null)
    			log.warn(line);    		
    	} catch (Exception e) {
    		log.error("logOpenFileHandles(): error", e);
    	}    
    }

    public static String getRelativePath(String targetPath, String basePath) {
    	return getRelativePath(FileUtil.unify(targetPath), FileUtil.unify(basePath), "/");    		    	
    }
    private static String getRelativePath(String targetPath, String basePath, String pathSeparator) {

        //  We need the -1 argument to split to make sure we get a trailing 
        //  "" token if the base ends in the path separator and is therefore
        //  a directory. We require directory paths to end in the path
        //  separator -- otherwise they are indistinguishable from files.
        String[] base = basePath.split(Pattern.quote(pathSeparator), -1);
        String[] target = targetPath.split(Pattern.quote(pathSeparator), 0);

        //  First get all the common elements. Store them as a string,
        //  and also count how many of them there are. 
        String common = "";
        int commonIndex = 0;
        for (int i = 0; i < target.length && i < base.length; i++) {
            if (target[i].equals(base[i])) {
                common += target[i] + pathSeparator;
                commonIndex++;
            }
            else break;
        }

        if (commonIndex == 0)
        {
            //  Whoops -- not even a single common path element. This most
            //  likely indicates differing drive letters, like C: and D:. 
            //  These paths cannot be relativized. Return the target path.
            return targetPath;
            //  This should never happen when all absolute paths
            //  begin with / as in *nix. 
        }

        String relative = "";
        if (base.length == commonIndex) {
            //  Comment this out if you prefer that a relative path not start with ./
            //relative = "." + pathSeparator;
        }
        else {
            int numDirsUp = base.length - commonIndex - 1;
            //  The number of directories we have to backtrack is the length of 
            //  the base path MINUS the number of common path elements, minus
            //  one because the last element in the path isn't a directory.
            for (int i = 0; i <= (numDirsUp); i++) {
                relative += ".." + pathSeparator;
            }
        }
        relative += targetPath.substring(common.length());

        return relative;
    }
    
    static public void deleteDirectory(String directory) {
    	deleteDirectory(directory, false);
    }

    static public void deleteDirectory(String directory, boolean justFilesInIt) {
    	Vector<String> results = new Vector<String>();
    	recurseFileTree(directory, results);
    	for (String result : results)
    		new File(result).delete();
    	if (!justFilesInIt) {
    		new File(directory).delete();
    	}
    }

    static public byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
    
}
