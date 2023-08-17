package jp319.zerochan.controllers;

import jp319.zerochan.utils.statics.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class Search {
	private final CloseableHttpClient httpClient = HttpClients.createMinimal();
	private final HttpClientContext context = HttpClientContext.create();
	private HttpGet httpGet;
	private CloseableHttpResponse response;
	private HttpEntity entity;
	private int statusCode;
	private String link;
	private String result;
	private final String id;
	private String tags;
	private final String filters;
	public Search() {
		this(null,null,null);
	}
	public Search(String id) {
		this(id, null, null);
	}
	public Search(String tags, String filters) {
		this(null, tags, filters);
	}
	private Search(String id, String tags, String filters) {
		System.out.println("Link: " + Constants.API_BASE_URL);
		this.id = id != null ? "/" + id : null;
		this.tags = tags != null ? "/" + tags : null;
		this.filters = filters;
	}
	private void setLink() {
		if (id != null) {
			this.link = Constants.API_BASE_URL + id + "?json";
			return;
		}
		if (tags != null) {
			if (filters != null) {
				this.link = Constants.API_BASE_URL + tags + "?json" + filters;
			} else {
				this.link = Constants.API_BASE_URL + tags + "?json";
			}
			return;
		}
		
		link = null;
	}
	private void setHttpGet() {
		setLink();
		if (link == null) {
			httpGet = null;
			return;
		}
		if (httpGet != null) { // Sets the link instead of creating a new httpGet.
			httpGet.setURI(URI.create(link));
			return;
		}
		httpGet = new HttpGet(link);
		httpGet.addHeader("User-Agent",      Constants.USER_AGENT_HEADER);
		httpGet.addHeader("Accept",          Constants.ACCEPT_HEADER);
		httpGet.addHeader("Accept-Language", Constants.ACCEPT_LANGUAGE_HEADER);
		httpGet.addHeader("Cookie",          Constants.COOKIE_HEADER);
	}
	private void setHttpResponse() {
		setHttpGet();
		if (httpGet == null) {
			response = null;
			return;
		}
		try {
			response = httpClient.execute(httpGet, context);
		} catch (IOException e) {
			e.printStackTrace();
			response = null;
		}
	}
	private void validateResponse() {
		setHttpResponse();
		if (response == null) {
			statusCode = 1000; // Not a status code but this will
			return;            // mean that the response is null.
		}
		statusCode = response.getStatusLine().getStatusCode();
		
		System.out.println("Current Link: " + link);
		System.out.println("Response Code: " + statusCode);
		
		if (statusCode >= 300 && statusCode < 310) {
			tags = response.getFirstHeader("Location").getValue();
			setHttpResponse(); // Get new Response using the new tag.
			
			System.out.println("New Link: " + link);
			if (response == null) {
				statusCode = 1000;
				return;
			}
			statusCode = response.getStatusLine().getStatusCode();
			
			System.out.println("New Response Code: " + statusCode);
		}
	}
	private void setEntity() {
		validateResponse();
		if (statusCode == 1000) {
			entity = null;
			return;
		}
		entity = response.getEntity();
	}
	public String getResult() {
		setEntity();
		if (entity == null) {
			return null; // Equivalent to result = null;
		}
		try {
			result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			//result = result.replace("\\", "");
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error on parsing entity to string.");
			System.out.println("Error Message: " + e.getMessage());
			return null;
		}
	}
}
