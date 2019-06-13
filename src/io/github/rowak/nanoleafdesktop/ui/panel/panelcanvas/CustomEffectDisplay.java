package io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.rowak.Effect;
import io.github.rowak.Frame;
import io.github.rowak.Panel;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;

public class CustomEffectDisplay
{
	private int maxFrames;
	private boolean running;
	private Effect effect;
	private PanelCanvas canvas;
	private Map<Integer, List<PanelFrame>> frames;
	private Thread mainLoop;
	
	public CustomEffectDisplay(PanelCanvas canvas)
	{
		this.canvas = canvas;
	}
	
	public void changeEffect(Effect effect)
	{
		if (effect != null && (this.effect == null ||
				(this.effect != null && !this.effect.equals(effect))))
		{
			this.effect = effect;
			
			if (running)
			{
				new Thread(() ->
				{
					stop();
					try
					{
						Thread.sleep(2000);
					}
					catch (InterruptedException e)
					{
						// do nothing
					}
					parseAnimData(effect.getAnimData());
					start();
				}).start();
			}
			else
			{
				parseAnimData(effect.getAnimData());
				start();
			}
		}
	}
	
	public Effect getEffect()
	{
		return effect;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	private void parseAnimData(String animData)
	{
		frames = new HashMap<Integer, List<PanelFrame>>();
		
		final String[] dataTemp = animData.split(" ");
		final int numPanels = Integer.parseInt(dataTemp[0]);
		final int[] data = new int[dataTemp.length-1];
		for (int i = 1; i < dataTemp.length; i++)
		{
			data[i-1] = Integer.parseInt(dataTemp[i]);
		}
		
		maxFrames = 0;
		int x = 0;
		while (x < data.length)
		{
			int panelId = data[x];
			int numFrames = data[x+1];
			if (numFrames > maxFrames)
				maxFrames = numFrames;
			for (int i = 0; i < numFrames; i++)
			{
				int r = data[x + 2 + i*5];
				int g = data[x + 3 + i*5];
				int b = data[x + 4 + i*5];
				int w = data[x + 5 + i*5];
				int t = data[x + 6 + i*5];
				
				PanelFrame frame = new PanelFrame(getPanelById(
						panelId, canvas.getPanels(0)),
						new Frame(r, g, b, w, t));
				if (!frames.containsKey(i))
				{
					frames.put(i, new ArrayList<PanelFrame>());
				}
				frames.get(i).add(frame);
			}
			x += 2 + 5*numFrames;
		}
	}
	
	public void start()
	{
		if (!running)
		{
			running = true;
			run();
		}
	}
	
	public void stop()
	{
		if (running)
		{
			running = false;
			this.effect = null;
			try
			{
				Thread.sleep(1000);
				mainLoop.interrupt();
			}
			catch (InterruptedException ie)
			{
				new TextDialog(canvas.getTopLevelAncestor(),
						"Failed to stop the previous effect preview. "
						+ "Please relaunch the application.").setVisible(true);
			}
		}
	}
	
	private void run()
	{
		mainLoop = new Thread(() ->
		{
			while (running)
			{
				for (int i = 0; i < maxFrames; i++)
				{
					int numDone = 0;
					boolean[] doneFrames = new boolean[frames.get(i).size()];
					for (int k = 0; k < frames.get(i).size(); k++)
					{
						doneFrames[k] = false;
						PanelFrame pf = frames.get(i).get(k);
						Frame frame = pf.getFrame();
						Color c = new Color(frame.getRed(),
								frame.getGreen(), frame.getBlue());
						final int kf = k;
						new Thread(() ->
						{
							try
							{
								canvas.transitionToColor(pf.getPanel(), c,
										frame.getTransitionTime()*100);
							}
							catch (InterruptedException ie)
							{
								ie.printStackTrace();
							}
							doneFrames[kf] = true;
						}).start();
					}
					
					while (numDone < frames.get(i).size() && running)
					{
						numDone = 0;
						for (int k = 0; k < doneFrames.length; k++)
						{
							if (doneFrames[k])
							{
								numDone++;
							}
						}
					}
				}
			}
		});
		mainLoop.start();
	}
	
	private Panel getPanelById(int id, Panel[] panels)
	{
		for (Panel p : panels)
		{
			if (p.getId() == id)
			{
				return p;
			}
		}
		return null;
	}
}
