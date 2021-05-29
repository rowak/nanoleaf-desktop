package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafdesktop.shortcuts.Action;
import io.github.rowak.nanoleafdesktop.shortcuts.ActionType;
import io.github.rowak.nanoleafdesktop.shortcuts.RunType;
import io.github.rowak.nanoleafdesktop.shortcuts.Shortcut;
import io.github.rowak.nanoleafdesktop.shortcuts.ShortcutManager;
import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.combobox.ModernComboBox;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowDragListener;
import io.github.rowak.nanoleafdesktop.ui.textfield.ModernTextField;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ShortcutCreatorDialog extends JDialog {
	
	private NanoleafGroup group;
	private Component parent;
	private Shortcut oldShortcut;
	private JPanel contentPane;
	private JLabel lblEffect;
	private JLabel lblNumberField;
	private JComboBox<String> cmbxActionType;
	private JComboBox<String> cmbxRunType;
	private JComboBox<String> cmbxEffect;
	private JTextField txtKeys;
	private JTextField txtName;
	private JTextField txtNumberField;
	private JTextField txtAppName;
	private JButton btnCreate;
	private JLabel lblRunType, lblKeys, lblAppName;
	private List<Component> extraUI;
	
	/**
	 * @wbp.parser.constructor
	 */
	public ShortcutCreatorDialog(Component parent, NanoleafGroup group) {
		this.parent = parent;
		this.group = group;
		initUI(parent);
	}
	
	public ShortcutCreatorDialog(Component parent, Shortcut shortcut, NanoleafGroup group) {
		this.parent = parent;
		oldShortcut = shortcut;
		this.group = group;
		initUI(parent);
		loadUIFromShortcut(shortcut);
	}
	
	private void loadUIFromShortcut(Shortcut shortcut) {
		String name = shortcut.getName();
		txtName.setText(name);
		
		String actionType = enumValueToName(shortcut.getAction().getType());
		List<String> actionTypes = Arrays.asList(getActionTypes());
		int actionIndex = actionTypes.indexOf(actionType);
		cmbxActionType.setSelectedIndex(actionIndex);
		
		List<String> keysList = shortcut.getKeys();
		StringBuilder keys = new StringBuilder();
		for (int i = 0; i < keysList.size(); i++) {
			String key = keysList.get(i);
			keys.append(key);
			if (i < keysList.size()-1) {
				keys.append(" + ");
			}
		}
		txtKeys.setText(keys.toString());
		
		RunType runType = shortcut.getRunType();
		if (runType == RunType.WHEN_PRESSED) {
			cmbxRunType.setSelectedIndex(0);
		}
		else if (runType == RunType.WHILE_HELD) {
			cmbxRunType.setSelectedIndex(1);
		}
		else if (runType == RunType.WHEN_APP_RUN) {
			cmbxRunType.setSelectedIndex(2);
			txtAppName.setText((String)shortcut.getAction().getArgs()[1]);
		}
		else if (runType == RunType.WHEN_APP_CLOSED) {
			cmbxRunType.setSelectedIndex(3);
			txtAppName.setText((String)shortcut.getAction().getArgs()[1]);
		}
		
		if (cmbxEffect != null) {
			String effect = (String)shortcut.getAction().getArgs()[0];
			List<String> effects = Arrays.asList(getEffects());
			int effectIndex = effects.indexOf(effect);
			cmbxEffect.setSelectedIndex(effectIndex);
		}
		else if (txtNumberField != null) {
			int value = (int)shortcut.getAction().getArgs()[0];
			txtNumberField.setText(value + "");
		}
	}
	
	private void initUI(Component parent) {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(400, 225);
		setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new LineBorder(new Color(128, 128, 128), 2));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[grow][244.00,grow]", "[][][][][][grow]"));
		
		extraUI = new ArrayList<Component>();
		
		WindowDragListener wdl = new WindowDragListener(50);
		addMouseListener(wdl);
		addMouseMotionListener(wdl);
		
		CloseButton btnClose = new CloseButton(this, JFrame.DISPOSE_ON_CLOSE);
		contentPane.add(btnClose, "gapx 0 15, cell 1 0,alignx right");
		
		JLabel lblName = new JLabel("Name");
		lblName.setForeground(Color.WHITE);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 22));
		contentPane.add(lblName, "cell 0 1,gapx 0 15");
		
		txtName = new ModernTextField();
		contentPane.add(txtName, "cell 1 1,growx");
		
		JLabel lblType = new JLabel("Event");
		lblType.setForeground(Color.WHITE);
		lblType.setFont(new Font("Tahoma", Font.PLAIN, 22));
		contentPane.add(lblType, "cell 0 2,gapx 0 15");
		
		cmbxActionType = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(getActionTypes()));
		cmbxActionType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateHiddenUI();
			}
		});
		contentPane.add(cmbxActionType, "cell 1 2,growx");
		
		lblRunType = new JLabel("Run");
		lblRunType.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblRunType.setForeground(Color.WHITE);
		contentPane.add(lblRunType, "cell 0 3");
		
		String[] runTypes = getPlatformRunTypes();
		cmbxRunType = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(runTypes));
		cmbxRunType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateHiddenUI();
				showRunTypeHelpMessage();
			}
		});
		contentPane.add(cmbxRunType, "cell 1 3,growx");
		
		btnCreate = new ModernButton("Create");
		btnCreate.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnCreate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Shortcut shortcut = createShortcut();
				if (shortcut != null) {
					if (oldShortcut != null) {
						ShortcutManager.removeShortcut(shortcut.getName());
					}
					ShortcutManager.saveShortcut(shortcut);
					dispose();
				}
			}
		});
		contentPane.add(btnCreate, "cell 0 5 2 1,alignx center");
		
		lblEffect = new JLabel("Effect");
		lblEffect.setForeground(Color.WHITE);
		lblEffect.setFont(new Font("Tahoma", Font.PLAIN, 22));
		
		cmbxEffect = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(getEffects()));
		
		lblNumberField = new JLabel("Value");
		lblNumberField.setForeground(Color.WHITE);
		lblNumberField.setFont(new Font("Tahoma", Font.PLAIN, 22));
		
		txtNumberField = new ModernTextField();
		
		lblKeys = new JLabel("Trigger");
		lblKeys.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblKeys.setForeground(Color.WHITE);
		
		txtKeys = new ModernTextField("Click here to set keys");
		txtKeys.setEditable(false);
		txtKeys.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				txtKeys.setText("Waiting for input...");
			}
		});
		txtKeys.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (txtKeys.hasFocus()) {
					String key = KeyEvent.getKeyText(e.getKeyCode());
					if (!txtKeys.getText().equals("Waiting for input...")) {
						List<String> keys = getSelectedKeys();
						if (!keys.contains(key)) {
							StringBuilder keysStr = new StringBuilder();
							for (String k : keys) {
								keysStr.append(k + " + ");
							}
							keysStr.append(key);
							txtKeys.setText(keysStr.toString());
						}
					}
					else {
						txtKeys.setText(key);
					}
				}
			}
		});
		
		lblAppName = new JLabel("Name");
		lblAppName.setForeground(Color.WHITE);
		lblAppName.setFont(new Font("Tahoma", Font.PLAIN, 22));
		
		txtAppName = new ModernTextField();
		
		resize();
	}
	
	private Shortcut createShortcut() {
		String name = txtName.getText();
		ActionType actionType = getSelectedActionType();
		List<String> keys = getSelectedKeys();
		RunType runType = getSelectedRunType();
		
		if (userInputValid()) {
			Object[] args = null;
			if (cmbxEffect != null) {
				String effect = (String)cmbxEffect.getSelectedItem();
				args = new Object[]{effect};
			}
			else if (txtNumberField != null) {
				int value = getNumberFieldValue();
				args = new Object[]{value};
			}
			
			if (runType == RunType.WHEN_APP_RUN ||
						runType == RunType.WHEN_APP_CLOSED) {
				args = new Object[]{args[0], txtAppName.getText()};
			}
			Action action = new Action(actionType, args);
			return new Shortcut(name, keys, runType, action);
		}
		else {
			String message = "You must fill out all fields to continue.";
			new TextDialog(this, message).setVisible(true);
			return null;
		}
	}
	
	private String enumValueToName(Enum enumValue) {
		char[] type = enumValue.toString().toLowerCase().toCharArray();
		type[0] = (type[0] + "").toUpperCase().charAt(0);
		for (int j = 0; j < type.length; j++) {
			if (type[j] == '_') {
				type[j+1] = (type[j+1] + "").toUpperCase().charAt(0);
			}
		}
		return new String(type).replace("_", " ");
	}
	
	private String[] getActionTypes() {
		String[] types = new String[ActionType.values().length+1];
		types[0] = "Select an event...";
		for (int i = 0; i < ActionType.values().length; i++) {
			char[] type = ActionType.values()[i].toString().toLowerCase().toCharArray();
			type[0] = (type[0] + "").toUpperCase().charAt(0);
			for (int j = 0; j < type.length; j++) {
				if (type[j] == '_') {
					type[j+1] = (type[j+1] + "").toUpperCase().charAt(0);
				}
			}
			types[i+1] = new String(type).replace("_", " ");
		}
		return types;
	}
	
	private ActionType getSelectedActionType() {
		int index = cmbxActionType.getSelectedIndex()-1;
		ActionType type = index >= 0 ? ActionType.values()[index] : null;
		return type;
	}
	
	private RunType getSelectedRunType() {
		int index = cmbxRunType.getSelectedIndex();
		RunType type = index >= 0 ? RunType.values()[index] : null;
		return type;
	}
	
	private List<String> getSelectedKeys() {
		if (!txtKeys.getText().equals("Click here to set keys")) {
			return Arrays.asList(txtKeys.getText().split(" \\+ "));
		}
		return Arrays.asList(new String[]{});
	}
	
	private String[] getEffects() {
		List<String> effectsList = new ArrayList<String>();
		try {
			effectsList = group.getAllEffectsList();
		}
		catch (NanoleafException | IOException e) {
			e.printStackTrace();
		}
		String[] effects = new String[effectsList.size()];
		for (int i = 0; i < effects.length; i++) {
			effects[i] = effectsList.get(i);
		}
		return effects;
	}
	
	private int getNumberFieldValue() {
		String text = txtNumberField.getText();
		try {
			return Integer.parseInt(text);
		}
		catch (Exception e) {
			// Message shown later
			e.printStackTrace();
		}
		return -1;
	}
	
	private boolean userInputValid() {
		if (!txtName.getText().isEmpty() &&
				getSelectedActionType() != null) {
			RunType runType = getSelectedRunType();
			if (runType == RunType.WHEN_PRESSED ||
					runType == RunType.WHILE_HELD) {
				if (extraUI.contains(txtNumberField) &&
						!txtNumberField.getText().isEmpty()) {
					return getNumberFieldValue() != -1;
				}
				else if (extraUI.contains(txtAppName)) {
					return !txtAppName.getText().isEmpty();
				}
				else {
					return true;
				}
			}
			else if (runType == RunType.WHEN_APP_RUN ||
					runType == RunType.WHEN_APP_CLOSED) {
				if (extraUI.contains(txtNumberField) &&
						!txtNumberField.getText().isEmpty()) {
					return getNumberFieldValue() != -1;
				}
				else if (extraUI.contains(txtAppName)) {
					return !txtAppName.getText().isEmpty();
				}
				else {
					return true;
				}
			}
		}
		return false;
	}
	
	private static ActionType nameToActionType(String name) {
		name = name.toUpperCase().replace(' ', '_');
		for (ActionType type : ActionType.values()) {
			if (name.equals(type.toString())) {
				return type;
			}
		}
		return null;
	}
	
	// Disable some run types on certain platforms
	private String[] getPlatformRunTypes() {
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return new String[]{"When key(s) pressed",
					"While key(s) pressed", "When an application is run",
					"When an application is closed"};
		}
		else {
			return new String[]{"When key(s) pressed", "While key(s) pressed"};
		}
	}
	
	private void showRunTypeHelpMessage() {
		if (getSelectedRunType() == RunType.WHEN_APP_RUN ||
				getSelectedRunType() == RunType.WHEN_APP_CLOSED) {
			new TextDialog(parent, "Find the name of the application in task manager by " +
					"right-clicking on the program and then clicking \"properties\".").setVisible(true);
		}
	}
	
	private void updateHiddenUI() {
		String typeName = (String)cmbxActionType.getSelectedItem();
		ActionType actionType = nameToActionType(typeName);
		RunType runType = getSelectedRunType();
		if (actionType == ActionType.SET_EFFECT) {
			if (extraUI.contains(txtNumberField)) {
				removeExtraUI(lblNumberField);
				removeExtraUI(txtNumberField);
			}
			addExtraUI(lblEffect, "");
			addExtraUI(cmbxEffect, "growx");
		}
		else if ((actionType == ActionType.DECREASE_BRIGHTNESS ||
				actionType == ActionType.DECREASE_COLOR_TEMP ||
				actionType == ActionType.INCREASE_BRIGHTNESS ||
				actionType == ActionType.INCREASE_COLOR_TEMP ||
				actionType == ActionType.SET_BRIGHTNESS ||
				actionType == ActionType.SET_COLOR_TEMP ||
				actionType == ActionType.SET_HUE ||
				actionType == ActionType.SET_SATURATION ||
				actionType == ActionType.SET_RED ||
				actionType == ActionType.SET_GREEN ||
				actionType == ActionType.SET_BLUE)) {
			if (extraUI.contains(cmbxEffect)) {
				removeExtraUI(lblEffect);
				removeExtraUI(cmbxEffect);
			}
			addExtraUI(lblNumberField, "");
			addExtraUI(txtNumberField, "growx");
		}
		else {
			if (extraUI.contains(cmbxEffect)) {
				removeExtraUI(lblEffect);
				removeExtraUI(cmbxEffect);
			}
			if (extraUI.contains(txtNumberField)) {
				removeExtraUI(lblNumberField);
				removeExtraUI(txtNumberField);
			}
		}
		
		if (runType == RunType.WHEN_PRESSED ||
				runType == RunType.WHILE_HELD) {
			if (extraUI.contains(txtAppName)) {
				removeExtraUI(lblAppName);
				removeExtraUI(txtAppName);
			}
			addExtraUI(lblKeys, "gapx 0 15");
			addExtraUI(txtKeys, "flowx, growx");
		}
		else if (runType == RunType.WHEN_APP_RUN ||
				runType == RunType.WHEN_APP_CLOSED) {
			if (extraUI.contains(txtKeys)) {
				removeExtraUI(lblKeys);
				removeExtraUI(txtKeys);
			}
			addExtraUI(lblAppName, "");
			addExtraUI(txtAppName, "growx");
		}
		else {
			if (extraUI.contains(txtKeys)) {
				removeExtraUI(lblKeys);
				removeExtraUI(txtKeys);
			}
			if (extraUI.contains(txtAppName))
			{
				removeExtraUI(lblAppName);
				removeExtraUI(txtAppName);
			}
		}
	}
	
	private void addExtraUI(Component component, String layoutArgs) {
		if (!extraUI.contains(component)) {
			contentPane.remove(btnCreate);
			String layout = "cell ";
			layout += component instanceof JLabel ? "0 " : "1 ";
			layout += getNextRow();
			layout += !layoutArgs.equals("") ? "," + layoutArgs : "";
			extraUI.add(component);
			contentPane.add(component, layout);
			contentPane.add(btnCreate, "cell 1 " + (getNextRow()+1) + ",alignx right");
			resize();
		}
	}
	
	private void removeExtraUI(Component component) {
		contentPane.remove(btnCreate);
		contentPane.remove(component);
		extraUI.remove(component);
		contentPane.add(btnCreate, "cell 1 " + getNextRow() + ",alignx right");
		resize();
	}
	
	private int getNextRow() {
		int numComponents = contentPane.getComponentCount();
		return (numComponents+1)/2;
	}
	
	private void resize() {
		pack();
		setLocationRelativeTo(parent);
	}
}
