package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;

public class TripleEntryDialog extends BasicDialog
{
	private JTextField entry1, entry2, entry3;

	public TripleEntryDialog(Component parent, String entry1Label, String entry2Label,
			String entry3Label, String buttonLabel, ActionListener buttonListener)
	{
		super();
		
		entry1 = new JTextField(entry1Label);
		entry1.setForeground(Color.WHITE);
		entry1.setBackground(Color.DARK_GRAY);
		entry1.setBorder(new LineBorder(Color.GRAY));
		entry1.setCaretColor(Color.WHITE);
		entry1.setFont(new Font("Tahoma", Font.PLAIN, 22));
		entry1.addFocusListener(new TextFieldFocusListener(entry1));
		contentPanel.add(entry1, "cell 0 1, grow, gapx 2 2");
		
		entry2 = new JTextField(entry2Label);
		entry2.setForeground(Color.WHITE);
		entry2.setBackground(Color.DARK_GRAY);
		entry2.setBorder(new LineBorder(Color.GRAY));
		entry2.setCaretColor(Color.WHITE);
		entry2.setFont(new Font("Tahoma", Font.PLAIN, 22));
		entry2.addFocusListener(new TextFieldFocusListener(entry2));
		contentPanel.add(entry2, "cell 0 2, grow, gapx 2 2");
		
		entry3 = new JTextField(entry3Label);
		entry3.setForeground(Color.WHITE);
		entry3.setBackground(Color.DARK_GRAY);
		entry3.setBorder(new LineBorder(Color.GRAY));
		entry3.setCaretColor(Color.WHITE);
		entry3.setFont(new Font("Tahoma", Font.PLAIN, 22));
		entry3.addFocusListener(new TextFieldFocusListener(entry3));
		contentPanel.add(entry3, "cell 0 3, grow, gapx 2 2");
		
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
	
	public JTextField getEntry3()
	{
		return entry3;
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
