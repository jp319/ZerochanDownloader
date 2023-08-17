package jp319.zerochan.utils.sanitations;

import java.text.DecimalFormat;

public class ConvertByte {
	public static String formatBytesToMB(String bytesString) {
		try {
			long bytes = Long.parseLong(bytesString);
			double megabytes = (double) bytes / (1024 * 1024); // Convert to MB
			DecimalFormat df = new DecimalFormat("#,##0.0");
			return df.format(megabytes) + " MB";
		} catch (NumberFormatException e) {
			return "Invalid input";
		}
	}
}
