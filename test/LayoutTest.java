import com.formdev.flatlaf.FlatDarculaLaf;
import jp319.zerochan.controllers.Search;
import jp319.zerochan.models.PreviewImageItem;
import jp319.zerochan.models.PreviewImagesList;
import jp319.zerochan.utils.sanitations.CleanSearchResult;
import jp319.zerochan.views.components.Layout;

import javax.swing.*;

public class LayoutTest {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			FlatDarculaLaf.setup();
			Layout layout = new Layout();

			String result = CleanSearchResult.clean(new Search("Honkai+Star+Rail", "p=1&l=5").getResult());
			PreviewImagesList imagesList =  jp319.zerochan.utils.statics.Gson.gson.fromJson(result, PreviewImagesList.class);

			for (int i = 0; i < imagesList.getItems().size(); i++) {
				PreviewImageItem previewImageItem = new PreviewImageItem(imagesList.getItems().get(i));
				layout.getBody().addImage(previewImageItem);
			}

			layout.setVisible(true);
		});
	}
}
