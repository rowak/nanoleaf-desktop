package io.github.rowak.nanoleafdesktop.ui.panel.spotify;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.rowak.Aurora;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernToggleButton;
import io.github.rowak.nanoleafdesktop.ui.combobox.ModernComboBox;
import io.github.rowak.nanoleafdesktop.ui.dialog.OptionDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker.PalettePicker;
import io.github.rowak.nanoleafdesktop.ui.listener.ComponentChangeListener;
import io.github.rowak.nanoleafdesktop.ui.slider.ModernSliderUI;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JSlider;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JButton;

public class SpotifyPanel extends JPanel
{
	private final String[] EFFECTS = {"Pulse Beats"};
	private final int DEFAULT_SENSITIVITY = 9;
	
	private Color[] palette =
		{
			new Color(0, 0, 255),
			new Color(0, 255, 255),
			new Color(0, 255, 0),
			new Color(255, 255, 0),
			new Color(255, 100, 0),
			new Color(255, 0, 0),
			new Color(255, 0, 255)
		};
	
	private boolean adjustingPalette;
	private int sensitivity;
	private SpotifyAuthenticator authenticator;
	private SpotifyPlayer player;
	private Aurora aurora;
	
	private JToggleButton btnEnableDisable;
	private JComboBox<String> cmbxEffect;
	private JSlider sensitivitySlider;
	
	public SpotifyPanel(Aurora aurora)
	{
		this.aurora = aurora;
		initUI();
		loadUserSettings();
	}
	
	public void setAurora(Aurora aurora)
	{
		this.aurora = aurora;
		if (player != null)
		{
			player.setAurora(aurora);
		}
	}
	
	private void initUI()
	{
		setBorder(new LineBorder(Color.GRAY, 1, true));
		setBackground(Color.DARK_GRAY);
		setLayout(new MigLayout("", "[][grow][]", "[][][]"));
		
		JLabel lblStatus = new JLabel("Status");
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblStatus.setForeground(Color.WHITE);
		add(lblStatus, "cell 0 0,gapx 0 15");
		
		btnEnableDisable = new ModernToggleButton("Enable");
		btnEnableDisable.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleEnabled();
			}
		});
		add(btnEnableDisable, "cell 1 0");
		
		JLabel lblEffect = new JLabel("Effect");
		lblEffect.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblEffect.setForeground(Color.WHITE);
		add(lblEffect, "cell 0 1");
		
		cmbxEffect = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(EFFECTS));
		add(cmbxEffect, "cell 1 1,growx");
		
		JButton btnPalette = new ModernButton("Palette");
		btnPalette.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setPalette();
			}
		});
		add(btnPalette, "cell 2 1");
		
		JLabel lblSensitivity = new JLabel("Sensitivity");
		lblSensitivity.setFont(new Font("Tahoma", Font.PLAIN, 25));
		lblSensitivity.setForeground(Color.WHITE);
		add(lblSensitivity, "cell 0 2,gapx 0 15");
		
		sensitivitySlider = new JSlider();
		sensitivitySlider.setValue(DEFAULT_SENSITIVITY);
		sensitivitySlider.setMaximum(10);
		sensitivitySlider.setMinimum(0);
		sensitivitySlider.setBackground(Color.DARK_GRAY);
		sensitivitySlider.setUI(new ModernSliderUI(sensitivitySlider,
				Color.GRAY, Color.DARK_GRAY, Color.DARK_GRAY));
		sensitivitySlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (!sensitivitySlider.getValueIsAdjusting())
				{
					sensitivity = sensitivitySlider.getValue();
					setProperty("spotifySensitivity", sensitivity);
					if (player != null)
					{
						player.setSensitivity(sensitivity);
					}
				}
			}
		});
		add(sensitivitySlider, "cell 1 2,growx");
	}
	
	private void toggleEnabled()
	{
		if (btnEnableDisable.getText().equals("Enable"))
		{
			new Thread(() ->
			{
				if (player != null)
				{
					player.start();
					btnEnableDisable.setText("Disable");
				}
				else
				{
					String message = "You will now be prompted to login with your Spotify account through your web browser.";
					new OptionDialog(SpotifyPanel.this.getFocusCycleRootAncestor(),
							message, "Ok", "Cancel",
							new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent e)
								{
									OptionDialog dialog = (OptionDialog)((JButton)e.getSource())
											.getTopLevelAncestor();
									dialog.dispose();
									new Thread(() ->
									{
										trySetupPlayer();
										btnEnableDisable.setText("Disable");
									}).start();
								}
							}, new ActionListener()
							{
								@Override
								public void actionPerformed(ActionEvent e)
								{
									OptionDialog dialog = (OptionDialog)((JButton)e.getSource())
											.getTopLevelAncestor();
									dialog.dispose();
								}
							})
					.setVisible(true);
				}
			}).start();
		}
		else if (btnEnableDisable.getText().equals("Disable"))
		{
			player.stop();
			btnEnableDisable.setText("Enable");
		}
	}
	
	private boolean trySetupPlayer()
	{
		try
		{
			authenticator = new SpotifyAuthenticator();
			player = new SpotifyPlayer(authenticator.getSpotifyApi(),
					getSelectedEffect(), convertPalette(palette), aurora);
			player.setSensitivity(sensitivity);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private SpotifyEffect.Type getSelectedEffect()
	{
		return SpotifyEffect.Type.values()[cmbxEffect.getSelectedIndex()];
	}
	
	private void setPalette()
	{
		PalettePicker palettePicker = new PalettePicker(this.getFocusCycleRootAncestor());
		palettePicker.setPalette(palette);
		palettePicker.setVisible(true);
		palettePicker.getPalettePanel().addChangeListener(new ComponentChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (!adjustingPalette)
				{
					adjustingPalette = true;
					new Thread(() ->
					{
						if (player != null)
						{
							player.setPalette(convertPalette(palettePicker.getPalette()));
						}
						palette = palettePicker.getPalette();
						adjustingPalette = false;
					}).start();
				}
			}
		});
	}
	
	private io.github.rowak.Color[] convertPalette(java.awt.Color[] awtPalette)
	{
		io.github.rowak.Color[] palette = new io.github.rowak.Color[awtPalette.length];
		for (int i = 0; i < awtPalette.length; i++)
		{
			Color c = awtPalette[i];
			palette[i] = io.github.rowak.Color.fromRGB(c.getRed(),
					c.getGreen(), c.getBlue());
		}
		return palette;
	}
	
	private void loadUserSettings()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		
		String lastSensitivity = manager.getProperty("spotifySensitivity");
		if (lastSensitivity != null)
		{
			try
			{
				sensitivity = Integer.parseInt(lastSensitivity);
				sensitivitySlider.setValue(sensitivity);
			}
			catch (NumberFormatException nfe)
			{
				sensitivity = DEFAULT_SENSITIVITY;
			}
		}
	}
	
	private void setProperty(String key, Object value)
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		manager.setProperty(key, value);
	}
}
