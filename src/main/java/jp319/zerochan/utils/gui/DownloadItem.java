package jp319.zerochan.utils.gui;

import javax.swing.*;
import java.awt.*;

public class DownloadItem extends JPanel {
	private final JProgressBar downloadProgressBar = new JProgressBar();
	private final JLabel percentage_lb = new JLabel("0%");
	public DownloadItem(String imageName) {
		setLayout(new GridBagLayout());
//		setBorder(BorderFactory.createCompoundBorder(
//				BorderFactory.createLineBorder(Color.GRAY,1,false),
//				BorderFactory.createEmptyBorder(5,5,5,5)
//		));
		
		JLabel imageName_lb = new JLabel();
		imageName_lb.setText(truncateAndReplace(imageName));
		imageName_lb.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		imageName_lb.setMaximumSize(new Dimension(50, imageName_lb.getPreferredSize().height));
		downloadProgressBar.setForeground(new Color(21, 240, 65));
		downloadProgressBar.setBorder(BorderFactory.createEmptyBorder(1,1,1,10));
		percentage_lb.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		
		add(imageName_lb, new GridBagConstraints(
				0,0,1,1,1.0,0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0,5,0,5),
				0,0
		));
		add(downloadProgressBar, new GridBagConstraints(
				1,0,3,1,1.0,0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL,
				new Insets(0,5,0,5),
				0,0
		));
		add(percentage_lb, new GridBagConstraints(
				4,0,1,1,1.0,0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0,5,0,5),
				0,0
		));
	}
	public void setDownloadProgressPercentage(int downloadPercentage) {
		SwingUtilities.invokeLater(() -> {
			percentage_lb.setText(downloadPercentage + "%");
			percentage_lb.revalidate();
			percentage_lb.repaint();
		});
	}
	public void setDownloadProgressBar(int progress) {
		SwingUtilities.invokeLater(() -> downloadProgressBar.setValue(progress));
	}
	private String truncateAndReplace(String str) {
		if (str.length() <= 15) {
			return str;
		} else {
			int endIndex = 15 - 3; // Keep space for the three dots
			return str.substring(0, endIndex) + "...";
		}
	}
}
