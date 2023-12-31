package jp319.zerochan.utils.sanitations;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SanitizeText {
	public static String sanitizeTextForLink(String input) {
		// Properly URL encode the sanitized text
		return URLEncoder.encode(input, StandardCharsets.UTF_8);
	}
	public static String removeIllegalCharacters(String input) {
		// Define a regex pattern to match illegal characters
		String illegalCharactersRegex = "[/\\\\:*?\"<>| ]";
		
		// Replace illegal characters with an empty string
		
		return input.replaceAll(illegalCharactersRegex, ".");
	}
	
}
