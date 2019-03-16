package io.github.rowak.nanoleafdesktop.spotify;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioAnalysisForTrackRequest;

import io.github.rowak.Aurora;
import io.github.rowak.Color;
import io.github.rowak.Effect.Direction;
import io.github.rowak.StatusCodeException;
import io.github.rowak.StatusCodeException.UnauthorizedException;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyPulseBeatsEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifySoundBarEffect;
import io.github.rowak.nanoleafdesktop.ui.panel.SpotifyPanel;

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
	private Color[] defaultPalette;
	private SpotifyPanel panel;
	
	public SpotifyPlayer(SpotifyApi spotifyApi, SpotifyEffectType defaultEffect,
			Color[] defaultPalette, Aurora aurora, SpotifyPanel panel) throws UnauthorizedException,
			HttpRequestException, StatusCodeException
	{
		this.spotifyApi = spotifyApi;
		this.defaultPalette = defaultPalette.clone();
		palette = defaultPalette.clone();
		this.aurora = aurora;
		this.panel = panel;
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
			}, 0, 2000);
		}
	}
	
	public void stop()
	{
		if (running)
		{
			running = false;
			effectTimer.cancel();
			effectTimer.purge();
			spotifyActionTimer.cancel();
			spotifyActionTimer.purge();
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
	
	public SpotifyEffect getEffect()
	{
		return effect;
	}
	
	public void setEffect(SpotifyEffectType effectType) throws StatusCodeException
	{
		switch (effectType)
		{
			case PULSE_BEATS:
				effect = new SpotifyPulseBeatsEffect(palette, aurora);
				break;
			case SOUNDBAR:
				if (palette.length > 1 &&
						Arrays.asList(palette).equals(Arrays.asList(defaultPalette)))
				{
					palette[0] = Color.fromRGB(0, 0, 0);
				}
				String directionStr = (String)getUserOptionArgs().get("direction");
				Direction direction = getDirectionFromStr(directionStr);
				effect = new SpotifySoundBarEffect(palette, direction, aurora);
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
			playing = true;
			updateTrackInfoText();
			updateTrackProgressText();
			
			if (effect != null)
			{
				effect.init();
			}
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
		catch (StatusCodeException sce)
		{
			init();
		}
	}
	
	private void update() throws UnauthorizedException,
		StatusCodeException, IOException
	{
		if (playing)
		{
			updateTrackProgressText();
			SpecificAudioAnalysis analysis = SpecificAudioAnalysis
					.getAnalysis(currentTrackAnalysis, progress, sensitivity);
			effect.run(analysis);
			progress += 100;
		}
	}
	
	private void updateTrackProgressText()
	{
		Date d = new Date((progress/1000) * 1000L);
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		panel.setTrackProgressText(df.format(d));
	}
	
	private void updateTrackInfoText()
	{
		if (playing)
		{
			String title = currentTrack.getName();
			String artists = getArtists();
			panel.setTrackInfoText(title + " | " + artists);
		}
		else
		{
			panel.setTrackInfoText("No song playing");
		}
	}
	
	private String getArtists()
	{
		ArtistSimplified[] artists = currentTrack.getArtists();
		String str = "";
		for (int i = 0; i < artists.length; i++)
		{
			str += artists[i].getName();
			if (i < artists.length-1)
			{
				str += ", ";
			}
		}
		return str;
	}
	
	private Direction getDirectionFromStr(String str)
	{
		if (str != null)
		{
			str = str.toLowerCase();
			for (Direction dir : Direction.values())
			{
				if (dir.name().toLowerCase().replace('_', ' ').equals(str))
				{
					return dir;
				}
			}
		}
		return null;
	}
	
	private Map<String, Object> getUserOptionArgs()
	{
		return panel.getUserOptionArgs();
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
			updateTrackInfoText();
			updateTrackProgressText();
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
			updateTrackInfoText();
			updateTrackProgressText();
		}
		// Detect if the user pauses a track
		else if (!current.getIs_playing() && playing)
		{
			playing = false;
			progress = current.getProgress_ms();
			updateTrackInfoText();
			updateTrackProgressText();
		}
		/*
		 * Detect if the local progress is significantly
		 * behind the actual progress
		 */
		else if (current.getIs_playing() && progressDiff >= 10)
		{
			progress = current.getProgress_ms();
			updateTrackInfoText();
			updateTrackProgressText();
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
