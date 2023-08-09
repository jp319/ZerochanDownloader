package jp319.zerochan.utils.gui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import jp319.zerochan.models.PreviewImageItem;
import jp319.zerochan.utils.sanitations.SanitizeText;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OverlayPanel extends JPanel {
	private final List<String> selectedImageIds = new ArrayList<>();
	private final JPanel buttons_pnl;
	private final JButton download_btn = new JButton("Download");
	private final JCheckBox selectAll_chk = new JCheckBox("Select All");
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
		
		buttons_pnl = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				int arcRadius = 40; // Adjust the arc radius as needed
				int width = getWidth();
				int height = getHeight();
				
				g2d.setColor(new Color(0x1E1F22));
				g2d.fillRoundRect(0, 0, width - 1, height - 1, arcRadius, arcRadius);
			}
		};
		initButtonsPanel();
		
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
		//add(download_btn); // The first one that is added will be
		add(buttons_pnl);
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
	public void setSelectAllCheckBox() {
		int selectedImageCount = 0;
		int totalImageCount = images_pnl.getComponentCount();
		for (Component component : images_pnl.getComponents()) {
			if (component instanceof PreviewImageItem previewImageItem) {
				if (previewImageItem.isSelected()) {
					selectedImageCount++;
				}
			}
		}
		// If selectedImageCount == totalImageCount then
		// that would mean all images are selected
		// so select all is checked.
		selectAll_chk
				.setSelected(selectedImageCount == totalImageCount);
	}
	private void initButtonsPanel() {
		int margin = 3;
		Insets wrapperInsets = new Insets(2+2,3+2,2+2,3+2);
		Insets chkInset = new Insets(1+2,1+5,1+2,1+5);
		Insets dwnInset = new Insets(2+3,2+6,2+3,2+6);
		JPanel selectAllWrapper = new JPanel();
		selectAllWrapper.setLayout(new BoxLayout(selectAllWrapper, BoxLayout.X_AXIS));
		selectAllWrapper.setOpaque(false);
		
		buttons_pnl.setLayout(new BoxLayout(buttons_pnl, BoxLayout.X_AXIS));
		
		selectAll_chk.setAlignmentY(Component.CENTER_ALIGNMENT);
		selectAll_chk.setForeground(Color.GRAY);
		selectAllWrapper.add(selectAll_chk);
		selectAllWrapper.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(margin,margin,margin,margin),
				new FlatLineBorder(chkInset,new Color(0x545658),1,50)
		));
		
		download_btn.setAlignmentY(Component.CENTER_ALIGNMENT);
		download_btn.setMargin(dwnInset);
		download_btn.setOpaque(false);
		download_btn.setContentAreaFilled(false);
		download_btn.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(margin,margin,margin,margin),
				new FlatLineBorder(dwnInset,new Color(0x545658),1,50)
		));
		
		buttons_pnl.add(selectAllWrapper);
		buttons_pnl.add(download_btn);
		
		buttons_pnl.setOpaque(false);
		buttons_pnl.setAlignmentX(0.92f);
		buttons_pnl.setAlignmentY(0.92f);
		//buttons_pnl.setBackground(new Color(1,1,1, 0));
		buttons_pnl.setBorder(new FlatLineBorder(wrapperInsets,new Color(0x1E1F22),2,50));
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
		// Download Button Listener
		download_btn.addActionListener(e -> {
			selectedImageIds.clear(); // Resets List when new download starts.
			for (Component component : images_pnl.getComponents()) {
				if (component instanceof PreviewImageItem previewImageItem) {
					if (previewImageItem.isSelected()) {
						String fileName = SanitizeText.removeIllegalCharacters(
								previewImageItem.getImageTag()
						);
						fileName = fileName + ".full." + previewImageItem.getImageId();
						selectedImageIds.add(fileName);
					}
				}
			}

			System.out.println("Selected Image IDs: " + selectedImageIds);
		});
		// Select all checkbox Listener
		selectAll_chk.addActionListener(e -> {
			boolean selected = selectAll_chk.isSelected();
			for (Component component : images_pnl.getComponents()) {
				if (component instanceof PreviewImageItem previewImageItem) {
					// On click if select all checkbox is checked/unchecked
					// change all items to checked/unchecked
					previewImageItem.getCheckBox().setSelected(selected);
				}
			}
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
