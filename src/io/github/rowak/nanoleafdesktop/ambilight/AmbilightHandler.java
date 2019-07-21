package io.github.rowak.nanoleafdesktop.ambilight;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Timer;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafapi.effectbuilder.CustomEffectBuilder;
import io.github.rowak.nanoleafdesktop.tools.CanvasAnimDataBuilder;
import io.github.rowak.nanoleafdesktop.tools.CanvasExtStreaming;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.panel.AmbilightPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

public class AmbilightHandler
{
	private int delay, transTime, brightness, mode;
	private boolean running, updating;
	private String previousEffect;
	private Aurora[] auroras;
	private Rectangle captureArea;
	private Color currentColor;
	private Timer timer;
	private Robot robot;
	private AmbilightPanel parent;
	private Panel[][] panels;
	private Panel[][][] rows;
	private PanelCanvas canvas;
	
	public AmbilightHandler(Aurora[] auroras,
			PanelCanvas canvas, AmbilightPanel parent)
	{
		this.auroras = auroras;
		this.canvas = canvas;
		this.parent = parent;
		delay = parent.getUpdateDelay();
		transTime = parent.getTransitionTime();
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
		panels = new Panel[auroras.length][];
		panels[0] = canvas.getGroupPanels();
		rows = new Panel[auroras.length][][];
		rows[0] = PanelTableSort.getRows(panels[0]);
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
			for (Aurora aurora : auroras)
			{
				String deviceType = getDeviceType(aurora);
				if (deviceType.equals("aurora"))
				{
					Effect ef = new CustomEffectBuilder(aurora)
							.addFrameToAllPanels(new Frame(originalColor.getRed(),
									originalColor.getGreen(), originalColor.getBlue(), 0, transTime))
							.build("", false);
					aurora.externalStreaming().sendStaticEffect(ef);
					currentColor = originalColor;
					ef = null;
				}
				else if (deviceType.equals("canvas"))
				{
					String animData = new CanvasAnimDataBuilder(aurora)
							.addFrameToAllPanels(new Frame(originalColor.getRed(),
									originalColor.getGreen(), originalColor.getBlue(), 0, transTime))
							.build();
					CanvasExtStreaming.sendAnimData(animData, aurora);
					currentColor = originalColor;
				}
			}
			
			new Thread(() ->
			{
				canvas.setColor(currentColor);
			}).start();
		}
	}
	
	private void applySelectionMode()
			throws StatusCodeException, IOException
	{
		CustomEffectBuilder ceb = new CustomEffectBuilder(panels[0]);
		CanvasAnimDataBuilder cadb = new CanvasAnimDataBuilder(panels[0]);
		BufferedImage img = getScreenImage();
		final int VERTICAL_SEPARATOR = captureArea.height/rows[0].length;
		for (int i = 0; i < rows[0].length; i++)
		{
			int captureY = VERTICAL_SEPARATOR*i + VERTICAL_SEPARATOR/2;
			
			Map<Panel, Color> colors = new HashMap<Panel, Color>();
			for (int j = 0; j < rows[0][i].length; j++)
			{
				final int HORIZONTAL_SEPARATOR = captureArea.width/rows[0][i].length;
				int captureX = HORIZONTAL_SEPARATOR*j + HORIZONTAL_SEPARATOR/2;
				
				try
				{
					if (img.getSubimage(captureX, captureY, 1, 1) != null)
					{
						Color color = new Color(img.getRGB(captureX, captureY));
						ceb.addFrame(rows[0][i][j], new Frame(color.getRed(),
								color.getGreen(), color.getBlue(), 0, transTime));
						cadb.addFrame(rows[0][i][j], new Frame(color.getRed(),
								color.getGreen(), color.getBlue(), 0, transTime));
						colors.put(rows[0][i][j], color);
					}
				}
				catch (RasterFormatException rfe)
				{
					// catch, but ignore
				}
			}
		}
		for (Aurora aurora : auroras)
		{
			String deviceType = getDeviceType(aurora);
			if (deviceType.equals("aurora"))
			{
				aurora.externalStreaming().sendStaticEffect(ceb.build("", false));
			}
			else if (deviceType.equals("canvas"))
			{
				CanvasExtStreaming.sendAnimData(cadb.build(), aurora);
			}
		}
	}
	
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
	
	private Panel[] getSortedPanels(Aurora aurora) 
	{
		Panel[] panels = null;
		try
		{
			panels = aurora.panelLayout().getPanelsRotated();
			PanelTableSort.sortPanelsAsRows(panels);
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
		return panels;
	}
	
	public void setAuroras(Aurora[] auroras)
	{
		this.auroras = auroras;
	}
	
	public void setUpdateDelay(int delay)
	{
		this.delay = delay;
		if (timer != null && timer.isRunning())
		{
			restartTimer();
		}
	}
	
	public void setTransitionTime(int transTime)
	{
		this.transTime = transTime;
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
			previousEffect = auroras[0].effects().getCurrentEffectName();
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
			auroras[0].effects().setEffect(previousEffect);
		}
		catch (StatusCodeException | NullPointerException scenpe)
		{
			showMessageBox("The previous effect could not be loaded.");
		}
	}
	
	private void startExternalStreaming()
	{
		for (Aurora aurora : auroras)
		{
			String deviceType = getDeviceType(aurora);
			if (deviceType.equals("aurora"))
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
			else if (deviceType.equals("canvas"))
			{
				try
				{
					CanvasExtStreaming.enable(aurora);
				}
				catch (StatusCodeException sce)
				{
					showMessageBox("Failed to start ambient lighting server. " +
							"Please reload the application and try again.");
				}
			}
			else
			{
				showMessageBox("Your device is not supported by this feature " +
						"or it has not been recognized correctly.");
			}
		}
	}
	
	private String getDeviceType(Aurora aurora)
	{
		if (aurora.getName().toLowerCase().contains("light panels") ||
				aurora.getName().toLowerCase().contains("aurora"))
		{
			return "aurora";
		}
		else if (aurora.getName().toLowerCase().contains("canvas"))
		{
			return "canvas";
		}
		return null;
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
	    for (int x = x0; x < x1; x++)
	    {
	        for (int y = y0; y < y1; y++)
	        {
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
	
	private void showMessageBox(String message)
	{
		new TextDialog(parent, message).setVisible(true);
	}
}
