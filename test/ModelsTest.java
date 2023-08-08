import jp319.zerochan.models.FullImageData;
import jp319.zerochan.models.PreviewImagesList;
import jp319.zerochan.utils.statics.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Objects;

public class ModelsTest {
	public static void main(String[] args) {
		FullImageData image = Gson
				.gson
				.fromJson(Objects.requireNonNull(
						readJson("D:\\My Files\\Projects\\Java\\ZerochanDownloader\\test\\resources\\Image.json")),
						FullImageData.class);
		System.out.println("Single Image Test");
		System.out.println("Image ID: " + image.getId());
		
		PreviewImagesList imagesList = Gson
				.gson
				.fromJson(Objects.requireNonNull(
						readJson("D:\\My Files\\Projects\\Java\\ZerochanDownloader\\test\\resources\\Images.json")),
						PreviewImagesList.class);
		System.out.println("Multiple Image Test");
		System.out.println("Image IDs: ");
		for (int i = 0; i < imagesList.getItems().size(); i++) {
			System.out.println("      (" + i + ") " + imagesList.getItems().get(i).getId());
		}
	}
	private static BufferedReader readJson(String filePath) {
		try {
			return new BufferedReader(new FileReader(Paths.get(filePath).toFile()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
