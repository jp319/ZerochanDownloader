package jp319.zerochan.utils.statics;

import java.io.IOException;
import java.io.InputStream;

public class Config {
	public static final java.util.Properties properties;
	static { properties = loadPropertiesFromFile(); }
	private static java.util.Properties loadPropertiesFromFile() {
		try (InputStream inputStream = Config.class.getResourceAsStream("/data's/config.properties")) {
			if (inputStream != null) {
				java.util.Properties properties = new java.util.Properties();
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
}
