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
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Timer;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import io.github.rowak.Aurora;
import io.github.rowak.Effect;
import io.github.rowak.Frame;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.effectbuilder.CustomEffectBuilder;
import io.github.rowak.nanoleafdesktop.tools.CanvasTempAnimDataBuilder;
import io.github.rowak.nanoleafdesktop.tools.CanvasTempExtStreaming;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.panel.AmbilightPanel;
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
			String deviceType = getDeviceType();
			if (deviceType.equals("aurora"))
			{
				Effect ef = new CustomEffectBuilder(aurora)
						.addFrameToAllPanels(new Frame(originalColor.getRed(),
								originalColor.getGreen(), originalColor.getBlue(), 0, 5))
						.build("", false);
				aurora.externalStreaming().sendStaticEffect(ef);
				currentColor = originalColor;
				ef = null;
			}
			else if (deviceType.equals("canvas"))
			{
				String animData = new CanvasTempAnimDataBuilder(aurora)
						.addFrameToAllPanels(new Frame(originalColor.getRed(),
								originalColor.getGreen(), originalColor.getBlue(), 0, 5))
						.build();
				CanvasTempExtStreaming.sendAnimData(animData, aurora);
				currentColor = originalColor;
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
		String deviceType = getDeviceType();
		if (deviceType.equals("aurora"))
		{
			CustomEffectBuilder ceb = new CustomEffectBuilder(aurora);
			BufferedImage img = getScreenImage();
			final int VERTICAL_SEPARATOR = captureArea.height/rows.length;
			for (int i = 0; i < rows.length; i++)
			{
				int captureY = VERTICAL_SEPARATOR*i + VERTICAL_SEPARATOR/2;
				
				Map<Panel, Color> colors = new HashMap<Panel, Color>();
				for (int j = 0; j < rows[i].length; j++)
				{
					final int HORIZONTAL_SEPARATOR = captureArea.width/rows[i].length;
					int captureX = HORIZONTAL_SEPARATOR*j + HORIZONTAL_SEPARATOR/2;
					
					try
					{
						if (img.getSubimage(captureX, captureY, 1, 1) != null)
						{
							Color color = new Color(img.getRGB(captureX, captureY));
							ceb.addFrame(rows[i][j], new Frame(color.getRed(),
									color.getGreen(), color.getBlue(), 0, 5));
							colors.put(rows[i][j], color);
						}
					}
					catch (RasterFormatException rfe)
					{
						// catch, but ignore
					}
				}
			}
			aurora.externalStreaming().sendStaticEffect(ceb.build("", false));
		}
		else if (deviceType.equals("canvas"))
		{
			CanvasTempAnimDataBuilder builder = new CanvasTempAnimDataBuilder(aurora);
			BufferedImage img = getScreenImage();
			final int VERTICAL_SEPARATOR = captureArea.height/rows.length;
			for (int i = 0; i < rows.length; i++)
			{
				int captureY = VERTICAL_SEPARATOR*i + VERTICAL_SEPARATOR/2;
				
				Map<Panel, Color> colors = new HashMap<Panel, Color>();
				for (int j = 0; j < rows[i].length; j++)
				{
					final int HORIZONTAL_SEPARATOR = captureArea.width/rows[i].length;
					int captureX = HORIZONTAL_SEPARATOR*j + HORIZONTAL_SEPARATOR/2;
					
					try
					{
						if (img.getSubimage(captureX, captureY, 1, 1) != null)
						{
							Color color = new Color(img.getRGB(captureX, captureY));
							builder.addFrame(rows[i][j], new Frame(color.getRed(),
									color.getGreen(), color.getBlue(), 0, 5));
							colors.put(rows[i][j], color);
						}
					}
					catch (RasterFormatException rfe)
					{
						// catch, but ignore
					}
				}
			}
			CanvasTempExtStreaming.sendAnimData(builder.build(), aurora);
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
	
	private Panel[] getSortedPanels() 
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
		String deviceType = getDeviceType();
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
				CanvasTempExtStreaming.enable(aurora);
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
	
	private String getDeviceType()
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
	
	private void showMessageBox(String message)
	{
		new TextDialog(parent, message).setVisible(true);
	}
}
