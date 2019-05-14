package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Effect.Direction;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.spotify.UserOption;
import io.github.rowak.nanoleafdesktop.tools.CanvasExtStreaming;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
import io.github.rowak.nanoleafdesktop.tools.SpotifyEffectUtils;

public class SpotifySoundBarEffect extends SpotifyEffect
{
	private boolean updating;
	private int beatCounter;
	private float loudness;
	private Panel[][] panelTable;
	private Direction direction;
	
	public SpotifySoundBarEffect(Color[] palette, Direction direction,
			Aurora aurora) throws StatusCodeException
	{
		super(SpotifyEffectType.SOUNDBAR, palette, aurora);
		userOptions.add(new UserOption("Direction",
				new String[]{"Right", "Up", "Down", "Left"}));
		requiresExtControl = true;
		this.direction = direction;
		init();
	}
	
	@Override
	public void init() throws StatusCodeException
	{
		initPanelTable();
		initPalette();
	}
	
	@Override
	public void run(SpecificAudioAnalysis analysis)
					throws StatusCodeException, IOException
	{
		AudioAnalysisSegment segment = analysis.getSegment();
		updateLoudness(segment);
		
		if (segment != null && palette.length > 0)
		{
			List<Panel> updated = new ArrayList<Panel>();
			int max = (int)(panelTable.length * loudness);
			float duration = 0.5f;
			if (segment != null)
			{
				duration = segment.getMeasure().getDuration();
			}
			
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
					setPanel(p, c.getRed(), c.getGreen(),
							c.getBlue(), 1);
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
		if (!updating)
		{
			updating = true;
			new Thread(() ->
			{
				try
				{
					Thread.sleep((int)(duration*1000));
					for (int i = 0; i < panelTable.length; i++)
					{
						for (Panel p : panelTable[i])
						{
							Color c = getBackgroundColor();
							setPanel(p, c.getRed(), c.getGreen(),
									c.getBlue(), (max-i)-1);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				updating = false;
			}).start();
		}
	}
	
	private void initPanelTable() throws StatusCodeException
	{
		Panel[] panels = aurora.panelLayout().getPanelsRotated();
		if (direction == Direction.RIGHT || direction == Direction.LEFT || direction == null)
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
	
	private void updateLoudness(AudioAnalysisSegment segment)
	{
		if (segment != null)
		{
			float avg = (segment.getLoudnessMax() +
					segment.getLoudnessStart()+0.1f)/2f;
			loudness = SpotifyEffectUtils.loudnessToPercent(avg,
					segment.getLoudnessMax());
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
		Color bg = getBackgroundColor();
		float factor = (max-i)/(float)max;
		if (factor+0.25f < 1.0f)
		{
			factor+=0.25f;
		}
		else
		{
			factor = 1.0f;
		}
		int r = (int)Math.abs((factor * color.getRed()) + ((1 - factor) * bg.getRed()));
		int g = (int)Math.abs((factor * color.getGreen()) + ((1 - factor) * bg.getGreen()));
		int b = (int)Math.abs((factor * color.getBlue()) + ((1 - factor) * bg.getBlue()));
		return new java.awt.Color(r, g, b);
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
	
	private void setPanel(Panel panel, int red,
			int green, int blue, int transitionTime)
					throws StatusCodeException, IOException
	{
		String deviceType = getDeviceType();
		if (deviceType.equals("aurora"))
		{
			aurora.externalStreaming().setPanel(panel, red,
					green, blue, transitionTime);
		}
		else if (deviceType.equals("canvas"))
		{
			CanvasExtStreaming.setPanel(panel, red, green,
					blue, transitionTime, aurora);
		}
	}
	
	private String getDeviceType()
	{
		if (aurora.getName().toLowerCase().contains("light panels") ||
				aurora.getName().toLowerCase().contains("aurora"))
		{
			return "aurora";
		}
		else if (aurora.getName().toLowerCase().contains("canvas"))
		{
			return "canvas";
		}
		return null;
	}
}
