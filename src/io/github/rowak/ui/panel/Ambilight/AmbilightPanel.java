package io.github.rowak.ui.panel.Ambilight;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.rowak.Aurora;
import io.github.rowak.Main;
import io.github.rowak.tools.PropertyManager;
import io.github.rowak.ui.button.ModernButton;
import io.github.rowak.ui.button.ModernToggleButton;
import io.github.rowak.ui.combobox.ModernComboBox;
import io.github.rowak.ui.dialog.TextDialog;
import io.github.rowak.ui.panel.panelcanvas.PanelCanvas;
import io.github.rowak.ui.slider.ModernSliderUI;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JSlider;

public class AmbilightPanel extends JPanel
{
	private final int DEFAULT_DELAY = 100; // default delay in milliseconds
	private final int DEFAULT_BRIGHTNESS = 2; // default brightness as an arbitrary coefficient
	private final String[] AMBILIGHT_MODES = {"Mode...", "Average", "Selection"};
	private int delay, brightness, monitor, mode;
	private Rectangle captureArea;
	private Aurora aurora;
	private AmbilightHandler handler;
	
	private JToggleButton btnAmbilightOnOff;
	private JComboBox<String> cmbxMonitor;
	private JComboBox<String> cmbxMode;
	private JSlider updateDelaySlider;
	private JSlider brightnessSlider;
	
	public AmbilightPanel(PanelCanvas canvas)
	{
		init();
		loadUserSettings();
		handler = new AmbilightHandler(aurora, canvas, this);
	}
	
