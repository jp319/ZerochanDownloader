import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.utils.gui.ScaledIcon;

import javax.swing.*;
import java.awt.*;

public class GifImageTest {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			FlatDarculaLaf.setup();
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			frame.setMinimumSize(new Dimension(600,600));
			
			ImageIcon loadingGif = new ImageIcon("D:/My Files/Projects/Java/ZerochanDownloader/src/main/resources/images/loading.gif");
			loadingGif.setImage(loadingGif.getImage().getScaledInstance(50,50, Image.SCALE_DEFAULT));
			System.out.println(loadingGif.getDescription());
			//JLabel label = new JLabel(ScaledIcon.createScaledIcon(loadingGif,150,150));
			//JLabel label = new JLabel("Loading...", loadingGif, SwingConstants.CENTER);
			
			String labelText = "Loading...";
			String htmlText = "<html><div style='text-align:center;'> <img src=\"file:" + loadingGif.getDescription() + "\"><br>" + labelText + "</div></html>";
			JLabel label = new JLabel(htmlText, SwingConstants.CENTER);
			
			frame.add(label, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
		});
	}
}
