package jp319.zerochan.views.components;

import javax.swing.*;
import java.awt.*;

public class Footer extends JPanel {
	private final JProgressBar progressBar = new JProgressBar();
	private final JLabel downloadRatio = new JLabel("0/0");
	private int totalItems;
	private int loadedItem = 0;
	public Footer() {
		initFooterPanel();
	}
	private void initFooterPanel() {
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 20, 10, 20), // Outside Margin
				BorderFactory.createLineBorder(Color.GRAY, 1, false) // LineBorder Inside the Margin
		));
		setLayout(new GridBagLayout());
		initComponents();
		add(downloadRatio, new GridBagConstraints(
				9,0,1,0,0.0,0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.EAST,
				new Insets(0,0,0,0),
				0,0
		));
		add(progressBar, new GridBagConstraints(
				0,0,8,0,1.0,0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL,
				new Insets(0,0,0,0),
				0,0
		));
	}
	private void initComponents() {
		downloadRatio.putClientProperty( "FlatLaf.styleClass", "monospaced" );
		downloadRatio.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		progressBar.putClientProperty("JProgressBar.largeHeight", true);
		progressBar.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		progressBar.setForeground(new Color(0, 255, 215));
		progressBar.setValue(100);
	}
	public JProgressBar getProgressBar() {
		return progressBar;
	}
	public void setProgressBarProgress(int progress) {
		progressBar.setValue(progress);
	}
	public void updateLoadedItemLabel() {
		SwingUtilities.invokeLater(() -> downloadRatio.setText(incrementLoadedItem() + "/" + totalItems));
	}
	public void setTotalItems(int totalDownloads) {
		SwingUtilities.invokeLater(() -> {
			totalItems = totalDownloads;
			resetLoadedItem();
			downloadRatio.setText(loadedItem+"/" + totalItems);
		});
	}
	private int incrementLoadedItem() {
		return loadedItem+=1;
	}
	public void resetLoadedItem() {
		loadedItem = 0;
	}
}
