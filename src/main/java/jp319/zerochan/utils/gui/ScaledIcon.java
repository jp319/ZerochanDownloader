package jp319.zerochan.utils.gui;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ScaledIcon {
	public static Icon createScaledIcon(ImageIcon originalIcon, int maxImageWidth, int maxImageHeight) {
		try {
			Image originalImage = originalIcon.getImage();
			int originalWidth = originalIcon.getIconWidth();
			int originalHeight = originalIcon.getIconHeight();
			// Calculate the scaled width and height while preserving aspect ratio
			int scaledWidth, scaledHeight;
			if (originalWidth > originalHeight) {
				// Landscape image
				double ratio = (double) maxImageWidth / originalWidth;
				scaledWidth = maxImageWidth;
				scaledHeight = (int) (originalHeight * ratio);
			} else {
				// Portrait or square image
				double ratio = (double) maxImageHeight / originalHeight;
				scaledWidth = (int) (originalWidth * ratio);
				scaledHeight = maxImageHeight;
			}
			// Scale the image to the new dimensions
			Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
			// Create a new ImageIcon from the scaled image
			return new ImageIcon(scaledImage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static ImageIcon createScaledIcon(ImageIcon originalIcon, int targetSize) {
		int originalWidth = originalIcon.getIconWidth();
		int originalHeight = originalIcon.getIconHeight();
		int width, height;
		
		if (originalWidth > originalHeight) {
			// Landscape image
			double ratio = (double) targetSize / originalWidth;
			width = targetSize;
			height = (int) (originalHeight * ratio);
		} else {
			// Portrait or square image
			double ratio = (double) targetSize / originalHeight;
			width = (int) (originalWidth * ratio);
			height = targetSize;
		}
		
		String fileExtension = originalIcon.getDescription();
		
		Image scaledImage;
		
		if (fileExtension != null && fileExtension.toLowerCase().endsWith(".gif")) {
			scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT);
		} else {
			scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
		}
		return new ImageIcon(scaledImage);
	}
}
