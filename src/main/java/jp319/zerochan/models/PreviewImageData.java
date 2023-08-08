package jp319.zerochan.models;

import java.util.List;

public class PreviewImageData {
	private final String id;
	private final int width;
	private final int height;
	private final String thumbnail;
	private final String source;
	private final String tag;
	private final List<String> tags;
	
	public PreviewImageData(String id, int width, int height, String thumbnail, String source, String tag, List<String> tags) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.thumbnail = thumbnail;
		this.source = source;
		this.tag = tag;
		this.tags = tags;
	}
	
	public String getId() {
		return id;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public String getThumbnail() {
		return thumbnail;
	}
	
	public String getSource() {
		return source;
	}
	
	public String getTag() {
		return tag;
	}
	
	public List<String> getTags() {
		return tags;
	}
}
