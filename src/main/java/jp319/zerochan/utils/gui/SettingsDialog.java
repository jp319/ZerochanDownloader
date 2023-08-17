package jp319.zerochan.utils.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SettingsDialog extends JDialog {
	private final JPanel settings_pnl = new JPanel(new BorderLayout());
	private final ScrollablePanel settingsBody = new ScrollablePanel();
	private final JScrollPane settingsBodyWrapper = new JScrollPane();
	private final JLabel settingsHeader_lb = new JLabel("Settings", SwingConstants.CENTER);
	private final JPanel downloadSettings_pnl = new JPanel();
	private String downloadDirectory = "";
	private final JButton saveSettings_btn = new JButton("Save");
	public SettingsDialog(Frame owner) {
		super(owner);
		setTitle("Settings");
		setMinimumSize(new Dimension(400, 400));
		setLocationRelativeTo(owner);
		setAlwaysOnTop(false);
		
		initSettingsBody();
		initDownloadSettings();
		initSaveSettingsButton();
		
		settings_pnl.add(settingsHeader_lb, BorderLayout.NORTH);
		settings_pnl.add(settingsBodyWrapper, BorderLayout.CENTER);
		settings_pnl.add(saveSettings_btn, BorderLayout.SOUTH);
		
		setDownloadDirectoryBTNListener();
		
		pack();
		setContentPane(settings_pnl);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setVisible(false);
	}
	private void initSettingsBody() {
		settingsBody.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
		settingsBody.setScrollableBlockIncrement(
				ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 200);
		settingsBody.setScrollableUnitIncrement(
				ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PIXELS, 15);
		settingsBody.setLayout(new BoxLayout(settingsBody, BoxLayout.Y_AXIS));
		settingsBodyWrapper.setViewportView(settingsBody);
		settingsBodyWrapper.setBorder(BorderFactory.createEmptyBorder(
				15,5,5,5
		));
	}
	private void initDownloadSettings() {
		downloadSettings_pnl.setLayout(new GridBagLayout());
		downloadSettings_pnl.setBorder(BorderFactory.createEmptyBorder(
				5,5,5,5
		));
		downloadSettings_pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel downloadDirectory_lb = new JLabel("Folder");
		JTextField downloadDirectory_tf = new JTextField();
		downloadDirectory_tf.setEditable(false);
		JButton downloadDirectory_btn = new JButton("Browse");
		
		downloadSettings_pnl.add(downloadDirectory_lb, new GridBagConstraints(
				0,0,1,1,0.0,0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.WEST,
				new Insets(0,0,0,10),
				0,0
		));
		downloadSettings_pnl.add(downloadDirectory_tf, new GridBagConstraints(
				1,0,4,1,1.0,0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL,
				new Insets(0,0,0,10),
				0,0
		));
		downloadSettings_pnl.add(downloadDirectory_btn, new GridBagConstraints(
				5,0,1,1,0.0,0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.EAST,
				new Insets(0,0,0,0),
				0,0
		));
		
		settingsBody.add(downloadSettings_pnl);
	}
	private void initSaveSettingsButton() {
		saveSettings_btn.setBorder(BorderFactory.createEmptyBorder(
				10,10,10,10
		));
	}
	// Getters
	public JTextField getDownloadDirectoryTF() {
		for (Component component : downloadSettings_pnl.getComponents()) {
			if (component instanceof JTextField textField) {
				return textField;
			}
		}
		return null;
	}
	public JButton getDownloadDirectoryBTN() {
		for (Component component : downloadSettings_pnl.getComponents()) {
			if (component instanceof JButton button) {
				return button;
			}
		}
		return null;
	}
	public String getDownloadDirectory() {
		if (!downloadDirectory.isEmpty()) {
			return downloadDirectory;
		} else {
			return "";
		}
	}
	public JButton getSaveSettingsButton() {
		return saveSettings_btn;
	}
	
	// Listener
	private void setDownloadDirectoryBTNListener() {
		getDownloadDirectoryBTN().addActionListener(e -> {
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int choice = jFileChooser.showOpenDialog(this);
			if (choice == JFileChooser.APPROVE_OPTION) {
				File selectedFile = jFileChooser.getSelectedFile();
				if (selectedFile != null) {
					downloadDirectory = selectedFile.getAbsolutePath() + "/";
					downloadDirectory = downloadDirectory.replace("\\", "/");
					getDownloadDirectoryTF().setText(downloadDirectory);
				}
			}
		});
	}
}
