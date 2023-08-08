import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.utils.gui.HyperLinkToolTip;

import javax.swing.*;

public class HyperLinkToolTipTest {
	public static void main(String[] args) {
		FlatDarculaLaf.setup();
		SwingUtilities.invokeLater(() -> {
			final JFrame frame = new JFrame(HyperLinkToolTip.class.getName());
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JPanel panel = new JPanel() {
				public JToolTip createToolTip() {
					JToolTip tip = new HyperLinkToolTip();
					tip.setComponent(this);
					return tip;
				}
			};
			panel.setToolTipText("<html><body><b>Source:</b> <a href=\"https://s3.zerochan.net/240/05/21/3906055.jpg\">Source</a></body></html>");
			frame.setContentPane(panel);
			SwingUtilities.invokeLater(() -> {
				frame.setSize(400, 400);
				frame.setVisible(true);
			});
		});
	}
}
