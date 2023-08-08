import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.controllers.Search;
import jp319.zerochan.models.PreviewImageItem;
import jp319.zerochan.models.PreviewImagesList;
import jp319.zerochan.utils.gui.WrapLayout;
import jp319.zerochan.utils.sanitations.CleanSearchResult;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PreviewImageItemTest {
	public static void main(String[] args) {
		FlatDarculaLaf.setup();
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Image Gallery");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setMinimumSize(new Dimension(600, 600));

			JPanel overlay_pnl = new JPanel() {
				public boolean isOptimizedDrawingEnabled() {
					return false;
				}
			};
			LayoutManager overlay = new OverlayLayout(overlay_pnl);
			overlay_pnl.setLayout(overlay);
			overlay_pnl.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(5,5,5,5),
					BorderFactory.createEmptyBorder(1,1,1,1)
			));

			JButton download_btn = new JButton("Download Selected");
			download_btn.setMinimumSize(new Dimension(50,50));
			download_btn.putClientProperty("JButton.buttonType", "roundRect");
			download_btn.setAlignmentX(0.98f);
			download_btn.setAlignmentY(0.98f);
			download_btn.setEnabled(false); // Since there is no selected images at first launch
											// then there are no images to download thus disabled.
			JPanel images_pnl = new JPanel(new WrapLayout(FlowLayout.LEFT));
			//imagesPanel.setPreferredSize(new Dimension(600, 600)); // Setting the preferred size of the image panel
																	 // will not show the scroll pane's scrollbar.

			String result = CleanSearchResult.clean(new Search("Honkai+Star+Rail", "p=1&l=5").getResult());
			PreviewImagesList imagesList =  jp319.zerochan.utils.statics.Gson.gson.fromJson(result, PreviewImagesList.class);

			for (int i = 0; i < imagesList.getItems().size(); i++) {
				PreviewImageItem previewImageItem = new PreviewImageItem(imagesList.getItems().get(i));
				images_pnl.add(previewImageItem);
			}

			JScrollPane scrollPane = new JScrollPane(images_pnl); // Add imagesPanel to the JScrollPane
			scrollPane.setPreferredSize(new Dimension(600, 500));
			scrollPane.setAlignmentX(0.98f);
			scrollPane.setAlignmentY(0.98f);

			overlay_pnl.add(download_btn);
			overlay_pnl.add(scrollPane);

			// Listeners
			// List to store selected image IDs
			List<String> selectedImageIds = new ArrayList<>();
			download_btn.addActionListener(e -> {
				for (Component component : images_pnl.getComponents()) {
					if (component instanceof PreviewImageItem previewImageItem) {
						if (previewImageItem.isSelected()) {
							selectedImageIds.add(previewImageItem.getImageId());
						}
					}
				}

				System.out.println("Selected Image IDs: " + selectedImageIds);
				selectedImageIds.clear();
			});
			// Update the download button state when any ImageItem's checkbox is changed
			for (Component component : images_pnl.getComponents()) {
				if (component instanceof PreviewImageItem previewImageItem) {
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
			}

			frame.add(overlay_pnl, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
		});
	}
}
