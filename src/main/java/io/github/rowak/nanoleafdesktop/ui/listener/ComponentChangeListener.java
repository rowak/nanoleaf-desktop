package io.github.rowak.nanoleafdesktop.ui.listener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public interface ComponentChangeListener extends ChangeListener
{
	@Override
	void stateChanged(ChangeEvent e);
}
