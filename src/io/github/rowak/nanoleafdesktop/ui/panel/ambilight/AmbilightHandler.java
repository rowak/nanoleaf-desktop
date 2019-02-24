package io.github.rowak.nanoleafdesktop.ui.panel.ambilight;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.Timer;

import io.github.rowak.Aurora;
import io.github.rowak.Effect;
import io.github.rowak.Frame;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.effectbuilder.CustomEffectBuilder;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

public class AmbilightHandler
{
	private int delay, brightness, mode;
	private boolean running, updating;
	private String previousEffect;
	private Aurora aurora;
	private Rectangle captureArea;
	private Color currentColor;
	private Timer timer;
	private Robot robot;
	private AmbilightPanel parent;
	private Panel[] panels;
	private Panel[][] rows;
	private PanelCanvas canvas;
	
	public AmbilightHandler(Aurora aurora,
			PanelCanvas canvas, AmbilightPanel parent)
	{
		this.aurora = aurora;
		this.canvas = canvas;
		this.parent = parent;
		delay = parent.getUpdateDelay();
		brightness = parent.getBrightness();
		captureArea = parent.getCaptureArea();
		mode = parent.getMode();
		
		try
		{
			robot = new Robot();
		}
		catch (AWTException awte)
		{
			awte.printStackTrace();
		}
	}
	
