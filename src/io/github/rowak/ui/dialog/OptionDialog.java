package io.github.rowak.ui.dialog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import io.github.rowak.ui.button.ModernButton;

import java.awt.Font;
import java.awt.event.ActionListener;

public class OptionDialog extends BasicDialog
{
	public OptionDialog(Component parent, String text, String choice1,
			String choice2, ActionListener action1, ActionListener action2)
	{
		JLabel lblText = new JLabel(text);
		lblText.setFont(FONT);
		lblText.setForeground(Color.WHITE);
		contentPanel.add(lblText, "gapx 15 0, cell 0 1,alignx center,aligny bottom");
		
		JButton btnChoice1 = new ModernButton(choice1);
		btnChoice1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnChoice1.addActionListener(action1);
		contentPanel.add(btnChoice1, "gapx 0 30, flowx,cell 0 2,alignx center,aligny bottom");
		
		JButton btnChoice2 = new ModernButton(choice2);
		btnChoice2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnChoice2.addActionListener(action2);
		contentPanel.add(btnChoice2, "cell 0 2,alignx center");
		
		JLabel spacer = new JLabel(" ");
		contentPanel.add(spacer, "cell 0 3");
		
		finalize(parent);
	}
}
