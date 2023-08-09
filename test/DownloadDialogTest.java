import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.utils.gui.DownloadDialog;
import jp319.zerochan.utils.gui.DownloadItem;

import javax.swing.*;

public class DownloadDialogTest {

	public static void main(String[] args) {
		FlatDarculaLaf.setup();
		SwingUtilities.invokeLater(DownloadDialogTest::createAndShowDialog);
	}

	private static void createAndShowDialog() {
		JFrame mainFrame = new JFrame("Main Frame");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(400, 300);
		mainFrame.setLocationRelativeTo(null);

		JButton showDialogButton = new JButton("Show Dialog");
		
		DownloadDialog downloadDialog = new DownloadDialog(mainFrame);
		showDialogButton.addActionListener(e -> downloadDialog.setVisible(true));
		
		
		for (int i = 0; i <= 10; i++) {
			DownloadItem downloadItem = new DownloadItem("Fate/Stay Night: Unlimited Blade Works"+i);
			downloadDialog.addDownloadItem(downloadItem);
		}
		
		mainFrame.add(showDialogButton);
		downloadDialog.setVisible(true);
		mainFrame.setVisible(true);
	}

}
