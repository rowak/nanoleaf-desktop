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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.border.LineBorder;

import io.github.rowak.Aurora;
import io.github.rowak.Effect;
import io.github.rowak.StatusCodeException;
import io.github.rowak.StatusCodeException.UnauthorizedException;
import io.github.rowak.nanoleafdesktop.models.DeviceGroup;
import io.github.rowak.nanoleafdesktop.models.DeviceInfo;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.tools.UIConstants;
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
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;

import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.InsetsUIResource;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import java.awt.Component;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.JButton;

public class Main extends JFrame
{
	public static final Version VERSION = new Version("v0.8.2", true);
	public static final String VERSION_HOST =
			"https://api.github.com/repos/rowak/nanoleaf-desktop/releases";
	public static final String GIT_REPO = "https://github.com/rowak/nanoleaf-desktop";
	public static final String OLD_PROPERTIES_FILEPATH =
			System.getProperty("user.home") + "/properties.txt";
	public static final String PROPERTIES_FILEPATH = getPropertiesFilePath();
	
	private final int DEFAULT_WINDOW_WIDTH = 1050;
	private final int DEFAULT_WINDOW_HEIGHT = 800;
	
	private Aurora[] devices;
	
	private SystemTray systemTray;
	private TrayIcon trayIcon;
	
	private JPanel contentPane;
	private PanelCanvas canvas;
	private InformationPanel infoPanel;
	private DiscoveryPanel discoveryPanel;
	private AmbilightPanel ambilightPanel;
	private SpotifyPanel spotifyPanel;
	private KeyShortcutsPanel shortcutsPanel;
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
	
	public void hideToSystemTray()
	{
		try
		{
			systemTray.add(trayIcon);
			setVisible(false);
		}
		catch (AWTException awte)
		{
			awte.printStackTrace();
		}
	}
	
	public void openFromSystemTray()
	{
		systemTray.remove(trayIcon);
		setVisible(true);
	}
	
	public void setDevices(Aurora[] devices)
	{
		EventQueue.invokeLater(() ->
		{
			this.devices = devices;
			if (devices.length == 1)
			{
				PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
				manager.setProperty("lastSession",
						devices[0].getHostName() + " " +
						devices[0].getPort() + " v1 " +
						devices[0].getAccessToken());
			}
			else if (devices.length > 1)
			{
				new TextDialog(this, "You are now in group mode. Your devices are displayed ON TOP " +
						"of each other in the preview window. You can move your devices around " +
						"to match your actual layout by dragging the panels in the preview window. " +
						"By doing this, your devices will sync together to simulate having " +
						"a single device.\n\n\n\n\n\n\n")
						.setVisible(true);
			}
			loadAuroraData();
			loadDeviceName();
			canvas.setAuroras(devices);
			canvas.repaint();
			infoPanel.setAuroras(devices);
			regEffectsPanel.setAuroras(devices);
			rhythEffectsPanel.setAuroras(devices);
			discoveryPanel.setAuroras(devices);
			ambilightPanel.setAuroras(devices);
			spotifyPanel.setAuroras(devices);
			shortcutsPanel.setAuroras(devices);
		});
	}
	
