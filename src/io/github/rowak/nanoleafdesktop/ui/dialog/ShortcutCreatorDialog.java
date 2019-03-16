package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

import org.jnativehook.keyboard.NativeKeyEvent;

import io.github.rowak.Aurora;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.shortcuts.Action;
import io.github.rowak.nanoleafdesktop.shortcuts.ActionType;
import io.github.rowak.nanoleafdesktop.shortcuts.RunType;
import io.github.rowak.nanoleafdesktop.shortcuts.Shortcut;
import io.github.rowak.nanoleafdesktop.shortcuts.ShortcutManager;
import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.combobox.ModernComboBox;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowDragListener;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class ShortcutCreatorDialog extends JDialog
{
	private Aurora device;
	private Component parent;
	private Shortcut oldShortcut;
	private JPanel contentPane;
	private JLabel lblEffect;
	private JLabel lblNumberField;
	private JComboBox<String> cmbxActionType;
	private JComboBox<String> cmbxRunType;
	private JComboBox<String> cmbxEffect;
	private JComboBox<String> cmbxKey1;
	private JComboBox<String> cmbxKey2;
	private JTextField txtName;
	private JTextField txtNumberField;
	private JButton btnCreate;
	private JLabel lblRunType;
	
	/**
	 * @wbp.parser.constructor
	 */
	public ShortcutCreatorDialog(Component parent, Aurora device)
	{
		this.parent = parent;
		this.device = device;
		initUI(parent);
	}
	
	public ShortcutCreatorDialog(Component parent, Shortcut shortcut, Aurora device)
	{
		this.parent = parent;
		oldShortcut = shortcut;
		this.device = device;
		initUI(parent);
		loadUIFromShortcut(shortcut);
	}
	
	private void loadUIFromShortcut(Shortcut shortcut)
	{
		String name = shortcut.getName();
		txtName.setText(name);
		
		String actionType = enumValueToName(shortcut.getAction().getType());
		List<String> actionTypes = Arrays.asList(getActionTypes());
		int actionIndex = actionTypes.indexOf(actionType);
		cmbxActionType.setSelectedIndex(actionIndex);
		
		List<String> keys = Arrays.asList(getKeys());
		String key1 = shortcut.getKeys().get(0);
		int key1Index = keys.indexOf(key1);
		cmbxKey1.setSelectedIndex(key1Index);
		if (shortcut.getKeys().size() > 1)
		{
			String key2 = shortcut.getKeys().get(1);
			int key2Index = keys.indexOf(key2);
			cmbxKey2.setSelectedIndex(key2Index);
		}
		
		RunType runType = shortcut.getRunType();
		if (runType == RunType.WHEN_PRESSED)
		{
			cmbxRunType.setSelectedIndex(0);
		}
		else if (runType == RunType.WHILE_HELD)
		{
			cmbxRunType.setSelectedIndex(1);
		}
		
		if (cmbxEffect != null)
		{
			String effect = (String)shortcut.getAction().getArgs()[0];
			List<String> effects = Arrays.asList(getEffects());
			int effectIndex = effects.indexOf(effect);
			cmbxEffect.setSelectedIndex(effectIndex);
		}
		else if (txtNumberField != null)
		{
			int value = (int)shortcut.getAction().getArgs()[0];
			txtNumberField.setText(value + "");
		}
	}
	
	private void initUI(Component parent)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(400, 225);
		setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new LineBorder(new Color(128, 128, 128), 2));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[grow][244.00,grow]", "[][][][][][grow]"));
		
		WindowDragListener wdl = new WindowDragListener(50);
		addMouseListener(wdl);
		addMouseMotionListener(wdl);
		
		CloseButton btnClose = new CloseButton(this, JFrame.DISPOSE_ON_CLOSE);
		contentPane.add(btnClose, "gapx 0 15, cell 1 0,alignx right");
		
		JLabel lblName = new JLabel("Name");
		lblName.setForeground(Color.WHITE);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 22));
		contentPane.add(lblName, "cell 0 1,gapx 0 15");
		
		txtName = new JTextField();
		txtName.setForeground(Color.WHITE);
		txtName.setBackground(Color.DARK_GRAY);
		txtName.setBorder(new LineBorder(Color.GRAY));
		txtName.setCaretColor(Color.WHITE);
		txtName.setFont(new Font("Tahoma", Font.PLAIN, 22));
		txtName.setColumns(10);
		contentPane.add(txtName, "cell 1 1,growx");
		
		JLabel lblType = new JLabel("Event");
		lblType.setForeground(Color.WHITE);
		lblType.setFont(new Font("Tahoma", Font.PLAIN, 22));
		contentPane.add(lblType, "cell 0 2,gapx 0 15");
		
		cmbxActionType = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(getActionTypes()));
		cmbxActionType.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateHiddenUI();
			}
		});
		contentPane.add(cmbxActionType, "cell 1 2,growx");
		
		JLabel lblKeys = new JLabel("Trigger");
		lblKeys.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblKeys.setForeground(Color.WHITE);
		contentPane.add(lblKeys, "cell 0 3,gapx 0 15");
		
		cmbxKey1 = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(getKeys()));
		contentPane.add(cmbxKey1, "flowx,cell 1 3,growx");
		
		lblRunType = new JLabel("Run");
		lblRunType.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblRunType.setForeground(Color.WHITE);
		contentPane.add(lblRunType, "cell 0 4");
		
		String[] runTypes = new String[]{"When key(s) pressed",
				"While key(s) pressed"};
		cmbxRunType = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(runTypes));
		contentPane.add(cmbxRunType, "cell 1 4,growx");
		
		JLabel lblSeparator = new JLabel("+");
		lblSeparator.setForeground(Color.WHITE);
		lblSeparator.setFont(new Font("Tahoma", Font.PLAIN, 22));
		contentPane.add(lblSeparator, "cell 1 3");
		
		cmbxKey2 = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(getKeys()));
		contentPane.add(cmbxKey2, "cell 1 3,growx");
		
		btnCreate = new ModernButton("Create");
		btnCreate.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnCreate.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Shortcut shortcut = createShortcut();
				if (shortcut != null)
				{
					if (oldShortcut != null)
					{
						ShortcutManager.removeShortcut(shortcut.getName());
					}
					ShortcutManager.saveShortcut(shortcut);
					dispose();
				}
			}
		});
		contentPane.add(btnCreate, "cell 0 5 2 1,alignx center");
		
		resize();
	}
	
	private Shortcut createShortcut()
	{
		String name = txtName.getText();
		ActionType actionType = getSelectedActionType();
		List<String> keys = getSelectedKeys();
		RunType runType = getSelectedRunType();
		
		if (userInputValid())
		{
			Object[] args = null;
			if (cmbxEffect != null)
			{
				String effect = (String)cmbxEffect.getSelectedItem();
				args = new Object[]{effect};
			}
			else if (txtNumberField != null)
			{
				int value = getNumberFieldValue();
				args = new Object[]{value};
			}
			Action action = new Action(actionType, args);
			return new Shortcut(name, keys, runType, action);
		}
		else
		{
			String message = "You must fill out all fields to continue.";
			new TextDialog(this, message).setVisible(true);
			return null;
		}
	}
	
	private String enumValueToName(Enum enumValue)
	{
		char[] type = enumValue.toString().toLowerCase().toCharArray();
		type[0] = (type[0] + "").toUpperCase().charAt(0);
		for (int j = 0; j < type.length; j++)
		{
			if (type[j] == '_')
			{
				type[j+1] = (type[j+1] + "").toUpperCase().charAt(0);
			}
		}
		return new String(type).replace("_", " ");
	}
	
	private String[] getActionTypes()
	{
		String[] types = new String[ActionType.values().length+1];
		types[0] = "Select an event...";
		for (int i = 0; i < ActionType.values().length; i++)
		{
			char[] type = ActionType.values()[i].toString().toLowerCase().toCharArray();
			type[0] = (type[0] + "").toUpperCase().charAt(0);
			for (int j = 0; j < type.length; j++)
			{
				if (type[j] == '_')
				{
					type[j+1] = (type[j+1] + "").toUpperCase().charAt(0);
				}
			}
			types[i+1] = new String(type).replace("_", " ");
		}
		return types;
	}
	
	private String[] getKeys()
	{
		Field[] fields = NativeKeyEvent.class.getDeclaredFields();
		List<String> keys = new ArrayList<String>();
		keys.add("Not assigned");
		for (Field field : fields)
		{
			if (Modifier.isPublic(field.getModifiers()) &&
					Modifier.isStatic(field.getModifiers()))
			{
				try
				{
					int key = field.getInt(null);
					String keyName = NativeKeyEvent.getKeyText(key);
					if (!keyName.equals("") && !keyName.startsWith("Unknown") &&
							!keyName.startsWith("Undefined"))
					{
						keys.add(keyName);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return keys.toArray(new String[]{});
	}
	
	private ActionType getSelectedActionType()
	{
		int index = cmbxActionType.getSelectedIndex()-1;
		ActionType type = index >= 0 ? ActionType.values()[index] : null;
		return type;
	}
	
	private RunType getSelectedRunType()
	{
		int index = cmbxRunType.getSelectedIndex();
		RunType type = index >= 0 ? RunType.values()[index] : null;
		return type;
	}
	
	private List<String> getSelectedKeys()
	{
		List<String> keys = new ArrayList<String>();
		if (cmbxKey1.getSelectedIndex() != 0)
		{
			keys.add((String)cmbxKey1.getSelectedItem());
		}
		if (cmbxKey2.getSelectedIndex() != 0)
		{
			keys.add((String)cmbxKey2.getSelectedItem());
		}
		return keys;
	}
	
	private String[] getEffects()
	{
		try
		{
			return device.effects().getEffectsList();
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
		return null;
	}
	
	private int getNumberFieldValue()
	{
		String text = txtNumberField.getText();
		try
		{
			return Integer.parseInt(text);
		}
		catch (Exception e)
		{
			// Message shown later
			e.printStackTrace();
		}
		return -1;
	}
	
	private boolean userInputValid()
	{
		if (!txtName.getText().isEmpty() && getSelectedActionType() != null &&
				!getSelectedKeys().isEmpty() && getSelectedRunType() != null)
		{
			if (txtNumberField != null)
			{
				return getNumberFieldValue() != -1;
			}
			else
			{
				return true;
			}
		}
		return false;
	}
	
	private static ActionType nameToActionType(String name)
	{
		name = name.toUpperCase().replace(' ', '_');
		for (ActionType type : ActionType.values())
		{
			if (name.equals(type.toString()))
			{
				return type;
			}
		}
		return null;
	}
	
	private void updateHiddenUI()
	{
		String typeName = (String)cmbxActionType.getSelectedItem();
		ActionType type = nameToActionType(typeName);
		if (type == ActionType.SET_EFFECT && cmbxEffect == null)
		{
			if (txtNumberField != null)
			{
				setNumberFieldVisible(false);
			}
			setEffectPickerVisible(true);
		}
		else if ((type == ActionType.DECREASE_BRIGHTNESS ||
				type == ActionType.DECREASE_COLOR_TEMP ||
				type == ActionType.INCREASE_BRIGHTNESS ||
				type == ActionType.INCREASE_COLOR_TEMP ||
				type == ActionType.SET_BRIGHTNESS ||
				type == ActionType.SET_COLOR_TEMP ||
				type == ActionType.SET_HUE ||
				type == ActionType.SET_SATURATION ||
				type == ActionType.SET_RED ||
				type == ActionType.SET_GREEN ||
				type == ActionType.SET_BLUE) &&
				txtNumberField == null)
		{
			if (cmbxEffect != null)
			{
				setEffectPickerVisible(false);
			}
			setNumberFieldVisible(true);
		}
		else
		{
			if (cmbxEffect != null)
			{
				setEffectPickerVisible(false);
			}
			if (txtNumberField != null)
			{
				setNumberFieldVisible(false);
			}
		}
	}
	
	private void setEffectPickerVisible(boolean visible)
	{
		if (visible)
		{
			lblEffect = new JLabel("Effect");
			lblEffect.setForeground(Color.WHITE);
			lblEffect.setFont(new Font("Tahoma", Font.PLAIN, 22));
			contentPane.add(lblEffect, "cell 0 5");
			
			cmbxEffect = new ModernComboBox<String>(
					new DefaultComboBoxModel<String>(getEffects()));
			contentPane.add(cmbxEffect, "cell 1 5,growx");
			
			contentPane.remove(btnCreate);
			contentPane.add(btnCreate, "cell 1 6,alignx right");
		}
		else
		{
			contentPane.remove(lblEffect);
			contentPane.remove(cmbxEffect);
			contentPane.remove(btnCreate);
			contentPane.add(btnCreate, "cell 1 5,alignx right");
			cmbxEffect = null;
		}
		resize();
	}
	
	private void setNumberFieldVisible(boolean visible)
	{
		if (visible)
		{
			lblNumberField = new JLabel("Value");
			lblNumberField.setForeground(Color.WHITE);
			lblNumberField.setFont(new Font("Tahoma", Font.PLAIN, 22));
			contentPane.add(lblNumberField, "cell 0 5");
			
			txtNumberField = new JTextField();
			txtNumberField.setForeground(Color.WHITE);
			txtNumberField.setBackground(Color.DARK_GRAY);
			txtNumberField.setBorder(new LineBorder(Color.GRAY));
			txtNumberField.setCaretColor(Color.WHITE);
			txtNumberField.setFont(new Font("Tahoma", Font.PLAIN, 22));
			txtNumberField.setColumns(10);
			contentPane.add(txtNumberField, "cell 1 5,growx");
			
			contentPane.remove(btnCreate);
			contentPane.add(btnCreate, "cell 1 6,alignx right");
		}
		else
		{
			contentPane.remove(lblNumberField);
			contentPane.remove(txtNumberField);
			contentPane.remove(btnCreate);
			contentPane.add(btnCreate, "cell 1 5,alignx right");
			txtNumberField = null;
		}
		resize();
	}
	
	private void resize()
	{
		pack();
		setLocationRelativeTo(parent);
	}
}
