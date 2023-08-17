package jp319.zerochan.utils.gui;

import jp319.zerochan.models.FullImageData;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FullImageView extends JPanel {
	private FullImageData fullImageData;
	JScrollPane image_pane;
	private JPanel image_pnl;
	private JLabel image_lb;
	private ImageIcon image_icn;
	private JProgressBar image_prg;
	private IIOReadProgressListener iioReadProgressListener;
	private final JPanel buttons_pnl = new JPanel(new BorderLayout());
	private final JPanel buttonsMid_pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private final JButton imageProperties_btn = new JButton("Details");
	private final JButton prevPage_btn = new JButton("⏪");
	private final JButton nextPage_btn = new JButton("⏩");
	public FullImageView() {
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(500,500));
		
		initImagePanel();
		initButtonsPanel();
		initLoading();
		initProgressListener();
		
		setImageLabelListener();
		
		add(image_pnl, BorderLayout.CENTER);
		add(buttons_pnl, BorderLayout.SOUTH);
	}
	public FullImageView(FullImageData fullImageData) {
		this.fullImageData = fullImageData;
		
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(500,500));
		
		initImagePanel();
		initButtonsPanel();
		initLoading();
		initProgressListener();
		
		setImageLabelListener();
		
		int processors = 1;
		ExecutorService service = Executors.newFixedThreadPool(processors);
		service.submit(loadImage(fullImageData.getFull()));
		service.shutdown();
		
		add(image_pnl, BorderLayout.CENTER);
		add(buttons_pnl, BorderLayout.SOUTH);
	}
	private void initImagePanel() {
		image_pnl = new JPanel(new BorderLayout());
	}
	private void initButtonsPanel() {
		buttons_pnl.add(prevPage_btn, BorderLayout.WEST);
		buttons_pnl.add(nextPage_btn, BorderLayout.EAST);
		buttons_pnl.add(buttonsMid_pnl, BorderLayout.CENTER);
		buttonsMid_pnl.add(imageProperties_btn);
	}
	private void initLoading() {
		ImageIcon loadingGif = new
				ImageIcon(Objects.requireNonNull(getClass()
				.getResource("/images/loading-mako-chan.gif")));
		image_lb = new JLabel(loadingGif);
		
		image_prg = new JProgressBar(0, 100);
		image_prg.setVisible(true);
		image_prg.setStringPainted(true);
		image_prg.setForeground(new Color(21, 240, 65));
		
		image_pane = new JScrollPane(image_lb);
		image_pane.getVerticalScrollBar().setUnitIncrement(16);
		image_pane.getHorizontalScrollBar().setUnitIncrement(16);
		
		image_pnl.add(image_pane, BorderLayout.CENTER);
		image_pnl.add(image_prg, BorderLayout.SOUTH);
	}
	private void initProgressListener() {
		iioReadProgressListener = new IIOReadProgressListener() {
			@Override
			public void imageStarted(ImageReader source, int imageIndex) {
				SwingUtilities.invokeLater(() -> {
					image_prg.setValue(0);
					image_prg.setVisible(true);
					image_prg.setForeground(new Color(21, 240, 65));
				});
			}
			@Override
			public void imageProgress(ImageReader source, float percentageDone) {
				image_prg.setValue((int) percentageDone);
			}
			@Override
			public void imageComplete(ImageReader source) {
				image_prg.setValue(100);
			}
			@Override
			public void sequenceStarted(ImageReader source, int minIndex) {}
			@Override
			public void sequenceComplete(ImageReader source) {}
			@Override
			public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {}
			@Override
			public void thumbnailProgress(ImageReader source, float percentageDone) {}
			@Override
			public void thumbnailComplete(ImageReader source) {}
			@Override
			public void readAborted(ImageReader source) {}
		};
	}
	// Public Methods
	public void showLoading() {
		ImageIcon loadingGif = new
				ImageIcon(Objects.requireNonNull(getClass()
				.getResource("/images/loading-mako-chan.gif")));
		image_lb.setIcon(loadingGif);
	}
	public void setImageLabel(FullImageData fullImageData) {
		this.fullImageData = fullImageData;
		int processors = 1;
		ExecutorService service = Executors.newFixedThreadPool(processors);
		service.submit(loadImage(fullImageData.getFull()));
		service.shutdown();
	}
	// Getter
	public JButton getImagePropertiesButton() {
		return imageProperties_btn;
	}
	public FullImageData getFullImageData() {
		return fullImageData;
	}
	public JButton getPrevPageButton() {
		return prevPage_btn;
	}
	public JButton getNextPageButton() {
		return nextPage_btn;
	}
	
	// Listeners
	private Runnable loadImage(String imageUrl) {
		return () -> {
			try {
				URI uri = URI.create(imageUrl);
				URL url = uri.toURL();
				URLConnection connection = url.openConnection();
				int contentLength = connection.getContentLength();
				
				try (InputStream inputStream = connection.getInputStream()) {
					byte[] buffer = new byte[1024];
					int bytesRead;
					int totalBytesRead = 0;
					image_prg.setVisible(true);
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						totalBytesRead += bytesRead;
						float downloadPercentage = (float) totalBytesRead / contentLength * 99;
						
						SwingUtilities.invokeLater(() -> {
							image_prg.setValue((int) downloadPercentage);
							image_prg.setString(((int) downloadPercentage) + "%");
						});
					}
				}
				
				ImageInputStream imageInputStream = ImageIO.createImageInputStream(url.openStream());
				Iterator<ImageReader> iterator = ImageIO.getImageReaders(imageInputStream);
				
				if (iterator.hasNext()) {
					SwingUtilities.invokeLater(() -> image_prg.setString("Loading Image"));
					ImageReader reader = iterator.next();
					reader.setInput(imageInputStream);
					reader.addIIOReadProgressListener(iioReadProgressListener);
					
					SwingUtilities.invokeLater(() -> {
						image_icn = new ImageIcon(url);
						scaleImageIcon(image_icn, getHeight());
					});
					
					SwingUtilities.invokeLater(() -> {
						image_prg.setString("Done");
						image_prg.setVisible(false);
					});
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}
	private void setImageLabelListener() {
		MouseAdapter ma = new MouseAdapter() {
			private Point origin;
			@Override
			public void mousePressed(MouseEvent e) {
				origin = new Point(e.getPoint());
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (origin != null) {
					JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, image_lb);
					if (viewPort != null) {
						int deltaX = origin.x - e.getX();
						int deltaY = origin.y - e.getY();
						
						Rectangle view = viewPort.getViewRect();
						view.x += deltaX;
						view.y += deltaY;
						
						image_lb.scrollRectToVisible(view);
					}
				}
			}
		};
		image_lb.addMouseListener(ma);
		image_lb.addMouseMotionListener(ma);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				//System.out.println("Resized");
				//System.out.println(getWidth());
				if (image_icn != null) {
					scaleImageIcon(image_icn, getHeight());
				}
			}
		});
	}
	private void scaleImageIcon(ImageIcon icon, int height) {
		
		int processors = 1;
		ExecutorService service = Executors.newFixedThreadPool(processors);
		
		
		Icon returnIcon = null;
		try {
			returnIcon = service.submit(() -> ScaledIcon.createScaledIcon(icon, height-10) ).get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		service.shutdown();
		
		Icon finalReturnIcon = returnIcon;
		SwingUtilities.invokeLater(() -> {
			image_lb.setIcon(finalReturnIcon);
			//image_lb.revalidate();
			image_lb.repaint();
		});
	}
}
