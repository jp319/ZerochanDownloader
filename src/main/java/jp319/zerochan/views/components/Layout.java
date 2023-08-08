package jp319.zerochan.views.components;

import jp319.zerochan.controllers.AppController;
import jp319.zerochan.views.Listeners.FrameListener;
import jp319.zerochan.views.callbacks.FrameListenerInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentListener;

public class Layout extends JFrame {
	private Container frameContainer;
	private Header header;
	private Body body;
	private Footer footer;
	private FrameListenerInterface frameCallback;
	private AppController appController;
	public Layout () {
		initFrame();
		initFrameContainer();
		// Add Components here { //
		initHeader();
		initBody();
		initFooter();
		
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
	private void initController() {
		appController = new AppController(this, header, body, footer);
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