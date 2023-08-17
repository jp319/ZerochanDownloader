import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.controllers.Search;
import jp319.zerochan.models.FullImageData;
import jp319.zerochan.utils.gui.DetailsDialog;
import jp319.zerochan.utils.sanitations.CleanSearchResult;
import jp319.zerochan.utils.statics.Gson;
import jp319.zerochan.utils.gui.FullImageView;

import javax.swing.*;
import java.awt.*;

public class FullImageViewTest {
	public static void main(String[] args) {
		FlatDarculaLaf.setup();
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Preview");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLayout(new BorderLayout());
			frame.setMinimumSize(new Dimension(500,500));
			
			Search search = new Search("3997132");
			FullImageData fullImageData = Gson.gson.fromJson(CleanSearchResult.clean(search.getResult()), FullImageData.class);
			System.out.println(fullImageData.getId());
			
			FullImageView fullImageView = new FullImageView(fullImageData);
			
			JButton button = new JButton("Load different Image");
			
			frame.add(button, BorderLayout.SOUTH);
			frame.add(fullImageView, BorderLayout.CENTER);
			frame.pack();
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame.setVisible(true);
			
			button.addActionListener(e -> loadImage(search, fullImageData, fullImageView));
			fullImageView.getImagePropertiesButton().addActionListener(e -> {
				DetailsDialog detailsDialog = new DetailsDialog(
						frame, "Details", fullImageView.getFullImageData()
				);
			});
		});
	}
	private static void loadImage(Search search, FullImageData fullImageData, FullImageView fullImageView) {
		fullImageView.showLoading();
		search = new Search("3997847");
		fullImageData = Gson.gson.fromJson(CleanSearchResult.clean(search.getResult()), FullImageData.class);
		System.out.println(fullImageData.getId());
		
		fullImageView.setImageLabel(fullImageData);
	}
}
