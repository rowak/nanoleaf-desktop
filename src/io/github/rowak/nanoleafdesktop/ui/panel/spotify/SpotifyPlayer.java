package io.github.rowak.nanoleafdesktop.ui.panel.spotify;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeasure;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSection;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioAnalysisForTrackRequest;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.StatusCodeException;
import io.github.rowak.StatusCodeException.UnauthorizedException;

public class SpotifyPlayer
{
	private boolean running, playing;
	private int progress, sensitivity;
	private Timer effectTimer, spotifyActionTimer;
	private SpotifyApi spotifyApi;
	private Track currentTrack;
	private AudioAnalysis currentTrackAnalysis;
	private Aurora aurora;
	private SpotifyEffect effect;
	private Color[] palette;
	
	public SpotifyPlayer(SpotifyApi spotifyApi, SpotifyEffect.Type defaultEffect,
			Color[] defaultPalette, Aurora aurora) throws UnauthorizedException,
			HttpRequestException, StatusCodeException
	{
		this.spotifyApi = spotifyApi;
		palette = defaultPalette;
		this.aurora = aurora;
		setEffect(defaultEffect);
		if (aurora != null)
		{
			aurora.externalStreaming().enable();
			start();
		}
	}
	
	public void start()
	{
		if (!running)
		{
			running = true;
			init();
			effectTimer = new Timer();
			effectTimer.scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					try
					{
						update();
					}
					catch (UnauthorizedException e)
					{
						e.printStackTrace();
					}
					catch (StatusCodeException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}, 0, 100);
			
			
			spotifyActionTimer = new Timer();
			spotifyActionTimer.scheduleAtFixedRate(new TimerTask()
			{
				@Override
				public void run()
				{
					try
					{
						checkTrackStateChange();
					}
					catch (SpotifyWebApiException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}, 0, 1000);
		}
	}
	
	public void stop()
	{
		if (running)
		{
			running = false;
			effectTimer.cancel();
			effectTimer.purge();
		}
	}
	
	public void setAurora(Aurora aurora)
	{
		this.aurora = aurora;
		if (aurora != null)
		{
			try
			{
				aurora.externalStreaming().enable();
			}
			catch (StatusCodeException sce)
			{
				sce.printStackTrace();
			}
			if (!running)
			{
				start();
			}
		}
	}
	
	public void setEffect(SpotifyEffect.Type effectType)
	{
		switch (effectType)
		{
			case PULSE_BEATS:
				effect = new SpotifyPulseBeatsEffect(palette, aurora);
				break;
		}
	}
	
	public void setPalette(Color[] palette)
	{
		effect.setPalette(palette);
	}
	
	public void setSensitivity(int sensitivity)
	{
		this.sensitivity = sensitivity;
	}
	
	private void init()
	{
		try
		{
			CurrentlyPlaying current = getCurrentlyPlaying();
			currentTrack = current.getItem();
			currentTrackAnalysis = getTrackAnalysis(currentTrack.getId());
			progress = current.getProgress_ms();
		}
		catch (SpotifyWebApiException swe)
		{
			swe.printStackTrace();
		}
		catch (NullPointerException npe)
		{
			init();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	private void update()
			throws UnauthorizedException, StatusCodeException, IOException
	{
		if (playing)
		{
			AudioAnalysisMeasure beat = getBeatAtPos();
			if (beat != null)
			{
				System.out.println("BEAT  start=" + beat.getStart() +
						"  confidence=" + beat.getConfidence() + "  duration=" + beat.getDuration());
				effect.runBeat();
			}
//			try
//			{
//				for (AudioAnalysisMeasure measure : currentTrackAnalysis.getBeats())
//				{
//					if (measure.getConfidence() > (float)(10-sensitivity)/10f)
//					{
//						double start = round(measure.getStart(), 1);
//						double pos = round((float)progress/1000, 1);
//						if (start == pos)
//						{
//							System.out.println("BEAT  start=" + start + "  pos=" + pos +
//									"  confidence=" + measure.getConfidence() + "  duration=" + measure.getDuration());	
//							effect.runBeat();
//							break;
				
				
//				for (AudioAnalysisMeasure measure : currentTrackAnalysis.getBeats())
//				{
//					//AudioAnalysisMeasure measure = section.getMeasure();
//					if (measure.getConfidence() > (float)(10-sensitivity)/10f)
//					{
//						double start = round(measure.getStart(), 1);
//						double pos = round((float)progress/1000, 1);
//						if (start == pos)
//						{
//							
//							//System.out.println("SEGMENT  " + section.getLoudnessStart() + "  " + section.getLoudnessEnd() + "  " + section.getLoudnessMax());
////							System.out.println("BEAT  start=" + start +
////									"  confidence=" + measure.getConfidence() + "  duration=" + measure.getDuration() + "  loudness=" + section.get());	
//							//effect.runBeat();
//							break;
							
							
							
							
							// "RIPPLE" PULSE BEATS WITH NO FADE
							
	//						int panelIndex = random.nextInt(panels.length);
	//						int panelId = panels[panelIndex].getId();
	//						int r = palette[paletteIndex].getRed();
	//						int g = palette[paletteIndex].getGreen();
	//						int b = palette[paletteIndex].getBlue();
	//						java.awt.Color original = new java.awt.Color(r, g, b);
	//						CustomEffectBuilder ceb = new CustomEffectBuilder(aurora);
	//						List<Integer> marked = new ArrayList<Integer>();
	//						final int INITIAL_TIME = 1;
	//						final int EXPLODE_FACTOR = 3;
	//						setNeighbors(panels[panelIndex], marked, EXPLODE_FACTOR, 
	//								panels, ceb, original, INITIAL_TIME);
	//						aurora.effects().displayEffect(ceb.build("", false));
	//						
	//						if (paletteIndex == palette.length-1)
	//						{
	//							paletteIndex = 0;
	//						}
	//						else
	//						{
	//							paletteIndex++;
	//						}
							
							
							
							
							// MULTI-PANEL PULSE BEATS WITH FADE
							
	//						int numPanels = random.nextInt(3);
	//						Panel[] changed = new Panel[numPanels];
	//						for (int i = 0; i < numPanels; i++)
	//						{
	//							int panelNum = random.nextInt(panels.length-1);
	//							int panelId = panels[panelNum].getId();
	//							int r = palette[paletteIndex].getRed();
	//							int g = palette[paletteIndex].getGreen();
	//							int b = palette[paletteIndex].getBlue();
	//							aurora.externalStreaming().setPanel(panelId, r, g, b, 1);
	//							changed[i] = panels[panelNum];
	//							
	//							if (paletteIndex == palette.length-1)
	//							{
	//								paletteIndex = 0;
	//							}
	//							else
	//							{
	//								paletteIndex++;
	//							}
	//						}
							
	//						new Thread(() ->
	//						{
	//							try
	//							{
	//								Thread.sleep(50);
	//							} catch (Exception e1)
	//							{
	//								e1.printStackTrace();
	//							}
	//							for (int i = 0; i < numPanels; i++)
	//							{
	//								try
	//								{
	//									aurora.externalStreaming().setPanel(changed[i].getId(), 0, 0, 0, 25);
	//								}
	//								catch (Exception e)
	//								{
	//									e.printStackTrace();
	//								}
	//							}
	//						}).start();
							
							
							
							
							// SINGLE-PANEL PULSE BEATS WITH FADE
							
	//						int panelId = panels[random.nextInt(panels.length)].getId();
	//						int r = palette[paletteIndex].getRed();
	//						int g = palette[paletteIndex].getGreen();
	//						int b = palette[paletteIndex].getBlue();
	//						aurora.externalStreaming().setPanel(panelId, r, g, b, 1);
	//						if (paletteIndex == palette.length-1)
	//						{
	//							paletteIndex = 0;
	//						}
	//						else
	//						{
	//							paletteIndex++;
	//						}
	//						
	//						new Thread(() ->
	//						{
	//							try
	//							{
	//								Thread.sleep(50);
	//							}
	//							catch (Exception e1)
	//							{
	//								e1.printStackTrace();
	//							}
	//							try
	//							{
	//								aurora.externalStreaming().setPanel(panelId, 0, 0, 0, 25);
	//							}
	//							catch (Exception e)
	//							{
	//								e.printStackTrace();
	//							}
	//						}).start();
	//						break;
//						}
//					}
//				}
//			}
//			catch (IOException ioe)
//			{
//				ioe.printStackTrace();
//			}
			progress+=100;
		}
	}
	
	private AudioAnalysisMeasure getBeatAtPos()
	{
		for (AudioAnalysisMeasure measure : currentTrackAnalysis.getBeats())
		{
			if (measure.getConfidence() > (float)(10-sensitivity)/10f)
			{
				double start = round(measure.getStart(), 1);
				double pos = round((float)progress/1000, 1);
				if (start == pos)
				{
					return measure;
				}
			}
		}
		return null;
	}
	
	private AudioAnalysisSegment getSegmentAtPos()
	{
		for (AudioAnalysisSegment segment : currentTrackAnalysis.getSegments())
		{
			AudioAnalysisMeasure measure = segment.getMeasure();
			if (measure.getConfidence() > (float)(10-sensitivity)/10f)
			{
				double start = round(measure.getStart(), 1);
				double pos = round((float)progress/1000, 1);
				if (start == pos)
				{
					return segment;
				}
			}
		}
		return null;
	}
	
	private AudioAnalysisSection getSectionAtPos()
	{
		for (AudioAnalysisSection section : currentTrackAnalysis.getSections())
		{
			AudioAnalysisMeasure measure = section.getMeasure();
			if (measure.getConfidence() > (float)(10-sensitivity)/10f)
			{
				double start = round(measure.getStart(), 1);
				double pos = round((float)progress/1000, 1);
				double end = start + round(measure.getDuration(), 1);
				if (start < pos && pos < end)
				{
					return section;
				}
			}
		}
		return null;
	}
	
	private double round(float num, int decimals)
	{
		return (double)Math.round((double)num * 10d)/10d;
	}
	
	private void checkTrackStateChange()
			throws SpotifyWebApiException, IOException
	{
		CurrentlyPlaying current = getCurrentlyPlaying();
		if (current == null)
		{
			checkTrackStateChange();
			return;
		}
		if (current != null && !currentTrack.getId().equals(current.getItem().getId()))
		{
			currentTrack = current.getItem();
			currentTrackAnalysis = getTrackAnalysis(currentTrack.getId());
			progress = current.getProgress_ms();
		}
		
		float progressDiff = Math.abs(current.getProgress_ms() - progress);
		
		/*
		 *  Detect if user starts playing a track (note: added
		 *  500ms to progress to simulate delay between pressing
		 *  the play button and the change taking effect)
		 */
		if (current.getIs_playing() && !playing)
		{
			playing = true;
			progress = current.getProgress_ms()+500;
		}
		// Detect if the user pauses a track
		else if (!current.getIs_playing() && playing)
		{
			playing = false;
			progress = current.getProgress_ms();
		}
		/*
		 * Detect if the local progress is significantly
		 * behind the actual progress
		 */
		else if (current.getIs_playing() && progressDiff >= 10)
		{
			progress = current.getProgress_ms();
		}
	}
	
	private CurrentlyPlaying getCurrentlyPlaying()
			throws SpotifyWebApiException, IOException
	{
		final GetUsersCurrentlyPlayingTrackRequest trackRequest = spotifyApi
				.getUsersCurrentlyPlayingTrack()
				.build();
		return trackRequest.execute();
	}
	
	private AudioAnalysis getTrackAnalysis(String trackId)
			throws SpotifyWebApiException, IOException
	{
		final GetAudioAnalysisForTrackRequest trackAnalysisRequest = spotifyApi
				.getAudioAnalysisForTrack(trackId)
				.build();
		return trackAnalysisRequest.execute();
	}
}
