package jp319.zerochan.utils.statics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Constants {
	// Request Header Constants
	public static final String API_BASE_URL = Config.properties.getProperty("api.base.url");
	public static final String STATIC_API_BASE_URL = Config.properties.getProperty("static.api.base.url");
	public static final String USER_AGENT_HEADER = Config.properties.getProperty("user.agent.header");
	public static final String ACCEPT_HEADER = Config.properties.getProperty("accept.header");
	public static final String ACCEPT_LANGUAGE_HEADER = Config.properties.getProperty("accept.language.header");
	public static final String COOKIE_HEADER = Config.properties.getProperty("cookie.header");
	// Search Constant
	public static String PREVIOUS_SEARCHED_STRING = "";
	// Page Constant
	private static int pageNumber = 1;
	public static String CURRENT_PAGE() {
		// Page Constant
		String pageString = "&p=";
		return pageString + pageNumber;
	}
	public static int getPageNumber() {
		return pageNumber;
	}
	public static synchronized void incrementPage() {
		pageNumber = pageNumber + 1;
	}
	public static synchronized void decrementPage() {
		if (pageNumber-1 != 0) { pageNumber = pageNumber - 1; }
	}
	public static synchronized void resetPage() {
		pageNumber = 1;
	}
	// Constant Filters
	public static Map<String, String> FILTERS = new HashMap<>();
	static {
		FILTERS.put("STRICT_MODE", "&strict");
		FILTERS.put("ALL",         "&d=all");
		FILTERS.put("LARGE",       "&d=large");
		FILTERS.put("HUGE",        "&d=huge");
		FILTERS.put("LANDSCAPE",   "&d=landscape");
		FILTERS.put("PORTRAIT",    "&d=portrait");
		FILTERS.put("SQUARE",      "&d=square");
		FILTERS.put("RECENT",      "&s=id");
		FILTERS.put("POPULAR",     "&s=fav");
		FILTERS.put("ALL_TIME",    "&t=0");
		FILTERS.put("TODAY",       "&t=1");
		FILTERS.put("THIS_WEEK",   "&t=2");
	}
	// Constant GUIs
	public static ImageIcon ScaledImageIcon(BufferedImage image, double width, double height) {
		int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();
		
		double scaleX = width / imgWidth;
		double scaleY = height / imgHeight;
		double scale = Math.min(scaleX, scaleY);
		
		int scaledWidth = (int) (imgWidth * scale);
		int scaledHeight = (int) (imgHeight * scale);
		
		java.awt.Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(scaledImage);
	}
	public static ImageIcon ScaledImageIcon(Image image, double width, double height) {
		int imgWidth = image.getWidth(null);
		int imgHeight = image.getHeight(null);
		
		double scaleX = width / imgWidth;
		double scaleY = height / imgHeight;
		double scale = Math.min(scaleX, scaleY);
		
		int scaledWidth = (int) (imgWidth * scale);
		int scaledHeight = (int) (imgHeight * scale);
		
		Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH);
		return new ImageIcon(scaledImage);
	}
	// Download Constants
	public static String DOWNLOAD_DIRECTORY = "C:/Users/"+System.getProperty("user.name") +"/Downloads/";
}
