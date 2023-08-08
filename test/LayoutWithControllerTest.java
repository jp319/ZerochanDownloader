import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.views.components.Layout;

import javax.swing.*;

public class LayoutWithControllerTest {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			FlatDarculaLaf.setup();
			new Layout().setVisible(true);
		});
	}
}
