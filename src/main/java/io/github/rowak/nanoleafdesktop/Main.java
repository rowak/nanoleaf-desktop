package io.github.rowak.nanoleafdesktop;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafapi.StatusCodeException.UnauthorizedException;
import io.github.rowak.nanoleafdesktop.models.DeviceGroup;
import io.github.rowak.nanoleafdesktop.models.DeviceInfo;
import io.github.rowak.nanoleafdesktop.shortcuts.Action;
import io.github.rowak.nanoleafdesktop.shortcuts.ActionType;
import io.github.rowak.nanoleafdesktop.tools.*;
import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.button.HideButton;
import io.github.rowak.nanoleafdesktop.ui.button.MaximizeButton;
import io.github.rowak.nanoleafdesktop.ui.button.MenuButton;
import io.github.rowak.nanoleafdesktop.ui.dialog.*;
import io.github.rowak.nanoleafdesktop.ui.listener.AuroraNullListener;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JFrame implements IListenToMessages {
    //TODO get this from POM (ideally also go to SNAPSHOT naming e.g. <version>0.1.0-SNAPSHOT</version> in POM; then prerelease flag will be obsolete
    public static final Version VERSION = new Version("v0.8.6", true);
    private static final PropertyReader propertyReader = new PropertyReader();
    public static final String PROPERTIES_FILEPATH = propertyReader.getPropertyFilePath();

    private final int DEFAULT_WINDOW_WIDTH = 1050;
    private final int DEFAULT_WINDOW_HEIGHT = 850;

    boolean uiEnabled;

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
    private EffectsPanel basicEffectsPanel;
    private EffectsPanel regEffectsPanel;
    private EffectsPanel rhythEffectsPanel;

    //TODO this is one step for de-coupling stuff
    //TODO maybe turn this into a runnable
    private final UpdateManager manager = new UpdateManager();

    public Main(String[] actions) {
        propertyReader.migrateOldProperties();

        PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
        String lastSession = manager.getProperty("lastSession");

        // Use the device from the last session
        if (lastSession != null && (actions == null || actions.length > 0)) {
            uiEnabled = true;
            setupOldAurora(lastSession);
        }

        if (actions != null && actions.length > 0) {
            new ActionHandler(actions);
        } else {
            uiEnabled = true;
            initUI();

            // Search for a a new device
            if (lastSession == null) {
                setupNewAurora();
            }

            checkForUpdate();
        }
    }

    private void checkForUpdate() {
        new Thread(() -> {
            try {
                manager.checkForUpdate(this, VERSION);
            } catch (HttpRequestException hre) {
                /*
                 * If the update server cannot be reached, ignore it (don't notify the user).
                 * The user will be notified about an update the next time they
                 * connect to the network and open the application.
                 */
            }
        }).start();
    }

    @Override
    public void render(UpdateOptionDialog updateDialog) {
        updateDialog.finalizeDialog(this);
    }

    @Override
    public void createDialog(IDeliverMessages message) {
        String messageText = message.getMessage();
        TextDialog textDialog = new TextDialog(this, messageText);
        textDialog.setVisible(true);
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
        } catch (AWTException awte) {
            awte.printStackTrace();
        }
    }

    public void openFromSystemTray() {
        systemTray.remove(trayIcon);
        setVisible(true);
    }

    public void setDevices(Aurora[] devices) {
        EventQueue.invokeLater(() ->
                               {
                                   this.devices = devices;
                                   if (devices.length > 1) {
                                       new TextDialog(this,
                                                      "You are now in group mode. Your devices are displayed ON TOP " +
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

    public void loadEffects() throws StatusCodeException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                regEffectsPanel.clearEffects();
                rhythEffectsPanel.clearEffects();
                new Thread(() ->
                           {
                               try {
                                   for (Aurora device : devices) {
                                       for (Effect effect : device.effects().getAllEffects()) {
                                           if (effect.getAnimType() == Effect.Type.PLUGIN &&
                                                   effect.getPluginType().equals("rhythm")) {
                                               rhythEffectsPanel.addEffect(effect.getName());
                                           } else {
                                               regEffectsPanel.addEffect(effect.getName());
                                           }
                                       }
                                   }
                               } catch (StatusCodeException sce) {
                                   sce.printStackTrace();
                               }

                               BasicEffects.initializeBasicEffects();
                               try {
                                   List<Effect> effects = BasicEffects.getBasicEffects(devices).get(0);
                                   for (Effect ef : effects) {
                                       basicEffectsPanel.addEffect(ef.getName());
                                   }
                               } catch (StatusCodeException sce) {
                                   sce.printStackTrace();
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

    public void loadStateComponents() throws StatusCodeException {
        if (devices[0].state().getOn()) {
            infoPanel.getBtnOnOff().setText("Turn Off");
        } else {
            infoPanel.getBtnOnOff().setText("Turn On");
        }

        infoPanel.setSliderBrightness(devices[0].state().getBrightness());
        infoPanel.setSliderColorTemp(devices[0].state().getColorTemperature());

        loadActiveScene();
    }

    public void loadActiveScene() throws StatusCodeException {
        String currentEffect = devices[0].effects().getCurrentEffectName();
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
        } catch (StatusCodeException sce) {
            sce.printStackTrace();
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
                    EventQueue.invokeLater(() ->
                                           {
                                               lblTitle.setText("Connected to " + groupName);
                                               loadAuroraData();
                                           });
                }
            } else {
                if (uiEnabled) {
                    resetDataFile();
                }
            }
        } else {
            String[] data = lastSession.split(" ");
            try {
                devices = new Aurora[1];
                devices[0] = new Aurora(data[0],
                                        Integer.parseInt(data[1]),
                                        data[2], data[3]);
                if (uiEnabled) {
                    EventQueue.invokeLater(() ->
                                           {
                                               loadDeviceName();
                                           });
                }
            } catch (StatusCodeException | HttpRequestException schre) {
                if (uiEnabled) {
                    new TextDialog(Main.this, "Failed to connect to the device. " +
                            "Please try again.").setVisible(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (uiEnabled) {
                    resetDataFile();
                }
            }
        }
    }

    private void setupNewAurora() {
        EventQueue.invokeLater(() ->
                               {
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
                        setDeviceName(devices[0].getHostName(), name);
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
        Aurora[] auroraDevices = new Aurora[groupDevices.length];
        for (int i = 0; i < groupDevices.length; i++) {
            try {
                auroraDevices[i] = new Aurora(groupDevices[i].getHostName(),
                                              groupDevices[i].getPort(), "v1", groupDevices[i].getAccessToken());
            } catch (HttpRequestException hre) {
                new TextDialog(this, "The device " + groupDevices[i].getHostName() +
                        " is offline.").setVisible(true);
            } catch (UnauthorizedException uae) {
                new TextDialog(this, "The device " + groupDevices[i].getHostName() +
                        " is unauthorized.").setVisible(true);
            } catch (StatusCodeException sce) {
                new TextDialog(this, "Unknown connection error for device " +
                        groupDevices[i].getHostName() + ".").setVisible(true);
            }
        }
        setDevices(auroraDevices);
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
                                                            OptionDialog dialog
                                                                    = (OptionDialog) ((JButton) e.getSource())
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
                                                            OptionDialog dialog
                                                                    = (OptionDialog) ((JButton) e.getSource())
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

    private void loadDeviceName() {
        if (devices.length == 1) {
            String deviceName = getDeviceName(devices[0].getHostName());
            if (deviceName != null) {
                lblTitle.setText("Connected to " + deviceName);
            } else {
                lblTitle.setText("Connected to " + devices[0].getName());
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
        canvas = new PanelCanvas(devices);
        canvas.setLayout(new GridBagLayout());
        canvas.setBorder(new TitledBorder(new LineBorder(Color.GRAY),
                                          "Preview", TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
        ((javax.swing.border.TitledBorder) canvas.getBorder())
                .setTitleFont(new Font("Tahoma", Font.BOLD, 22));
        contentPane.add(canvas, "cell 1 1 1 2,grow");
    }

    private void initEffectsPanels() {
        basicEffectsPanel = new EffectsPanel("Basic Effects", this, devices, canvas);
        getContentPane().add(basicEffectsPanel, "cell 0 1,grow");

        regEffectsPanel = new EffectsPanel("Color Effects", this, devices, canvas);
        getContentPane().add(regEffectsPanel, "cell 0 2,grow");

        rhythEffectsPanel = new EffectsPanel("Rhythm Effects", this, devices, canvas);
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
                    EventQueue.invokeLater(() ->
                                           {
                                               discoveryPanel.addTopEffects(1, new ArrayList<String>());
                                           });
                }
            }
        });
        contentPane.add(editor_1, "cell 1 3,grow");

        infoPanel = new InformationPanel(this, devices, canvas);
        editor_1.setFont(new Font("Tahoma", Font.BOLD, 17));
        editor_1.addTab("Control", null, infoPanel, null);

        discoveryPanel = new DiscoveryPanel(devices);
        editor_1.addTab("Discovery", null, discoveryPanel, null);

        ambilightPanel = new AmbilightPanel(canvas);
        editor_1.addTab("Ambient Lighting", null, ambilightPanel, null);

        spotifyPanel = new SpotifyPanel(devices, canvas);
        editor_1.addTab("Spotify Visualizer", null, spotifyPanel, null);

        shortcutsPanel = new KeyShortcutsPanel(devices);
        shortcutsPanel.setBorder(new LineBorder(Color.GRAY, 1, true));
        editor_1.addTab("Shortcuts", null, shortcutsPanel, null);

        AuroraNullListener anl = new AuroraNullListener(20, null,
                                                        infoPanel, canvas, discoveryPanel, ambilightPanel,
                                                        spotifyPanel, shortcutsPanel);
        anl.start();
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
                    EventQueue.invokeLater(() ->
                                           {
                                               canvas.initCanvas();
                                               canvas.repaint();
                                           });
                }
            }
        });

        if (devices != null && devices[0] != null) {
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
                                       if (e.getNewState() == JFrame.ICONIFIED) {
                                           hideToSystemTray();
                                       } else if (e.getNewState() == JFrame.NORMAL ||
                                               e.getNewState() == JFrame.MAXIMIZED_BOTH) {
                                           openFromSystemTray();
                                       }
                                   });
        } else {
            System.err.println("INFO: System tray not supported by OS. " +
                                       "Minimize to tray feature has been disabled.\n");
        }
    }

    public static void main(String[] args) {
        String[] actions = getActionArgs(args);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Main frame = new Main(actions);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class ActionHandler {
        public ActionHandler(String[] actions) {
            init();
            for (String action : actions) {
                Action actionObj = parseAction(action);
                if (actionObj != null) {
                    try {
                        actionObj.execute(devices, new Effect[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("error - failed to execute action");
                        System.exit(4);
                    }
                } else {
                    System.out.println("error - invalid action");
                    System.exit(2);
                }
            }
            System.exit(0);
        }

        private void init() {
            PropertyManager manager = new PropertyManager(PROPERTIES_FILEPATH);
            String lastSession = manager.getProperty("lastSession");

            if (lastSession != null) {
                setupOldAurora(lastSession);
            } else {
                System.out.println("error - device not set up");
                System.exit(1);
            }
        }

        private Action parseAction(String action) {
            action = action.replace("{", "").replace("}", "");
            String[] actionData = action.split(",");
            ActionType type = null;
            for (ActionType at : ActionType.values()) {
                if (at.toString().toUpperCase().equals(actionData[0].toUpperCase())) {
                    type = at;
                    break;
                }
            }
            if (type == ActionType.NEXT_EFFECT || type == ActionType.PREVIOUS_EFFECT) {
                System.out.println("error - action unsupported in this mode");
                System.exit(3);
            }
            Object[] args = new Object[1];
            if (actionData.length > 1) {
                if (type != null && type != ActionType.SET_EFFECT) {
                    args = new Object[]{Integer.parseInt(actionData[1])};
                } else {
                    args = new Object[]{actionData[1]};
                }
            }
            return type != null ? new Action(type, args) : null;
        }
    }

    private static String[] getActionArgs(String[] args) {
        List<String> arglist = new ArrayList<String>();
        String arg = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a") || args[i].equals("-action")) {
                while (++i < args.length && !arg.contains("}")) {
                    arg += args[i];
                    if (!args[i].contains(",") && i + 1 < args.length &&
                            !args[i + 1].equals("-a") &&
                            !args[i + 1].equals("-action")) {
                        arg += " ";
                    }
                }
                arglist.add(arg);
                arg = "";
                i--;
            }
        }
        return arglist.isEmpty() ? null : arglist.toArray(new String[]{});
    }
}
