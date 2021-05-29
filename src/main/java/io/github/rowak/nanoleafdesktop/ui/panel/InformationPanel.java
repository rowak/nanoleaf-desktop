package io.github.rowak.nanoleafdesktop.ui.panel;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.github.rowak.nanoleafapi.NanoleafCallback;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.UIConstants;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernToggleButton;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker.BrightnessSlider;
import io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker.ColorEntry;
import io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker.ColorPicker;
import io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker.ColorWheel;
import io.github.rowak.nanoleafdesktop.ui.label.LargeModernLabel;
import io.github.rowak.nanoleafdesktop.ui.listener.ComponentChangeListener;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;
import io.github.rowak.nanoleafdesktop.ui.slider.ModernSliderUI;
import net.miginfocom.swing.MigLayout;

public class InformationPanel extends JPanel {
	
	private boolean adjusting;
	private Main parent;
	private NanoleafGroup group;
	private PanelCanvas canvas;
	
	private JToggleButton btnOnOff;
	private JLabel lblActiveScene;
	private JSlider brightnessSlider;
	private JSlider ctSlider;
	
	public InformationPanel(Main parent, NanoleafGroup group, PanelCanvas canvas) {
		this.parent = parent;
		this.group = group;
		this.canvas = canvas;
		init();
		adjusting = false;
	}
	
	public JToggleButton getBtnOnOff() {
		return btnOnOff;
	}
	
	public void setAuroras(NanoleafGroup group) {
		this.group = group;
	}
	
	public void setScene(String scene) {
		lblActiveScene.setText(scene);
	}
	
	public void setSliderBrightness(int brightness) {
		brightnessSlider.setValue(brightness);
	}
	
	public void setSliderColorTemp(int temp) {
		ctSlider.setValue(temp);
	}
	
