package jp319.zerochan.models;

import jp319.zerochan.utils.gui.HyperLinkToolTip;
import jp319.zerochan.utils.gui.ScaledIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;

public class PreviewImageItem extends JLayeredPane {
	private final PreviewImageData imageData;
	private final JCheckBox imageItem_chk;
	private final JLabel image_lb;
	
	public PreviewImageItem(PreviewImageData imageData) {
		this.imageData = imageData;

		ImageIcon loadingGif = new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/loading.gif")));

		// Create the checkbox
		imageItem_chk = new JCheckBox();
		imageItem_chk.setBounds(0, 0, 20, 20);
		add(imageItem_chk, JLayeredPane.PALETTE_LAYER);
		
		// Scale the image while keeping the aspect ratio
		int width = 150; // Change the width as needed
		int height = 150; // Change the height as needed
		ImageIcon scaledImageIcon = ScaledIcon.createScaledIcon(loadingGif, width);
		image_lb = new JLabel(scaledImageIcon) {
			// Sets Custom Tool tip
			public JToolTip createToolTip() {
				JToolTip toolTip = new HyperLinkToolTip();
				toolTip.setComponent(this);
				return toolTip;
			}
		};
		image_lb.setBounds(0, 0, scaledImageIcon.getIconWidth(), scaledImageIcon.getIconHeight());
		add(image_lb, JLayeredPane.DEFAULT_LAYER);

		// Tool tip for image
		String toolTipText =
				"<html>" +
						"  Primary : " + imageData.getTag() + "<br>" +
						"Dimension : " + "(" + imageData.getWidth() + "x" + imageData.getHeight() + ")<br>" +
						"   Source : " + (! imageData.getSource().isEmpty() ?
						"<a href='"+imageData.getSource()+"'> "+imageData.getSource()+" </a>" : "n/a") + "<br>" +
						"     Tags :" + getFormattedTags() +
						"</html>";
		image_lb.setToolTipText(toolTipText);

		// Listeners
		imageItem_chk.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				image_lb.setBorder(BorderFactory.createLineBorder(new Color(7,132,181),2));
			} else {
				image_lb.setBorder(null);
			}
		});

		// Set the size of the ImageItem
		setPreferredSize(new Dimension(scaledImageIcon.getIconWidth(), height));
	}
	public void setImageLabel(ImageIcon scaledImageIcon) {
		SwingUtilities.invokeLater(() -> {
			image_lb.setIcon(scaledImageIcon);
			image_lb.setBounds(0, 0, scaledImageIcon.getIconWidth(), scaledImageIcon.getIconHeight());
			
			// Set the size of the ImageItem
			setPreferredSize(new Dimension(scaledImageIcon.getIconWidth(), 150));
			revalidate();
			repaint();
		});
	}
	public String getThumbnail() {
		return imageData.getThumbnail();
	}
	public JCheckBox getCheckBox() {
		return imageItem_chk;
	}
	public boolean isSelected() {
		return imageItem_chk.isSelected();
	}
	public String getImageId() {
		return this.imageData.getId();
	}
	public int getImageWidth() {
		return this.imageData.getWidth();
	}
	public int getImageHeight() {
		return this.imageData.getHeight();
	}
	public String getImageSource() {
		return this.imageData.getSource();
	}
	public String getImageTag() {
		return this.imageData.getTag();
	}
	public List<String> getImageTags() {
		return this.imageData.getTags();
	}
	public String getFormattedTags() {
		int size = this.imageData.getTags().size();
		List<String> tags = this.imageData.getTags();
		StringBuilder formattedTags = new StringBuilder();
		
		for (int i = 0; i < size; i++) {
			if (i > 0 && i % 6 == 0) {
				formattedTags.append("<br>");
			} else if (i > 0) {
				formattedTags.append(", ");
			}
			formattedTags.append(tags.get(i));
		}
		
		return formattedTags.toString();
	}
}
