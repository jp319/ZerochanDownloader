import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.utils.gui.SettingsDialog;
import jp319.zerochan.utils.gui.WrapLayout;

import javax.swing.*;

public class SettingsDialogTest {

	public static void main(String[] args) {
		FlatDarculaLaf.setup();
		SwingUtilities.invokeLater(SettingsDialogTest::createAndShowDialog);
	}

	private static void createAndShowDialog() {
		JFrame mainFrame = new JFrame("Main Frame");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(400, 300);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setLayout(new WrapLayout());

		JButton showDialogButton = new JButton("Show Dialog");
		JButton getDownloadDirectory = new JButton("Show Directory");
		
		SettingsDialog settingsDialog = new SettingsDialog(mainFrame);
		
		showDialogButton.addActionListener(e -> settingsDialog.setVisible(true));
		getDownloadDirectory.addActionListener(e -> System.out.println(settingsDialog.getDownloadDirectory()));
		
		mainFrame.add(showDialogButton);
		mainFrame.add(getDownloadDirectory);
		settingsDialog.setVisible(true);
		mainFrame.setVisible(true);
	}

}
