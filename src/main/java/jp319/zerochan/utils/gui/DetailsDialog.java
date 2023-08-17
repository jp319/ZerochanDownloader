package jp319.zerochan.utils.gui;

import jp319.zerochan.models.FullImageData;
import jp319.zerochan.utils.sanitations.ConvertByte;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

public class DetailsDialog extends JDialog {
	private final FullImageData fullImageData;
	private final JPanel details_pnl = new JPanel(new GridBagLayout());
	private final JLabel[] detailLabels = new JLabel[15];
	private final JTextPane[] detailValues = new JTextPane[15];
	JLabel header_lb = new JLabel("Image Properties", SwingConstants.CENTER);
	JButton close_btn = new JButton("Close");
	public DetailsDialog(JFrame parent, String title, FullImageData fullImageData) {
		super(parent, title, true);
		this.fullImageData = fullImageData;
		JPanel pane = new JPanel(new BorderLayout());
		pane.setBorder(BorderFactory.createEmptyBorder(
				0,50,10,50
		));
		setMinimumSize(new Dimension(300, 550));
		setPreferredSize(new Dimension(500, 550));
		
		initMisc();
		initDetailLabels();
		initDetailValues();
		
		pane.add(header_lb, BorderLayout.NORTH);
		pane.add(details_pnl, BorderLayout.CENTER);
		pane.add(close_btn, BorderLayout.SOUTH);
		
		setContentPane(pane);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(parent);
		setVisible(false);
	}
	private void initMisc() {
		header_lb.setFont(new Font("Arial",Font.BOLD, 15));
		header_lb.setBorder(BorderFactory.createEmptyBorder(
				5,5,5,5
		));
		close_btn.addActionListener(e -> setVisible(false));
	}
	private void initDetailLabels() {
		details_pnl.setAlignmentX(CENTER_ALIGNMENT);
		String[] labelStrings = new String[]
				{
						"Tags",
						" ",
						"ID",
						"Hash",
						"Primary",
						" ",
						"Size",
						"File Size",
						" ",
						"Full",
						"Small",
						"Medium",
						" ",
						"Source",
						" "
				};
		int labelCount = 0;
		for (String string : labelStrings) {
			detailLabels[labelCount++] = new JLabel(string);
		}
		labelCount = 0;
		for (JLabel jLabel : detailLabels) {
			jLabel.setOpaque(false);
			jLabel.setFont(new Font("Arial",Font.BOLD, 13));
			details_pnl.add(jLabel, new GridBagConstraints(
					0,labelCount++,1,1,0.0,0.0,
					GridBagConstraints.WEST,
					GridBagConstraints.WEST,
					new Insets(5,0,5,10),
					0,0
			));
		}
	}
	
	private void initDetailValues() {
		String[] valueStrings = new String[]
				{
						fullImageData.getFormattedTags(),
						" ",
						fullImageData.getId(),
						fullImageData.getHash(),
						fullImageData.getPrimary(),
						" ",
						fullImageData.getHeight() + "x" + fullImageData.getWidth(),
						ConvertByte.formatBytesToMB(fullImageData.getSize()),
						" ",
						"<a href='" + fullImageData.getFull()   + "'>" + fullImageData.getFull()   + "</a>",
						"<a href='" + fullImageData.getSmall()  + "'>" + fullImageData.getSmall()  + "</a>",
						"<a href='" + fullImageData.getMedium() + "'>" + fullImageData.getMedium() + "</a>",
						" ",
						(fullImageData.getSource() != null && !fullImageData.getSource().isEmpty()) ?
								"<a href='" + fullImageData.getSource() + "'>" +
										fullImageData.getSource() + "</a>"
								:
								"",
						" "
				};
		int labelCount = 0;
		for (String string : valueStrings) {
			detailValues[labelCount] = new JTextPane();
			detailValues[labelCount].setContentType("text/html");
			detailValues[labelCount].setText(string);
			detailValues[labelCount].setEditable(false);
			detailValues[labelCount].setBackground(null);
			detailValues[labelCount].setBorder(null);
			labelCount++;
		}
		labelCount = 0;
		for (JTextPane jLabel : detailValues) {
			if (labelCount == 0) {
				JScrollPane scrollPane = new JScrollPane(jLabel);
				scrollPane.setPreferredSize(new Dimension(50,50));
				details_pnl.add(scrollPane, new GridBagConstraints(
						1,labelCount++,1,1,1.0,1.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.BOTH,
						new Insets(5,0,5,10),
						0,0
				));
			} else {
				details_pnl.add(jLabel, new GridBagConstraints(
						1, labelCount++, 1, 1, 1.0, 0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.HORIZONTAL,
						new Insets(5, 0, 5, 10),
						0, 0
				));
			}
			jLabel.addHyperlinkListener(e -> {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					openBrowser(e.getURL().toString());
				}
			});
		}
	}
	private static void openBrowser(String url) {
		try {
			Desktop.getDesktop().browse(new java.net.URI(url));
		} catch (IOException | URISyntaxException ex) {
			ex.printStackTrace();
		}
	}
}
