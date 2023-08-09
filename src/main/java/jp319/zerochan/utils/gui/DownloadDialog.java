package jp319.zerochan.utils.gui;

import javax.swing.*;
import java.awt.*;

public class DownloadDialog extends JDialog {
	private final JLabel header_lb = new JLabel("Downloads", SwingConstants.CENTER);
	private final ScrollablePanel downloads_pnl = new ScrollablePanel();
	private final JScrollPane downloadsWrapper = new JScrollPane();
	private final JPanel footer_pnl = new JPanel(new GridBagLayout());
	private final JProgressBar downloadProgressBar = new JProgressBar();
	private final JLabel downloadRatio = new JLabel("0/0");
	int totalDownload = 0, currentDownload, finished = 0;
	public DownloadDialog(Frame owner) {
		super(owner);
		setSize(new Dimension(400,400));
		setTitle("Download Images");
		setLocationRelativeTo(owner);
		setAlwaysOnTop(false);
		setResizable(false);
		
		initHeader();
		initDownloadsPanel();
		initFooterPanel();
		
		JPanel layout_pnl = new JPanel(new BorderLayout());
		layout_pnl.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		layout_pnl.add(header_lb, BorderLayout.NORTH);
		layout_pnl.add(downloadsWrapper, BorderLayout.CENTER);
		layout_pnl.add(footer_pnl, BorderLayout.SOUTH);
		
		setContentPane(layout_pnl);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setVisible(false);
	}
	private void initHeader() {
		header_lb.setBorder(BorderFactory.createEmptyBorder(0,5,20,5));
	}
	private void initDownloadsPanel() {
		downloads_pnl.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
		downloads_pnl.setScrollableBlockIncrement(
				ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 200);
		downloads_pnl.setScrollableUnitIncrement(
				ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PIXELS, 15);
		downloads_pnl.setLayout(new WrapLayout(FlowLayout.LEFT,0,0));
		downloadsWrapper.setViewportView(downloads_pnl);
	}
	private void initFooterPanel() {
		footer_pnl.setBorder(BorderFactory.createEmptyBorder(10,2,10,2));
		
		downloadProgressBar.putClientProperty("JProgressBar.largeHeight", true);
		downloadProgressBar.setForeground(new Color(21, 240, 65));
		
		downloadRatio.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		downloadRatio.putClientProperty( "FlatLaf.styleClass", "monospaced" );
		
		footer_pnl.add(downloadProgressBar, new GridBagConstraints(
				0,0,8,0,1.0,0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0,0,0,0),
				0,0
		));
		footer_pnl.add(downloadRatio, new GridBagConstraints(
				8,0,1,0,0.0,0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0,0,0,0),
				0,0
		));
	}
	// Methods
	public void addDownloadItem(JComponent downloadItem) {
		downloadItem.setPreferredSize(new Dimension(350, downloadItem.getPreferredSize().height));
		downloads_pnl.add(downloadItem);
		SwingUtilities.invokeLater(() -> {
			downloads_pnl.revalidate();
			downloads_pnl.repaint();
		});
	}
	public void removeAllItems() {
		downloads_pnl.removeAll();
		SwingUtilities.invokeLater(() -> {
			downloads_pnl.revalidate();
			downloads_pnl.repaint();
		});
	}
	public void updateDownloadRatio() {
		SwingUtilities.invokeLater(() -> downloadRatio.setText(incrementDownloadedItem() + "/" + totalDownload));
	}
	public void setTotalDownloadedItems(int totalDownloads) {
		SwingUtilities.invokeLater(() -> {
			totalDownload = totalDownloads;
			resetDownloadedItem();
			downloadRatio.setText(currentDownload+"/" + totalDownload);
		});
	}
	private int incrementDownloadedItem() {
		return currentDownload+=1;
	}
	public void resetDownloadedItem() {
		currentDownload = 0;
	}
	public void updateProgress() {
		finished = finished + 1;
		int percentage = (int) (((double) finished / totalDownload) * 100);
		downloadProgressBar.setValue(percentage);
	}
	// Listeners
}
