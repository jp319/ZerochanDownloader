package jp319.zerochan.views.components;

import jp319.zerochan.utils.gui.HyperLinkToolTip;
import jp319.zerochan.utils.gui.MarginPanel;
import jp319.zerochan.utils.gui.ScaledIcon;
import jp319.zerochan.utils.gui.WrapLayout;
import jp319.zerochan.views.callbacks.FrameListenerInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

public class Header extends MarginPanel implements FrameListenerInterface {
	private final JLabel header_lb = new JLabel("ZeroArtFetcher");
	private final JTextField search_tf = new JTextField(){
		@Override
		public JToolTip createToolTip() {
			JToolTip toolTip = new HyperLinkToolTip();
			toolTip.setComponent(this);
			return toolTip;
		}
	};
	private final JButton search_btn = new JButton();
	private final JPanel sort_pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private final JPanel filter_pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private final ButtonGroup sort_btg = new ButtonGroup();
	private final ButtonGroup filter_btg = new ButtonGroup();
	private final JRadioButton[] sort_rBtnA = new JRadioButton[3];
	private final JRadioButton[] filter_rBtnA = new JRadioButton[7];
	private final int totalSort_rBtnA = sort_rBtnA.length - 1;
	private final int totalFilter_rBtnA = filter_rBtnA.length - 1;
	JPanel options_pnl = new JPanel();
	private final JRadioButton strictMode_rBtn = new JRadioButton("Strict Mode");
	private final JSpinner entryLimit_spn = new JSpinner(new SpinnerNumberModel(25, 1, 200, 1));
	private final JSpinner.NumberEditor editor = (JSpinner.NumberEditor) entryLimit_spn.getEditor();
	private final JTextField colorFilter_tf = new JTextField();
	private final String[] options = {"All Time", "Last 7000 Entries", "Last 15000 Entries"};
	private final JComboBox<String> popularityButton_cmb = new JComboBox<>(options);
	private final JButton filterDropdown_btn = new JButton("Filters ｜▼");
	private final JPopupMenu filterPopup_pop = new JPopupMenu();
	private final JButton applyFilter_btn = new JButton("Apply");
	private final JButton prevPage_btn = new JButton("⏪");
	private final JButton nextPage_btn = new JButton("⏩");

