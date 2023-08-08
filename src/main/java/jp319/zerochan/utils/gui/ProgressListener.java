package jp319.zerochan.utils.gui;

import jp319.zerochan.views.components.Footer;

import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.swing.*;
import java.awt.*;

public class ProgressListener implements IIOReadProgressListener {
	Footer footer;
	public ProgressListener(Footer footer) {
		this.footer = footer;
	}
	@Override
	public void imageStarted(ImageReader source, int imageIndex) {
		SwingUtilities.invokeLater(() -> {
			footer.setProgressBarProgress(0);
			footer.getProgressBar().setVisible(true);
			footer.getProgressBar().setForeground(new Color(21, 240, 65));
			footer.updateLoadedItemLabel();
		});
	}
	@Override
	public void imageProgress(ImageReader source, float percentageDone) {
		SwingUtilities.invokeLater(() -> footer.setProgressBarProgress((int) percentageDone));
	}
	@Override
	public void imageComplete(ImageReader source) {
		SwingUtilities.invokeLater(() -> footer.setProgressBarProgress(100));
	}
	// Extra Methods
	@Override
	public void sequenceStarted(ImageReader source, int minIndex) {}
	@Override
	public void sequenceComplete(ImageReader source) {}
	@Override
	public void thumbnailStarted(ImageReader source, int imageIndex, int thumbnailIndex) {}
	@Override
	public void thumbnailProgress(ImageReader source, float percentageDone) {}
	@Override
	public void thumbnailComplete(ImageReader source) {}
	@Override
	public void readAborted(ImageReader source) {}
}
