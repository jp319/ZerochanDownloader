package jp319.zerochan.views.components;

import jp319.zerochan.utils.gui.OverlayPanel;
import jp319.zerochan.utils.gui.WrapLayout;

import javax.swing.*;
import java.awt.*;

public class Body extends OverlayPanel {
	public Body() {
		initBodyPanel();
	}
	private void initBodyPanel() {
		setPreferredSize(new Dimension(560,560));
		// Customize ScrollPane, Panel, and the Button inside the OverlayPanel
		getScrollPane().putClientProperty("JScrollBar.showButtons", true);
		getScrollPane().setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 10, 10), // Outside Margin
				BorderFactory.createLineBorder(Color.GRAY, 3, true) // LineBorder Inside the Margin
		));
		
		getImagesPanel().setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
		getImagesPanel().setBorder(BorderFactory.createEmptyBorder(20,5,10,5));
		
		getLoadingPanel().setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(10, 10, 10, 10), // Outside Margin
				BorderFactory.createLineBorder(Color.GRAY, 3, true) // LineBorder Inside the Margin
		));
	}
}
