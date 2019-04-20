package io.github.rowak.nanoleafdesktop;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.border.LineBorder;

import io.github.rowak.Aurora;
import io.github.rowak.Effect;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.tools.UpdateManager;
import io.github.rowak.nanoleafdesktop.tools.Version;
import io.github.rowak.nanoleafdesktop.ui.button.*;
import io.github.rowak.nanoleafdesktop.ui.dialog.AuroraFinder;
import io.github.rowak.nanoleafdesktop.ui.dialog.OptionDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.SingleEntryDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.listener.*;
import io.github.rowak.nanoleafdesktop.ui.panel.AmbilightPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.DiscoveryPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.InformationPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.KeyShortcutsPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.EffectsPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.SpotifyPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.InsetsUIResource;

import org.json.JSONObject;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import java.awt.Component;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.JButton;

public class Main extends JFrame
{
	public static final Version VERSION = new Version("v0.7.0", true);
	public static final String VERSION_HOST =
			"https://api.github.com/repos/rowak/nanoleaf-desktop/releases";
	public static final String GIT_REPO = "https://github.com/rowak/nanoleaf-desktop";
	public static final String OLD_PROPERTIES_FILEPATH =
			System.getProperty("user.home") + "/properties.txt";
	public static final String PROPERTIES_FILEPATH = getPropertiesFilePath();
	
	private final int DEFAULT_WINDOW_WIDTH = 1050;
	private final int DEFAULT_WINDOW_HEIGHT = 800;
	
	private Aurora device;
	
	private JPanel contentPane;
	private PanelCanvas canvas;
	private InformationPanel infoPanel;
	private DiscoveryPanel discoveryPanel;
	private AmbilightPanel ambilightPanel;
	private JLabel lblTitle;
	private EffectsPanel regEffectsPanel;
	private EffectsPanel rhythEffectsPanel;
	
	public Main()
	{
		migrateOldProperties();
		
		PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
		String lastSession = manager.getProperty("lastSession");
		
		// Use the device from the last session
		if (lastSession != null)
		{
			setupOldAurora(lastSession);
		}
		
		initUI();
		
		// Search for a a new device
		if (lastSession == null)
		{
			setupNewAurora();
		}
		
		checkForUpdate();
	}
	
	public static String getPropertiesFilePath()
	{
		String dir = "";
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win"))
		{
			dir = System.getenv("APPDATA") + "/Nanoleaf for Desktop";
		}
		else if (os.contains("mac"))
		{
			dir = System.getProperty("user.home") +
					"/Library/Application Support/Nanoleaf for Desktop";
		}
		else if (os.contains("nux"))
		{
			dir = System.getProperty("user.home") + "/.Nanoleaf for Desktop";
		}
		
		File dirFile = new File(dir);
		if (!dirFile.exists())
		{
			dirFile.mkdir();
		}
		
		return dir + "/preferences.txt";
	}
	
