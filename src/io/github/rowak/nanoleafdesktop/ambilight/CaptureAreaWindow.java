package io.github.rowak.nanoleafdesktop.ambilight;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.panel.AmbilightPanel;

public class CaptureAreaWindow extends JFrame
{
	private int monitor;
	private Rectangle maxArea;
	private Rectangle selectedArea;
	private AmbilightPanel parent;
	
	public CaptureAreaWindow(int monitor, AmbilightPanel parent)
	{
		this.monitor = monitor;
		this.parent = parent;
		loadSettings();
		
		initUI();
		
		showInfoMessage();
	}
	
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		drawSelectionBox(g);
	}
	
	private void drawSelectionBox(Graphics g)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(5));
		g.setColor(Color.RED);
		g.drawRect(selectedArea.x, selectedArea.y,
				selectedArea.width, selectedArea.height);
		g2d.setStroke(new BasicStroke(1));
	}
	
	private Rectangle getMaxCaptureArea()
	{
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsConfiguration config = gs[monitor].getConfigurations()[0];
		return config.getBounds();
	}
	
	private Rectangle getDefaultSelectionArea()
	{
		maxArea = getMaxCaptureArea();
		return new Rectangle(maxArea.width/2 - 400, maxArea.height/2 - 200, 800, 400);
	}
	
	private void loadSettings()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		String lastSelection = manager.getProperty("ambilightSelection");
		
		Rectangle defaultSelection = getDefaultSelectionArea();
		
		if (lastSelection != null)
		{
			String[] data = manager.getProperty("ambilightSelection").split(" ");
			if (data.length == 4)
			{
				int x = Integer.parseInt(data[0]);
				int y = Integer.parseInt(data[1]);
				int width = Integer.parseInt(data[2]);
				int height = Integer.parseInt(data[3]);
				selectedArea = new Rectangle(x, y, width, height);
				return;
			}
		}
		selectedArea = defaultSelection;
	}
	
	private void saveChanges()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		String selection = selectedArea.x + " " + selectedArea.y + " " +
				selectedArea.width + " " + selectedArea.height;
		manager.setProperty("ambilightSelection", selection);
		parent.setCaptureArea(selectedArea);
	}
	
	private void showInfoMessage()
	{
		EventQueue.invokeLater(() ->
		{
			String message = "You can move around and resize the red box to change your selection. " +
					"Press the escape key when you are done.";
			new TextDialog(this, message).setVisible(true);
		});
	}
	
	private void initUI()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(maxArea.x, maxArea.y, maxArea.width, maxArea.height);
		setUndecorated(true);
		setBackground(new Color(255, 255, 255, 1));
		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyChar() == KeyEvent.VK_ESCAPE)
				{
					saveChanges();
					dispose();
				}
			}
		});
		BoxDragListener bdl = new BoxDragListener(this);
		addMouseListener(bdl);
		addMouseMotionListener(bdl);
		setVisible(true);
	}
	
	private class BoxDragListener extends MouseAdapter
	{
		private boolean inArea;
		private int edge;
		private Point mouseLast;
		private Point lastLocation;
		private Dimension lastSize;
		private CaptureAreaWindow window;
		
		public BoxDragListener(CaptureAreaWindow window)
		{
			this.window = window;
			lastLocation = new Point(selectedArea.x, selectedArea.y);
			lastSize = new Dimension(selectedArea.width, selectedArea.height);
		}
		
		private void moveBox(Point mouse)
		{
			int xdiff = mouse.x - mouseLast.x;
			int ydiff = mouse.y - mouseLast.y;
			selectedArea.setLocation(lastLocation.x + xdiff,
					lastLocation.y + ydiff);
			repaint();
		}
		
		private void resizeBox(Point mouse, Dimension direction)
		{
			int xdiff = mouse.x - mouseLast.x;
			int ydiff = mouse.y - mouseLast.y;
			int xdirection = direction.width;
			int ydirection = direction.height;
			
			selectedArea.setSize(lastSize.width + xdiff*xdirection,
					lastSize.height + ydiff*ydirection);
			
			if (xdirection == -1 || ydirection == -1)
			{
				selectedArea.setLocation(lastLocation.x - xdiff * xdirection,
						lastLocation.y - ydiff * ydirection);
			}
			repaint();
		}
		
		private Dimension getResizeXY(int edge)
		{
			switch (edge)
			{
				case 0:
					return new Dimension(0, -1);
				case 1:
					return new Dimension(1, 0);
				case 2:
					return new Dimension(0, 1);
				case 3:
					return new Dimension(-1, 0);
				default:
					return new Dimension(0, 0);
			}
		}
		
		private int getEdge(Point mouse)
		{
			int leftEdge = selectedArea.x;
			int topEdge = selectedArea.y;
			int rightEdge = selectedArea.x + selectedArea.width;
			int bottomEdge = selectedArea.y + selectedArea.height;
			
			// check top edge
			if (mouse.x >= leftEdge && mouse.x <= rightEdge &&
					 mouse.y >= topEdge - 5 && mouse.y <= topEdge + 5)
			{
				window.setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
				return 0;
			}
			// check right edge
			else if (mouse.x >= rightEdge - 5 && mouse.x <= rightEdge + 5 &&
					 mouse.y >= topEdge && mouse.y <= bottomEdge)
			{
				window.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				return 1;
			}
			// check bottom edge
			else if (mouse.x >= leftEdge && mouse.x <= rightEdge &&
					 mouse.y >= bottomEdge - 5 && mouse.y <= bottomEdge + 5)
			{
				window.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
				return 2;
			}
			// check left edge
			else if (mouse.x >= leftEdge - 5 && mouse.x <= leftEdge + 5 &&
				mouse.y >= topEdge && mouse.y <= bottomEdge)
			{
				window.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
				return 3;
			}
			return -1;
		}
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			mouseLast = e.getPoint();
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			mouseLast = null;
			inArea = false;
			edge = -1;
			lastLocation = new Point(selectedArea.x, selectedArea.y);
			lastSize = new Dimension(selectedArea.width, selectedArea.height);
			window.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e))
			{
				if (edge != -1)
				{
					resizeBox(e.getPoint(), getResizeXY(edge));
				}
				else if (inArea)
				{
					moveBox(e.getPoint());
				}
			}
		}
		
		@Override
		public void mouseMoved(MouseEvent e)
		{
			edge = getEdge(e.getPoint());
			if (selectedArea.contains(e.getPoint()) && edge == -1)
			{
				window.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				inArea = true;
			}
		}
	}
}
