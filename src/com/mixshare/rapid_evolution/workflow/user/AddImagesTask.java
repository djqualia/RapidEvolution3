package com.mixshare.rapid_evolution.workflow.user;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.mixshare.rapid_evolution.RapidEvolution3;
import com.mixshare.rapid_evolution.data.DataConstants;
import com.mixshare.rapid_evolution.data.profile.common.image.Image;
import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.mixshare.rapid_evolution.data.profile.search.SearchProfile;
import com.mixshare.rapid_evolution.data.util.filesystem.FileSystemAccess;
import com.mixshare.rapid_evolution.util.FileUtil;
import com.mixshare.rapid_evolution.util.StringUtil;
import com.mixshare.rapid_evolution.workflow.CommonTask;

public class AddImagesTask extends CommonTask implements DataConstants {
	
    static private Logger log = Logger.getLogger(AddImagesTask.class);
	
	private List<String> filenames;
	private SearchProfile profile;
	
	public AddImagesTask(SearchProfile profile, List<String> filenames) {
		this.profile = profile;
		this.filenames = filenames;
	}
	public AddImagesTask(SearchProfile profile, Collection<String> filenames) {
		this.profile = profile;
		this.filenames = new ArrayList<String>();
		for (String filename : filenames)
			this.filenames.add(filename);
	}
	
	public String toString() { return "Adding images " + StringUtil.getTruncatedDescription(filenames); }
	
	public void execute() {
		if ((profile != null) && (filenames != null)) {
			boolean imagesAdded = false;
			int numFilenames = filenames.size();
			int numAdded = 0;
			for (String filename : filenames) {
				if (RapidEvolution3.isTerminated || isCancelled())
					return;
				try {
					BufferedImage bi = ImageIO.read(new File(filename));
					if (bi != null) {
						String name = FileUtil.getFilenameMinusDirectory(filename);
						String relativePath = "/user/images/" + name;
						FileSystemAccess.getFileSystem().saveImage(relativePath, bi, false);						
						profile.addImage(new Image(relativePath, relativePath, DATA_SOURCE_USER));
						imagesAdded = true;
					}
				} catch (IOException e) { 
					log.error("execute(): IO exception=" + e);
				} catch (InvalidImageException e) {
					log.error("execute(): invalid image exception=" + filename);
				}
				++numAdded;
				setProgress(((float)numAdded) / numFilenames);
			} 
			if (imagesAdded)
				profile.save();
		}
	}

}
