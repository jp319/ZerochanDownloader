package jp319.zerochan.utils.sanitations;

public class CleanSearchResult {
	public static String clean(String result) {
		if (result != null) {
			if (!result.contains("\"items\":")) {
				return result; // Doesn't do anything if the response
			}                  // is a single item.
			int startIndex = result.indexOf("\"items\":");
			if (startIndex >= 0) {
				return "{" + result.substring(startIndex);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	public static String sanitizeJson(String jsonString) {
		// Well Formed JSON
		return com.google.json.JsonSanitizer.sanitize(jsonString);
	}
}
