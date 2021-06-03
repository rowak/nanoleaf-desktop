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
import java.util.List;
import java.util.Map;

import javax.swing.Timer;

import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.NanoleafCallback;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StaticEffect;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.panel.AmbilightPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

public class AmbilightHandler {
	
    private int delay, transTime, brightness, mode;
    private boolean running, updating;
    private NanoleafGroup group;
    private Rectangle captureArea;
    private Color currentColor;
    private Color originalColor;
    private Timer timer;
    private Robot robot;
    private AmbilightPanel parent;
    private Map<NanoleafDevice, String> previousEffects;
    private List<Panel> panels;
    private Panel[][] rows;
    private PanelCanvas canvas;

    public AmbilightHandler(NanoleafGroup group,
    		PanelCanvas canvas, AmbilightPanel parent) {
        this.group = group;
        this.canvas = canvas;
        this.parent = parent;
        delay = parent.getUpdateDelay();
        transTime = parent.getTransitionTime();
        brightness = parent.getBrightness();
        captureArea = parent.getCaptureArea();
        mode = parent.getMode();
        previousEffects = new HashMap<NanoleafDevice, String>();

        try {
            robot = new Robot();
        }
        catch (AWTException awte) {
            awte.printStackTrace();
        }
    }

    public void start() {
    	try {
			panels = group.getAllPanelsRotated();
			rows = PanelTableSort.getRows(panels.toArray(new Panel[0]));
		}
    	catch (NanoleafException | IOException e) {
    		e.printStackTrace();
    	}
        saveCurrentEffect();
        startExternalStreaming();
        timer = new Timer(delay, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                	updateDisplay();
                }).start();
            }
        });
        timer.start();
        running = true;
    }

    public void stop() {
        timer.stop();
        loadPreviousEffect();
        running = false;
    }

    private void updateDisplay() {
        if (!updating) {
            updating = true;
            try {
                switch (mode) {
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
            catch (NanoleafException | IOException e) {
                e.printStackTrace();
            }
            updating = false;
        }
    }

    private void applyAverageMode()
            throws NanoleafException, IOException {
        originalColor = getAverageScreenColor();
        originalColor = addAdditionalBrightness(originalColor, brightness);
        if (!sameColor(originalColor, currentColor)) {
        	group.forEach((device) -> {
        		try {
	        		StaticEffect ef = new StaticEffect.Builder(device)
	        					.setAllPanels(new Frame(originalColor.getRed(),
	        							originalColor.getGreen(), originalColor.getBlue(), transTime))
	        					.build(null);
	        		device.sendStaticEffectExternalStreaming(ef);
        		}
        		catch (Exception e) {
        			e.printStackTrace();
        		}
        		currentColor = originalColor;
        	});

            new Thread(() -> {
            	canvas.setColor(currentColor);
            }).start();
        }
    }

    private void applySelectionMode()
            throws NanoleafException, IOException {
    	group.forEach((device) -> {
	        StaticEffect.Builder ef = new StaticEffect.Builder(panels);
	        BufferedImage img = getScreenImage();
	        final int VERTICAL_SEPARATOR = captureArea.height / rows.length;
	        for (int i = 0; i < rows.length; i++) {
	            int captureY = VERTICAL_SEPARATOR * i + VERTICAL_SEPARATOR / 2;
	
	            Map<Panel, Color> colors = new HashMap<Panel, Color>();
	            for (int j = 0; j < rows[i].length; j++) {
	                final int HORIZONTAL_SEPARATOR = captureArea.width / rows[i].length;
	                int captureX = HORIZONTAL_SEPARATOR * j + HORIZONTAL_SEPARATOR / 2;
	
	                try {
	                    if (img.getSubimage(captureX, captureY, 1, 1) != null) {
	                        Color color = new Color(img.getRGB(captureX, captureY));
	                        ef.setPanel(rows[i][j], new Frame(color.getRed(),
	                                                              color.getGreen(), color.getBlue(), transTime));
	                        colors.put(rows[i][j], color);
	                    }
	                }
	                catch (RasterFormatException rfe) {
	                    // catch, but ignore
	                }
	            }
	        }
	        try {
	        	device.sendStaticEffectExternalStreaming(ef.build(null));
	        }
	        catch (Exception e) {
	        	e.printStackTrace();
	        }
    	});
    }

    public boolean isRunning() {
        return running;
    }

    private Rectangle getMonitorCaptureArea() {
        Point monitorLocation = getMonitorLocation();
        int x = monitorLocation.x + captureArea.x;
        int y = monitorLocation.y + captureArea.y;
        return new Rectangle(x, y, captureArea.width, captureArea.height);
    }

    public void setAuroras(NanoleafGroup group) {
        this.group = group;
    }

    public void setUpdateDelay(int delay) {
        this.delay = delay;
        if (timer != null && timer.isRunning()) {
            restartTimer();
        }
    }

    public void setTransitionTime(int transTime) {
        this.transTime = transTime;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public void setCaptureArea(Rectangle captureArea) {
        this.captureArea = captureArea;
        if (timer != null && timer.isRunning()) {
            restartTimer();
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private void restartTimer() {
        timer.stop();
        timer.start();
    }

    private boolean saveCurrentEffect() {
    	for (NanoleafDevice device : group.getDevices().values()) {
    		try {
    			previousEffects.put(device, device.getCurrentEffectName());
    		}
    		catch (NanoleafException | IOException e) {
    			return false;
    		}
    	}
    	return true;
    }

    private void loadPreviousEffect() {
        for (NanoleafDevice device : group.getDevices().values()) {
        	String previous = previousEffects.get(device);
        	if (previous == null || previous.equals("*Dynamic*") || previous.equals("*Solid*")) {
        		continue;
        	}
			device.setEffectAsync(previousEffects.get(device), (status, data, caller) -> {
				if (status != NanoleafCallback.SUCCESS) {
//					showMessageBox("The previous effect could not be loaded.");
				}
			});
    	}
    }

    private void startExternalStreaming() {
    	  try {
    		  group.enableExternalStreaming();
          }
    	  catch (Exception sce) {
              showMessageBox("Failed to start ambient lighting server. " +
                                     "Please reload the application and try again.");
          }
    }

    private boolean sameColor(Color color1, Color color2) {
        if (color1 != null && color2 != null) {
            return color1.getRed() == color2.getRed() &&
                    color1.getGreen() == color2.getGreen() &&
                    color1.getBlue() == color2.getBlue();
        }
        return false;
    }

    private Color addAdditionalBrightness(Color color, int amount) {
        for (int i = 0; i < amount; i++) {
            color = color.brighter();
        }
        return color;
    }

    // Source: https://goo.gl/mQCJ2M
    private static Color averageColor(BufferedImage bi,
    		int x0, int y0, int w, int h) {
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
        return new Color((int) sumr / num, (int) sumg / num, (int) sumb / num);
    }

    private BufferedImage getScreenImage() {
        return robot.createScreenCapture(getMonitorCaptureArea());
    }

    private Color getAverageScreenColor() {
        BufferedImage img = getScreenImage();
        Color color = averageColor(img, 0, 0, img.getWidth(), img.getHeight());
        img = null;
        return color;
    }

    private Point getMonitorLocation() {
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsConfiguration config = gs[parent.getMonitor()].getConfigurations()[0];
        return config.getBounds().getLocation();
    }

    private void showMessageBox(String message) {
        new TextDialog(parent, message).setVisible(true);
    }
}
