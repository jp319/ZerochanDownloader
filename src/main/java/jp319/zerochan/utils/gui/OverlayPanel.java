package jp319.zerochan.utils.gui;

import jp319.zerochan.models.PreviewImageItem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OverlayPanel extends JPanel {
	private final List<String> selectedImageIds = new ArrayList<>();
	private final JButton download_btn = new JButton("Download");
	//private final JPanel images_pnl = new JPanel(new WrapLayout(FlowLayout.LEFT, 25,5));
	private final ScrollablePanel images_pnl = new ScrollablePanel(); // Much better than WrapLayout
	private final JPanel loading_pnl = new JPanel(new BorderLayout());
	private final JScrollPane scrollPane = new JScrollPane();
	public OverlayPanel() {
		LayoutManager overlay = new OverlayLayout(this);
		setLayout(overlay);
		
		loading_pnl.setOpaque(false);
		loading_pnl.setAlignmentX(0.92f);
		loading_pnl.setAlignmentY(0.92f);
		loading_pnl.setVisible(false);
		
		download_btn.setAlignmentX(0.92f);
		download_btn.setAlignmentY(0.92f);
		download_btn.setEnabled(false);
		
		images_pnl.setScrollableWidth( ScrollablePanel.ScrollableSizeHint.FIT );
		images_pnl.setScrollableBlockIncrement(
				ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 200);
		images_pnl.setScrollableUnitIncrement(ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PIXELS, 15);
		
		scrollPane.setAlignmentX(0.92f);
		scrollPane.setAlignmentY(0.92f);
		scrollPane.setViewportView(images_pnl);

		setListeners();
		
		add(loading_pnl);
		add(download_btn); // The first one that is added will be
		add(scrollPane);   // at the top.
	}
	// Getters
	public JPanel getLoadingPanel() {
		return loading_pnl;
	}
	public JButton getDownloadButton() {
		return download_btn;
	}
	public JScrollPane getScrollPane() {
		return scrollPane;
	}
	public JPanel getImagesPanel() {
		return images_pnl;
	}
	public List<String> getSelectedImageIds() {
		return selectedImageIds;
	}
	public void addImage(PreviewImageItem previewImageItem) {
		setImageItemCheckBoxListener(previewImageItem);
		images_pnl.add(previewImageItem);
	}
	// Methods
	public void showLoading() {
		SwingUtilities.invokeLater(() -> {
			loading_pnl.removeAll();
			
			ImageIcon loadingGif = new ImageIcon("src/main/resources/images/loading.gif");
			
			String labelText = "Searching...";
			setLabelHtmlText(loadingGif, labelText);
		});
	}
	private void setLabelHtmlText(ImageIcon loadingGif, String labelText) {
		String htmlText =
				"<html><div style='text-align:center;'> " +
						"<img src=\"file:" + loadingGif.getDescription() + "\" width=100 height=100>" +
						"<br>" + labelText + "</div></html>";
		
		JLabel label = new JLabel(htmlText, SwingConstants.CENTER);
		
		loading_pnl.add(label, BorderLayout.CENTER);
		loading_pnl.setVisible(true);
		loading_pnl.revalidate();
		loading_pnl.repaint();
	}
	public void showError() {
		SwingUtilities.invokeLater(() -> {
			loading_pnl.removeAll();
			
			ImageIcon noResultGif = new ImageIcon("src/main/resources/images/no-result.gif");
			
			String labelText = "No Results Found";
			setLabelHtmlText(noResultGif, labelText);
		});
	}
	public void hideLoading() {
		SwingUtilities.invokeLater(() -> loading_pnl.setVisible(false));
	}
	public void emptyImagePanel() {
		SwingUtilities.invokeLater(() -> {
			images_pnl.removeAll();
			images_pnl.revalidate();
			images_pnl.repaint();
		});
	}
	// Listeners
	private void setListeners() {
		download_btn.addActionListener(e -> {
			selectedImageIds.clear(); // Resets List when new download starts.
			for (Component component : images_pnl.getComponents()) {
				if (component instanceof PreviewImageItem previewImageItem) {
					if (previewImageItem.isSelected()) {
						selectedImageIds.add(previewImageItem.getImageId());
					}
				}
			}

			System.out.println("Selected Image IDs: " + selectedImageIds);
		});
	}
	private void setImageItemCheckBoxListener(PreviewImageItem previewImageItem) {
		previewImageItem.getCheckBox().addItemListener(e -> {
			// Check if any ImageItem is selected
			boolean anySelected = false;
			for (Component comp : images_pnl.getComponents()) {
				if (comp instanceof PreviewImageItem item) {
					if (item.isSelected()) {
						anySelected = true;
						break;
					}
				}
			}
			// Enable or disable the download button based on selection
			download_btn.setEnabled(anySelected);
		});
	}
	// Overrides
	@Override
	public boolean isOptimizedDrawingEnabled() {
		return false;
	}
}
