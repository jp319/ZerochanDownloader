package jp319.zerochan.utils.gui;

import jp319.zerochan.models.FullImageData;

import javax.swing.*;
import java.awt.*;

public class FullImageFrame extends JFrame {
	FullImageData fullImageData;
	FullImageView fullImageView;
	DetailsDialog detailsDialog;
	public FullImageFrame() {
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(500,500));
		setIconImage(Toolkit.getDefaultToolkit().getImage("src/main/resources/images/icon256x256.png"));
		fullImageView = new FullImageView();
		pack();
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(false);
	}
	public void setFullImageData(FullImageData fullImageData) {
		this.fullImageData = fullImageData;
		SwingUtilities.invokeLater(() -> {
			setTitle(fullImageData.getPrimary() + " - ZeroArtFetcher");
			
			initImageView();
		
			add(fullImageView, BorderLayout.CENTER);
		});
	}
	private void initImageView() {
		fullImageView.setImageLabel(fullImageData);
		detailsDialog = new DetailsDialog(this, "Details",fullImageData);
	}
	// Getters
	public FullImageView getFullImageView() {
		return fullImageView;
	}
	public DetailsDialog getDetailsDialog() {
		return detailsDialog;
	}
	public void showImageFrame() {
		setVisible(true);
	}
}
