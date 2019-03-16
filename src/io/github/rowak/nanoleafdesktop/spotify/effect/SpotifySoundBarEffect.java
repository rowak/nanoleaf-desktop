package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Effect;
import io.github.rowak.Effect.Direction;
import io.github.rowak.Frame;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.effectbuilder.CustomEffectBuilder;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.spotify.UserOption;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;

public class SpotifySoundBarEffect extends SpotifyEffect
{
	private int beatCounter;
	private float loudness;
	private Panel[][] panelTable;
	private Direction direction;
	
	public SpotifySoundBarEffect(Color[] palette, Direction direction,
			Aurora aurora) throws StatusCodeException
	{
		super(SpotifyEffectType.SOUNDBAR, palette, aurora);
		userOptions.add(new UserOption("Direction",
				new String[]{"Up", "Down", "Left", "Right"}));
		this.direction = direction;
		initPanelTable();
		initPalette();
		clearDisplay();
		
		init();
	}
	
	@Override
	public void init() throws StatusCodeException
	{
		aurora.externalStreaming().enable();
	}
	
	@Override
	public void run(SpecificAudioAnalysis analysis)
					throws StatusCodeException, IOException
	{
		updateLoudness(analysis);
		
		if (analysis.getSegment() != null)
		{
			List<Panel> updated = new ArrayList<Panel>();
			int max = (int)(panelTable.length * loudness);
			float duration = analysis.getSegment().getMeasure().getDuration();
			
			for (int i = 0; i < panelTable.length; i++)
			{
				pulse(i, max, updated);
			}
			fadePanelsToBackground(max, duration);
			
			setNextPaletteColor();
		}
	}
	
