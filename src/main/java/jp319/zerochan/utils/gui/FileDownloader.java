package jp319.zerochan.utils.gui;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import java.io.*;
import java.net.URI;

public class FileDownloader {
	private final URI fileUri;
	private final String destination;
	private final DownloadDialog downloadDialog;
	private final DownloadItem downloadItem;
	
	public FileDownloader(URI fileUri, String destination, DownloadDialog downloadDialog, DownloadItem downloadItem) {
		this.fileUri = fileUri;
		this.destination = destination;
		this.downloadDialog = downloadDialog;
		this.downloadItem = downloadItem;
	}
	
	public Runnable downloadImage() {
		return this::download;
	}
	
	public void download() {
		int maxRetries = 3; // Maximum number of retries
		int retryCount = 0;
		
		while (retryCount <= maxRetries) {
			HttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(fileUri);
			
			try {
				HttpResponse response = httpClient.execute(httpGet);
				int statusCode = response.getStatusLine().getStatusCode();
				
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					if (entity != null) {
						try (InputStream in = entity.getContent();
							 OutputStream out = new FileOutputStream(destination)) {
							
							byte[] buffer = new byte[4096];
							int bytesRead;
							int totalBytesRead = 0;
							
							while ((bytesRead = in.read(buffer)) != -1) {
								out.write(buffer, 0, bytesRead);
								totalBytesRead += bytesRead;
								updateProgress(totalBytesRead, (int) entity.getContentLength());
							}
						}
					}
					break; // Successful download, exit the loop
				} else if (statusCode == 503) {
					retryCount++;
					// Wait for a short period before retrying
					Thread.sleep(1000); // You can adjust the sleep duration
				} else {
					break; // Other error, exit the loop
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void updateProgress(int bytesRead, int totalBytes) {
		SwingUtilities.invokeLater(() -> {
			int progress = (int) ((float) bytesRead / totalBytes * 100);
			downloadItem.setDownloadProgressPercentage(progress);
			downloadItem.setDownloadProgressBar(progress);
			if (progress == 100) {
				downloadDialog.updateDownloadRatio();
				downloadDialog.updateProgress();
			}
		});
	}
}
