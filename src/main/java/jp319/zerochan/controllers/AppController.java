package jp319.zerochan.controllers;

import jp319.zerochan.models.FullImageData;
import jp319.zerochan.models.PreviewImageItem;
import jp319.zerochan.models.PreviewImagesList;
import jp319.zerochan.utils.gui.*;
import jp319.zerochan.utils.sanitations.CleanSearchResult;
import jp319.zerochan.utils.sanitations.SanitizeText;
import jp319.zerochan.utils.statics.CheckURL;
import jp319.zerochan.utils.statics.Constants;
import jp319.zerochan.utils.statics.Gson;
import jp319.zerochan.views.components.Body;
import jp319.zerochan.views.components.Footer;
import jp319.zerochan.views.components.Header;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppController {
	JFrame mainFrame;
	Header header;
	Body body;
	Footer footer;
	FullImageData singleImageItem;
	PreviewImagesList multipleImageItem;
	DownloadDialog downloadDialog;
	public AppController(JFrame mainFrame, Header header, Body body, Footer footer, DownloadDialog downloadDialog) {
		this.mainFrame = mainFrame;
		this.header = header;
		this.body = body;
		this.footer = footer;
		this.downloadDialog = downloadDialog;
		
		setListener();
	}
	private void setListener() {
		// Search Listener
		header.getSearch_tf().addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				super.keyTyped(e);
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				super.keyPressed(e);
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String searchInput = header.getSearch_tf().getText().trim();
					if (!searchInput.isEmpty()) {
						resetPage(); // Page is set to page 1
						Constants.PREVIOUS_SEARCHED_STRING = searchInput;
						doSearch(searchInput);
					} else {
						showError();
					}
				}
			}
		});
		// Search Button Listener
		header.getSearch_btn().addActionListener(e -> {
			String searchInput = header.getSearch_tf().getText().trim();
			if (!searchInput.isEmpty()) {
				resetPage();
				Constants.PREVIOUS_SEARCHED_STRING = searchInput;
				doSearch(searchInput);
			} else {
				showError();
			}
		});
		// Apply Button Listener
		header.getApplyFilter_btn().addActionListener(e -> {
			String searchInput = header.getSearch_tf().getText().trim();
			if (!searchInput.isEmpty()) {
				resetPage();
				Constants.PREVIOUS_SEARCHED_STRING = searchInput;
				doSearch(searchInput);
			} else if (!Constants.PREVIOUS_SEARCHED_STRING.isEmpty()) {
				resetPage();
				doSearch(Constants.PREVIOUS_SEARCHED_STRING);
			} else {
				showError();
			}
		});
		// Next Button Listener
		header.getNextPage_btn().addActionListener(e -> {
			String searchInput = header.getSearch_tf().getText().trim();
			if (!searchInput.isEmpty()) {
				nextPage();
				Constants.PREVIOUS_SEARCHED_STRING = searchInput;
				doSearch(searchInput);
			} else if (!Constants.PREVIOUS_SEARCHED_STRING.isEmpty()) {
				nextPage();
				doSearch(Constants.PREVIOUS_SEARCHED_STRING);
			} else {
				showError();
			}
		});
		// Prev Button Listener
		header.getPrevPage_btn().addActionListener(e -> {
			String searchInput = header.getSearch_tf().getText().trim();
			if (!searchInput.isEmpty()) {
				prevPage();
				Constants.PREVIOUS_SEARCHED_STRING = searchInput;
				doSearch(searchInput);
			} else if (!Constants.PREVIOUS_SEARCHED_STRING.isEmpty()) {
				prevPage();
				doSearch(Constants.PREVIOUS_SEARCHED_STRING);
			} else {
				showError();
			}
		});
		// Download Button Listener
		body.getDownloadButton().addActionListener(e ->
				SwingUtilities.invokeLater(this::downloadMultipleImages)
		);
		// Image Listener
		for (Component component : body.getImagesPanel().getComponents()) {
			if (component instanceof PreviewImageItem previewImageItem) {
				previewImageItem.getImageLabel().addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						// TODO: Show Image Preview
					}
				});
			}
		}
	}
	// Main Methods
	private void doSearch(String searchInput) {
		String stringToSearch = sanitizeText(searchInput);
		String filters = getFilters();
		
		body.showLoading(); // Show loading panel when searching
		String searchResult = CleanSearchResult.clean(new Search(stringToSearch, filters).getResult());
		
		if (searchResult != null && !searchResult.isEmpty()) {
			boolean isSingleItem = isSingleItem(searchResult);
			if (isSingleItem) {
				singleImageItem = Gson.gson.fromJson(searchResult, FullImageData.class);
			} else {
				multipleImageItem = Gson.gson.fromJson(searchResult, PreviewImagesList.class);
				loadMultipleImages(multipleImageItem);
			}
			body.getImagesPanel().revalidate();
			body.hideLoading();
		}
		
		if (searchResult != null) {
			if (searchResult.replaceAll("\\s+", "").equals("{}")) {
				showError();
			}
		}
		
		setPrevPageButton();
	}
	private void showError() {
		FrameAction.shakeFrame(mainFrame);
		System.out.println("No results");
		body.emptyImagePanel();
		body.showError();
		footer.setForeground(new Color(255, 0, 11));
	}
	private String getFilters() {
		return getStrictModeState() +
				//"&p=1" + // Temporary default page 1
				getCurrentPage() +
				getEntryLimit() +
				getSortFilter() + // Already included the popularity scale
				getDimensionFilter() +
				getColorFilter();
	}
	private void loadMultipleImages(PreviewImagesList multipleImageItem) {
		body.getImagesPanel().removeAll(); // Remove all previous images
		
		//int processors = OptimalProcessorUtil.getOptimalProcessorCount(); // Will use maximum amount of processor
		int processors = 2;
		ExecutorService service = Executors.newFixedThreadPool(processors);
		
		int size = multipleImageItem.getItems().size();
		
		System.out.println("Number of processors used: "+processors);
		footer.setTotalItems(size);
		
		for (int i = 0; i < size; i++) {
			PreviewImageItem previewImageItem = new PreviewImageItem(multipleImageItem.getItems().get(i));
			addCheckBoxListener(previewImageItem.getCheckBox());
			OnlineImageLoader imageLoader = new OnlineImageLoader(footer, body, previewImageItem);
			
			service.submit(imageLoader.loadImage());
		}
		
		service.shutdown();
		SwingUtilities.invokeLater(() -> {
			body.revalidate();
			body.repaint();
			
			footer.revalidate();
			footer.repaint();
		});
		
	}
	private void downloadMultipleImages() {
		downloadDialog.removeAllItems();
		downloadDialog.setVisible(true);
		List<DownloadItem> downloadItems = new ArrayList<>();
		List<String> validURLs = new ArrayList<>();
		
		for (String id : body.getSelectedImageIds()) {
			String draftURL = Constants.STATIC_API_BASE_URL + "/" + id;
			String validURL = CheckURL.makeURLValid(draftURL);
			
			DownloadItem item = new DownloadItem(id);
			downloadDialog.addDownloadItem(item);
			
			downloadItems.add(item);
			validURLs.add(validURL);
		}
		
		downloadDialog.setTotalDownloadedItems(downloadItems.size());
		doDownload(downloadItems, validURLs);
	}
	private void doDownload(List<DownloadItem> downloadItems, List<String> validURLs) {
		String baseDestination = Constants.DOWNLOAD_DIRECTORY;
		int downloadCount = 0;
		
		ExecutorService service = Executors.newSingleThreadExecutor();
		
		for (DownloadItem downloadItem : downloadItems) {
			URI uri = URI.create(validURLs.get(downloadCount));
			String destination = baseDestination + removeBaseURL(validURLs.get(downloadCount));
			service.submit(new FileDownloader(
					uri, destination, downloadDialog, downloadItem
			).downloadImage());
			
			downloadCount++;
		}
		
		service.shutdown();
	}
	// Helper Methods
	private String sanitizeText(String input) {
		return SanitizeText.sanitizeTextForLink(input);
	}
	private String removeBaseURL(String fullUrl) {
		return CheckURL.removeBaseUrl(fullUrl, "https://static.zerochan.net/");
	}
	private boolean isSingleItem(String result) {
		return !result.contains("\"items\":"); // returns true if it doesn't contain the string "items"
	}
	// Filter Helper Methods
	private String getColorFilter() {
		String color = header.getColorFilter_tf().getText().trim();
		if (!color.isEmpty()) {
			return "&c=" + color;
		} else {
			return "";
		}
	}
	private String  getEntryLimit() {
		Object limit = header.getEntryLimit_spn().getValue();
		if (limit instanceof Integer) {
			return "&l=" + limit;
		} else {
			return "";
		}
	}
	private String getStrictModeState() {
		boolean isStrict = header.getStrictMode_rBtn().isSelected();
		if (isStrict) {
			return Constants.FILTERS.get("STRICT_MODE");
		} else {
			return "";
		}
	}
	private String getCurrentPage() {
		return Constants.CURRENT_PAGE();
	}
	private void nextPage() {
		Constants.incrementPage();
	}
	private void prevPage() {
		Constants.decrementPage();
	}
	private void resetPage() {
		Constants.resetPage();
	}
	private void setPrevPageButton() {
		// If current page number is 1 then prev page button is disabled else enabled
		header.getPrevPage_btn().setEnabled(Constants.getPageNumber() != 1);
	}
	private String getDimensionFilter() {
		for (JRadioButton radioButton : header.getFilter_rBtnA()) {
			if (radioButton.isSelected()) {
				String selectedDimension = radioButton.getText();
				return selectedDimension(selectedDimension);
			}
		}
		return "";
	}
	private String getSortFilter() {
		for (JRadioButton radioButton : header.getSort_rBtnA()) {
			if (radioButton.isSelected()) {
				String selectedSort = radioButton.getText();
				return selectedSortFilter(selectedSort);
			}
		}
		return "";
	}
	// Filter Helper Methods
	private String selectedDimension(String selectedDimension) {
		return switch (selectedDimension) {
			case "All"       -> Constants.FILTERS.get("ALL");
			case "Large"     -> Constants.FILTERS.get("LARGE");
			case "Huge"      -> Constants.FILTERS.get("HUGE");
			case "Landscape" -> Constants.FILTERS.get("LANDSCAPE");
			case "Portrait"  -> Constants.FILTERS.get("PORTRAIT");
			case "Square"    -> Constants.FILTERS.get("SQUARE");
			default          -> "";
		};
	}
	private String selectedSortFilter(String selectedSort) {
		return switch (selectedSort) {
			case "Recent"  -> Constants.FILTERS.get("RECENT");
			case "Popular" -> Constants.FILTERS.get("POPULAR") + selectedPopularity();
			default ->  "";
		};
	}
	private String selectedPopularity() {
		String selectedOption = (String) header.getPopularityButton_cmb().getSelectedItem();
		if (selectedOption != null) {
			return switch (selectedOption) {
				case "All Time" -> Constants.FILTERS.get("ALL_TIME");
				case "Last 7000 Entries" -> Constants.FILTERS.get("TODAY");
				case "Last 15000 Entries" -> Constants.FILTERS.get("THIS_WEEK");
				default -> "";
			};
		} else {
			return "";
		}
	}
	private void resetOptionalFilters() {
	
	}
	// Additional Listeners
	private void addCheckBoxListener(JCheckBox checkBox) {
		checkBox.addActionListener(e -> body.setSelectAllCheckBox());
	}
}
