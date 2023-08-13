package jp319.zerochan.views.components;

import com.formdev.flatlaf.FlatDarculaLaf;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.swing.*;
import javax.swing.SwingWorker;

public class PreviewImage extends JPanel {
	
	ImageIcon img;
	private boolean init = true;
	private int zoomLevel = 0;
	private int minZoomLevel = -20;
	private int maxZoomLevel = 10;
	private double zoomMultiplicationFactor = 1.2;
	
	private Point dragStartScreen;
	private Point dragEndScreen;
	private AffineTransform coordTransform = new AffineTransform();
	
	private JProgressBar progressBar;
	private JLabel loading;
	private double loadingLabelX = 0.0;
	private double loadingLabelY = 0.0;
	private double loadingLabelWidth = 200.0;
	private double loadingLabelHeight = 100.0;
	
	public PreviewImage(String imageUrl) throws MalformedURLException {
		URL gifUrl = URI.create("https://i.imgur.com/I4a9zaK.gif").toURL();
		ImageIcon gifIcon = new ImageIcon(gifUrl);
		loading = new JLabel(gifIcon);
		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);
		progressBar.setString("Loading...");
		loading.setPreferredSize(new Dimension(250, 250));
		progressBar.setPreferredSize(new Dimension(450, 60));
		setLayout(new GridBagLayout());
		
		
		// Create constraints for centering
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		
		// Add progress bar and loading label with constraints
		add(loading, constraints);
		constraints.gridy = 1;
		add(progressBar, constraints);
		
		SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
			@Override
			protected Void doInBackground() throws Exception {
				// Simulate image loading
				for (int i = 0; i <= 100; i += 10) {
					Thread.sleep(500);
					publish(i);
				}
				
				// Load the actual image
				//img = ImageIO.read(URI.create("https://static.zerochan.net/Fate.stay.night.full.3661728.gif").toURL());
				img = new ImageIcon(URI.create(imageUrl).toURL());
				
				return null;
			}
			
			@Override
			protected void process(List<Integer> chunks) {
				int progress = chunks.get(chunks.size() - 1);
				progressBar.setValue(progress);
			}
			
			@Override
			protected void done() {
				progressBar.setVisible(false);
				loading.setVisible(false);
				repaint();
			}
		};
		
		worker.execute();
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dragStartScreen = e.getPoint();
				dragEndScreen = null;
			}
		});
		
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				pan(e);
			}
		});
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					zoom(e);
				}
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (img == null) {
			return; // Image not loaded yet
		}
		
		Graphics2D g2 = (Graphics2D) g;
		
		int x = (int) (this.getWidth() - (img.getIconWidth() * 0.5)) / 2;
		int y = (int) (this.getHeight() - (img.getIconHeight() * 0.5)) / 2;
		
		AffineTransform at = new AffineTransform();
		at.translate(x, y);
		at.scale(0.5, 0.5); // Display at 50% of original size
		if (init) {
			g2.setTransform(at);
			init = false;
			coordTransform = g2.getTransform();
		} else {
			g2.setTransform(coordTransform);
		}
		
		//g2.drawImage(img, 0, 0, this);
		img.paintIcon(this, g2, 0, 0);
		
		g2.dispose();
		
		double transformScaleX = coordTransform.getScaleX();
		double transformScaleY = coordTransform.getScaleY();
		loadingLabelX = coordTransform.getTranslateX() + x * transformScaleX;
		loadingLabelY = coordTransform.getTranslateY() + y * transformScaleY;
		
		loading.setBounds((int) loadingLabelX, (int) loadingLabelY, (int) loadingLabelWidth, (int) loadingLabelHeight);
		progressBar.setBounds((int) loadingLabelX, (int) (loadingLabelY + loadingLabelHeight), (int) loadingLabelWidth, 30);
	}
	
	private void pan(MouseEvent e) {
		try {
			dragEndScreen = e.getPoint();
			Point2D.Float dragStart = transformPoint(dragStartScreen);
			Point2D.Float dragEnd = transformPoint(dragEndScreen);
			double dx = dragEnd.getX() - dragStart.getX();
			double dy = dragEnd.getY() - dragStart.getY();
			coordTransform.translate(dx, dy);
			dragStartScreen = dragEndScreen;
			dragEndScreen = null;
			repaint();
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
	}
	
	private void zoom(MouseWheelEvent e) {
		try {
			int wheelRotation = e.getWheelRotation();
			Point p = e.getPoint();
			if (wheelRotation > 0) {
				if (zoomLevel < maxZoomLevel) {
					zoomLevel++;
					Point2D p1 = transformPoint(p);
					coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
					Point2D p2 = transformPoint(p);
					coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
					repaint();
				}
			} else {
				if (zoomLevel > minZoomLevel) {
					zoomLevel--;
					Point2D p1 = transformPoint(p);
					coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
					Point2D p2 = transformPoint(p);
					coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
					repaint();
				}
			}
		} catch (NoninvertibleTransformException ex) {
			ex.printStackTrace();
		}
	}
	
	private Point2D.Float transformPoint(Point p1) throws NoninvertibleTransformException {
		AffineTransform inverse = coordTransform.createInverse();
		Point2D.Float p2 = new Point2D.Float();
		inverse.transform(p1, p2);
		return p2;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 600);
	}
	// Getter
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	
	public static void main(String[] args) {
		FlatDarculaLaf.setup();
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Preview");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			try {
				frame.add(new PreviewImage("https://static.zerochan.net/Cookie.Run.full.3453716.gif"), BorderLayout.CENTER);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			frame.pack();
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame.setVisible(true);
		});
	}
}
