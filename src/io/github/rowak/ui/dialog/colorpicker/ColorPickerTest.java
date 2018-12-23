package io.github.rowak.ui.dialog.colorpicker;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import io.github.rowak.ui.button.CloseButton;
import io.github.rowak.ui.listener.WindowDragListener;
import net.miginfocom.swing.MigLayout;

public class ColorPickerTest extends JDialog
{
	public static void main(String[] args)
	{
		try
		{
			ColorPickerTest dialog = new ColorPickerTest();
			dialog.setVisible(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public ColorPickerTest()
	{
		setUndecorated(true);
		JPanel contentPanel = new JPanel()
		{
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				paintColorPicker(g);
				System.out.println(getColor(200, 100));
			}
		};
		contentPanel.setLayout(new MigLayout("", "[432.00,grow]", "[][]"));
		contentPanel.setBackground(Color.DARK_GRAY);
		contentPanel.setBorder(new LineBorder(new Color(128, 128, 128), 2));
		setContentPane(contentPanel);
		
		WindowDragListener wdl = new WindowDragListener(50);
		addMouseListener(wdl);
		addMouseMotionListener(wdl);
		
		CloseButton btnClose = new CloseButton(this, JFrame.DISPOSE_ON_CLOSE);
		contentPanel.add(btnClose, "cell 0 0,alignx right,gapx 0 15");
		
		pack();
		
		setSize(getWidth(), getHeight()+200);
	}
	
	public void paintColorPicker(Graphics g)
	{
		final int PADDING = 20;
		final int TITLE_BAR_SIZE = 50;
		Insets insets = new Insets(PADDING + TITLE_BAR_SIZE, PADDING, PADDING, PADDING + getWidth()/2);
		g.setColor(Color.GRAY);
		g.drawRect(insets.left, insets.top, getWidth() - insets.right - insets.left,
				getHeight() - insets.bottom - insets.top);
		

		/*
		 * Color wheel algorithm source:
		 * https://rosettacode.org/wiki/Color_wheel
		 */
		float centerX = getWidth()/2;
		float centerY = getHeight()/2;
		float radius = centerX;
		if (centerY < radius)
		{
			radius = centerY;
		}
		for (int y = 0; y < getHeight(); y++)
		{
			float dy = y - centerY;
			for (int x = 0; x < getWidth(); x++)
			{
				float dx = x - centerX;
				float dist = (float)Math.sqrt(dx*dx + dy*dy);
				if (dist <= radius)
				{
					float hue = (float)(((Math.atan2(dx, dy) / Math.PI) + 1f) / 2f);
					Color rgb = new Color(Color.HSBtoRGB(hue, 1f, 1f));
					g.setColor(rgb);
					g.drawRect(x, y, 1, 1);
				}
			}
		}
	}
	
	public Color getColor(int x, int y)
	{
		float centerX = getWidth()/2;
		float centerY = getHeight()/2;
		float radius = centerX;
		if (centerY < radius)
		{
			radius = centerY;
		}
		for (int circy = 0; y < getHeight(); y++)
		{
			float dy = y - centerY;
			for (int circx = 0; x < getWidth(); x++)
			{
				if (circx == x && circy == y)
				{
					float dx = x - centerX;
					float dist = (float)Math.sqrt(dx*dx + dy*dy);
					if (dist <= radius)
					{
						float hue = (float)(((Math.atan2(dx, dy) / Math.PI) + 1f) / 2f);
						return new Color(Color.HSBtoRGB(hue, 1f, 1f));
					}
				}
			}
		}
		return null;
	}
}
