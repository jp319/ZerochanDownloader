package jp319.zerochan.utils.sanitations;

import java.text.DecimalFormat;

public class ConvertByte {
	public static String formatBytesToKB(String bytesString) {
		try {
			long bytes = Long.parseLong(bytesString);
			double kilobytes = (double) bytes / 1024;
			DecimalFormat df = new DecimalFormat("#,##0.0");
			return df.format(kilobytes) + " kB";
		} catch (NumberFormatException e) {
			return "Invalid input";
		}
	}
}