	private void init() {
		setBorder(new LineBorder(UIConstants.darkForeground, 1, true));
		setBackground(UIConstants.darkBackground);
		setLayout(new MigLayout("", "[][428.00]", "[][][][][]"));
		
		JLabel lblOnOff = new LargeModernLabel("On/Off");
		add(lblOnOff, "cell 0 0,aligny center");
		
		btnOnOff = new ModernToggleButton("Turn On");
		btnOnOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JToggleButton btn = (JToggleButton)e.getSource();
				if (btn.getText().equals("Turn On")) {
					btn.setText("Turn Off");
					group.setOnAsync(true, (status, data, device) -> {
						if (status == NanoleafCallback.SUCCESS) { 
							canvas.setOn(true, device);
						}
						else {
							new TextDialog(parent,
									"The requested action could not be completed. " +
									"Please try again.").setVisible(true);
						}
					});
				}
				else {
					btn.setText("Turn On");
					group.setOnAsync(false, (status, data, device) -> {
						if (status == NanoleafCallback.SUCCESS) { 
							canvas.setOn(false, device);
						}
						else {
							new TextDialog(parent,
									"The requested action could not be completed. " +
									"Please try again.").setVisible(true);
						}
					});
					canvas.toggleOn();
				}
			}
		});
		add(btnOnOff, "cell 1 0");
		
		JLabel lblCurrentScene = new LargeModernLabel("Active Scene:");
		add(lblCurrentScene, "cell 0 1");
		
		lblActiveScene = new LargeModernLabel("*None*");
		add(lblActiveScene, "cell 1 1");
		
		JLabel lblBrightness = new LargeModernLabel("Brightness");
		add(lblBrightness, "cell 0 2");
		
		brightnessSlider = new JSlider();
		brightnessSlider.setBackground(Color.DARK_GRAY);
		brightnessSlider.setUI(new ModernSliderUI(brightnessSlider));
		brightnessSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!adjusting) {
					adjusting = true;
					new Thread(() -> {
						try {
							JSlider slider = (JSlider)e.getSource();
							if (slider.getValueIsAdjusting()) {
								group.setBrightness(slider.getValue());
							}
							else {
								canvas.checkAuroraStateForAll();
								parent.loadActiveScene();
							}
							adjusting = false;
						}
						catch (IOException e1) {
							new TextDialog(parent,
									"Lost connection to the device. " +
									"Please try again.").setVisible(true);
						}
						catch (NanoleafException e1) {
							new TextDialog(parent,
									"The requested action could not be completed. " +
									"Please try again.").setVisible(true);
						}
					}).start();
				}
			}
		});
		add(brightnessSlider, "cell 1 2,growx");
		
		JLabel lblColorTemperature = new LargeModernLabel("Color Temperature");
		add(lblColorTemperature, "cell 0 3,gapx 0 15");
		
		ctSlider = new JSlider();
		ctSlider.setMaximum(6400);
		ctSlider.setMinimum(1200);
		ctSlider.setBackground(UIConstants.darkBackground);
		ctSlider.setUI(new ModernSliderUI(ctSlider));
		ctSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!adjusting) {
					adjusting = true;
					new Thread(() -> {
						try {
							JSlider slider = (JSlider)e.getSource();
							if (slider.getValueIsAdjusting()) {
								group.setColorTemperature(slider.getValue());
							}
							else {
								canvas.checkAuroraStateForAll();
								parent.loadActiveScene();
							}
						}
						catch (IOException e1) {
							new TextDialog(parent,
									"Lost connection to the device. " +
									"Please try again.").setVisible(true);
						}
						catch (NanoleafException e1)
						{
							new TextDialog(parent,
									"The requested action could not be completed. " +
									"Please try again.").setVisible(true);
						}
						adjusting = false;
					}).start();
				}
			}
		});
		add(ctSlider, "cell 1 3,growx");
		
		JLabel lblSolidColor = new LargeModernLabel("Solid Color");
		add(lblSolidColor, "cell 0 4");
		
		JButton btnSetSolidColor = new ModernButton("Set Solid Color");
		btnSetSolidColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton btn = (JButton)e.getSource();
				JFrame frame = (JFrame)btn.getFocusCycleRootAncestor();
				ColorPicker colorPicker = new ColorPicker(frame);
				colorPicker.setVisible(true);
				colorPicker.getColorEntry().addChangeListener(new ComponentChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (!adjusting) {
							adjusting = true;
							ColorEntry entry = (ColorEntry)e.getSource();
							int[] hsb = entry.getHSB();
							int hue = (int)(hsb[0]*360);
							int sat = (int)(hsb[1]*100);
							int bri = (int)(hsb[2]*100);
							try {
								group.setColor(io.github.rowak.nanoleafapi.Color.fromHSB(hue, sat, bri));
								parent.loadStateComponents();
							}
							catch (Exception e1) {
								new TextDialog(parent,
										"The requested action could not be completed. " +
										"Please try again.").setVisible(true);
							}
							canvas.setColor(entry.getColor());
							adjusting = false;
						}
					}
				});
				colorPicker.getColorWheel().addChangeListener(new ComponentChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (!adjusting) {
							adjusting = true;
							ColorWheel wheel = (ColorWheel)e.getSource();
							Color color = wheel.getColor();
							float[] hsb = new float[3];
							hsb = Color.RGBtoHSB(color.getRed(),
									color.getGreen(), color.getBlue(), hsb);
							int hue = (int)(hsb[0]*360);
							int sat = (int)(hsb[1]*100);
							int bri = (int)(hsb[2]*100);
							group.setColorAsync(io.github.rowak.nanoleafapi.Color.fromHSB(hue, sat, bri), null);
							canvas.setColor(color);
							adjusting = false;
						}
					}
				});
				colorPicker.getBrightnessSlider().addChangeListener(
						new ComponentChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						if (!adjusting) {
							adjusting = true;
							BrightnessSlider slider = (BrightnessSlider)e.getSource();
							int brightness = slider.getValue();
							group.setBrightnessAsync(brightness, (status, data, device) -> {
								if (status != NanoleafCallback.SUCCESS) {
									new TextDialog(parent,
											"The requested action could not be completed. " +
											"Please try again.").setVisible(true);
								}
							});
							adjusting = false;
						}
					}
				});
			}
		});
		add(btnSetSolidColor, "cell 1 4");
	}
}
