package io.github.rowak.nanoleafdesktop.ui.button;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.ui.dialog.*;
import io.github.rowak.nanoleafdesktop.ui.menu.ModernMenuItem;
import io.github.rowak.nanoleafdesktop.ui.menu.ModernPopupMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class MenuButton extends ModernButton {
	
	public MenuButton(Main main) {
		init(main);
	}
	
	private void init(Main main) {
		JPopupMenu menu = new ModernPopupMenu();
		JMenuItem itemChangeDevice = new ModernMenuItem("Change Device");
		itemChangeDevice.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new DeviceChangerDialog((Main)MenuButton.this.getFocusCycleRootAncestor())
					.setVisible(true);
			}
		});
		JMenuItem itemResetSettings = new ModernMenuItem("Reset Settings");
		itemResetSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetSettings();
			}
		});
		JMenuItem itemCreateGroup = new ModernMenuItem("Create Group");
		itemCreateGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new GroupCreatorDialog(MenuButton.this.getFocusCycleRootAncestor())
					.setVisible(true);
			}
		});
		JMenuItem itemDeleteGroup = new ModernMenuItem("Delete Group");
		itemDeleteGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new GroupDeleterDialog(MenuButton.this.getFocusCycleRootAncestor())
					.setVisible(true);
			}
		});
		JMenuItem itemHideToTray = new ModernMenuItem("Hide to Tray");
		itemHideToTray.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				main.hideToSystemTray();
			}
		});
		JMenuItem itemAbout = new ModernMenuItem("About");
		itemAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new AboutDialog(MenuButton.this.getFocusCycleRootAncestor(), main.VERSION)
					.setVisible(true);
			}
		});
		menu.add(itemChangeDevice);
		menu.add(itemResetSettings);
		menu.add(itemCreateGroup);
		menu.add(itemDeleteGroup);
		if (SystemTray.isSupported()) {
			menu.add(itemHideToTray);
		}
		menu.add(itemAbout);

		URL menuIconUnpressedPath =
				Main.class.getResource("/images/menu_button_icon_unpressed.png");
		URL menuIconHighlightedPath =
				Main.class.getResource("/images/menu_button_icon_highlighted.png");
		URL menuIconPressedPath =
				Main.class.getResource("/images/menu_button_icon_pressed.png");
		setIcon(new ImageIcon(menuIconUnpressedPath));
		setRolloverIcon(new ImageIcon(menuIconHighlightedPath));
		setPressedIcon(new ImageIcon(menuIconPressedPath));
		setBorder(null);
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menu.show(MenuButton.this, getX(), getY());
			}
		});
	}
	
	private void resetSettings() {
		new OptionDialog(this.getFocusCycleRootAncestor(),
			"Are you sure you want to reset all of your settings?",
			"Yes", "No",
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
					manager.clearProperties();
					String message = "Done. This will take effect the next " +
							"time you open the application.";
					new TextDialog(MenuButton.this.getFocusCycleRootAncestor(), message)
						.setVisible(true);
					JButton source = (JButton)e.getSource();
					((OptionDialog)source.getFocusCycleRootAncestor()).dispose();
				}
			},
			new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JButton source = (JButton)e.getSource();
					((OptionDialog)source.getFocusCycleRootAncestor()).dispose();
				}
			}
		).setVisible(true);
	}
}
