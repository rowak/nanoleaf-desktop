package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;

public class SingleEntryDialog extends BasicDialog
{
	private JTextField entry;
	
	public SingleEntryDialog(Component parent, String entryLabel,
			String buttonLabel, ActionListener buttonListener)
	{
		super();
		
		entry = new JTextField(entryLabel);
		entry.setForeground(Color.WHITE);
		entry.setBackground(Color.DARK_GRAY);
		entry.setBorder(new LineBorder(Color.GRAY));
		entry.setCaretColor(Color.WHITE);
		entry.setFont(new Font("Tahoma", Font.PLAIN, 22));
		entry.addFocusListener(new TextFieldFocusListener(entry));
		contentPanel.add(entry, "cell 0 1, grow, gapx 2 2");
		
		JButton btnConfirm = new ModernButton(buttonLabel);
		btnConfirm.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnConfirm.addActionListener(buttonListener);
		contentPanel.add(btnConfirm, "cell 0 3, alignx center");
		
		JLabel spacer = new JLabel(" ");
		contentPanel.add(spacer, "cell 0 4");
		
		finalize(parent);
		
		btnConfirm.requestFocus();
	}
	
	public JTextField getEntryField()
	{
		return entry;
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
