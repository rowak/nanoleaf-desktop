package io.github.rowak.nanoleafdesktop.ui.button;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class MaximizeButton extends JLabel
{
	public MaximizeButton(final JFrame frame)
	{
		setText("\u20de");
		setForeground(Color.WHITE);
		setFont(new Font("Tahoma", Font.BOLD, 25));
		addMouseListener(new MouseAdapter()
		{
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	if (e.getButton() == MouseEvent.BUTTON1)
            	{
            		if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH)
            		{
            			frame.setExtendedState(JFrame.NORMAL);
            		}
            		else if (frame.getExtendedState() == JFrame.NORMAL)
            		{
            			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
