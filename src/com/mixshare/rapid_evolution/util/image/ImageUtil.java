package com.mixshare.rapid_evolution.util.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

public class ImageUtil implements ImageFileTypes {

	static private Logger log = Logger.getLogger(ImageUtil.class);
	
    static public int getImageFileType(String filename) {
        if (filename != null) {
            String lc_filename = filename.toLowerCase();
            if (lc_filename.endsWith(".jpg")) return IMAGE_FILE_TYPE_JPG;
            if (lc_filename.endsWith(".jpeg")) return IMAGE_FILE_TYPE_JPG;
            if (lc_filename.endsWith(".gif")) return IMAGE_FILE_TYPE_GIF;
            if (lc_filename.endsWith(".png")) return IMAGE_FILE_TYPE_PNG;
            if (lc_filename.endsWith(".tiff")) return IMAGE_FILE_TYPE_TIFF;
            if (lc_filename.endsWith(".tif")) return IMAGE_FILE_TYPE_TIFF;
            if (lc_filename.endsWith(".pcx")) return IMAGE_FILE_TYPE_PCX;
            if (lc_filename.endsWith(".tga")) return IMAGE_FILE_TYPE_TGA;
            if (lc_filename.endsWith(".bmp")) return IMAGE_FILE_TYPE_BMP;
        }
        return IMAGE_FILE_TYPE_UNKNOWN;
    }
    
    static public boolean isSupportedImageFileType(String filename) {
    	return getImageFileType(filename) != IMAGE_FILE_TYPE_UNKNOWN; 
    }
    
    static public String[] getSupportedImageFileExtensions() {
        return new String[] { ".jpg", ".jpeg", ".gif", ".png", ".tiff", ".pcx", ".tga", "bmp" };
    }
    
    static private String getSupportedImageFileExtensionsDescription() {
    	StringBuffer result = new StringBuffer();
    	result.append("(");
    	for (String extension : getSupportedImageFileExtensions()) {
    		if (result.length() > 1)
    			result.append(" ");
    		result.append("*");
    		result.append(extension);
    	}
    	result.append(")");
    	return result.toString();
    }

    static public List<String> getImageFilters() {
    	List<String> result = new ArrayList<String>();
    	result.add("ALL image files " + getSupportedImageFileExtensionsDescription());
    	result.add("JPG files (*.jpg *.jpeg)");
    	result.add("GIF files (*.gif)");
    	result.add("PNG files (*.png)");
    	result.add("TIFF files (*.tiff *.tif)");
    	result.add("PCX files (*.pcx)");
    	result.add("TGA files (*.tga)");
    	result.add("BMP files (*.bmp)");
    	return result;
    }
	
	static public String getContentType(String filename) {
		int imageType = getImageFileType(filename);
		if (imageType == IMAGE_FILE_TYPE_JPG)
			return "image/jpeg";
		if (imageType == IMAGE_FILE_TYPE_GIF)
			return "image/gif";
		if (imageType == IMAGE_FILE_TYPE_PNG)
			return "image/png";
		return "image/unknown";
	}
	
    /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    static public BufferedImage getScaledInstance(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint) {
    	
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;

        // Use one-step technique: scale directly from original
        // size to target size with a single drawImage() call
        w = targetWidth;
        h = targetHeight;
        
        do {            
            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
    
    static public boolean areImagesEqual(BufferedImage image1, BufferedImage image2) {
    	try {    		
    		Raster r1 = image1.getData();
    		Raster r2 = image2.getData();
    		
    		DataBuffer db1 = r1.getDataBuffer();
    		int size1 = db1.getSize();
    		
    		DataBuffer db2 = r2.getDataBuffer();
    		int size2 = db2.getSize();
    		
    		if (size1 == size2) {
    			for (int i = 0; i < size1; i++) {
          		  	int px1 = db1.getElem(i);
          		  	int px2 = db2.getElem(i);
          		  	if (px1 != px2)
          		  		return false;
          		}
    			return true;
    		}    		
    	} catch (Exception e) {
    		log.error("areImagesEqual(): error", e);
    	}
    	return false;
    }
    
    static public void cropAndSave(String sourceImageFilename, String targetFilename, int targetWidth, int targetHeight) {
		try {
			BufferedImage sourceImage = ImageIO.read(new File(sourceImageFilename));
			if (sourceImage != null) {
				int sourceWidth = sourceImage.getWidth();
				int sourceHeight = sourceImage.getHeight();
				float widthRatio = ((float)sourceWidth) / targetWidth;
				float widthHeight = ((float)sourceHeight) / targetHeight;
				float cropRatio = 1.0f / Math.min(widthRatio, widthHeight);
				
				int tempWidth = (int)(cropRatio * sourceWidth);
				if (tempWidth < targetWidth)
					tempWidth = targetWidth;
				int tempHeight = (int)(cropRatio * sourceHeight);
				if (tempHeight < targetHeight)
					tempHeight = targetHeight;
				BufferedImage tempImage = getScaledInstance(sourceImage, (int)tempWidth, (int)tempHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				
				int type = (sourceImage.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
				BufferedImage finalImage = null;				
				if (tempWidth > targetWidth) {
					int deltaWidth = (tempWidth - targetWidth) / 2;
					finalImage = tempImage.getSubimage(deltaWidth, 0, targetWidth, targetHeight);
				} else {
					int deltaHeight = (tempHeight - targetHeight) / 2;
					finalImage = tempImage.getSubimage(0, deltaHeight, targetWidth, targetHeight);
				}								
				ImageIO.write(finalImage, "jpg", new File(targetFilename));				
			}
		} catch (Exception e) {
			log.error("cropAndSave(): error", e);
		}
		                       
	}    
    
}
