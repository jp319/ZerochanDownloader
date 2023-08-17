package jp319.zerochan.utils.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class MenuBar extends JMenuBar {
	private final JMenu fileMenu = new JMenu("File");
	private final JMenuItem openDownloadDirectoryItem = new JMenuItem("Open Download Directory");
	private final JMenuItem settingsItem = new JMenuItem("Settings");
	private final JMenuItem showDownloadItem = new JMenuItem("Show Download");
	private final JMenuItem exitItem = new JMenuItem("Exit");
	public MenuBar() {
		InputStream openImage = MenuBar.class.getResourceAsStream("/images/open.png");
		InputStream settingsImage = MenuBar.class.getResourceAsStream("/images/settings.png");
		InputStream downloadImage = MenuBar.class.getResourceAsStream("/images/download.png");
		InputStream closeImage = MenuBar.class.getResourceAsStream("/images/close.png");
		
		ImageIcon folderIcon = null;
		ImageIcon settingsIcon = null;
		ImageIcon downloadIcon = null;
		ImageIcon exitIcon = null;
		try {
			if (openImage != null && settingsImage != null && downloadImage != null && closeImage != null) {
				folderIcon = new ImageIcon(ImageIO.read(openImage));
				settingsIcon = new ImageIcon(ImageIO.read(settingsImage));
				downloadIcon = new ImageIcon(ImageIO.read(downloadImage));
				exitIcon = new ImageIcon(ImageIO.read(closeImage));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		openDownloadDirectoryItem.setIcon(folderIcon);
		settingsItem.setIcon(settingsIcon);
		showDownloadItem.setIcon(downloadIcon);
		exitItem.setIcon(exitIcon);
		
		fileMenu.add(openDownloadDirectoryItem);
		fileMenu.add(settingsItem);
		fileMenu.add(showDownloadItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		add(fileMenu);
	}
	public JMenu getFileMenu() {
		return fileMenu;
	}
	public JMenuItem getOpenDownloadDirectoryItem() {
		return openDownloadDirectoryItem;
	}
	public JMenuItem getSettingsItem() {
		return settingsItem;
	}
	public JMenuItem getShowDownloadItem() {
		return showDownloadItem;
	}
	public JMenuItem getExitItem() {
		return exitItem;
	}
}
