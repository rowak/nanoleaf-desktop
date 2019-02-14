package io.github.rowak.nanoleafdesktop.ui.button;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.ui.dialog.AboutDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.DeviceChangerDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.OptionDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.menu.ModernMenuItem;
import io.github.rowak.nanoleafdesktop.ui.menu.ModernPopupMenu;

public class MenuButton extends ModernButton
{
	public MenuButton()
	{
		init();
	}
	
	private void init()
	{
		JPopupMenu menu = new ModernPopupMenu();
		JMenuItem itemChangeDevice = new ModernMenuItem("Change Device");
		itemChangeDevice.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new DeviceChangerDialog((Main)MenuButton.this.getFocusCycleRootAncestor())
					.setVisible(true);
			}
		});
		JMenuItem itemResetSettings = new ModernMenuItem("Reset Settings");
		itemResetSettings.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				resetSettings();
			}
		});
		JMenuItem itemAbout = new ModernMenuItem("About");
		itemAbout.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new AboutDialog(MenuButton.this.getFocusCycleRootAncestor())
					.setVisible(true);
			}
		});
		menu.add(itemChangeDevice);
		menu.add(itemResetSettings);
		menu.add(itemAbout);
		
		URL menuIconUnpressedPath =
				Main.class.getResource("resources/images/menu_button_icon_unpressed.png");
		URL menuIconPressedPath =
				Main.class.getResource("resources/images/menu_button_icon_pressed.png");
		setIcon(new ImageIcon(menuIconUnpressedPath));
		setPressedIcon(new ImageIcon(menuIconPressedPath));
		setBorder(null);
		addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				menu.show(MenuButton.this, getX(), getY());
			}
		});
	}
	
	private void resetSettings()
	{
		new OptionDialog(this.getFocusCycleRootAncestor(),
				"Are you sure you want to reset all of your settings?",
				"Yes", "No", new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
						manager.clearProperties();
						String message = "Done. This will take effect the next " +
								"time you open the application.";
						new TextDialog(MenuButton.this.getFocusCycleRootAncestor(), message)
							.setVisible(true);
						JButton source = (JButton)e.getSource();
						((OptionDialog)source.getFocusCycleRootAncestor()).dispose();
					}
				}, new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						JButton source = (JButton)e.getSource();
						((OptionDialog)source.getFocusCycleRootAncestor()).dispose();
					}
				}).setVisible(true);
	}
}
