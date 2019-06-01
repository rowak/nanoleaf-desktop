package io.github.rowak.nanoleafdesktop.ui.panel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import io.github.rowak.Aurora;
import io.github.rowak.nanoleafdesktop.shortcuts.RunType;
import io.github.rowak.nanoleafdesktop.shortcuts.Shortcut;
import io.github.rowak.nanoleafdesktop.shortcuts.ShortcutManager;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.dialog.ShortcutCreatorDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.listener.GlobalShortcutListener;
import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;
import net.miginfocom.swing.MigLayout;
import javax.swing.JList;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;

public class KeyShortcutsPanel extends JPanel
{
	private List<Shortcut> shortcuts;
	private DefaultListModel<Shortcut> model;
	private GlobalShortcutListener shortcutListener;
	private Aurora[] devices;
	private Timer refreshTimer;
	private Timer appRunningTimer;
	
	public KeyShortcutsPanel(Aurora[] devices)
	{
		this.devices = devices;
		shortcuts = new ArrayList<Shortcut>();
		model = new DefaultListModel<Shortcut>();
		refreshShortcuts();
		initUI();
		startKeyListener();
	}
	
	public void setAuroras(Aurora[] devices)
	{
		this.devices = devices;
		if (shortcutListener != null)
		{
			shortcutListener.setAuroras(devices);
		}
	}
	
	private void startRefreshTimer()
	{
		if (refreshTimer == null)
		{
			refreshTimer = new Timer();
			refreshTimer.scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					refreshShortcuts();
				}
			}, 1000, 1000);
		}
	}
	
	private void stopRefreshTimer()
	{
		if (refreshTimer != null)
		{
			refreshTimer.cancel();
			refreshTimer.purge();
			refreshTimer = null;
		}
	}
	
	private void startAppRunningTimer()
	{
		if (appRunningTimer == null)
		{
			appRunningTimer = new Timer();
			appRunningTimer.scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					shortcutListener.checkAppShortcuts();
				}
			}, 1000, 1000);
		}
	}
	
	private void refreshShortcuts()
	{
		Shortcut[] arr = ShortcutManager.getSavedShortcuts();
		if (arr.length > model.size())
		{
			for (Shortcut s : arr)
			{
				if (!shortcuts.contains(s))
				{
					shortcuts.add(s);
					model.addElement(s);
				}
				
				if (s.getRunType() == RunType.WHEN_APP_RUN ||
						s.getRunType() == RunType.WHEN_APP_CLOSED)
				{
					startAppRunningTimer();
				}
			}
			stopRefreshTimer();
		}
		
		for (Shortcut s : shortcuts)
		{
			boolean hashortcut = false;
			for (Shortcut arrS : arr)
			{
				if (s.equals(arrS))
				{
					hashortcut = true;
				}
			}
			if (!hashortcut)
			{
				shortcuts.remove(s);
				model.removeElement(s);
				break;
			}
		}
	}
	
	private void initUI()
	{
		setBackground(Color.DARK_GRAY);
		setLayout(new MigLayout("", "[grow]", "[grow][]"));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
		scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
		scrollPane.setBorder(null);
		add(scrollPane, "cell 0 0,grow");
		
		JList<Shortcut> shortcutsList = new JList<Shortcut>(model);
		shortcutsList.setBackground(Color.DARK_GRAY);
		shortcutsList.setFont(new Font("Tahoma", Font.PLAIN, 20));
		shortcutsList.setForeground(Color.WHITE);
		shortcutsList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					Shortcut s = shortcutsList.getSelectedValue();
					new ShortcutCreatorDialog(KeyShortcutsPanel.this.getFocusCycleRootAncestor(),
							s, devices).setVisible(true);
					startRefreshTimer();
				}
			}
		});
		scrollPane.setViewportView(shortcutsList);
		
		JButton btnAdd = new ModernButton("Add");
		btnAdd.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openShortcutCreatorDialog();
			}
		});
		add(btnAdd, "flowx,cell 0 1,growx");
		
		JButton btnRemove = new ModernButton("Remove");
		btnRemove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Shortcut s = shortcutsList.getSelectedValue();
				removeShortcut(s);
			}
		});
		add(btnRemove, "cell 0 1,growx");
		
		JButton btnEdit = new ModernButton("Edit");
		btnEdit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Shortcut s = shortcutsList.getSelectedValue();
				openShortcutEditorDialog(s);
			}
		});
		add(btnEdit, "cell 0 1,growx");
	}
	
	private void startKeyListener()
	{
		try
		{
			shortcutListener = new GlobalShortcutListener(shortcuts, devices);
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(shortcutListener);
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(Level.OFF);
		}
		catch (NativeHookException nhe)
		{
			nhe.printStackTrace();
			if (nhe.getMessage().equals("Failed to enable access for assistive devices.") &&
					System.getProperty("os.name").toLowerCase().contains("mac"))
			{
				new TextDialog(KeyShortcutsPanel.this.getFocusCycleRootAncestor(),
						"Please enable accessibility control to use the shortcuts feature.")
						.setVisible(true);
			}
		}
		catch (UnsatisfiedLinkError ule)
		{
			ule.printStackTrace();
			new TextDialog(KeyShortcutsPanel.this.getFocusCycleRootAncestor(),
					"Failed to setup shortcuts. Your platform may not be supported.")
					.setVisible(true);
		}
	}
	
	private void openShortcutCreatorDialog()
	{
		new ShortcutCreatorDialog(KeyShortcutsPanel.this.getFocusCycleRootAncestor(),
				devices).setVisible(true);
		startRefreshTimer();
	}
	
	private void openShortcutEditorDialog(Shortcut shortcut)
	{
		new ShortcutCreatorDialog(KeyShortcutsPanel.this.getFocusCycleRootAncestor(),
				shortcut, devices).setVisible(true);
		startRefreshTimer();
	}
	
	private void removeShortcut(Shortcut shortcut)
	{
		ShortcutManager.removeShortcut(shortcut.getName());
		if (shortcuts.contains(shortcut))
		{
			shortcuts.remove(shortcut);
			model.removeElement(shortcut);
		}
		refreshShortcuts();
	}
}
