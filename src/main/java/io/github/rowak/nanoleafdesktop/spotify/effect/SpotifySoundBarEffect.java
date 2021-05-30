package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSection;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;

import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Direction;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.spotify.UserOption;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
import io.github.rowak.nanoleafdesktop.tools.SpotifyEffectUtils;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

public class SpotifySoundBarEffect extends SpotifyEffect
{
	private boolean updating;
	private float loudness;
	private Panel[][] panelTable;
	private Direction direction;
	private List<Float> times;
	private List<Float> sections;
	private PanelCanvas canvas;
	
	public SpotifySoundBarEffect(Color[] palette, Direction direction,
			NanoleafGroup group, PanelCanvas canvas) throws NanoleafException
	{
//		super(SpotifyEffectType.SOUNDBAR, palette, group);
		super(null, palette, group);
		userOptions.add(new UserOption("Direction",
				new String[]{"Right", "Up", "Down", "Left"}));
		requiresExtControl = true;
		this.direction = direction;
		this.canvas = canvas;
		init();
	}
	
	@Override
	public void init() throws NanoleafException
	{
		times = new ArrayList<Float>();
		sections = new ArrayList<Float>();
		initPanelTable();
		initPalette();
	}
	
	@Override
	public void reset()
	{
		times.clear();
		sections.clear();
	}
	
	@Override
	public void run(SpecificAudioAnalysis analysis)
					throws NanoleafException, IOException
	{
		AudioAnalysisSegment segment = analysis.getSegment();
		updateLoudness(segment);
		
		group.forEach((device) -> {
			if (segment != null && palette.length > 0 &&
					!times.contains(segment.getMeasure().getStart()))
			{
				times.add(segment.getMeasure().getStart());
				List<Panel> updated = new ArrayList<Panel>();
				int max = (int)(panelTable.length * loudness);
				float duration = segment.getMeasure().getDuration();
				
				AudioAnalysisSection section = analysis.getSection();
				if (section != null && !sections.contains(section.getMeasure().getStart()))
				{
					sections.add(section.getMeasure().getStart());
					setNextPaletteColor();
				}
				
				for (int i = 0; i < panelTable.length; i++)
				{
					pulse(device, i, max, updated);
				}
				fadePanelsToBackground(device, max, duration);
			}
		});
	}
	
	private void pulse(NanoleafDevice device, int i, int max, List<Panel> updated)
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
//					setPanel(p, c.getRed(), c.getGreen(),
//							c.getBlue(), 1);
//					System.out.println(p.getId() + "  " + c);
					device.setPanelExternalStreaming(p, c.getRed(), c.getGreen(), c.getBlue(), 1);
//					Thread.sleep(200);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void fadePanelsToBackground(NanoleafDevice device, int max, float duration)
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
//							setPanel(p, c.getRed(), c.getGreen(),
//									c.getBlue(), (max-i)-1);
							device.setPanelExternalStreaming(p, c.getRed(), c.getGreen(), c.getBlue(), (max-i)-1);
//							Thread.sleep(200);
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
	
	private void initPanelTable() throws NanoleafException
	{
		Panel[] combinedPanels = null;
		try {
			combinedPanels = group.getAllPanelsRotated().toArray(new Panel[0]);
		}
		catch (Exception e) {
			combinedPanels = new Panel[0];
		}
		panelTable = null;
		if (direction == Direction.RIGHT || direction == Direction.LEFT || direction == null)
		{
			panelTable = PanelTableSort.getColumns(combinedPanels);
		}
		else if (direction == Direction.UP || direction == Direction.DOWN)
		{
			panelTable = PanelTableSort.getRows(combinedPanels);
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
		if (paletteIndex == palette.length-1)
		{
			paletteIndex = palette.length > 1 ? 1 : 0;
		}
		else
		{
			paletteIndex++;
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
}