	public Header() {
		initHeaderPanel();
		initComponents();
	}
	private void initHeaderPanel() {
		setLayout(new GridBagLayout());
		// Set individual margins (left, right, top, and bottom) using setters
		setLeftMarginPercentage(15.0);
		setRightMarginPercentage(15.0);
		setTopMarginPercentage(5.0);
		setBottomMarginPercentage(5.0);
	}
	private void initComponents() {
		setHeader_lb();
		setSearch_tf();
		setButton_groups();
		setOptions();
	}
	private void setHeader_lb() {
		header_lb.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		try {
			// Load the font file from the "fonts" folder
			File fontFile = new File("src/main/resources/fonts/Quicksand.otf");
			Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
			
			// Derive the desired font size and style
			Font customFontSized = customFont.deriveFont(Font.BOLD, 20);
			header_lb.setFont(customFontSized);
		} catch (FontFormatException | IOException e) {
			System.out.println("Error on loading Fonts");
			e.printStackTrace();
		}
		add(header_lb, new GridBagConstraints(
				0,0,8,1,1.0,0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.CENTER,
				new Insets(0,0,0,0),
				0,0
		));
	}
	private void setSearch_tf() {
		search_btn.setIcon(createSearchIcon());
		search_btn.putClientProperty( "JButton.buttonType", "roundRect" );
		search_tf.putClientProperty("JTextField.trailingComponent", search_btn);
		search_tf.putClientProperty("JComponent.roundRect", true);
		search_tf.putClientProperty("JTextField.placeholderText", "Search Tag(s)/ID: Naruto,Sakura | 3793685");
		search_tf.setPreferredSize(new Dimension(68, 35));
		search_tf.setToolTipText("<html>When Searching with multiple tags make sure<br>" +
				"to separate it with comma \",\"." +
				"<br>----------------------------------------------------------" +
				"<br>(e.g., Fate/Stay Night: Unlimited Blade Works,Tohsaka Rin)</html>");
		add(search_tf, new GridBagConstraints(
				0,1,8,1,1.0,0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0,0,0,0),
				0,0
		));
	}
	private void setButton_groups() {
		SwingUtilities.invokeLater(()-> {
			// Add Sort & Filter Panels
			add(sort_pnl, new GridBagConstraints(
					0, 2, 4, 1, 1.0, 1.0,
					GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 0, 0),
					0, 0
			));
			add(filter_pnl, new GridBagConstraints(
					4, 2, 4, 1, 1.0, 1.0,
					GridBagConstraints.EAST,
					GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 0, 0),
					0, 0
			));
			// Sort Buttons
			sort_rBtnA[0] = new JRadioButton("Recent");
			sort_rBtnA[1] = new JRadioButton("Popular");
			popularityButton_cmb.setPreferredSize(new Dimension(25,popularityButton_cmb.getPreferredSize().height));
			popularityButton_cmb.setEnabled(false); // Set popularity scale to disabled since
													// the default is Recent filter.
			
			// Filter Buttons
			filter_rBtnA[0] = new JRadioButton("All");
			filter_rBtnA[1] = new JRadioButton("Large");
			filter_rBtnA[2] = new JRadioButton("Huge");
			filter_rBtnA[3] = new JRadioButton("Landscape");
			filter_rBtnA[4] = new JRadioButton("Portrait");
			filter_rBtnA[5] = new JRadioButton("Square");
			// Add Radio Buttons to their respective groups
			setButton_groups(totalSort_rBtnA, sort_btg, sort_rBtnA, sort_pnl);
			setButton_groups(totalFilter_rBtnA, filter_btg, filter_rBtnA, filter_pnl);
			sort_pnl.add(popularityButton_cmb);
		});
	}
	private void setButton_groups(int totalButtons, ButtonGroup buttonGroup,
	                                JRadioButton[] radioButtonArray,
	                                JPanel radioButtonPanel) {
		for (int i = 0; i <= totalButtons; i++ ) {
			buttonGroup.add(radioButtonArray[i]);
			if (i < totalButtons) {
				radioButtonPanel.add(radioButtonArray[i]);
			}
			if (i == 0) {
				radioButtonArray[i].setSelected(true);
			}
			if (radioButtonArray[i] != null && radioButtonArray[i].getText().equals("Popular")) {
				// Add Listener to Popularity button
				setPopularityButtonListener(radioButtonArray[i]);
			}
		}
	}
	private void setOptions() {
		SwingUtilities.invokeLater(()-> {
			JPanel options_pnl_wrapper = new JPanel(new BorderLayout());
			prevPage_btn.setPreferredSize(new Dimension(
					20, 1
			));
			nextPage_btn.setPreferredSize(new Dimension(
					20, 1
			));
			
			options_pnl.setLayout(new BorderLayout());
			options_pnl.setBorder(BorderFactory.createLineBorder(Color.GRAY,1));
			JPanel optionHolder = new JPanel();
			
			strictMode_rBtn.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
			editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
			JLabel entryLimitLabel = new JLabel("No. of Entries");
			colorFilter_tf.putClientProperty("JComponent.roundRect", true);
			colorFilter_tf.putClientProperty("JTextField.placeholderText", "Color");
			applyFilter_btn.putClientProperty("JComponent.roundRect", true);
			
			optionHolder.add(strictMode_rBtn);
			optionHolder.add(colorFilter_tf);
			optionHolder.add(entryLimit_spn);
			optionHolder.add(entryLimitLabel);
			options_pnl.add(optionHolder, BorderLayout.CENTER);
			options_pnl.add(applyFilter_btn,BorderLayout.EAST);
			
			options_pnl_wrapper.add(prevPage_btn, BorderLayout.WEST);
			options_pnl_wrapper.add(options_pnl, BorderLayout.CENTER);
			options_pnl_wrapper.add(nextPage_btn, BorderLayout.EAST);
			
			add(options_pnl_wrapper, new GridBagConstraints(
					0,3,8,1,1.0,0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL,
					new Insets(0,0,0,0),
					0,0
			));
			
			revalidate();
			repaint();
		});
	}
	// Helper & Listener Methods
	private void setPopularityButtonListener(JRadioButton popularityButton) {
		popularityButton.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				popularityButton_cmb.setEnabled(true);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				popularityButton_cmb.setEnabled(false);
			}
		});
	}
	private void showFilterButtons() {
		filter_pnl.removeAll();
		
		for (int i = 0; i <= 5; i++ ) {
			filter_pnl.add(filter_rBtnA[i]);
		}
		SwingUtilities.invokeLater(() -> {
			filter_pnl.revalidate();
			filter_pnl.repaint();
			
			sort_pnl.revalidate();
			sort_pnl.repaint();
		});
	}
	private void hideFilterButtons() {
		filter_pnl.removeAll();
		
		if (filterPopup_pop.getComponentCount() == 0) {
			filterDropdown_btn.setVerticalTextPosition(SwingConstants.CENTER);
			filterDropdown_btn.setHorizontalTextPosition(SwingConstants.CENTER);
			filterDropdown_btn.setPreferredSize(new Dimension(filterDropdown_btn.getPreferredSize().width, filterDropdown_btn.getPreferredSize().height));
			
			for (int i = 0; i <= 5; i++ ) {
				filterPopup_pop.add(filter_rBtnA[i]);
			}
			
			// Calculate the preferred width and height based on the preferred sizes of the buttons
			// Also puts padding through empty border
			filterPopup_pop.setLayout(new WrapLayout(FlowLayout.LEFT));
			filterPopup_pop.setBorder(BorderFactory.createEmptyBorder(
					1,1,1,1
			));
			
			filterPopup_pop
					.setPopupSize(
							filterDropdown_btn.getPreferredSize().width,
							filter_rBtnA[0].getPreferredSize().height*8
					);
			filterDropdown_btn.addActionListener(e -> filterPopup_pop.show(filterDropdown_btn, 0, filterDropdown_btn.getHeight()));
		}
		
		SwingUtilities.invokeLater(() -> {
			filter_pnl.add(filterDropdown_btn, BorderLayout.WEST);
			filter_pnl.revalidate();
			filter_pnl.repaint();
			
			sort_pnl.revalidate();
			sort_pnl.repaint();
		});
	}
	@Override
	public void frameDimensionQuery(JFrame frame) {
		int width = frame.getWidth();
		if (width > 899) {
			showFilterButtons();
		} else {
			hideFilterButtons();
		}
	}
	private static Icon createSearchIcon() {
		// Replace this URL with the actual URL of your search icon image
		File image = new File("src/main/resources/images/search.png");
		ImageIcon imageIcon = new ImageIcon(image.getAbsolutePath());
		return ScaledIcon.createScaledIcon(imageIcon, 16, 16);
	}
	// Getters for Controller
	public JTextField getSearch_tf() {
		return search_tf;
	}
	public JButton getSearch_btn() {
		return search_btn;
	}
	public JRadioButton[] getSort_rBtnA() {
		return sort_rBtnA;
	}
	public JRadioButton[] getFilter_rBtnA() {
		return filter_rBtnA;
	}
	public JComboBox<String> getPopularityButton_cmb() {
		return popularityButton_cmb;
	}
	public JButton getNextPage_btn() {
		return nextPage_btn;
	}
	public JButton getPrevPage_btn() {
		return prevPage_btn;
	}
	public JRadioButton getStrictMode_rBtn() {
		return strictMode_rBtn;
	}
	public JSpinner getEntryLimit_spn() {
		return entryLimit_spn;
	}
	public JTextField getColorFilter_tf() {
		return colorFilter_tf;
	}
	public JButton getApplyFilter_btn() {
		return applyFilter_btn;
	}
}
