package jp319.zerochan.views.Listeners;

import jp319.zerochan.views.callbacks.FrameListenerInterface;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class FrameListener implements ComponentListener {
	JFrame frame;
	FrameListenerInterface callback;
	public FrameListener(JFrame frame, FrameListenerInterface callback) {
		this.frame = frame;
		this.callback = callback;
	}
	@Override
	public void componentResized(ComponentEvent e) {
		callback.frameDimensionQuery(frame);
	}
	@Override
	public void componentMoved(ComponentEvent e) {}
	@Override
	public void componentShown(ComponentEvent e) {}
	@Override
	public void componentHidden(ComponentEvent e) {}
}
