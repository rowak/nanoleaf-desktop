package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.textfield.ModernTextField;

public class DoubleEntryDialog extends BasicDialog
{
	private JTextField entry1, entry2;

	public DoubleEntryDialog(Component parent, String entry1Label,
			String entry2Label, String buttonLabel, ActionListener buttonListener)
	{
		super();
		
		entry1 = new ModernTextField(entry1Label);
		entry1.addFocusListener(new TextFieldFocusListener(entry1));
		contentPanel.add(entry1, "cell 0 1, grow, gapx 2 2");
		
		entry2 = new ModernTextField(entry2Label);
		entry2.addFocusListener(new TextFieldFocusListener(entry2));
		contentPanel.add(entry2, "cell 0 2, grow, gapx 2 2");
		
		JButton btnConfirm = new ModernButton(buttonLabel);
		btnConfirm.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnConfirm.addActionListener(buttonListener);
		contentPanel.add(btnConfirm, "cell 0 4, alignx center");
		
		JLabel spacer = new JLabel(" ");
		contentPanel.add(spacer, "cell 0 5");
		
		finalize(parent);
		
		btnConfirm.requestFocus();
	}
	
	public JTextField getEntry1()
	{
		return entry1;
	}
	
	public JTextField getEntry2()
	{
		return entry2;
	}
	
	private class TextFieldFocusListener extends FocusAdapter
	{
		String defaultText;
		
		public TextFieldFocusListener(JTextField parent)
		{
			defaultText = parent.getText();
		}
		
		@Override
		public void focusGained(FocusEvent e)
		{
			JTextField entry = (JTextField)e.getSource();
			if (entry.getText().equals(defaultText))
			{
				entry.setText("");
			}
		}
		
		@Override
		public void focusLost(FocusEvent e)
		{
			JTextField entry = (JTextField)e.getSource();
			if (entry.getText().equals(""))
			{
				entry.setText(defaultText);
			}
		}
	}
}
