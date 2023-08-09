package jp319.zerochan.utils.statics;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class CheckURL {
	public static boolean isURL(String input) {
		try {
			new URI(input);
			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}
	
	private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
			".png", ".jpg", ".jpeg", ".gif", ".webp",
			".bmp", ".tiff", ".ico", ".svg", ".eps",
			".pdf", ".psd", ".ai"
	);
	
	public static String makeURLValid(String urlString) {
		for (String extension : IMAGE_EXTENSIONS) {
			if (isURLValid(urlString + extension)) {
				return urlString + extension;
			}
		}
		return "";
	}
	
	public static boolean isURLValid(String urlString) {
		int maxRetryCount = 3; // Maximum number of retries
		int retryCount = 0;
		
		HttpClient httpClient = HttpClients.createDefault();
		
		while (retryCount < maxRetryCount) {
			try {
				URI uri = new URI(urlString);
				HttpHead httpHead = new HttpHead(uri);
				HttpResponse response = httpClient.execute(httpHead);
				int responseCode = response.getStatusLine().getStatusCode();
				System.out.println("Checking: " + urlString + " - " + responseCode);
				
				// Handle 503 Service Unavailable
				if (responseCode == HttpStatus.SC_SERVICE_UNAVAILABLE) {
					System.out.println("503 error encountered. Retrying after a delay...");
					retryCount++;
					// Sleep for a few seconds before retrying
					try {
						Thread.sleep(3000); // Sleep for 3 seconds (adjust as needed)
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					continue;
				}
				
				return responseCode != HttpStatus.SC_NOT_FOUND; // 404
			} catch (URISyntaxException | IOException e) {
				return false; // Error occurred or invalid URL
			}
		}
		
		return false; // Max retries reached
	}
	
	public static String removeBaseUrl(String fullUrl, String baseUrl) {
		if (fullUrl.startsWith(baseUrl)) {
			return fullUrl.substring(baseUrl.length());
		} else {
			return fullUrl; // If the URL doesn't start with the base URL, return as is
		}
	}
}
