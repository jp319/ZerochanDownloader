package jp319.zerochan.utils.statics;

import java.net.URI;
import java.net.URISyntaxException;

public class CheckURL {
	public static boolean isURL(String input) {
		try {
			new URI(input);
			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}
}
