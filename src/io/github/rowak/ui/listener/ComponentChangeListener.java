package io.github.rowak.ui.listener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public interface ComponentChangeListener extends ChangeListener
{
	@Override
	public void stateChanged(ChangeEvent e);
}
