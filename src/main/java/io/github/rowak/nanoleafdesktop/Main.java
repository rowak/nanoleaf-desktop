package io.github.rowak.nanoleafdesktop;

import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.PluginEffect;
import io.github.rowak.nanoleafdesktop.models.DeviceGroup;
import io.github.rowak.nanoleafdesktop.models.DeviceInfo;
import io.github.rowak.nanoleafdesktop.tools.*;
import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.button.HideButton;
import io.github.rowak.nanoleafdesktop.ui.button.MaximizeButton;
import io.github.rowak.nanoleafdesktop.ui.button.MenuButton;
import io.github.rowak.nanoleafdesktop.ui.dialog.DeviceChangerDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.OptionDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.SingleEntryDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.listener.ComponentResizer;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowDragListener;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowOpeningListener;
import io.github.rowak.nanoleafdesktop.ui.panel.*;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;
import net.miginfocom.swing.MigLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.InsetsUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JFrame {
	
    public static final Version VERSION = new Version("v0.9.0", true);
    public static final String VERSION_HOST =
            "https://api.github.com/repos/rowak/nanoleaf-desktop/releases";
    public static final String GIT_REPO = "https://github.com/rowak/nanoleaf-desktop";
    public static final String OLD_PROPERTIES_FILEPATH =
            System.getProperty("user.home") + "/properties.txt";
    public static final String PROPERTIES_FILEPATH = getPropertiesFilePath();

    private final int DEFAULT_WINDOW_WIDTH = 1050;
    private final int DEFAULT_WINDOW_HEIGHT = 850;

    boolean uiEnabled;

    private List<NanoleafDevice> devices;
    private NanoleafGroup group;

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
    private EffectsPanel basicEffectsPanel;
    private EffectsPanel regEffectsPanel;
    private EffectsPanel rhythEffectsPanel;

    public Main(CLICommand command) {
        migrateOldProperties();

        PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
        String lastSession = manager.getProperty("lastSession");
        
        group = new NanoleafGroup();

        // Use the device from the last session
        if (lastSession != null && command == null) {
            uiEnabled = true;
            setupOldAurora(lastSession);
        }

        if (command != null) {
        	uiEnabled = false;
            new CLICommandHandler(command);
        }
        else {
            uiEnabled = true;
            initUI();

            // Search for a new device
            if (lastSession == null) {
                setupNewAurora();
            }

            checkForUpdate();
        }
    }

    public static String getPropertiesFilePath() {
        String dir = "";
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            dir = System.getenv("APPDATA") + "/Nanoleaf for Desktop";
        }
        else if (os.contains("mac")) {
            dir = System.getProperty("user.home") +
                    "/Library/Application Support/Nanoleaf for Desktop";
        }
        else if (os.contains("nux")) {
            dir = System.getProperty("user.home") + "/.Nanoleaf for Desktop";
        }

        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        return dir + "/preferences.txt";
    }

    private void migrateOldProperties() {
        File oldProperties = new File(OLD_PROPERTIES_FILEPATH);
        if (oldProperties.exists()) {
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try {
                reader = new BufferedReader(new FileReader(OLD_PROPERTIES_FILEPATH));
                writer = new BufferedWriter(new FileWriter(getPropertiesFilePath()));
                String data = "";
                String line = "";
                while ((line = reader.readLine()) != null) {
                    data += line + "\n";
                }
                writer.write(data);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                    if (writer != null) {
                        writer.close();
                    }
                    oldProperties.renameTo(new File(
                            System.getProperty("user.home") +
                                    "/propertiesOLD.txt"));
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    private void checkForUpdate() {
        new Thread(() -> {
        	try {
        		UpdateManager manager = new UpdateManager(VERSION_HOST, GIT_REPO);
        		if (manager.updateAvailable(VERSION)) {
        			manager.showUpdateMessage(this);
        		}
        	}
        	catch (IOException e) {
        		e.printStackTrace();
        		System.err.println("ERROR: Failed to check for updates.");
        	}
        }).start();
    }

    private int getUserWindowWidth() {
        PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
        String width = manager.getProperty("windowWidth");
        if (width != null) {
            return Integer.parseInt(width);
        }
        return DEFAULT_WINDOW_WIDTH;
    }

    private int getUserWindowHeight() {
        PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
        String height = manager.getProperty("windowHeight");
        if (height != null) {
            return Integer.parseInt(height);
        }
        return DEFAULT_WINDOW_HEIGHT;
    }

    public PanelCanvas getCanvas() {
        return this.canvas;
    }

    public void hideToSystemTray() {
        try {
            systemTray.add(trayIcon);
            setVisible(false);
        }
        catch (AWTException awte) {
            awte.printStackTrace();
        }
    }

    public void openFromSystemTray() {
        systemTray.remove(trayIcon);
        setVisible(true);
    }

    public void setDevices(List<NanoleafDevice> devices) {
        EventQueue.invokeLater(() -> {
        	this.devices = devices;
        	loadAuroraData();
        	loadDeviceName();
        	canvas.setAuroras(group);
        	devices.forEach((device) -> {
        		DeviceEventHandler handler = new DeviceEventHandler(device, canvas);
        		device.registerEventListener(handler, true, true, false, false);
        	});
        });
    }
    
    public void addDevice(String name, NanoleafDevice device) {
    	group.addDevice(name, device);
    }
    
    public void removeAllDevices() {
    	group.removeAllDevices();
    }

    public void loadEffects() throws NanoleafException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                regEffectsPanel.clearEffects();
                rhythEffectsPanel.clearEffects();
                new Thread(() -> {
                	List<String> regEffects = new ArrayList<String>();
                	List<String> rhythmEffects = new ArrayList<String>();
                	try {
                		for (NanoleafDevice device : devices) {
                			for (Effect effect : device.getAllEffects()) {
                				if (effect.getEffectType().equals("plugin")) {
                					if (((PluginEffect)effect).getPlugin().getType().equals("rhythm")) {
                						rhythmEffects.add(effect.getName());
                					}
                					else {
                						regEffects.add(effect.getName());
                    				}
                				}
                				else {
                					regEffects.add(effect.getName());
                				}
                			}
                		}
                	}
                	catch (NanoleafException | IOException e) {
                		e.printStackTrace();
                	}
                	
                	regEffects.sort(new Comparator<String>() {
						@Override
						public int compare(String ef1, String ef2) {
							return ef1.compareTo(ef2);
						}
                	});
                	rhythmEffects.sort(new Comparator<String>() {
						@Override
						public int compare(String ef1, String ef2) {
							return ef1.compareTo(ef2);
						}
                	});
                	regEffectsPanel.addEffects(regEffects);
                	rhythEffectsPanel.addEffects(rhythmEffects);

                	BasicEffects.initializeBasicEffects();
                	try {
                		List<Effect> effects = BasicEffects.getBasicEffects(devices).get(0);
                		for (Effect ef : effects) {
                			basicEffectsPanel.addEffect(ef.getName());
                		}
                	}
                	catch (NanoleafException | IOException e) {
                		e.printStackTrace();
                	}

                	if (regEffectsPanel.getModel().size() > 0) {
                		regEffectsPanel.setViewportView(regEffectsPanel.getList());
                	}
                	if (rhythEffectsPanel.getModel().size() > 0) {
                		rhythEffectsPanel.setViewportView(rhythEffectsPanel.getList());
                	}
                	basicEffectsPanel.setViewportView(basicEffectsPanel.getList());
                }).start();
            }
        });
    }

    public void loadStateComponents() throws NanoleafException, IOException {
        if (devices.get(0).getOn()) {
            infoPanel.getBtnOnOff().setText("Turn Off");
        }
        else {
            infoPanel.getBtnOnOff().setText("Turn On");
        }

        infoPanel.setSliderBrightness(devices.get(0).getBrightness());
        infoPanel.setSliderColorTemp(devices.get(0).getColorTemperature());

        loadActiveScene();
    }

    public void loadActiveScene() throws NanoleafException, IOException {
        String currentEffect = devices.get(0).getCurrentEffectName();
        infoPanel.setScene(currentEffect);
    }

    public void unselectAllExcept(EffectsPanel selected) {
        if (basicEffectsPanel != selected) {
            basicEffectsPanel.getList().clearSelection();
        }
        if (regEffectsPanel != selected) {
            regEffectsPanel.getList().clearSelection();
        }
        if (rhythEffectsPanel != selected) {
            rhythEffectsPanel.getList().clearSelection();
        }
    }

    private void loadAuroraData() {
        try {
            loadEffects();
            loadStateComponents();
        }
        catch (NanoleafException | IOException e) {
            e.printStackTrace();
        }
    }

    private void setupOldAurora(String lastSession) {
        if (lastSession.startsWith("GROUP:")) {
            String groupName = lastSession.split(":")[1];
            DeviceGroup group = null;
            List<DeviceGroup> groups = getDeviceGroups();
            for (DeviceGroup g : groups) {
                if (g.getName().equals(groupName)) {
                    group = g;
                }
            }

            if (group != null) {
                connectToGroup(group);
                if (uiEnabled) {
                    EventQueue.invokeLater(() -> {
                    	lblTitle.setText("Connected to " + groupName);
                    	loadAuroraData();
                    });
                }
            }
            else {
                if (uiEnabled) {
                    resetDataFile();
                }
            }
        }
        else {
            String[] data = lastSession.split(" ");
            try {
            	NanoleafDevice device = NanoleafDevice.createDevice(data[0],
                        Integer.parseInt(data[1]), data[3]);
                devices = new ArrayList<NanoleafDevice>();
                devices.add(device);
                group.addDevice(device.getName(), device);
                if (uiEnabled) {
                    EventQueue.invokeLater(() -> {
                    	loadDeviceName();
                    });
                }
            }
            catch (NanoleafException | IOException schre) {
                if (uiEnabled) {
                    new TextDialog(Main.this, "Failed to connect to the device. " +
                            "Please try again.").setVisible(true);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if (uiEnabled) {
                    resetDataFile();
                }
            }
        }
    }

    private void setupNewAurora() {
        EventQueue.invokeLater(() -> {
        	DeviceChangerDialog finder = new DeviceChangerDialog(this);
        	finder.setVisible(true);
        });
    }

    public void setupDeviceName(String message) {
        OptionDialog nameDeviceDialog = new OptionDialog(Main.this,
        		message, "Yes", "No", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton okButton = (JButton) e.getSource();
                OptionDialog optionDialog =
                        (OptionDialog) okButton.getFocusCycleRootAncestor();
                new SingleEntryDialog(Main.this, "Device Name", "Ok", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JButton okButton = (JButton) e.getSource();
                        SingleEntryDialog entryDialog =
                                (SingleEntryDialog) okButton.getFocusCycleRootAncestor();
                        String name = entryDialog.getEntryField().getText();
                        setDeviceName(devices.get(0).getHostname(), name);
                        lblTitle.setText("Connected to " + name);
                        entryDialog.dispose();
                        optionDialog.dispose();
                    }
                })
                .setVisible(true);
            }
        },
        new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		JButton button = (JButton) e.getSource();
        		OptionDialog thisDialog =
        				(OptionDialog) button.getFocusCycleRootAncestor();
        		thisDialog.dispose();
        	}
        });
        nameDeviceDialog.setVisible(true);
    }

    private void connectToGroup(DeviceGroup group) {
        DeviceInfo[] groupDevices = group.getDevices();
        List<NanoleafDevice> devices = new ArrayList<NanoleafDevice>();
        for (int i = 0; i < groupDevices.length; i++) {
            try {
            	NanoleafDevice device = NanoleafDevice.createDevice(groupDevices[i].getHostName(),
                        groupDevices[i].getPort(), groupDevices[i].getAccessToken());
                devices.add(device);
                this.group.addDevice(device.getName(), device);
            }
            catch (NanoleafException | IOException e) {
                new TextDialog(this, "Unknown connection error for device " +
                        groupDevices[i].getHostName() + ".").setVisible(true);
            }
        }
        setDevices(devices);
    }

    private void resetDataFile() {
        OptionDialog errorDialog = new OptionDialog(this,
        		"The data file has been modified or has become corrupt. " +
        				"Would you like to fix this now?", "Yes", "No",
        				new ActionListener() {
        					@Override
        					public void actionPerformed(ActionEvent e) {
        						new PropertyManager(PROPERTIES_FILEPATH)
        							.removeProperty("lastSession");
        						OptionDialog dialog = (OptionDialog) ((JButton) e.getSource())
        								.getTopLevelAncestor();
        						dialog.dispose();
        						new TextDialog(Main.this,
        								"Relaunch the application to setup a new device.")
        							.setVisible(true);
        					}
        				},
        				new ActionListener() {
        					@Override
        					public void actionPerformed(ActionEvent e) {
        						OptionDialog dialog = (OptionDialog) ((JButton) e.getSource())
        								.getTopLevelAncestor();
        						dialog.dispose();
        					}
        				});
        errorDialog.setVisible(true);
        EventQueue.invokeLater(() -> {
        	errorDialog.requestFocus();
        });
    }

    private void loadDeviceName() {
        if (devices.size() == 1) {
            String deviceName = getDeviceName(devices.get(0).getHostname());
            if (deviceName != null) {
                lblTitle.setText("Connected to " + deviceName);
            }
            else {
                lblTitle.setText("Connected to " + devices.get(0).getName());
                setupDeviceName("It looks like you haven't set a name for your device yet. " +
                                        "Do you want to do this now?");
            }
        }
    }

    private void setDeviceName(String ip, String name) {
        Map<String, Object> devices = getDevices();
        devices.put(ip, name);
        JSONObject json = new JSONObject(devices);
        PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
        manager.setProperty("devices", json.toString());
    }

    private String getDeviceName(String ip) {
        return (String) getDevices().get(ip);
    }

    private Map<String, Object> getDevices() {
        PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
        String devicesStr = manager.getProperty("devices");
        if (devicesStr != null) {
            JSONObject json = new JSONObject(devicesStr);
            return json.toMap();
        }
        return new HashMap<String, Object>();
    }

    private List<DeviceGroup> getDeviceGroups() {
        PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
        String devicesStr = manager.getProperty("deviceGroups");
        if (devicesStr != null) {
            List<DeviceGroup> groups = new ArrayList<DeviceGroup>();
            JSONArray json = new JSONArray(devicesStr);
            for (int i = 0; i < json.length(); i++) {
                groups.add(DeviceGroup.fromJSON(json.getJSONObject(i).toString()));
            }
            return groups;
        }
        return new ArrayList<DeviceGroup>();
    }

    private void initUI() {
        initWindow();

        initWindowButtons();

        initPanelCanvas();

        initEffectsPanels();

        initUIPrefs();

        initTabbedPane();

        initWindowListeners();

        initSystemTray();
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        if (getUserWindowWidth() != DEFAULT_WINDOW_WIDTH ||
                getUserWindowHeight() != DEFAULT_WINDOW_HEIGHT) {
            setSize(getUserWindowWidth(), getUserWindowHeight());
        }
        setUndecorated(true);
        URL iconPath = getClass().getResource("/images/icon.png");
        ImageIcon imageIcon = new ImageIcon(iconPath);
        Image image = imageIcon.getImage();
        setIconImage(image);

        contentPane = new JPanel();
        contentPane.setBackground(Color.DARK_GRAY);
        contentPane.setBorder(new LineBorder(new Color(128, 128, 128), 3, true));
        setContentPane(contentPane);
        contentPane.setLayout(new MigLayout("", "[-27.00,grow][755.00,grow]",
                                            "[][120px:120px,grow][300px:400px,grow][100px:400px,grow]"));
    }

    private void initWindowButtons() {
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

    private void initPanelCanvas() {
        canvas = new PanelCanvas(group);
        canvas.setLayout(new GridBagLayout());
        canvas.setBorder(new TitledBorder(new LineBorder(Color.GRAY),
                                          "Preview", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
        ((javax.swing.border.TitledBorder) canvas.getBorder())
                .setTitleFont(new Font("Tahoma", Font.BOLD, 22));
        contentPane.add(canvas, "cell 1 1 1 2,grow");
    }

    private void initEffectsPanels() {
        basicEffectsPanel = new EffectsPanel("Basic Effects", this, group, canvas);
        getContentPane().add(basicEffectsPanel, "cell 0 1,grow");

        regEffectsPanel = new EffectsPanel("Color Effects", this, group, canvas);
        getContentPane().add(regEffectsPanel, "cell 0 2,grow");

        rhythEffectsPanel = new EffectsPanel("Rhythm Effects", this, group, canvas);
        getContentPane().add(rhythEffectsPanel, "cell 0 3,grow");
    }

    private void initTabbedPane() {
        JTabbedPane editor_1 = new JTabbedPane(JTabbedPane.TOP);
        editor_1.setForeground(Color.WHITE);
        editor_1.setBackground(Color.DARK_GRAY);
        editor_1.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 1, true),
                                            "Edit", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
        ((javax.swing.border.TitledBorder) editor_1.getBorder())
                .setTitleFont(new Font("Tahoma", Font.BOLD, 22));
        editor_1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane editor = (JTabbedPane) e.getSource();
                if (editor.getSelectedComponent().equals(discoveryPanel)) {
                    EventQueue.invokeLater(() -> {
                    	discoveryPanel.addTopEffects(1, new ArrayList<String>());
                    });
                }
            }
        });
        contentPane.add(editor_1, "cell 1 3,grow");

        infoPanel = new InformationPanel(this, group, canvas);
        editor_1.setFont(new Font("Tahoma", Font.BOLD, 17));
        editor_1.addTab("Control", null, infoPanel, null);

        discoveryPanel = new DiscoveryPanel(group);
        editor_1.addTab("Discovery", null, discoveryPanel, null);

        ambilightPanel = new AmbilightPanel(group, canvas);
        editor_1.addTab("Ambient Lighting", null, ambilightPanel, null);

        spotifyPanel = new SpotifyPanel(group, canvas);
        editor_1.addTab("Spotify Visualizer", null, spotifyPanel, null);

        shortcutsPanel = new KeyShortcutsPanel(group);
        shortcutsPanel.setBorder(new LineBorder(Color.GRAY, 1, true));
        editor_1.addTab("Shortcuts", null, shortcutsPanel, null);

