package jp319.zerochan.utils.statics;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
	public static Properties properties;
	private static final String PROPERTIES_FILE_PATH = "/data's/config.properties";
	private static final String EXTERNAL_CONFIG_PATH =
			System.getenv("APPDATA") + File.separator + "ZerochanDownloader" +
					File.separator + "config.properties";
	private static final File EXTERNAL_CONFIG_FILE = new File(EXTERNAL_CONFIG_PATH);
	static { properties = loadPropertiesFromFile(); }
	private static Properties loadPropertiesFromFile() {
		if (!EXTERNAL_CONFIG_FILE.exists()) {
			copyToAppData();
			System.out.println(EXTERNAL_CONFIG_PATH.replace("\\", "/"));
		}
		return loadPropertiesFromStream();
	}
	
	private static Properties loadPropertiesFromStream() {
		try (InputStream inputStream = new FileInputStream(EXTERNAL_CONFIG_FILE)) {
			Properties properties = new Properties();
			properties.load(inputStream);
			return properties;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void copyToAppData() {
		InputStream inputStream = Config.class.getResourceAsStream(PROPERTIES_FILE_PATH);

		String appDataDir = System.getenv("APPDATA");
		String destinationDirectoryPath = appDataDir + File.separator + "ZerochanDownloader";
		File destinationDirectory = new File(destinationDirectoryPath);
		if (!destinationDirectory.exists()) {
			destinationDirectory.mkdirs();
		}

		String destinationFilePath = destinationDirectoryPath + File.separator + "config.properties";
		File destinationFile = new File(destinationFilePath);

		try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("Resource copied to " + destinationFilePath);
	}
	
	public static void updateProperty(String key, String value) {
		try {
			properties.setProperty(key, value);
			Path configFilePath = Paths.get(Config.class.getResource(EXTERNAL_CONFIG_PATH).toURI());
			
			try (OutputStream outputStream = new FileOutputStream(configFilePath.toFile())) {
				properties.store(outputStream, null);
				System.out.println("Properties updated successfully.");
			}
		} catch (IOException e) {
			System.err.println("Error on updating config.properties.");
			e.printStackTrace();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	public static void updatePropertyV2(String key, String value) {
		try {
			//first load old one:
			FileInputStream configStream = new FileInputStream(EXTERNAL_CONFIG_FILE);
			properties.load(configStream);
			configStream.close();
			
			//modifies existing or adds new property
			properties.setProperty(key, value);
			
			//save modified property file
			FileOutputStream output = new FileOutputStream(EXTERNAL_CONFIG_FILE);
			properties.store(output, "Property updated");
			output.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		System.out.println(new File(Config.class.getResource(PROPERTIES_FILE_PATH).toURI()).getAbsolutePath());
	}
}
