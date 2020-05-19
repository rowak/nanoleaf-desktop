package io.github.rowak.nanoleafdesktop.ui.list;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafdesktop.discovery.EffectMetadata;

public class DiscoveryCellRenderer extends JLabel implements ListCellRenderer<EffectMetadata>
{
	public DiscoveryCellRenderer()
	{
		setOpaque(true);
		setBorder(new CompoundBorder(
				new LineBorder(java.awt.Color.GRAY, 1),
				new EmptyBorder(0, 0, 10, 0)));
		setVerticalTextPosition(JLabel.BOTTOM);
		setHorizontalTextPosition(JLabel.CENTER);
		setIconTextGap(10);
		setFont(new Font("Tahoma", Font.PLAIN, 22));
		setForeground(java.awt.Color.WHITE);
	}
	
	@Override
	public Component getListCellRendererComponent(JList<? extends EffectMetadata> list,
			EffectMetadata value, int index, boolean isSelected, boolean cellHasFocus)
	{
		EffectMetadata effect = (EffectMetadata)value;
		Color[] palette = effect.getPalette();
		final int RECT_HEIGHT = 10;
		BufferedImage img = new BufferedImage(list.getParent().getWidth(),
				RECT_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		final int RECT_WIDTH = img.getWidth()/palette.length;
		for (int i = 0; i < palette.length; i++)
		{
			Color c = palette[i];
			g.setColor(new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue()));
			g.fillRect(i*RECT_WIDTH, 0, RECT_WIDTH, RECT_HEIGHT);
		}
		ImageIcon icon = new ImageIcon(img);
		
		setIcon(icon);
		//setText(effect.getName() + " (" + effect.getDownloads() + " downloads)");
		setText(String.format("<html><font size=6>%s<font size=5> (%d downloads)</html>", effect.getName(), effect.getDownloads()));
		list.setSelectionBackground(java.awt.Color.GRAY);
		
		if (isSelected)
		{
			setBackground(list.getSelectionBackground());
		}
		else
		{
			setBackground(list.getBackground());
		}
		
		return this;
	}
}
