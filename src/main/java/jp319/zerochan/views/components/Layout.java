package jp319.zerochan.views.components;

import jp319.zerochan.controllers.AppController;
import jp319.zerochan.utils.gui.DownloadDialog;
import jp319.zerochan.utils.gui.FullImageFrame;
import jp319.zerochan.utils.gui.MenuBar;
import jp319.zerochan.utils.gui.SettingsDialog;
import jp319.zerochan.views.Listeners.FrameListener;
import jp319.zerochan.views.callbacks.FrameListenerInterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.InputStream;

public class Layout extends JFrame {
	private Container frameContainer;
	private Header header;
	private Body body;
	private Footer footer;
	FrameListenerInterface frameCallback;
	AppController appController;
	private DownloadDialog downloadDialog;
	private MenuBar menuBar;
	private SettingsDialog settingsDialog;
	public Layout () {
		initFrame();
		initFrameContainer();
		// Add Components here { //
		initHeader();
		initBody();
		initFooter();
		initDownloadDialog();
		initMenuBar();
		initSettingsDialog();
		
		initCallbacks();
		initToolTipManager();
		initController();
		// } Add Components here //
		pack();
		centerFrameOnScreen();
	}
	private void initFrame() {
		setTitle("ZeroArtFetcher");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int preferredFrameWidth = 600;
		int preferredFrameHeight = 600;
		setPreferredSize(new Dimension(preferredFrameWidth, preferredFrameHeight));
		setMinimumSize(new Dimension(preferredFrameWidth, preferredFrameHeight));
		InputStream frameIconImage = FullImageFrame.class.getResourceAsStream("/images/icon256x256.png");
		Image image = null;
		try {
			if (frameIconImage != null) {
				image = ImageIO.read(frameIconImage);
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }
		setIconImage(image);
	}
	private void initFrameContainer() {
		frameContainer = getContentPane();
		frameContainer.setLayout(new BorderLayout());
	}
	private void centerFrameOnScreen() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (screenSize.width - getWidth()) / 2;
		int centerY = (screenSize.height - getHeight()) / 3;
		setLocation(centerX, centerY);
	}
	// Components
	private void initHeader() {
		header = new Header();
		frameContainer.add(header, BorderLayout.NORTH);
	}
	private void initBody() {
		body = new Body();
		frameContainer.add(body, BorderLayout.CENTER);
	}
	private void initFooter() {
		footer = new Footer();
		frameContainer.add(footer, BorderLayout.SOUTH);
	}
	private void initMenuBar() {
		menuBar = new MenuBar();
		setJMenuBar(menuBar);
	}
	private void initDownloadDialog() {
		downloadDialog = new DownloadDialog(this);
	}
	private void initSettingsDialog() {
		settingsDialog = new SettingsDialog(this);
	}
	private void initController() {
		appController = new AppController(this, header, body, footer, downloadDialog, menuBar, settingsDialog);
	}
	// Listeners
	private void initCallbacks() {
		frameCallback = header;
		ComponentListener frameListener =
				new FrameListener(this, frameCallback);
		addComponentListener(frameListener);
	}
	// Helper Methods
	private void initToolTipManager() {
		ToolTipManager.sharedInstance().setInitialDelay(500);
		ToolTipManager.sharedInstance().setReshowDelay(1000);
		ToolTipManager.sharedInstance().setDismissDelay(15000);
	}
	// Getters
	public Body getBody() {
		return body;
	}
}
