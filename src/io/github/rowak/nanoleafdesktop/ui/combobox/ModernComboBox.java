package io.github.rowak.nanoleafdesktop.ui.combobox;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class ModernComboBox<T> extends JComboBox<T>
{
	public ModernComboBox()
	{
		initUI();
	}
	
	public ModernComboBox(DefaultComboBoxModel<T> model)
	{
		initUI();
		setModel(model);
	}
	
	private void initUI()
	{
		setUI(new ModernComboBoxUI(this));
		setFont(new Font("Tahoma", Font.PLAIN, 20));
		setBackground(Color.DARK_GRAY);
		setForeground(Color.WHITE);
	}
}