	private void pulse(int i, int max, List<Panel> updated)
	{
		if (i < max)
		{
			for (Panel p : panelTable[i])
			{
				int r = palette[paletteIndex].getRed();
				int g = palette[paletteIndex].getGreen();
				int b = palette[paletteIndex].getBlue();
				java.awt.Color c = new java.awt.Color(r, g, b);
				c = applyLoudnessToColor(c, i, max);
				updated.add(p);
				try
				{
					aurora.externalStreaming().setPanel(p,
							c.getRed(), c.getGreen(), c.getBlue(), 1);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (max-i > 0)
		{
			for (Panel p : panelTable[max-i])
			{
				Color c = getBackgroundColor();
				try
				{
					aurora.externalStreaming().setPanel(p,
							c.getRed(), c.getGreen(), c.getBlue(), 1);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void fadePanelsToBackground(int max, float duration)
	{
		new Thread(() ->
		{
			try
			{
				Thread.sleep(100);
				for (int i = 0; i < panelTable.length; i++)
				{
					for (Panel p : panelTable[i])
					{
						Color c = getBackgroundColor();
						aurora.externalStreaming().setPanel(p, c.getRed(),
								c.getGreen(), c.getBlue(), max-i + (int)(2*duration));
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}).start();
	}
	
	private void initPanelTable() throws StatusCodeException
	{
		Panel[] panels = aurora.panelLayout().getPanels();
		if (direction == Direction.RIGHT || direction == Direction.LEFT)
		{
			panelTable = PanelTableSort.getColumns(panels);
		}
		else if (direction == Direction.UP || direction == Direction.DOWN)
		{
			panelTable = PanelTableSort.getRows(panels);
		}
		
		if (direction == Direction.UP || direction == Direction.LEFT)
		{
			reversePanelTable(panelTable);
		}
	}
	
	private void initPalette()
	{
		if (palette.length > 1)
		{
			paletteIndex = 1;
		}
		else
		{
			paletteIndex = 0;
		}
	}
	
	private void clearDisplay() throws StatusCodeException
	{
		Effect clear = new CustomEffectBuilder(aurora)
				.addFrameToAllPanels(getBackgroundFrame(1))
				.build("", false);
		aurora.effects().displayEffect(clear);
	}
	
	private void updateLoudness(SpecificAudioAnalysis analysis)
	{
		if (analysis.getSegment() != null)
		{
			loudness = loudnessToPercent(analysis.getSegment().getLoudnessStart());
		}
	}
	
	@Override
	protected void setNextPaletteColor()
	{
		beatCounter++;
		if (beatCounter >= 50)
		{
			if (paletteIndex == palette.length-1)
			{
				paletteIndex = palette.length > 1 ? 1 : 0;
			}
			else
			{
				paletteIndex++;
			}
			beatCounter = 0;
		}
	}
	
	private java.awt.Color applyLoudnessToColor(java.awt.Color color, int i, int max)
	{
		float[] hsb = new float[3];
		hsb = java.awt.Color.RGBtoHSB(color.getRed(),
				color.getGreen(), color.getBlue(), hsb);
		float loudnessFactor = (max-i)/(float)max;
		hsb[2] = ((hsb[2]*100f)*loudnessFactor)/100f;
		return java.awt.Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}
	
	private float loudnessToPercent(float loudness)
	{
		final float MAX = 0f;
		final float MIN = -40.0f;
		if (loudness < MIN)
		{
			return 0f;
		}
		else if (loudness > MAX)
		{
			return 1f;
		}
		return (1 - loudness/MIN);
	}
	
	private void reversePanelTable(Panel[][] table)
	{
		for (int i = 0; i < table.length/2; i++)
		{
			Panel[] temp = table[i];
			table[i] = table[table.length-1-i];
			table[table.length-1-i] = temp;
		}
	}
	
	private Color getBackgroundColor()
	{
		Color c = palette[0];
		if (palette.length == 1)
		{
			c = Color.fromRGB(0, 0, 0);
		}
		return c;
	}
	
	private Frame getBackgroundFrame(int transTime)
	{
		Color c = getBackgroundColor();
		return new Frame(c.getRed(), c.getGreen(), c.getBlue(), 0, transTime);
	}
}


//package io.github.rowak.nanoleafdesktop.ui.panel.spotify;
//
//import java.io.IOException;
//import java.util.Arrays;
//
//import io.github.rowak.Aurora;
//import io.github.rowak.Color;
//import io.github.rowak.Effect;
//import io.github.rowak.Effect.Direction;
//import io.github.rowak.Frame;
//import io.github.rowak.Panel;
//import io.github.rowak.StatusCodeException;
//import io.github.rowak.effectbuilder.CustomEffectBuilder;
//import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
//
//public class SpotifySoundBarEffect extends SpotifyEffect
//{
//	private int beatCounter;
//	private float loudness;
//	private Panel[][] panelTable;
//	private Direction direction;
//	
//	public SpotifySoundBarEffect(Color[] palette, Direction direction,
//			Aurora aurora) throws StatusCodeException
//	{
//		super(SpotifyEffectType.SOUNDBAR, palette, aurora);
//		userOptions.add(new UserOption("Direction",
//				new String[]{"Up", "Down", "Left", "Right"}));
//		this.direction = direction;
//		
//		initPanelTable();
//		initPalette();
//		clearDisplay();
//	}
//	
//	@Override
//	public void run(SpecificAudioAnalysis analysis)
//					throws StatusCodeException, IOException
//	{
//		updateLoudness(analysis);
//		
//		if (analysis.getSegment() != null)
//		{
//			System.out.println(analysis.getSegment().getMeasure().getDuration());
//			CustomEffectBuilder ceb = new CustomEffectBuilder(aurora);
//			int max = (int)(panelTable.length * loudness);
//			
//			for (int i = 0; i < panelTable.length; i++)
//			{
//				pulse(i, max, analysis.getSegment().getMeasure().getDuration(), ceb);
//			}
//			
//			new Thread(() ->
//			{
//				try
//				{
//					aurora.effects().displayEffect(ceb.build("", false));
//				}
//				catch (StatusCodeException sce)
//				{
//					sce.printStackTrace();
//				}
//			}).start();
//			setNextPaletteColor();
//		}
//	}
//	
//	private void pulse(int i, int max, float durFactor, CustomEffectBuilder ceb)
//	{
//		if (i < max)
//		{
//			for (Panel p : panelTable[i])
//			{
//				int r = palette[paletteIndex].getRed();
//				int g = palette[paletteIndex].getGreen();
//				int b = palette[paletteIndex].getBlue();
//				java.awt.Color c = new java.awt.Color(r, g, b);
//				c = applyLoudnessToColor(c, i, max);
//				ceb.addFrame(p, new Frame(c.getRed(),
//						c.getGreen(), c.getBlue(), 0, 1));
//				ceb.addFrame(p, getBackgroundFrame(max-i + (int)(2*durFactor)));
//			}
//		}
//		else if (max-i > 0)
//		{
//			for (Panel p : panelTable[max-i])
//			{
//				ceb.addFrame(p, getBackgroundFrame(1));
//			}
//		}
//	}
//	
//	private void initPanelTable() throws StatusCodeException
//	{
//		Panel[] panels = aurora.panelLayout().getPanels();
//		if (direction == Direction.RIGHT || direction == Direction.LEFT)
//		{
//			panelTable = PanelTableSort.getColumns(panels);
//		}
//		else if (direction == Direction.UP || direction == Direction.DOWN)
//		{
//			panelTable = PanelTableSort.getRows(panels);
//		}
//		
//		if (direction == Direction.UP || direction == Direction.LEFT)
//		{
//			reversePanelTable(panelTable);
//		}
//	}
//	
//	private void initPalette()
//	{
//		if (palette.length > 1)
//		{
//			paletteIndex = 1;
//		}
//		else
//		{
//			paletteIndex = 0;
//		}
//	}
//	
//	private void clearDisplay() throws StatusCodeException
//	{
//		Effect clear = new CustomEffectBuilder(aurora)
//				.addFrameToAllPanels(getBackgroundFrame(1))
//				.build("", false);
//		aurora.effects().displayEffect(clear);
//	}
//	
//	private void updateLoudness(SpecificAudioAnalysis analysis)
//	{
//		if (analysis.getSegment() != null)
//		{
//			loudness = loudnessToPercent(analysis.getSegment().getLoudnessStart());
//		}
//	}
//	
//	@Override
//	protected void setNextPaletteColor()
//	{
//		beatCounter++;
//		if (beatCounter >= 50)
//		{
//			if (paletteIndex == palette.length-1)
//			{
//				paletteIndex = palette.length > 1 ? 1 : 0;
//			}
//			else
//			{
//				paletteIndex++;
//			}
//			beatCounter = 0;
//		}
//	}
//	
//	private java.awt.Color applyLoudnessToColor(java.awt.Color color, int i, int max)
//	{
//		float[] hsb = new float[3];
//		hsb = java.awt.Color.RGBtoHSB(color.getRed(),
//				color.getGreen(), color.getBlue(), hsb);
//		float loudnessFactor = (max-i)/(float)max;
//		hsb[2] = ((hsb[2]*100f)*loudnessFactor)/100f;
//		return java.awt.Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
//	}
//	
//	private float loudnessToPercent(float loudness)
//	{
//		final float MAX = 0f;
//		final float MIN = -40.0f;
//		if (loudness < MIN)
//		{
//			return 0f;
//		}
//		else if (loudness > MAX)
//		{
//			return 1f;
//		}
//		return (1 - loudness/MIN);
//	}
//	
//	private void reversePanelTable(Panel[][] table)
//	{
//		for (int i = 0; i < table.length/2; i++)
//		{
//			Panel[] temp = table[i];
//			table[i] = table[table.length-1-i];
//			table[table.length-1-i] = temp;
//		}
//	}
//	
//	private Frame getBackgroundFrame(int transTime)
//	{
//		Color c = palette[0];
//		if (palette.length == 1)
//		{
//			c = Color.fromRGB(0, 0, 0);
//		}
//		return new Frame(c.getRed(), c.getGreen(),
//				c.getBlue(), 0, transTime);
//	}
//}
