package com.mixshare.rapid_evolution.data.util.filesystem;

import java.awt.image.BufferedImage;

import com.mixshare.rapid_evolution.data.profile.common.image.InvalidImageException;
import com.trolltech.qt.gui.QImage;

public interface FileSystemInterface {

	public String[] getFilenames(String directoryPath);
	
	public boolean doesExist(String relativePath);
	public long getLastModified(String relativePath);
	
	public byte[] readData(String relativePath);
	
	public String saveImage(String relativePath, QImage image, boolean overwrite);
	public String saveData(String relativePath, byte[] data, boolean overwrite);	
	public String saveImage(String relativePath, BufferedImage image, boolean overwrite);
	
	public QImage readQImage(String relativePath) throws InvalidImageException;
	public BufferedImage readBufferedImage(String relativePath) throws InvalidImageException;
	
}
