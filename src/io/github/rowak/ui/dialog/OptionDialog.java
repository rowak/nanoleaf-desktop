package io.github.rowak.ui.dialog;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

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
		
		JButton btnChoice1 = new JButton(choice1);
		btnChoice1.setContentAreaFilled(false);
		btnChoice1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnChoice1.setBackground(Color.DARK_GRAY);
		btnChoice1.setForeground(Color.WHITE);
		btnChoice1.addActionListener(action1);
		contentPanel.add(btnChoice1, "gapx 0 30, flowx,cell 0 2,alignx center,aligny bottom");
		
		JButton btnChoice2 = new JButton(choice2);
		btnChoice2.setContentAreaFilled(false);
		btnChoice2.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnChoice2.setForeground(Color.WHITE);
		btnChoice2.setBackground(Color.DARK_GRAY);
		btnChoice2.addActionListener(action2);
		contentPanel.add(btnChoice2, "cell 0 2,alignx center");
		
		JLabel spacer = new JLabel(" ");
		contentPanel.add(spacer, "cell 0 3");
		
		finalize(parent);
	}
}
