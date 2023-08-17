package jp319.zerochan.utils.gui;

import javax.swing.*;

public class MenuBar extends JMenuBar {
	private final JMenu fileMenu = new JMenu("File");
	private final JMenuItem openDownloadDirectoryItem = new JMenuItem("Open Download Directory");
	private final JMenuItem settingsItem = new JMenuItem("Settings");
	private final JMenuItem showDownloadItem = new JMenuItem("Show Download");
	private final JMenuItem exitItem = new JMenuItem("Exit");
	public MenuBar() {
		ImageIcon folderIcon = new ImageIcon("src/main/resources/images/open.png");
		ImageIcon settingsIcon = new ImageIcon("src/main/resources/images/settings.png");
		ImageIcon downloadIcon = new ImageIcon("src/main/resources/images/download.png");
		ImageIcon exitIcon = new ImageIcon("src/main/resources/images/close.png");
		
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
