import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.controllers.Search;
import jp319.zerochan.models.PreviewImageItem;
import jp319.zerochan.models.PreviewImagesList;
import jp319.zerochan.utils.gui.OverlayPanel;
import jp319.zerochan.utils.sanitations.CleanSearchResult;

import javax.swing.*;
import java.awt.*;

public class OverlayPanelTest {
	public static void main(String[] args) {
		FlatDarculaLaf.setup();
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Image Gallery");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setMinimumSize(new Dimension(600, 600));

			OverlayPanel images_pnl = new OverlayPanel();
			images_pnl.getScrollPane().setPreferredSize(new Dimension(550,550));
			images_pnl.getDownloadButton().putClientProperty("JButton.buttonType", "roundRect");

			String result = CleanSearchResult.clean(new Search("Honkai+Star+Rail", "p=1&l=5").getResult());
			PreviewImagesList imagesList =  jp319.zerochan.utils.statics.Gson.gson.fromJson(result, PreviewImagesList.class);

			for (int i = 0; i < imagesList.getItems().size(); i++) {
				PreviewImageItem previewImageItem = new PreviewImageItem(imagesList.getItems().get(i));
				images_pnl.addImage(previewImageItem);
			}

			frame.add(images_pnl, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
		});
	}
}
