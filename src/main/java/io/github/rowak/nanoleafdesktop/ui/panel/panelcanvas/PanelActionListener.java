package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Canvas;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.Shapes;

public class PanelActionListener extends MouseAdapter {
	private int lastXDiff;
	private Map<NanoleafDevice, List<Panel>> panels, tempPanels;
	private NanoleafDevice selectedDevice;
	private Point mouseLast;
	private PanelCanvas canvas;
	
	public PanelActionListener(PanelCanvas canvas,
			Map<NanoleafDevice, List<Panel>> panels) {
		this.canvas = canvas;
		this.panels = panels;
		tempPanels = clonePanels(panels);
	}
	
	public Map<NanoleafDevice, List<Panel>> getTempPanels() {
		return tempPanels;
	}
	
	public Map<NanoleafDevice, List<Panel>> getPanels() {
		return panels;
	}
	
	private Map<NanoleafDevice, List<Panel>> clonePanels(Map<NanoleafDevice, List<Panel>> original) {
		Map<NanoleafDevice, List<Panel>> tempMap = new HashMap<NanoleafDevice, List<Panel>>();
		for (NanoleafDevice device : original.keySet()) {
			List<Panel> list = original.get(device); 
			List<Panel> tempList = new ArrayList<Panel>();
			for (Panel p : list) {
				tempList.add(new Panel(p.getId(), p.getX(), p.getY(), p.getOrientation(), p.getShape()));
			}
			tempMap.put(device, tempList);
		}
		return tempMap;
	}
	
	private void movePanelsUsingMouse(Point mouse) {
		if (selectedDevice != null) {
			// ******* "Snappy" layouts DISABLED **********
//			int xdiff = roundToNearest(mouse.x - mouseLast.x, 150f/2f);
//			int ydiff = roundToNearest(mouse.y - mouseLast.y, 130f/2f);
			int xdiff = mouse.x - mouseLast.x;
			int ydiff = mouse.y - mouseLast.y;
			
			Map<NanoleafDevice, Point> offset = canvas.getPanelOffset();
			offset.get(selectedDevice).setLocation(xdiff, ydiff);
			
			canvas.repaint();
		}
	}
	
	private void rotatePanelsUsingMouse(Point mouse) {
		if (selectedDevice != null) {
			int xdiff = roundToNearest((mouse.x - mouseLast.x)/3, 10);
			int rotation = canvas.getTempRotation(selectedDevice) + xdiff - lastXDiff;
			canvas.setTempRotation(rotation, selectedDevice);
			lastXDiff = xdiff;
			canvas.repaint();
		}
	}
	
	private void scalePanelsUsingMouse(int rotationdiff) {
		float scaleFactor = canvas.getScaleFactor();
		scaleFactor += rotationdiff * 0.05f;
		if (scaleFactor > 0) {
			canvas.setScaleFactor(scaleFactor);
			canvas.repaint();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (mouseLast == null) {
			mouseLast = e.getPoint();
			canvas.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			selectedDevice = getSelectedDevice(e.getPoint());
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (lastXDiff != 0) {
			canvas.setRotation(canvas.getTempRotation(
					selectedDevice), selectedDevice);
		}
		
		mouseLast = null;
		lastXDiff = 0;
		tempPanels = clonePanels(panels);
		if (selectedDevice != null) {
			Map<NanoleafDevice, Point> offset = canvas.getPanelOffset();
			List<Panel> localTempPanels = tempPanels.get(selectedDevice);
			Point localOffset = offset.get(selectedDevice);
			for (Panel p : localTempPanels) {
				p.setX(p.getX() + localOffset.x);
				p.setY(p.getY() + localOffset.y);
			}
			offset.put(selectedDevice, new Point(0, 0));
			selectedDevice = null;
		}
			
		panels = clonePanels(tempPanels);
		canvas.setPanels(panels);
		canvas.checkAuroraState(selectedDevice);
		canvas.repaint();
		canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (mouseLast != null) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				movePanelsUsingMouse(e.getPoint());
			}
			else if (SwingUtilities.isRightMouseButton(e)) {
				rotatePanelsUsingMouse(e.getPoint());
			}
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		scalePanelsUsingMouse(e.getWheelRotation());
	}
	
	private NanoleafDevice getSelectedDevice(Point mouse) {
		for (NanoleafDevice device : tempPanels.keySet()) {
			for (Panel p : tempPanels.get(device)) {
				PanelShape shape = null;
				if (device instanceof Aurora) {
					int o = p.getOrientation();
					if (o == 0 || Math.abs(o) % 120 == 0) {
						shape = new UprightPanel(p.getX(),
								p.getY(), canvas.getRotation(device));
					}
					else {
						shape = new InvertedPanel(p.getX(),
								p.getY(), canvas.getRotation(device));
					}
				}
				else if (device instanceof Canvas) {
					shape = new SquarePanel(p.getX(),
							p.getY(), canvas.getRotation(device));
				}
				else if (device instanceof Shapes) {
					shape = new HexagonPanel(p.getX(),
							p.getY(), canvas.getRotation(device));
				}
				
				if (shape != null && shape.contains(mouse)) {
					return device;
				}
			}
		}
		return null;
	}
	
	private int roundToNearest(int num, float factor) {
		return (int)(Math.ceil(num / (float)factor) * factor);
	}
}