	private void init()
	{
		setBorder(new LineBorder(Color.GRAY, 1, true));
		setBackground(Color.DARK_GRAY);
		setLayout(new MigLayout("", "[][grow][][]", "[][][][][][]"));
		
		JLabel lblAmbilightStatus = new JLabel("Status");
		lblAmbilightStatus.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblAmbilightStatus.setForeground(Color.WHITE);
		add(lblAmbilightStatus, "cell 0 0,gapx 0 15");
		
		btnAmbilightOnOff = new ModernToggleButton("Enable");
		btnAmbilightOnOff.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleAmbilight();
			}
		});
		add(btnAmbilightOnOff, "cell 1 0");
		
		JLabel lblUpdateDelay = new JLabel("Update Delay");
		lblUpdateDelay.setForeground(Color.WHITE);
		lblUpdateDelay.setFont(new Font("Tahoma", Font.PLAIN, 25));
		add(lblUpdateDelay, "cell 0 1,gapx 0 15");
		
		updateDelaySlider = new JSlider();
		updateDelaySlider.setValue(100);
		updateDelaySlider.setMaximum(1000);
		updateDelaySlider.setBackground(Color.DARK_GRAY);
		updateDelaySlider.setUI(new ModernSliderUI(updateDelaySlider,
				Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY));
		updateDelaySlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (!updateDelaySlider.getValueIsAdjusting())
				{
					delay = updateDelaySlider.getValue();
					setProperty("ambilightDelay", delay);
					if (handler != null)
					{
						handler.setUpdateDelay(delay);
					}
				}
			}
		});
		add(updateDelaySlider, "cell 1 1 3 1,growx");
		
		JLabel lblBrightness = new JLabel("Brightness");
		lblBrightness.setForeground(Color.WHITE);
		lblBrightness.setFont(new Font("Tahoma", Font.PLAIN, 25));
		add(lblBrightness, "cell 0 2");
		
		brightnessSlider = new JSlider();
		brightnessSlider.setValue(2);
		brightnessSlider.setMaximum(5);
		brightnessSlider.setBackground(Color.DARK_GRAY);
		brightnessSlider.setUI(new ModernSliderUI(brightnessSlider,
				Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY));
		brightnessSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (brightnessSlider.getValueIsAdjusting())
				{
					brightness = brightnessSlider.getValue();
					setProperty("ambilightBrightness", brightness);
					if (handler != null)
					{
						handler.setBrightness(brightness);
					}
				}
			}
		});
		add(brightnessSlider, "cell 1 2 3 1,growx");
		
		JLabel lblCaptureArea = new JLabel("Capture Area");
		lblCaptureArea.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblCaptureArea.setForeground(Color.WHITE);
		add(lblCaptureArea, "cell 0 3");
		
		String[] monitors = getMonitors();
		cmbxMonitor = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(monitors));
		cmbxMonitor.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setMonitor(cmbxMonitor.getSelectedIndex()-1);
			}
		});
		add(cmbxMonitor, "flowx,cell 1 3,growx");
		
		JButton btnSetArea = new ModernButton("Set Area");
		btnSetArea.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new CaptureAreaWindow(monitor, AmbilightPanel.this);
			}
		});
		add(btnSetArea, "cell 2 3");
		
		JButton btnResetArea = new ModernButton("Reset Area");
		btnResetArea.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				resetCaptureArea();
			}
		});
		add(btnResetArea, "cell 3 3");
		
		JLabel lblMode = new JLabel("Mode");
		lblMode.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblMode.setForeground(Color.WHITE);
		add(lblMode, "cell 0 4,gapx 0 15");
		
		cmbxMode = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(AMBILIGHT_MODES));
		cmbxMode.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setMode(cmbxMode.getSelectedIndex()-1);
			}
		});
		add(cmbxMode, "cell 1 4,growx");
	}
	
	public int getUpdateDelay()
	{
		return delay;
	}
	
	public int getBrightness()
	{
		return brightness;
	}
	
	public int getMonitor()
	{
		return monitor;
	}
	
	public Rectangle getCaptureArea()
	{
		return captureArea;
	}
	
	public int getMode()
	{
		return mode;
	}
	
	private void resetCaptureArea()
	{
		Rectangle defaultSelection = getDefaultSelectionArea();
		setProperty("ambilightSelection", defaultSelection.x + " " +
				defaultSelection.y + " " + defaultSelection.width + " " +
				defaultSelection.height);
		setCaptureArea(defaultSelection);
		new TextDialog(AmbilightPanel.this, "Capture area reset.").setVisible(true);
	}
	
	private void loadUserSettings()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		
		String lastSelection = manager.getProperty("ambilightSelection");
		if (lastSelection != null)
		{
			String[] data = manager.getProperty("ambilightSelection").split(" ");
			if (data.length == 4)
			{
				try
				{
					int x = Integer.parseInt(data[0]);
					int y = Integer.parseInt(data[1]);
					int width = Integer.parseInt(data[2]);
					int height = Integer.parseInt(data[3]);
					captureArea = new Rectangle(x, y, width, height);
				}
				catch (NumberFormatException nfe)
				{
					captureArea = null;
				}
			}
		}
		
		String lastMonitor = manager.getProperty("ambilightMonitor");
		if (lastMonitor != null)
		{
			try
			{
				monitor = Integer.parseInt(lastMonitor);
				cmbxMonitor.setSelectedIndex(monitor+1);
			}
			catch (NumberFormatException nfe)
			{
				monitor = -1;
			}
		}
		
		String lastDelay = manager.getProperty("ambilightDelay");
		if (lastMonitor != null)
		{
			try
			{
				delay = Integer.parseInt(lastDelay);
				updateDelaySlider.setValue(delay);
			}
			catch (NumberFormatException nfe)
			{
				delay = DEFAULT_DELAY;
			}
		}
		
		String lastBrightness = manager.getProperty("ambilightBrightness");
		if (lastBrightness != null)
		{
			try
			{
				brightness = Integer.parseInt(lastBrightness);
				brightnessSlider.setValue(brightness);
			}
			catch (NumberFormatException nfe)
			{
				brightness = DEFAULT_BRIGHTNESS;
			}
		}
		
		String lastMode = manager.getProperty("ambilightMode");
		if (lastMode != null)
		{
			try
			{
				mode = Integer.parseInt(lastMode);
				cmbxMode.setSelectedIndex(mode+1);
			}
			catch (NumberFormatException nfe)
			{
				mode = -1;
			}
		}
	}
	
	public void setAurora(Aurora aurora)
	{
		this.aurora = aurora;
		handler.setAurora(aurora);
	}
	
	private boolean allFieldsSet()
	{
		return monitor != -1 && captureArea != null && mode != -1;
	}
	
	private void toggleAmbilight()
	{
		if (btnAmbilightOnOff.getText().equals("Enable"))
		{
			if (allFieldsSet())
			{
				handler.start();
				btnAmbilightOnOff.setText("Disable");
			}
			else
			{
				showFieldsNotSetDialog();
			}
		}
		else if (btnAmbilightOnOff.getText().equals("Disable"))
		{
			handler.stop();
			btnAmbilightOnOff.setText("Enable");
		}
	}
	
	private Rectangle getDefaultSelectionArea()
	{
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsConfiguration config = gs[monitor].getConfigurations()[0];
		return new Rectangle(config.getBounds().width/2 - 400,
				config.getBounds().height/2 - 200, 800, 400);
	}
	
	public void setCaptureArea(Rectangle captureArea)
	{
		this.captureArea = captureArea;
		handler.setCaptureArea(captureArea);
	}
	
	private void setMonitor(int monitor)
	{
		this.monitor = monitor;
		setProperty("ambilightMonitor", monitor);
	}
	
	private void setMode(int mode)
	{
		this.mode = mode;
		setProperty("ambilightMode", mode);
		if (handler != null)
		{
			handler.setMode(mode);
		}
	}
	
	private void setProperty(String key, Object value)
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		manager.setProperty(key, value);
	}
	
	private void showFieldsNotSetDialog()
	{
		String message = "Please set all fields before enabling ambient lighting.";
		new TextDialog(this, message).setVisible(true);
	}
	
	private String[] getMonitors()
	{
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		String[] monitors = new String[gs.length+1];
		monitors[0] = "Monitor...";
		monitors[1] = "Default";
		for (int i = 0; i < gs.length - 1; i++)
		{
			monitors[i+2] = "Display " + (i+2);
		}
		return monitors;
	}
}
