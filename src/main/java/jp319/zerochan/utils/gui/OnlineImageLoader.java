package jp319.zerochan.utils.gui;

import jp319.zerochan.models.PreviewImageItem;
import jp319.zerochan.views.components.Body;
import jp319.zerochan.views.components.Footer;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

public class OnlineImageLoader {
	ProgressListener progressListener;
	Footer footer;
	Body body;
	PreviewImageItem previewImageItem;
	public OnlineImageLoader(Footer footer, Body body, PreviewImageItem previewImageItem) {
		this.footer = footer;
		this.body = body;
		this.previewImageItem = previewImageItem;
		
		progressListener = new ProgressListener(footer);
	}
	public Runnable loadImage() {
		body.addImage(previewImageItem);
		body.revalidate();
		body.repaint();
		
		return () ->{
			try {
				// This code is tested with gif, and it animates the gifs just fine.
				// URI uri = URI.create("https://static.zerochan.net/Kuro.Usagi.%28Mondai-ji-tachi%29.full.3985360.gif");
				
				URI uri = URI.create(previewImageItem.getThumbnail());
				ImageInputStream imageInputStream = ImageIO.createImageInputStream(uri.toURL().openStream());
				Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
				
				if (iterator.hasNext()) {
					ImageReader reader = iterator.next();
					reader.setInput(imageInputStream);
					reader.addIIOReadProgressListener(progressListener);
					
					int numImages = reader.getNumImages(true); // Get the number of images in the sequence (for animated GIFs)
					ImageIcon[] frames = new ImageIcon[numImages];
					
					for (int i = 0; i < numImages; i++) {
						// Read each frame of the animated GIF
						Image image = reader.read(i);
						ImageIcon frameIcon = new ImageIcon(image);
						frames[i] = ScaledIcon.createScaledIcon(frameIcon, 150);
					}
					
					if (previewImageItem.isDisplayable()) {
						SwingUtilities.invokeLater(() -> {
							body.getLoadingPanel().setVisible(false); // Hide loading panel when done searching
							// Create a Timer to loop through the frames and update the image on the JLabel
							final int[] frameIndex = {0};
							Timer timer = new Timer(100, e -> {
								previewImageItem.setImageLabel(frames[frameIndex[0]]);
								previewImageItem.revalidate();
								previewImageItem.repaint();
								frameIndex[0] = (frameIndex[0] + 1) % numImages;
							});
							timer.setRepeats(true);
							timer.start();
						});
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}
}
