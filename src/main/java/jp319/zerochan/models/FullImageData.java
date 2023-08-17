package jp319.zerochan.models;

import java.util.List;

public class FullImageData {
	private final String id;
	private final String small;
	private final String medium;
	private final String full;
	private final String width;
	private final String height;
	private final String size;
	private final String hash;
	private final String primary;
	private final String source;
	private final List<String> tags;
	
	public FullImageData(String id, String small, String medium, String full, String width, String height, String size, String hash, String primary, String source, List<String> tags) {
		this.id = id;
		this.small = small;
		this.medium = medium;
		this.full = full;
		this.width = width;
		this.height = height;
		this.size = size;
		this.hash = hash;
		this.primary = primary;
		this.source = source;
		this.tags = tags;
	}
	
	public String getId() {
		return id;
	}
	
	public String getSmall() {
		return small;
	}
	
	public String getMedium() {
		return medium;
	}
	
	public String getFull() {
		return full;
	}
	
	public String getWidth() {
		return width;
	}
	
	public String getHeight() {
		return height;
	}
	
	public String getSize() {
		return size;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getPrimary() {
		return primary;
	}
	
	public String getSource() {
		return source;
	}
	
	public List<String> getTags() {
		return tags;
	}
	public String getFormattedTags() {
		int size = this.getTags().size();
		List<String> tags = this.getTags();
		StringBuilder formattedTags = new StringBuilder();
		
		formattedTags.append("<html>");
		for (int i = 0; i < size; i++) {
			if (i > 0 && i % 3 == 0) {
				formattedTags.append("<br>");
			} else if (i > 0) {
				formattedTags.append(", ");
			}
			formattedTags.append(tags.get(i));
		}
		formattedTags.append("</html>");
		
		return formattedTags.toString();
	}
}
