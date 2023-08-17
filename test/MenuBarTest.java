import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.utils.gui.MenuBar;

import javax.swing.*;

public class MenuBarTest {
	public static void main(String[] args) {
		FlatDarculaLaf.setup();
		JFrame frame = new JFrame("Menu Bar Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);

		MenuBar menuBar = new MenuBar();
		
		frame.setJMenuBar(menuBar);

		frame.setVisible(true);
	}
}
