package jp319.zerochan.utils.gui;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.ToolTipUI;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HyperLinkToolTip extends JToolTip {
	private final JEditorPane theEditorPane;
	public HyperLinkToolTip() {
		setLayout(new BorderLayout());
		LookAndFeel.installBorder(this, "ToolTip.border");
		LookAndFeel.installColors(this, "ToolTip.background", "ToolTip.foreground");
		theEditorPane = new JEditorPane();
		theEditorPane.setContentType("text/html");
		theEditorPane.setEditable(false);
		theEditorPane.addHyperlinkListener(e -> {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				openWebpage(e.getURL().toString());
				hideToolTip();
			}
		});
		add(theEditorPane);
		setBorder(BorderFactory.createLineBorder(new Color(7,132,181), 1, true));
	}
	private void hideToolTip() {
		JComponent comp = getComponent();
		if (comp != null) {
			JComponent parent = (JComponent) comp.getParent();
			if (parent != null) {
				ToolTipManager.sharedInstance().setEnabled(false);
				ToolTipManager.sharedInstance().setEnabled(true);
			}
		}
	}
	public void setTipText(String tipText) {
		theEditorPane.setText(tipText);
	}

	public void updateUI() {
		setUI(new ToolTipUI() {});
	}
	private void openWebpage(String url) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException | URISyntaxException ex) {
				ex.printStackTrace();
			}
		}
	}
}
