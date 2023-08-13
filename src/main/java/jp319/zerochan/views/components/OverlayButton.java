package jp319.zerochan.views.components;

import java.io.*;
import java.net.*;
import javax.swing.*;

public class OverlayButton {
	
	public static void main(String[] args) throws IOException {
		URL gifUrl = new URL("https://i.imgur.com/I4a9zaK.gif");
		
		ImageIcon gifIcon = new ImageIcon(gifUrl);
		JLabel label = new JLabel(gifIcon);
		
		JFrame frame = new JFrame("Processed GIF");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(label);
		
		frame.pack();
		frame.setVisible(true);
	}
}