	public void start()
	{
		panels = getSortedPanels();
		rows = PanelTableSort.getRows(panels);
		saveCurrentEffect();
		startExternalStreaming();
		timer = new Timer(delay, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread(() ->
				{
					updateDisplay();
				}).start();
			}
		});
		timer.start();
		running = true;
	}
	
	public void stop()
	{
		timer.stop();
		loadPreviousEffect();
		running = false;
	}
	
	private void updateDisplay()
	{
		if (!updating)
		{
			updating = true;
			try
			{
				switch (mode)
				{
					case 0:
						applyAverageMode();
						break;
					case 1:
						applySelectionMode();
						break;
					default:
						applyAverageMode();
						break;
				}
			}
			catch (StatusCodeException sce)
			{
				sce.printStackTrace();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			updating = false;
		}
	}
	
	private void applyAverageMode()
			throws StatusCodeException, IOException
	{
		Color originalColor = getAverageScreenColor();
		originalColor = addAdditionalBrightness(originalColor, brightness);
		if (!sameColor(originalColor, currentColor))
		{
			Effect ef = new CustomEffectBuilder(aurora)
					.addFrameToAllPanels(new Frame(originalColor.getRed(),
							originalColor.getGreen(), originalColor.getBlue(), 0, 5))
					.build("", false);
			aurora.externalStreaming().sendStaticEffect(ef);
			currentColor = originalColor;
			ef = null;
			
			new Thread(() ->
			{
				canvas.setColor(currentColor);
			}).start();
		}
	}
	
	private void applySelectionMode()
			throws StatusCodeException, IOException
	{
		CustomEffectBuilder ceb = new CustomEffectBuilder(aurora);
		BufferedImage img = getScreenImage();
		final int VERTICAL_SEPARATOR = captureArea.height/rows.length;
		for (int i = 0; i < rows.length; i++)
		{
			int captureY = VERTICAL_SEPARATOR*i + VERTICAL_SEPARATOR/2;
			
			for (int j = 0; j < rows[i].length; j++)
			{
				final int HORIZONTAL_SEPARATOR = captureArea.width/rows[i].length;
				int captureX = HORIZONTAL_SEPARATOR*j + HORIZONTAL_SEPARATOR/2;
				
				if (img.getSubimage(captureX, captureY, 1, 1) != null)
				{
					Color color = new Color(img.getRGB(captureX, captureY));
					ceb.addFrame(rows[i][j], new Frame(color.getRed(),
							color.getGreen(), color.getBlue(), 0, 5));
				}
			}
		}
		aurora.externalStreaming().sendStaticEffect(ceb.build("", false));
	}
	
//	private void applySelectionMode()
//			throws StatusCodeException, IOException
//	{
//		BufferedImage img = getScreenImage();
//		System.out.println(captureArea.x + " " + captureArea.y + "    " + img.getWidth() + " " + img.getHeight());
//		final int VERTICAL_SEPARATOR = captureArea.height/rows.length;
//		for (int i = 0; i < rows.length; i++)
//		{
//			for (int j = 0; j < rows[i].length; j++)
//			{
//				final int HORIZONTAL_SEPARATOR = captureArea.width/rows[i].length;
//				int captureX = HORIZONTAL_SEPARATOR*j + HORIZONTAL_SEPARATOR/2;
//				int captureY = VERTICAL_SEPARATOR*i + VERTICAL_SEPARATOR/2;
//				
//				if (img.getSubimage(captureX, captureY, 1, 1) != null)
//				{
//					Color color = new Color(img.getRGB(captureX, captureY));
//					Effect ef = new CustomEffectBuilder(aurora)
//							.addFrame(rows[i][j], new Frame(color.getRed(),
//									color.getGreen(), color.getBlue(), 0, 5))
//							.build("", false);
//					aurora.externalStreaming().sendStaticEffect(ef);
//				}
//			}
//		}
//	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	private Rectangle getMonitorCaptureArea()
	{
		Point monitorLocation = getMonitorLocation();
		int x = monitorLocation.x + captureArea.x;
		int y = monitorLocation.y + captureArea.y;
		return new Rectangle(x, y,
				captureArea.width, captureArea.height);
	}
	
	private Panel[] getSortedPanels() 
	{
		Panel[] panels = null;
		try
		{
			panels = aurora.panelLayout().getPanels();
			PanelTableSort.sortPanelsAsTable(panels);
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
		return panels;
	}
	
	public void setAurora(Aurora aurora)
	{
		this.aurora = aurora;
	}
	
	public void setUpdateDelay(int delay)
	{
		this.delay = delay;
		if (timer != null && timer.isRunning())
		{
			restartTimer();
		}
	}
	
	public void setBrightness(int brightness)
	{
		this.brightness = brightness;
	}
	
	public void setCaptureArea(Rectangle captureArea)
	{
		this.captureArea = captureArea;
		if (timer != null && timer.isRunning())
		{
			restartTimer();
		}
	}
	
	public void setMode(int mode)
	{
		this.mode = mode;
	}
	
	private void restartTimer()
	{
		timer.stop();
		timer.start();
	}
	
	private void saveCurrentEffect()
	{
		try
		{
			previousEffect = aurora.effects().getCurrentEffectName();
		}
		catch (StatusCodeException sce)
		{
			// ignore this exception for now, handle it
			// later in the loadPreviousEffect() method
		}
	}
	
	private void loadPreviousEffect()
	{
		try
		{
			aurora.effects().setEffect(previousEffect);
		}
		catch (StatusCodeException | NullPointerException scenpe)
		{
			showMessageBox("The previous effect could not be loaded.");
		}
	}
	
	private void startExternalStreaming()
	{
		try
		{
			aurora.externalStreaming().enable();
		}
		catch (StatusCodeException sce)
		{
			showMessageBox("Failed to start ambient lighting server. " +
					"Please reload the application and try again.");
		}
	}
	
	private boolean sameColor(Color color1, Color color2)
	{
		if (color1 != null && color2 != null)
		{
			return color1.getRed() == color2.getRed() &&
					color1.getGreen() == color2.getGreen() &&
					color1.getBlue() == color2.getBlue();
		}
		return false;
	}
	
	private Color addAdditionalBrightness(Color color, int amount)
	{
		for (int i = 0; i < amount; i++)
			color = color.brighter();
		return color;
	}
	
	// Source: https://goo.gl/mQCJ2M
	private static Color averageColor(BufferedImage bi,
			int x0, int y0, int w, int h)
	{
	    int x1 = x0 + w;
	    int y1 = y0 + h;
	    long sumr = 0, sumg = 0, sumb = 0;
	    for (int x = x0; x < x1; x++) {
	        for (int y = y0; y < y1; y++) {
	            Color pixel = new Color(bi.getRGB(x, y));
	            sumr += pixel.getRed();
	            sumg += pixel.getGreen();
	            sumb += pixel.getBlue();
	            pixel = null;
	        }
	    }
	    int num = w * h;
	    return new Color((int)sumr / num, (int)sumg / num, (int)sumb / num);
	}
	
	private BufferedImage getScreenImage()
	{
		return robot.createScreenCapture(getMonitorCaptureArea());
	}
	
	private Color getAverageScreenColor()
	{
		BufferedImage img = getScreenImage();
		Color color = averageColor(img, 0, 0, img.getWidth(), img.getHeight());
		img = null;
		return color;
	}
	
	private Point getMonitorLocation()
	{
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsConfiguration config = gs[parent.getMonitor()].getConfigurations()[0];
		return config.getBounds().getLocation();
	}
	
//	private Point getMonitorLocation()
//	{
//		GraphicsEnvironment ge = GraphicsEnvironment
//				.getLocalGraphicsEnvironment();
//		GraphicsDevice[] gs = ge.getScreenDevices();
//		GraphicsConfiguration config = gs[parent.getMonitor()].getConfigurations()[0];
//		Point bounds = config.getBounds().getLocation();
//		DisplayMode mode = gs[parent.getMonitor()].getDisplayMode();
//		int x = 0, y = 0;
//    	if (bounds.x != 0)
//    	{
//    		x = (bounds.x / Math.abs(bounds.x)) * mode.getWidth();
//    	}
//    	if (bounds.y != 0)
//    	{
//    		y = (bounds.y / Math.abs(bounds.y)) * mode.getHeight();
//    	}
//    	bounds.setLocation(x, y);
//		return bounds;
//	}
	
	private void showMessageBox(String message)
	{
		new TextDialog(parent, message).setVisible(true);
	}
}
