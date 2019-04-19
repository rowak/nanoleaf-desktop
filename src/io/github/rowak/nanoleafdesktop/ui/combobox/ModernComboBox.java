package io.github.rowak.nanoleafdesktop.ui.combobox;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class ModernComboBox<T> extends JComboBox<T>
{
	public ModernComboBox()
	{
		init();
	}
	
	public ModernComboBox(DefaultComboBoxModel<T> model)
	{
		init();
		setModel(model);
	}
	
	private void init()
	{
		setUI(new ModernComboBoxUI(this));
		setFont(new Font("Tahoma", Font.PLAIN, 20));
		setBackground(Color.DARK_GRAY);
		setForeground(Color.WHITE);
	}
}
