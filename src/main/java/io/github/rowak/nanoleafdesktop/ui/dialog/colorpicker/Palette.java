package io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;
import net.miginfocom.swing.MigLayout;

public class Palette extends JPanel {
	
	private int width, height;
	private Color selectedColor;
	private List<Color> palette;
	private ColorEntry colorEntry;
	private JScrollBar scrollBar;
	
	public Palette(int width, int height, ColorEntry colorEntry) {
		this.width = width;
		this.height = height;
		this.colorEntry = colorEntry;
		initUI();
	}
	
	private void initUI() {
		setLayout(new MigLayout("", "[]", "[]"));
		add(Box.createRigidArea(new Dimension(width, height)), "cell 0 0,alignx left,aligny top");
		setBackground(Color.DARK_GRAY);
		palette = new ArrayList<Color>();
		palette.add(new Color(255, 0, 0));
		palette.add(new Color(0, 255, 0));
		palette.add(new Color(0, 0, 255));
		
		scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
		scrollBar.setUI(new ModernScrollBarUI());
		scrollBar.setPreferredSize(new Dimension(width, 20));
		scrollBar.setMinimum(0);
		scrollBar.setValue(0);
		updateScrollBar();
		scrollBar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				repaint();
			}
		});
		add(scrollBar, "cell 0 1");
		
		MouseHandler mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);
		addMouseWheelListener(mouseHandler);
	}
	
	public Color[] getPalette() {
		return palette.toArray(new Color[]{});
	}
	
	public void setPalette(Color[] palette) {
		this.palette = new ArrayList<Color>();
		for (Color c : palette) {
			this.palette.add(c);
		}
		updateScrollBar();
	}
	
	private Color getCurrentColor() {
		return colorEntry.getColor();
	}
	
//	private Color getCurrentColor()
//	{
//		Color hue = wheel.getColor();
//		float brightness = slider.getValue()/100f;
//		float[] hsb = new float[3];
//		hsb = Color.RGBtoHSB(hue.getRed(), hue.getGreen(),
//				hue.getBlue(), hsb);
//		return Color.getHSBColor(hsb[0], hsb[1], brightness);
//	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintPalette(g);
	}
	
	private void paintPalette(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int scrollFactor = scrollBar.getValue()*10;
		final int OFFSET = 5;
		int colorNum = 0;
		for (Color color : palette) {
			final int DIAMETER = 50;
			final int SEPARATION = 10 + DIAMETER;
			
			int x = colorNum*SEPARATION + OFFSET - scrollFactor;
			int y = 5;
			
			g.setColor(color);
			g.fillOval(x, y, DIAMETER, DIAMETER);
			g.setColor(Color.GRAY);
			if (color == selectedColor) {
				g2d.setStroke(new BasicStroke(5));
			}
			else {
				g2d.setStroke(new BasicStroke(3));
			}
			g2d.drawOval(x, y, DIAMETER, DIAMETER);
			g2d.setStroke(new BasicStroke(1));
			
			colorNum++;
		}
		int cx = colorNum*60 + OFFSET - scrollFactor;
		int cy = OFFSET;
		g.setColor(Color.GRAY);
		g2d.setStroke(new BasicStroke(3));
		g2d.drawOval(colorNum*60 + OFFSET - scrollFactor,
				OFFSET, 50, 50);
		g.drawLine(cx + 15, cy + 25, cx + 35, cy + 25);
		g.drawLine(cx + 25, cy + 15, cx + 25, cy + 35);
		g2d.setStroke(new BasicStroke(1));
	}
	
	public void addChangeListener(ChangeListener listener) {
	    listenerList.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
	    listenerList.remove(ChangeListener.class, listener);
	}

	public ChangeListener[] getChangeListeners() {
	    return listenerList.getListeners(ChangeListener.class);
	}

	protected void fireChangeListeners() {
	    ChangeEvent event = new ChangeEvent(this);
	    for (ChangeListener listener : getChangeListeners()) {
	        listener.stateChanged(event);
	    }
	}
	
	private void updateScrollBar() {
		if (palette.size() > 7) {
			scrollBar.setVisible(true);
			scrollBar.setMaximum(palette.size()*6 - 30);
		}
		else {
			scrollBar.setVisible(false);
		}
	}
	
	private class MouseHandler extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			Point mouse = e.getPoint();
			
			int scrollFactor = scrollBar.getValue()*10;
			final int OFFSET = 5;
			final int DIAMETER = 50;
			int colorNum = 0;
			for (int i = 0; i < palette.size(); i++) {
				Color color = palette.get(i);
				final int SEPARATION = 10 + DIAMETER;
				
				int circX = colorNum*SEPARATION + OFFSET + DIAMETER/2 - scrollFactor;
				int circY = OFFSET;
				
				if (mouse.distance(circX, circY) < DIAMETER/2+OFFSET) {
					if (e.getButton() == 3) {
						palette.remove(color);
						fireChangeListeners();
						updateScrollBar();
						if (palette.size() < 8) {
							scrollBar.setValue(0);
						}
						repaint();
					}
				}
				colorNum++;
			}
			
			// Add a new color to the palette if the user clicks the "+" button
			int cX = colorNum*60 + OFFSET + DIAMETER/2 - scrollFactor;
			int cY = OFFSET;
			if (mouse.distance(cX, cY) < DIAMETER/2+OFFSET) {
				palette.add(getCurrentColor());
				fireChangeListeners();
				updateScrollBar();
				repaint();
			}
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			scrollBar.setValue((int)(scrollBar.getValue() + e.getWheelRotation()*2));
		}
	}
}
