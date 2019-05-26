package io.github.rowak.nanoleafdesktop.spotify.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Panel;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.spotify.SpecificAudioAnalysis;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.tools.CanvasExtStreaming;

public class SpotifyStreakingNotesEffect extends SpotifyEffect
{
	private List<Panel> edges;
	private List<Float> times;
	private Random random;
	
	public SpotifyStreakingNotesEffect(Color[] palette, Aurora aurora)
	{
		super(SpotifyEffectType.STREAKING_NOTES, palette, aurora);
		requiresExtControl = true;
		random = new Random();
		init();
	}

	@Override
	public void init()
	{
		random = new Random();
		times = new ArrayList<Float>();
		getEdgePanels();
	}
	
	@Override
	public void reset()
	{
		times.clear();
	}

	@Override
	public void run(SpecificAudioAnalysis analysis)
			throws StatusCodeException, IOException
	{
		AudioAnalysisSegment segment = analysis.getSegment();
		
		if (segment != null && palette.length > 0 &&
				!times.contains(segment.getMeasure().getStart()))
		{
			times.add(segment.getMeasure().getStart());
			new Thread(() ->
			{
				List<Panel> path = getPath();
				for (int i = 0; i < path.size(); i++)
				{
					try
					{
						Color dark = Color.fromRGB((int)(palette[paletteIndex].getRed()*0.5),
								(int)(palette[paletteIndex].getGreen()*0.5),
								(int)(palette[paletteIndex].getBlue()*0.5));
						setPanel(path.get(i), dark, 3);
						if (i+1 < path.size())
						{
							setPanel(path.get(i+1), palette[paletteIndex], 3);
						}
						Thread.sleep((int)(path.size()/
								segment.getMeasure().getDuration())*6);
						setPanel(path.get(i), Color.BLACK, 3);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				setNextPaletteColor();
			}).start();
		}
	}
	
	private List<Panel> getPath()
	{
		List<Panel> path = new ArrayList<Panel>();
		Panel start = edges.get(random.nextInt(edges.size()));
		path.add(start);
		Panel[] neighbors = start.getNeighbors(panels);
		Panel p = neighbors[random.nextInt(neighbors.length)];
		path.add(p);
		while (!isEdgePanel(p))
		{
			neighbors = p.getNeighbors(panels);
			int i = random.nextInt(neighbors.length);
			if (!path.contains(neighbors[i]))
			{
				p = neighbors[i];
				path.add(p);
			}
		}
		return path;
	}
	
	private boolean isEdgePanel(Panel p)
	{
		return p.getNeighbors(panels).length < 2;
	}
	
	// An "edge" panel is defined as a panel with less than two neighbors
	private void getEdgePanels()
	{
		edges = new ArrayList<Panel>();
		for (Panel p : panels)
		{
			if (isEdgePanel(p))
			{
				edges.add(p);
			}
		}
	}
	
	private void setPanel(Panel panel, Color color, int transitionTime)
					throws StatusCodeException, IOException
	{
		String deviceType = getDeviceType();
		if (deviceType.equals("aurora"))
		{
			aurora.externalStreaming().setPanel(panel, color.getRed(),
					color.getGreen(), color.getBlue(), transitionTime);
		}
		else if (deviceType.equals("canvas"))
		{
			CanvasExtStreaming.setPanel(panel, color.getRed(),
					color.getGreen(), color.getBlue(),
					transitionTime, aurora);
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