	public void loadEffects() throws StatusCodeException
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				regEffectsPanel.clearEffects();
				rhythEffectsPanel.clearEffects();
				new Thread(() ->
				{
					try
					{
						for (Aurora device : devices)
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
		if (devices[0].state().getOn())
		{
			infoPanel.getBtnOnOff().setText("Turn Off");
		}
		else
		{
			infoPanel.getBtnOnOff().setText("Turn On");
		}
		
		infoPanel.setSliderBrightness(devices[0].state().getBrightness());
		infoPanel.setSliderColorTemp(devices[0].state().getColorTemperature());
		
		loadActiveScene();
	}
	
	public void loadActiveScene() throws StatusCodeException
	{
		String currentEffect = devices[0].effects().getCurrentEffectName();
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
		if (lastSession.startsWith("GROUP:"))
		{
			String groupName = lastSession.split(":")[1];
			DeviceGroup group = null;
			List<DeviceGroup> groups = getDeviceGroups();
			for (DeviceGroup g : groups)
			{
				if (g.getName().equals(groupName))
				{
					group = g;
				}
			}
			
			if (group != null)
			{
				connectToGroup(group);
				EventQueue.invokeLater(() ->
				{
					lblTitle.setText("Connected to " + groupName);
					loadAuroraData();
				});
			}
			else
			{
				resetDataFile();
			}
		}
		else
		{
			String[] data = lastSession.split(" ");
			try
			{
				devices = new Aurora[1];
				devices[0] = new Aurora(data[0],
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
				resetDataFile();
			}
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
							devices = new Aurora[1];
							devices[0] = new Aurora(finder.getHostName(),
									finder.getPort(), "v1", finder.getAccessToken());
							this.cancel();
							
							PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
							manager.setProperty("lastSession",
									devices[0].getHostName() + " " +
									devices[0].getPort() + " v1 " +
									devices[0].getAccessToken());
							lblTitle.setText("Connected to " + devices[0].getName());
							Map<String, Object> localDevices = getDevices();
							if (localDevices.containsKey(devices[0].getHostName()))
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
						canvas.setAuroras(devices);
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
										setDeviceName(devices[0].getHostName(), name);
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
	
	private void connectToGroup(DeviceGroup group)
	{
		DeviceInfo[] groupDevices = group.getDevices();
		Aurora[] auroraDevices = new Aurora[groupDevices.length];
		for (int i = 0; i < groupDevices.length; i++)
		{
			try
			{
				auroraDevices[i] = new Aurora(groupDevices[i].getHostName(),
						groupDevices[i].getPort(), "v1", groupDevices[i].getAccessToken());
			}
			catch (HttpRequestException hre)
			{
				new TextDialog(this, "The device " + groupDevices[i].getHostName() +
						" is offline.").setVisible(true);
			}
			catch (UnauthorizedException uae)
			{
				new TextDialog(this, "The device " + groupDevices[i].getHostName() +
						" is unauthorized.").setVisible(true);
			}
			catch (StatusCodeException sce)
			{
				new TextDialog(this, "Unknown connection error for device " +
						groupDevices[i].getHostName() + ".").setVisible(true);
			}
		}
		setDevices(auroraDevices);
	}
	
	private void resetDataFile()
	{
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
	
	private void loadDeviceName()
	{
		if (devices.length == 1)
		{
			String deviceName = getDeviceName(devices[0].getHostName());
			if (deviceName != null)
			{
				lblTitle.setText("Connected to " + deviceName);
			}
			else
			{
				lblTitle.setText("Connected to " + devices[0].getName());
				setupDeviceName("It looks like you haven't set a name for your device yet. " +
					"Do you want to do this now?");
			}
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
	
	private List<DeviceGroup> getDeviceGroups()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		String devicesStr = manager.getProperty("deviceGroups");
		if (devicesStr != null)
		{
			List<DeviceGroup> groups = new ArrayList<DeviceGroup>();
			JSONArray json = new JSONArray(devicesStr);
			for (int i = 0; i < json.length(); i++)
			{
				groups.add(DeviceGroup.fromJSON(json.getJSONObject(i).toString()));
			}
			return groups;
		}
		return new ArrayList<DeviceGroup>();
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
		
		initSystemTray();
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
		JButton btnMenu = new MenuButton(this);
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
		canvas = new PanelCanvas(devices);
		canvas.setLayout(new GridBagLayout());
		canvas.setBorder(new TitledBorder(new LineBorder(Color.GRAY),
				"Preview", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
		((javax.swing.border.TitledBorder)canvas.getBorder())
			.setTitleFont(new Font("Tahoma", Font.BOLD, 22));
		contentPane.add(canvas, "cell 1 1,grow");
	}
	
	private void initEffectsPanels()
	{
		regEffectsPanel = new EffectsPanel("Regular Effects", this, devices, canvas);
		add(regEffectsPanel, "cell 0 1,grow");
		
		rhythEffectsPanel = new EffectsPanel("Rhythm Effects", this, devices, canvas);
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
						discoveryPanel.addTopEffects(1, new ArrayList<String>());
					});
				}
			}
		});
		contentPane.add(editor, "cell 1 2,grow");
		
		infoPanel = new InformationPanel(this, devices, canvas);
		editor.setFont(new Font("Tahoma", Font.BOLD, 17));
		editor.addTab("Control", null, infoPanel, null);
		
		discoveryPanel = new DiscoveryPanel(devices);
		editor.addTab("Discovery", null, discoveryPanel, null);
		
		ambilightPanel = new AmbilightPanel(canvas);
		editor.addTab("Ambient Lighting", null, ambilightPanel, null);
		
		spotifyPanel = new SpotifyPanel(devices, canvas);
		editor.addTab("Spotify Visualizer", null, spotifyPanel, null);
		
		shortcutsPanel = new KeyShortcutsPanel(devices);
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
		
		if (devices != null && devices[0] != null)
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
	
	private void initSystemTray()
	{
		if (SystemTray.isSupported())
		{
			systemTray = SystemTray.getSystemTray();
			URL iconPath = Main.class.getResource(
					"resources/images/icon.png");
			Image img = new ImageIcon(iconPath).getImage();
			PopupMenu menu = new PopupMenu();
			MenuItem itemOpen = new MenuItem("Open");
			itemOpen.setFont(UIConstants.smallLabelFont);
			itemOpen.addActionListener((e) ->
			{
				setVisible(true);
				setExtendedState(JFrame.NORMAL);
				openFromSystemTray();
			});
			menu.add(itemOpen);
			MenuItem itemExit = new MenuItem("Exit");
			itemExit.setFont(UIConstants.smallLabelFont);
			itemExit.addActionListener((e) ->
			{
				System.exit(0);
			});
			menu.add(itemExit);
			trayIcon = new TrayIcon(img,
					"Nanoleaf for Desktop", menu);
			trayIcon.setImageAutoSize(true);
			
			addWindowStateListener((e) ->
			{
				if (e.getNewState() == JFrame.ICONIFIED)
				{
					hideToSystemTray();
				}
				else if (e.getNewState() == JFrame.NORMAL ||
						e.getNewState() == JFrame.MAXIMIZED_BOTH)
				{
					openFromSystemTray();
				}
			});
		}
		else
		{
			System.err.println("INFO: System tray not supported by OS. " +
					"Minimize to tray feature has been disabled.\n");
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
