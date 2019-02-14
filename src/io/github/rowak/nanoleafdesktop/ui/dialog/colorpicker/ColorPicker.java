package io.github.rowak.nanoleafdesktop.ui.dialog.colorpicker;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

import io.github.rowak.nanoleafdesktop.ui.dialog.BasicDialog;
import io.github.rowak.nanoleafdesktop.ui.listener.ComponentChangeListener;
import net.miginfocom.swing.MigLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Color;
import javax.swing.JTextField;

public class ColorPicker extends BasicDialog
{
	private Color color;
	
	private ColorWheel wheel;
	private BrightnessSlider slider;
	private JTextField txtRgb;
	private JTextField txtHsb;
	private JTextField txtHex;

	public ColorPicker(Component parent)
	{
		color = Color.WHITE;
		
		wheel = new ColorWheel(200, 200);
		wheel.addChangeListener(new ComponentChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				ColorWheel wheel = (ColorWheel)e.getSource();
				color = wheel.getColor();
				updateColorValues(color);
			}
		});
		contentPanel.add(wheel, "cell 0 1");
		wheel.setLayout(new MigLayout("", "[]", "[]"));
		
		slider = new BrightnessSlider(30, 200);
		slider.addChangeListener(new ComponentChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				BrightnessSlider slider = (BrightnessSlider)e.getSource();
				updateBrightnessValues(slider.getValue());
			}
		});
		getContentPane().add(slider, "flowx, cell 0 1");
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.DARK_GRAY);
		getContentPane().add(panel, "flowx, cell 0 1");
		panel.setLayout(new MigLayout("", "[]", "[][][]"));
		
		JLabel lblRgb = new JLabel("RGB");
		lblRgb.setForeground(Color.WHITE);
		lblRgb.setFont(new Font("Tahoma", Font.PLAIN, 20));
		panel.add(lblRgb, "flowx,cell 0 0");
		
		txtRgb = new JTextField();
		txtRgb.setFont(new Font("Tahoma", Font.PLAIN, 20));
		txtRgb.setForeground(Color.WHITE);
		txtRgb.setBackground(Color.DARK_GRAY);
		txtRgb.setText("128, 128, 128");
		txtRgb.setColumns(10);
		txtRgb.setCaretColor(Color.WHITE);
		txtRgb.addKeyListener(new EnterColorListener(EnterColorListener.RGB));
		panel.add(txtRgb, "cell 0 0,gapx 15 0");
		
		JLabel lblHsb = new JLabel("HSB");
		lblHsb.setForeground(Color.WHITE);
		lblHsb.setFont(new Font("Tahoma", Font.PLAIN, 20));
		panel.add(lblHsb, "flowx,cell 0 1");
		
		txtHsb = new JTextField();
		txtHsb.setText("0, 0, 50");
		txtHsb.setFont(new Font("Tahoma", Font.PLAIN, 20));
		txtHsb.setForeground(Color.WHITE);
		txtHsb.setBackground(Color.DARK_GRAY);
		txtHsb.setColumns(10);
		txtHsb.setCaretColor(Color.WHITE);
		txtHsb.addKeyListener(new EnterColorListener(EnterColorListener.HSB));
		panel.add(txtHsb, "cell 0 1,gapx 15 0,aligny top");
		
		JLabel lblHex = new JLabel("HEX");
		lblHex.setForeground(Color.WHITE);
		lblHex.setFont(new Font("Tahoma", Font.PLAIN, 20));
		panel.add(lblHex, "flowx,cell 0 2");
		
		txtHex = new JTextField();
		txtHex.setForeground(Color.WHITE);
		txtHex.setText("#808080");
		txtHex.setFont(new Font("Tahoma", Font.PLAIN, 20));
		txtHex.setBackground(Color.DARK_GRAY);
		txtHex.setColumns(10);
		txtHex.setCaretColor(Color.WHITE);
		txtHex.addKeyListener(new EnterColorListener(EnterColorListener.HEX));
		panel.add(txtHex, "cell 0 2,gapx 15 0");
		
		pack();
		setLocationRelativeTo(parent);
		setSize(getWidth() + 25, getHeight());
	}
	
	public ColorWheel getColorWheel()
	{
		return this.wheel;
	}
	
	public BrightnessSlider getBrightnessSlider()
	{
		return this.slider;
	}
	
	public Color getColor()
	{
		return this.color;
	}
	
	public void setColor(Color color)
	{
		this.color = color;
		wheel.setColor(color);
		slider.setColor(color);
	}
	
	private void updateColorValues(Color color)
	{
		String rgb = String.format("%d, %d, %d", color.getRed(),
				color.getGreen(), color.getBlue());
		float[] hsbVals = new float[3];
		hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(),
				color.getBlue(), hsbVals);
		String hsb = String.format("%d, %d, %d", (int)(hsbVals[0]*360),
				(int)(hsbVals[1]*100), (int)(hsbVals[2]*100));
		String hex = String.format("#%02x%02x%02x", color.getRed(),
				color.getGreen(), color.getBlue());
		txtRgb.setText(rgb);
		txtHsb.setText(hsb);
		txtHex.setText(hex);
	}
	
	private void updateBrightnessValues(int brightness)
	{
		float[] hsbVals = new float[3];
		hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(),
				color.getBlue(), hsbVals);
		hsbVals[2] = brightness/100f;
		color = new Color(Color.HSBtoRGB(hsbVals[0], hsbVals[1], hsbVals[2]));
		updateColorValues(color);
	}
	
	private class EnterColorListener extends KeyAdapter
	{
		public static final int RGB = 0;
		public static final int HSB = 1;
		public static final int HEX = 2;
		
		private int colorType;
		
		public EnterColorListener(int colorType)
		{
			this.colorType = colorType;
		}
		
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				try
				{
					switch (colorType)
					{
						case RGB:
							String[] rgb = txtRgb.getText().replace(" ", "").split(",");
							Color c = new Color(Integer.parseInt(rgb[0]),
									Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
							updateColorValues(c);
							setColor(c);
							break;
						case HSB:
							String[] hsb = txtHsb.getText().replace(" ", "").split(",");
							c = Color.getHSBColor(Integer.parseInt(hsb[0])/360f,
									Integer.parseInt(hsb[1])/100f, Integer.parseInt(hsb[2])/100f);
							updateColorValues(c);
							setColor(c);
							break;
						case HEX:
							String hex = txtHex.getText();
							c = Color.decode(hex);
							updateColorValues(c);
							setColor(c);
							break;
					}
				}
				catch (NumberFormatException nfe)
				{
					nfe.printStackTrace();
				}
			}
			super.keyPressed(e);
		}
	}
}
