package jp319.zerochan.utils.statics;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
	public static Properties properties;
	private static final String PROPERTIES_FILE_PATH = "/data's/config.properties";
	static { properties = loadPropertiesFromFile(); }
	private static Properties loadPropertiesFromFile() {
		try (InputStream inputStream = Config.class.getResourceAsStream(PROPERTIES_FILE_PATH)) {
			if (inputStream != null) {
				Properties properties = new Properties();
				properties.load(inputStream);
				return properties;
			} else {
				System.err.println("config.properties file not found.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void updateProperty(String key, String value) {
		try {
			properties.setProperty(key, value);
			Path configFilePath = Paths.get(Config.class.getResource(PROPERTIES_FILE_PATH).toURI());
			
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
			FileInputStream configStream = new FileInputStream(new File(Config.class.getResource(PROPERTIES_FILE_PATH).toURI()).getAbsolutePath());
			properties.load(configStream);
			configStream.close();
			
			//modifies existing or adds new property
			properties.setProperty(key, value);
			
			//save modified property file
			FileOutputStream output = new FileOutputStream(new File(Config.class.getResource(PROPERTIES_FILE_PATH).toURI()).getAbsolutePath());
			properties.store(output, "Property updated");
			output.close();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
		System.out.println(new File(Config.class.getResource(PROPERTIES_FILE_PATH).toURI()).getAbsolutePath());
	}
}
