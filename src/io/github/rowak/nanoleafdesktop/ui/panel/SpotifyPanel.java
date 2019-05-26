package io.github.rowak.nanoleafdesktop.ui.panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.rowak.Aurora;
import io.github.rowak.StatusCodeException;
import io.github.rowak.Effect.Direction;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyAuthenticator;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyPlayer;
import io.github.rowak.nanoleafdesktop.spotify.UserOption;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyFireworksEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyPulseBeatsEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifySoundBarEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyStreakingNotesEffect;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.tools.UIConstants;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernToggleButton;
import io.github.rowak.nanoleafdesktop.ui.combobox.ModernComboBox;
import io.github.rowak.nanoleafdesktop.ui.dialog.OptionDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker.PalettePicker;
import io.github.rowak.nanoleafdesktop.ui.label.LargeModernLabel;
import io.github.rowak.nanoleafdesktop.ui.label.SmallModernLabel;
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
	private final int DEFAULT_SENSITIVITY = 9;
	private final int MAX_SENSITIVITY = 10;
	private final int MIN_SENSITIVITY = 0;
	private final int DEFAULT_AUDIO_OFFSET = 25;
	private final int MAX_AUDIO_OFFSET = 1000;
	private final int MIN_AUDIO_OFFSET = -1000;
	
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
	private int audioOffset;
	private int sensitivity;
	private SpotifyAuthenticator authenticator;
	private SpotifyPlayer player;
	private Aurora aurora;
	private Map<String, Object> userOptionArgs;
	
	private JToggleButton btnEnableDisable;
	private JComboBox<String> cmbxEffect;
	private JSlider sensitivitySlider;
	private JLabel lblTrackInfo;
	private JLabel lblTrackProgress;
	private List<JLabel> lblOptions;
	private List<JComboBox<String>> cmbxOptions;
	private JLabel lblAudioOffset;
	private JSlider audioOffsetSlider;
	
	public SpotifyPanel(Aurora aurora)
	{
		this.aurora = aurora;
		userOptionArgs = new HashMap<String, Object>();
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
	
	public void setTrackInfoText(String text)
	{
		lblTrackInfo.setText(text);
	}
	
	public void setTrackProgressText(String text)
	{
		lblTrackProgress.setText(text);
	}
	
	public Map<String, Object> getUserOptionArgs()
	{
		return userOptionArgs;
	}
	
	private void initUI()
	{
		setBorder(new LineBorder(Color.GRAY, 1, true));
		setBackground(Color.DARK_GRAY);
		setLayout(new MigLayout("", "[][grow][]", "[][][][][][]"));
		
		lblOptions = new ArrayList<JLabel>();
		cmbxOptions = new ArrayList<JComboBox<String>>();
		
		JLabel lblStatus = new LargeModernLabel("Status");
		add(lblStatus, "cell 0 0,gapx 0 15");
		
		btnEnableDisable = new ModernToggleButton("Enable");
		btnEnableDisable.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (getSelectedEffect() != null)
				{
					toggleEnabled();
				}
				else if (btnEnableDisable.getText().equals("Enable"))
				{
					new TextDialog(SpotifyPanel.this.getFocusCycleRootAncestor(),
							"You must select an effect before enabling the visualizer.")
							.setVisible(true);
				}
			}
		});
		add(btnEnableDisable, "cell 1 0");
		
		JLabel lblEffect = new LargeModernLabel("Effect");
		add(lblEffect, "cell 0 1");
		
		cmbxEffect = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(getEffectTypes()));
		cmbxEffect.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new Thread(() ->
				{
					if (player != null)
					{
						try
						{
							showEffectOptions(false);
							player.setEffect(getSelectedEffect());
							player.initEffect();
							showEffectOptions(true);
						}
						catch (StatusCodeException sce)
						{
							sce.printStackTrace();
						}
					}
					else
					{
						showEffectOptions(false);
						showEffectOptions(true);
					}
				}).start();
			}
		});
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
		
		JLabel lblSensitivity = new LargeModernLabel("Sensitivity");
		add(lblSensitivity, "cell 0 2,gapx 0 15");
		
		sensitivitySlider = new JSlider();
		sensitivitySlider.setValue(DEFAULT_SENSITIVITY);
		sensitivitySlider.setMaximum(MAX_SENSITIVITY);
		sensitivitySlider.setMinimum(MIN_SENSITIVITY);
		sensitivitySlider.setBackground(UIConstants.darkBackground);
		sensitivitySlider.setUI(new ModernSliderUI(sensitivitySlider));
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
		
		lblAudioOffset = new LargeModernLabel("Audio Offset (ms)");
		add(lblAudioOffset, "cell 0 3,gapx 0 15");
		
		audioOffsetSlider = new JSlider();
		audioOffsetSlider.setValue(DEFAULT_AUDIO_OFFSET);
		audioOffsetSlider.setMaximum(MAX_AUDIO_OFFSET);
		audioOffsetSlider.setMinimum(MIN_AUDIO_OFFSET);
		audioOffsetSlider.setBackground(UIConstants.darkBackground);
		audioOffsetSlider.setPaintTicks(true);
		audioOffsetSlider.setPaintLabels(true);
		audioOffsetSlider.setFont(UIConstants.smallLabelFont);
		audioOffsetSlider.setMajorTickSpacing(500);
		audioOffsetSlider.setUI(new ModernSliderUI(audioOffsetSlider));
		audioOffsetSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (!audioOffsetSlider.getValueIsAdjusting())
				{
					audioOffset = audioOffsetSlider.getValue();
					setProperty("spotifyAudioOffset", audioOffset);
					if (player != null)
					{
						player.setAudioOffset(audioOffset);
					}
				}
			}
		});
		add(audioOffsetSlider, "cell 1 3,growx");
		
		lblTrackInfo = new SmallModernLabel("No song playing");
		add(lblTrackInfo, "cell 0 4 3 1,alignx center");
		
		lblTrackProgress = new SmallModernLabel("00:00:00");
		add(lblTrackProgress, "cell 0 5 3 1,alignx center");
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
					String message = "You will now be prompted to login with your " +
							"Spotify account through your web browser.";
					OptionDialog spotifyAuth = new OptionDialog(
							SpotifyPanel.this.getFocusCycleRootAncestor(),
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
							});
					if (SpotifyAuthenticator.getSavedAccessToken() == null)
					{
						spotifyAuth.setVisible(true);
					}
					else
					{
						new Thread(() ->
						{
							trySetupPlayer();
							btnEnableDisable.setText("Disable");
						}).start();
					}
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
					getSelectedEffect(), convertPalette(palette), aurora, this);
			player.setSensitivity(sensitivity);
			player.setAudioOffset(audioOffset);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void showEffectOptions(boolean visible)
	{
		SpotifyEffect effect = getEffectFromType(getSelectedEffect());
		if (visible && effect != null)
		{
			remove(lblTrackInfo);
			remove(lblTrackProgress);
			
			List<UserOption> options = effect.getUserOptions();
			for (int i = 0; i < options.size(); i++)
			{
				UserOption option = options.get(i);
				JLabel lblOption = new LargeModernLabel(option.getName());
				lblOptions.add(lblOption);
				JComboBox<String> cmbxOption = new ModernComboBox<String>(
						new DefaultComboBoxModel<String>(option.getOptions()));
				cmbxOption.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						userOptionArgs.put(option.getName().toLowerCase(),
								cmbxOption.getSelectedItem());
						if (player != null)
						{
							try
							{
								player.setEffect(getSelectedEffect());
								player.initEffect();
							}
							catch (StatusCodeException sce)
							{
								sce.printStackTrace();
							}
						}
					}
				});
				cmbxOptions.add(cmbxOption);
				
				userOptionArgs.put(option.getName().toLowerCase(), option.getOptions()[0]);
				
				add(lblOptions.get(i), "cell 0 " + (i + 4) + ",gapx 0 15");
				add(cmbxOptions.get(i), "cell 1 " + (i + 4) + ",growx");
			}
			add(lblTrackInfo, "cell 0 " + (options.size() + 4) + " 3 1,alignx center");
			add(lblTrackProgress, "cell 0 " + (options.size() + 5) + " 3 1,alignx center");
			
			revalidate();
		}
		else if (!lblOptions.isEmpty() && !cmbxOptions.isEmpty())
		{
			remove(lblTrackInfo);
			remove(lblTrackProgress);
			
			for (int i = 0; i < lblOptions.size(); i++)
			{
				remove(lblOptions.get(i));
			}
			for (int i = 0; i < cmbxOptions.size(); i++)
			{
				remove(cmbxOptions.get(i));
			}
			
			add(lblTrackInfo, "cell 0 4 3 1,alignx center");
			add(lblTrackProgress, "cell 0 5 3 1,alignx center");
			
			userOptionArgs.clear();
			
			revalidate();
		}
	}
	
	private SpotifyEffect getEffectFromType(SpotifyEffectType type)
	{
		if (type != null)
		{
			try
			{
				switch (type)
				{
					case PULSE_BEATS:
						return new SpotifyPulseBeatsEffect(
								convertPalette(palette), aurora);
					case SOUNDBAR:
						return new SpotifySoundBarEffect(
								convertPalette(palette), Direction.RIGHT, aurora);
					case FIREWORKS:
						return new SpotifyFireworksEffect(
								convertPalette(palette), aurora);
					case STREAKING_NOTES:
						return new SpotifyStreakingNotesEffect(
								convertPalette(palette), aurora);
				}
			}
			catch (StatusCodeException sce)
			{
				sce.printStackTrace();
			}
		}
		return null;
	}
	
	private SpotifyEffectType getSelectedEffect()
	{
		int index = cmbxEffect.getSelectedIndex()-1;
		if (index != -1)
		{
			return SpotifyEffectType.values()[index];
		}
		return null;
	}
	
	private String[] getEffectTypes()
	{
		String[] types = new String[SpotifyEffectType.values().length+1];
		types[0] = "Select an effect...";
		for (int i = 0; i < SpotifyEffectType.values().length; i++)
		{
			char[] type = SpotifyEffectType.values()[i].toString().toLowerCase().toCharArray();
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
							try
							{
								player.setPalette(convertPalette(
										palettePicker.getPalette()));
							}
							catch (Exception e1)
							{
								e1.printStackTrace();
							}
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
		String lastAudioOffset = manager.getProperty("spotifyAudioOffset");
		if (lastAudioOffset != null)
		{
			try
			{
				audioOffset = Integer.parseInt(lastAudioOffset);
				audioOffsetSlider.setValue(audioOffset);
			}
			catch (NumberFormatException nfe)
			{
				audioOffset = DEFAULT_AUDIO_OFFSET;
			}
		}
	}
	
	private void setProperty(String key, Object value)
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		manager.setProperty(key, value);
	}
}