//        AuroraNullListener anl = new AuroraNullListener(20, null,
//                                                        infoPanel, canvas, discoveryPanel, ambilightPanel,
//                                                        spotifyPanel, shortcutsPanel);
//        anl.start();
    }

    private void initUIPrefs() {
        UIManager.put("TabbedPane.contentBorderInsets",
                      new InsetsUIResource(0, 0, 0, 0));
        UIManager.put("TabbedPane.focus", new Color(162, 184, 205));
        UIManager.put("TabbedPane.darkShadow", Color.DARK_GRAY);
        UIManager.put("TabbedPane.borderHightlightColor", Color.GRAY);
        UIManager.put("TabbedPane.light", Color.LIGHT_GRAY);
        UIManager.put("TabbedPane.selected", Color.DARK_GRAY);
    }

    private void initWindowListeners() {
        ComponentResizer cr = new ComponentResizer();
        cr.registerComponent(this);
        cr.setSnapSize(new Dimension(10, 10));
        cr.setMinimumSize(new Dimension(200, 200));

        WindowDragListener wdl = new WindowDragListener(50);
        addMouseListener(wdl);
        addMouseMotionListener(wdl);

        WindowOpeningListener wol = new WindowOpeningListener(this);
        addWindowListener(wol);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
                manager.setProperty("windowWidth", getWidth());
                manager.setProperty("windowHeight", getHeight());

                if (canvas != null) {
                    EventQueue.invokeLater(() -> {
                    	canvas.initCanvas();
                    	canvas.repaint();
                    });
                }
            }
        });

        if (devices != null && devices.get(0) != null) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    loadAuroraData();
                }
            });
        }
    }

    private void initSystemTray() {
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();
            URL iconPath = Main.class.getResource(
                    "/images/icon.png");
            Image img = new ImageIcon(iconPath).getImage();
            PopupMenu menu = new PopupMenu();
            MenuItem itemOpen = new MenuItem("Open");
            itemOpen.setFont(UIConstants.smallLabelFont);
            itemOpen.addActionListener((e) -> {
            	setVisible(true);
            	setExtendedState(JFrame.NORMAL);
            	openFromSystemTray();
            });
            menu.add(itemOpen);
            MenuItem itemExit = new MenuItem("Exit");
            itemExit.setFont(UIConstants.smallLabelFont);
            itemExit.addActionListener((e) -> {
            	System.exit(0);
            });
            menu.add(itemExit);
            trayIcon = new TrayIcon(img,
                                    "Nanoleaf for Desktop", menu);
            trayIcon.setImageAutoSize(true);

            addWindowStateListener((e) -> {
            	if (e.getNewState() == JFrame.ICONIFIED) {
            		hideToSystemTray();
            	}
            	else if (e.getNewState() == JFrame.NORMAL ||
            			e.getNewState() == JFrame.MAXIMIZED_BOTH) {
            		openFromSystemTray();
            	}
            });
        }
        else {
            System.err.println("INFO: System tray not supported by OS. " +
                                       "Minimize to tray feature has been disabled.\n");
        }
    }

    public static void main(String[] args) {
        CLICommand cmd = parseCommand(args);
        boolean help = hasArg("--help", null, false, args);
        if (!help) {
	        EventQueue.invokeLater(new Runnable() {
	            public void run() {
	                try {
	                    Main frame = new Main(cmd);
	                    frame.setVisible(true);
	                }
	                catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        });
        }
        else {
        	showHelp();
        }
    }
    
    private static void showHelp() {
    	System.out.println("Nanoleaf for Desktop (NFD) -- GUI and CLI interface for Nanoleaf Aurora and Canvas\n" +
    					   "usage: nfd [action [value]]\n\n" +
    					   "Actions:\n" +
    					   "  on                    turns on the device(s)\n" +
    					   "  off                   turns off the device(s)\n" +
    					   "  toggle                toggles the device(s) on or off\n" +
    					   "  brightness [x/+x/-x]  changes the master brightness (set/up/down)\n" +
    					   "  temp [x/+x/-x]        changes the color temperature (set/up/down)\n" +
    					   "  effect [name]         sets the effect by name");
    }

    private class CLICommandHandler {
    	
        public CLICommandHandler(CLICommand command) {
            init();
            if (command != null) {
                try {
                    command.execute(group);
                    group.closeAsyncForAll();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error: failed to execute action");
                    System.exit(4);
                }
            }
            else {
                System.out.println("Error: invalid action");
                System.exit(2);
            }
            System.exit(0);
        }

        private void init() {
            PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
            String lastSession = manager.getProperty("lastSession");

            if (lastSession != null) {
                setupOldAurora(lastSession);
            }
            else {
                System.out.println("Error: device not set up");
                System.exit(1);
            }
        }
    }
    
    private static CLICommand parseCommand(String[] args) {
		int commandId = -1;
		String arg = null;
		for (int i = 0; i < args.length; i++) {
			String argTemp = i+1 < args.length ? args[i+1] : null;
			int id = CLICommand.getCommandId(args[i], argTemp);
			if (id != -1) {
				commandId = id;
				arg = argTemp;
			}
		}
		return commandId != -1 ? new CLICommand(commandId, arg) : null;
	}
    
    private static boolean hasArg(String arg, String shortArg,
			boolean defaultArg, String[] args) {
		for (String a : args) {
			if ((arg != null && a.equals(arg)) || a.equals(shortArg)) {
				return true;
			}
		}
		return defaultArg;
	}
}
