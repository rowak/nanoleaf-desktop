package io.github.rowak.nanoleafdesktop.ui.combobox;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;

public class ModernComboBoxUI extends MetalComboBoxUI
{
	private JComboBox comboBox;
	
	public ModernComboBoxUI(JComboBox comboBox)
	{
		this.comboBox = comboBox;
	}
	
	@Override
	protected ComboPopup createPopup()
	{
		return new BasicComboPopup(comboBox)
		{
			@Override
			protected JScrollPane createScroller()
			{
				// Override the default Metal scrollbar
				JScrollPane scroller = super.createScroller();
				scroller.getVerticalScrollBar().setUI(new ModernScrollBarUI());
				return scroller;
			}
		};
	}
}
