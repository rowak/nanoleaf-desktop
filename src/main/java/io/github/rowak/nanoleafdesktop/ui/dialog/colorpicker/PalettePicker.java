package io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker;

import java.awt.Component;

import javax.swing.event.ChangeEvent;

import io.github.rowak.nanoleafdesktop.ui.dialog.BasicDialog;
import io.github.rowak.nanoleafdesktop.ui.listener.ComponentChangeListener;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;

public class PalettePicker extends BasicDialog {
	
	private Color color;
	private ColorEntry colorEntry;
	private ColorWheel wheel;
	private BrightnessSlider slider;
	private Palette palette;

	public PalettePicker(Component parent) {
		color = Color.WHITE;
		wheel = new ColorWheel(200, 200);
		wheel.addChangeListener(new ComponentChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ColorWheel wheel = (ColorWheel)e.getSource();
				color = wheel.getColor();
				updateBrightnessValues(slider.getValue());
			}
		});
		contentPanel.add(wheel, "cell 0 1");
		wheel.setLayout(new MigLayout("", "[]", "[]"));
		
		slider = new BrightnessSlider(30, 200);
		slider.addChangeListener(new ComponentChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				BrightnessSlider slider = (BrightnessSlider)e.getSource();
				updateBrightnessValues(slider.getValue());
			}
		});
		getContentPane().add(slider, "flowx, cell 0 1");
		
		colorEntry = new ColorEntry(getContentPane());
		colorEntry.addChangeListener(new ComponentChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				String[] rgb = colorEntry.getRgbTextField()
						.getText().replace(" ", "").split(",");
				Color c = new Color(Integer.parseInt(rgb[0]),
						Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
				wheel.setColor(c);
				
				String[] hsb = colorEntry.getHsbTextField()
						.getText().replace(" ", "").split(",");
				int brightness = Integer.parseInt(hsb[2]);
				slider.setValue(brightness);
			}
		});
		
		palette = new Palette(470, 50, colorEntry);
		contentPanel.add(palette, "flowx, cell 0 2");
		
		pack();
		setLocationRelativeTo(parent);
		setSize(getWidth(), getHeight());
	}
	
	public ColorWheel getColorWheel() {
		return wheel;
	}
	
	public BrightnessSlider getBrightnessSlider() {
		return slider;
	}
	
	public Palette getPalettePanel() {
		return palette;
	}
	
	public Color[] getPalette() {
		return palette.getPalette();
	}
	
	public void setPalette(Color[] palette) {
		this.palette.setPalette(palette);
	}
	
	private void updateColorValues(Color color) {
		String rgb = String.format("%d, %d, %d", color.getRed(),
				color.getGreen(), color.getBlue());
		float[] hsbVals = new float[3];
		hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(),
				color.getBlue(), hsbVals);
		String hsb = String.format("%d, %d, %d", (int)(hsbVals[0]*360),
				(int)(hsbVals[1]*100), (int)(hsbVals[2]*100));
		String hex = String.format("#%02x%02x%02x", color.getRed(),
				color.getGreen(), color.getBlue());
		colorEntry.getRgbTextField().setText(rgb);
		colorEntry.getHsbTextField().setText(hsb);
		colorEntry.getHexTextField().setText(hex);
	}
	
	private void updateBrightnessValues(int brightness) {
		float[] hsbVals = new float[3];
		hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(),
				color.getBlue(), hsbVals);
		hsbVals[2] = brightness/100f;
		color = new Color(Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]));
		updateColorValues(color);
	}
}

