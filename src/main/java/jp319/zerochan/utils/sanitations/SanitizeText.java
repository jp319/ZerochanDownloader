package jp319.zerochan.utils.sanitations;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SanitizeText {
	public static String sanitizeTextForLink(String input) {
		// Properly URL encode the sanitized text
		return URLEncoder.encode(input, StandardCharsets.UTF_8);
	}
}
