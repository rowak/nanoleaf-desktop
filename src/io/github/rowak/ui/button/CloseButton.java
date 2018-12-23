package io.github.rowak.ui.button;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class CloseButton extends JLabel
{
	public CloseButton(final Component frame, int operation)
	{
		setText("x");
		setForeground(Color.WHITE);
		setFont(new Font("Tahoma", Font.BOLD, 30));
		addMouseListener(new MouseAdapter()
		{
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	if (e.getButton() == MouseEvent.BUTTON1)
            	{
            		if (operation == JFrame.EXIT_ON_CLOSE)
            		{
            			System.exit(0);
            		}
            		else if (operation == JFrame.HIDE_ON_CLOSE)
            		{
            			frame.setVisible(false);
            		}
            		// Only available for JDialog components
            		else if (operation == JFrame.DISPOSE_ON_CLOSE)
            		{
            			((JDialog)frame).dispose();
            		}
            	}
            }
            
            @Override
			public void mouseEntered(MouseEvent e)
			{
            	((JLabel)e.getSource()).setForeground(Color.LIGHT_GRAY);
			}
            
            @Override
            public void mouseExited(MouseEvent e)
            {
            	((JLabel)e.getSource()).setForeground(Color.WHITE);
            }
        });
	}
}