	private void migrateOldProperties()
	{
		File oldProperties = new File(OLD_PROPERTIES_FILEPATH);
		if (oldProperties.exists())
		{
			BufferedReader reader = null;
			BufferedWriter writer = null;
			try
			{
				reader = new BufferedReader(new FileReader(OLD_PROPERTIES_FILEPATH));
				writer = new BufferedWriter(new FileWriter(getPropertiesFilePath()));
				String data = "";
				String line = "";
				while ((line = reader.readLine()) != null)
				{
					data += line + "\n";
				}
				writer.write(data);
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			finally
			{
				try
				{
					if (reader != null)
					{
						reader.close();
					}
					if (writer != null)
					{
						writer.close();
					}
					oldProperties.renameTo(new File(
							System.getProperty("user.home") +
							"/propertiesOLD.txt"));
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		}
	}
	
	private void checkForUpdate()
	{
		new Thread(() ->
		{
			try
			{
				UpdateManager manager = new UpdateManager(VERSION_HOST, GIT_REPO);
				if (manager.updateAvailable(VERSION))
				{
					manager.showUpdateMessage(this);
				}
			}
			catch (HttpRequestException hre)
			{
				/*
				 * If the update server cannot be reached, ignore it (don't notify the user).
				 * The user will be notified about an update the next time they
				 * connect to the network and open the application.
				 */
			}
		}).start();
	}
	
	private int getUserWindowWidth()
	{
		PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
		String width = manager.getProperty("windowWidth");
		if (width != null)
		{
			return Integer.parseInt(width);
		}
		return DEFAULT_WINDOW_WIDTH;
	}
	
	private int getUserWindowHeight()
	{
		PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
		String height = manager.getProperty("windowHeight");
		if (height != null)
		{
			return Integer.parseInt(height);
		}
		return DEFAULT_WINDOW_HEIGHT;
	}
	
	public PanelCanvas getCanvas()
	{
		return this.canvas;
	}
	
	public void setDevice(Aurora device)
	{
		this.device = device;
		PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
		manager.setProperty("lastSession",
				device.getHostName() + " " +
				device.getPort() + " v1 " +
				device.getAccessToken());
		loadAuroraData();
		loadDeviceName();
		canvas.setAurora(device);
		canvas.initCanvas();
		discoveryPanel.setAurora(device);
		ambilightPanel.setAurora(device);
	}
	
	public void loadEffects() throws StatusCodeException
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				new Thread(() ->
				{
					try
					{
						for (Effect effect : device.effects().getAllEffects())
						{
							if (effect.getAnimType() == Effect.Type.PLUGIN &&
									effect.getPluginType().equals("rhythm"))
							{
								rhythEffectsPanel.addEffect(effect.getName());
							}
							else
							{
								regEffectsPanel.addEffect(effect.getName());
							}
						}
					}
					catch (StatusCodeException sce)
					{
						sce.printStackTrace();
					}
					
					if (regEffectsPanel.getModel().size() > 0)
					{
						regEffectsPanel.setViewportView(regEffectsPanel.getList());
					}
					if (rhythEffectsPanel.getModel().size() > 0)
					{
						rhythEffectsPanel.setViewportView(rhythEffectsPanel.getList());
					}
				}).start();
			}
		});
	}
	
	public void loadStateComponents() throws StatusCodeException
	{
		if (device.state().getOn())
		{
			infoPanel.getBtnOnOff().setText("Turn Off");
		}
		else
		{
			infoPanel.getBtnOnOff().setText("Turn On");
		}
		
		infoPanel.setSliderBrightness(device.state().getBrightness());
		infoPanel.setSliderColorTemp(device.state().getColorTemperature());
		
		loadActiveScene();
	}
	
	public void loadActiveScene() throws StatusCodeException
	{
		String currentEffect = device.effects().getCurrentEffectName();
		infoPanel.setScene(currentEffect);
	}
	
	private void loadAuroraData()
	{
		try
		{
			loadEffects();
			loadStateComponents();
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
	}
	
	private void setupOldAurora(String lastSession)
	{
		String[] data = lastSession.split(" ");
		try
		{
			device = new Aurora(data[0],
					Integer.parseInt(data[1]),
					data[2], data[3]);
			EventQueue.invokeLater(() ->
			{
				loadDeviceName();
			});
		}
		catch (StatusCodeException | HttpRequestException schre)
		{
			new TextDialog(Main.this, "Failed to connect to the device. " +
					"Please try again.").setVisible(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			OptionDialog errorDialog = new OptionDialog(this,
					"The data file has been modified or has become corrupt. " +
					"Would you like to fix this now?", "Yes", "No",
					new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							new PropertyManager(PROPERTIES_FILEPATH)
								.removeProperty("lastSession");
							OptionDialog dialog = (OptionDialog)((JButton)e.getSource())
										.getTopLevelAncestor();
							dialog.dispose();
							new TextDialog(Main.this,
									"Relaunch the application to setup a new device.")
									.setVisible(true);
						}
					},
					new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							OptionDialog dialog = (OptionDialog)((JButton)e.getSource())
									.getTopLevelAncestor();
							dialog.dispose();
						}
					});
			errorDialog.setVisible(true);
			EventQueue.invokeLater(() ->
			{
				errorDialog.requestFocus();
			});
		}
	}
	
	private void setupNewAurora()
	{
		new Thread(() ->
		{
			AuroraFinder finder = new AuroraFinder(Main.this);
			finder.setVisible(true);
			new Timer().scheduleAtFixedRate(new TimerTask()
			{
				public void run()
				{
					if (finder.getAccessToken() != null)
					{
						try
						{
							device = new Aurora(finder.getHostName(),
									finder.getPort(), "v1", finder.getAccessToken());
							this.cancel();
							
							PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
							manager.setProperty("lastSession",
									device.getHostName() + " " +
									device.getPort() + " v1 " +
									device.getAccessToken());
							lblTitle.setText("Connected to " + device.getName());
							Map<String, Object> devices = getDevices();
							if (devices.containsKey(device.getHostName()))
							{
								loadDeviceName();
							}
							else
							{
								setupDeviceName("Would you like to give this device a name?");
							}
							loadAuroraData();
						}
						catch (StatusCodeException | HttpRequestException schre)
						{
							new TextDialog(Main.this,
									"An error occurred while connecting to the Aurora." +
									"Please try again.").setVisible(true);
						}
						canvas.setAurora(device);
					}
				}
			}, 0, 1000);
		}).start();
	}
	
	public void setupDeviceName(String message)
	{
		OptionDialog nameDeviceDialog = new OptionDialog(Main.this,
				message, "Yes", "No", new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						JButton okButton = (JButton)e.getSource();
						OptionDialog optionDialog =
								(OptionDialog)okButton.getFocusCycleRootAncestor();
							new SingleEntryDialog(Main.this, "Device Name", "Ok", new ActionListener()
								{
									@Override
									public void actionPerformed(ActionEvent e)
									{
										JButton okButton = (JButton)e.getSource();
										SingleEntryDialog entryDialog =
												(SingleEntryDialog)okButton.getFocusCycleRootAncestor();
										String name = entryDialog.getEntryField().getText();
										setDeviceName(device.getHostName(), name);
										lblTitle.setText("Connected to " + name);
										entryDialog.dispose();
										optionDialog.dispose();
									}
								})
							.setVisible(true);
					}
				},
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						JButton button = (JButton)e.getSource();
						OptionDialog thisDialog =
								(OptionDialog)button.getFocusCycleRootAncestor();
						thisDialog.dispose();
					}
				});
		nameDeviceDialog.setVisible(true);
	}
	
	private void loadDeviceName()
	{
		String deviceName = getDeviceName(device.getHostName());
		if (deviceName != null)
		{
			lblTitle.setText("Connected to " + deviceName);
		}
		else
		{
			lblTitle.setText("Connected to " + device.getName());
			setupDeviceName("It looks like you haven't set a name for your device yet. " +
				"Do you want to do this now?");
		}
	}
	
	private void setDeviceName(String ip, String name)
	{
		Map<String, Object> devices = getDevices();
		devices.put(ip, name);
		
		JSONObject json = new JSONObject(devices);
		PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
		manager.setProperty("devices", json.toString());
	}
	
	private String getDeviceName(String ip)
	{
		return (String)getDevices().get(ip);
	}
	
	private Map<String, Object> getDevices()
	{
		PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
		String devicesStr = manager.getProperty("devices");
		if (devicesStr != null)
		{
			JSONObject json = new JSONObject(devicesStr);
			return json.toMap();
		}
		return new HashMap<String, Object>();
	}
	
	private void initUI()
	{
		initWindow();
		
		initWindowButtons();
		
		initPanelCanvas();
		
		initEffectsPanels();
		
		initUIPrefs();
		
		initTabbedPane();
		
		initWindowListeners();
	}
	
	private void initWindow()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
		if (getUserWindowWidth() != DEFAULT_WINDOW_WIDTH ||
				getUserWindowHeight() != DEFAULT_WINDOW_HEIGHT)
		{
			setSize(getUserWindowWidth(), getUserWindowHeight());
		}
		setUndecorated(true);
		URL iconPath = getClass().getResource("resources/images/icon.png");
		setIconImage(new ImageIcon(iconPath).getImage());
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new LineBorder(new Color(128, 128, 128), 3, true));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[-27.00,grow][755.00,grow]",
				"[][680.00,growprio 105,grow][grow]"));
	}
	
	private void initWindowButtons()
	{
		// Must be initiated before lblTitle
		JButton btnMenu = new MenuButton();
		contentPane.add(btnMenu, "flowx,cell 0 0,gapx 0 10");
		
		lblTitle = new JLabel("Not Connected");
		lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblTitle.setForeground(Color.WHITE);
		contentPane.add(lblTitle, "cell 0 0,growx");
		
		HideButton btnHide = new HideButton(this);
		contentPane.add(btnHide, "cell 1 0,alignx right,gapx 0 15");
		
		MaximizeButton btnMax = new MaximizeButton(this);
		contentPane.add(btnMax, "cell 1 0,alignx right,gapx 0 15");
		
		CloseButton btnClose = new CloseButton(this, JFrame.EXIT_ON_CLOSE);
		contentPane.add(btnClose, "cell 1 0,alignx right,gapx 0 15");
	}
	
	private void initPanelCanvas()
	{
		canvas = new PanelCanvas(device);
		canvas.setLayout(new GridBagLayout());
		canvas.setBorder(new TitledBorder(new LineBorder(Color.GRAY),
				"Preview", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
		((javax.swing.border.TitledBorder)canvas.getBorder())
			.setTitleFont(new Font("Tahoma", Font.BOLD, 22));
		contentPane.add(canvas, "cell 1 1,grow");
	}
	
	private void initEffectsPanels()
	{
		regEffectsPanel = new EffectsPanel("Regular Effects", this, device, canvas);
		add(regEffectsPanel, "cell 0 1,grow");
		
		rhythEffectsPanel = new EffectsPanel("Rhythm Effects", this, device, canvas);
		add(rhythEffectsPanel, "cell 0 2,grow");
	}
	
	private void initTabbedPane()
	{
		JTabbedPane editor = new JTabbedPane(JTabbedPane.TOP);
		editor.setForeground(Color.WHITE);
		editor.setBackground(Color.DARK_GRAY);
		editor.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 1, true),
				"Edit", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
		((javax.swing.border.TitledBorder)editor.getBorder())
			.setTitleFont(new Font("Tahoma", Font.BOLD, 22));
		editor.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				JTabbedPane editor = (JTabbedPane)e.getSource();
				if (editor.getSelectedComponent().equals(discoveryPanel))
				{
					EventQueue.invokeLater(() ->
					{
						discoveryPanel.addTopEffects(1, Main.this);
					});
				}
			}
		});
		contentPane.add(editor, "cell 1 2,grow");
		
		infoPanel = new InformationPanel(this, device, canvas);
		editor.setFont(new Font("Tahoma", Font.BOLD, 17));
		editor.addTab("Control", null, infoPanel, null);
		
		discoveryPanel = new DiscoveryPanel(device);
		editor.addTab("Discovery", null, discoveryPanel, null);
		
		ambilightPanel = new AmbilightPanel(canvas);
		editor.addTab("Ambient Lighting", null, ambilightPanel, null);
		
		SpotifyPanel spotifyPanel = new SpotifyPanel(device);
		editor.addTab("Spotify Visualizer", null, spotifyPanel, null);
		
		KeyShortcutsPanel shortcutsPanel = new KeyShortcutsPanel(device);
		shortcutsPanel.setBorder(new LineBorder(Color.GRAY, 1, true));
		editor.addTab("Shortcuts", null, shortcutsPanel, null);
		
		AuroraNullListener anl = new AuroraNullListener(20, null,
				infoPanel, canvas, discoveryPanel, ambilightPanel,
				spotifyPanel, shortcutsPanel);
		anl.start();
	}
	
	private void initUIPrefs()
	{
		UIManager.put("TabbedPane.contentBorderInsets",
				new InsetsUIResource(0, 0, 0, 0));
		UIManager.put("TabbedPane.focus", new Color(162, 184, 205));
		UIManager.put("TabbedPane.darkShadow", Color.DARK_GRAY);
		UIManager.put("TabbedPane.borderHightlightColor", Color.GRAY);
		UIManager.put("TabbedPane.light", Color.LIGHT_GRAY);
		UIManager.put("TabbedPane.selected", Color.DARK_GRAY);
	}
	
	private void initWindowListeners()
	{
		ComponentResizer cr = new ComponentResizer();
		cr.registerComponent(this);
		cr.setSnapSize(new Dimension(10, 10));
		cr.setMinimumSize(new Dimension(200, 200));
		
		WindowDragListener wdl = new WindowDragListener(50);
		addMouseListener(wdl);
		addMouseMotionListener(wdl);
		
		WindowOpeningListener wol = new WindowOpeningListener(this);
		addWindowListener(wol);
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
				manager.setProperty("windowWidth", getWidth());
				manager.setProperty("windowHeight", getHeight());
				
				if (canvas != null)
				{
					EventQueue.invokeLater(() ->
					{
						canvas.initCanvas();
						canvas.repaint();
					});
				}
			}
		});
		
		if (device != null)
		{
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					loadAuroraData();
				}
			});
		}
	}
	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					Main frame = new Main();
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
}
