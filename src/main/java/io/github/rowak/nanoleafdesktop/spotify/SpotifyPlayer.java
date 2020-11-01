package io.github.rowak.nanoleafdesktop.spotify;

import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysis;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeasure;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisMeta;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSection;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisSegment;
import com.wrapper.spotify.model_objects.miscellaneous.AudioAnalysisTrack;
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Image;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.player.GetUsersCurrentlyPlayingTrackRequest;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.Effect.Direction;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafapi.StatusCodeException.UnauthorizedException;
import io.github.rowak.nanoleafapi.effectbuilder.CustomEffectBuilder;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyFireworksEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyPulseBeatsEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifySoundBarEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyStreakingNotesEffect;
import io.github.rowak.nanoleafdesktop.tools.CanvasExtStreaming;
import io.github.rowak.nanoleafdesktop.tools.PanelLocations;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.panel.SpotifyPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;

public class SpotifyPlayer
{
	private boolean running, playing, usingDefaultPalette;
	private int progress, sensitivity, audioOffset;
	private int previousHue, previousSat, previousBri;
	private String previousEffect;
	private Timer effectTimer, spotifyActionTimer;
	private SpotifyApi spotifyApi;
	private Track lastTrack, currentTrack;
	private AlbumSimplified currentAlbum;
	private AudioAnalysis currentTrackAnalysis;
	private Aurora[] auroras;
	private SpotifyEffect effect;
	private Color[] palette;
	private Color[] defaultPalette;
	private SpotifyPanel panel;
	private PanelLocations panelLocations;
	
	public SpotifyPlayer(SpotifyApi spotifyApi, SpotifyEffectType defaultEffect,
			Color[] defaultPalette, Aurora[] auroras, SpotifyPanel panel,
			PanelLocations panelLocations) throws UnauthorizedException,
			HttpRequestException, StatusCodeException
	{
		this.spotifyApi = spotifyApi;
		this.defaultPalette = defaultPalette.clone();
		palette = defaultPalette.clone();
		usingDefaultPalette = true;
		this.auroras = auroras;
		this.panel = panel;
		this.panelLocations = panelLocations;
		setEffect(defaultEffect);
		if (auroras != null)
		{
			enableExternalStreaming();
//			start();
		}
	}
	
	public void start()
	{
		if (!running)
		{
			try
			{
				enableExternalStreaming();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			running = true;
			saveCurrentEffect();
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
			loadPreviousEffect();
		}
	}
	
	public void setAuroras(Aurora[] auroras)
	{
		this.auroras = auroras;
		if (auroras != null)
		{
			try
			{
				enableExternalStreaming();
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
	
	public void setUsingDefaultPalette(boolean usingDefaultPalette)
	{
		this.usingDefaultPalette = usingDefaultPalette;
	}
	
	public SpotifyEffect getEffect()
	{
		return effect;
	}
	
	public boolean isPlaying()
	{
		return playing;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public void setEffect(SpotifyEffectType effectType) throws StatusCodeException
	{
		switch (effectType)
		{
			case PULSE_BEATS:
				effect = new SpotifyPulseBeatsEffect(palette, auroras);
				break;
			case SOUNDBAR:
				if (palette.length > 1 &&
						(Arrays.asList(palette).equals(Arrays.asList(defaultPalette)) ||
								usingDefaultPalette))
				{
					palette[0] = Color.fromRGB(0, 0, 0);
				}
				String directionStr = (String)getUserOptionArgs().get("direction");
				Direction direction = getDirectionFromStr(directionStr);
				effect = new SpotifySoundBarEffect(palette, direction, auroras, panelLocations);
				break;
			case FIREWORKS:
				effect = new SpotifyFireworksEffect(palette, auroras);
				break;
			case STREAKING_NOTES:
				effect = new SpotifyStreakingNotesEffect(palette, auroras, panelLocations);
				break;
		}
	}
	
	public void setPalette(Color[] palette)
			throws IOException, StatusCodeException
	{
		this.palette = palette;
		effect.setPalette(palette);
	}
	
	public void setSensitivity(int sensitivity)
	{
		this.sensitivity = sensitivity;
	}
	
	public void setAudioOffset(int audioOffset)
	{
		this.audioOffset = audioOffset;
	}
	
	private void saveCurrentEffect()
	{
		try
		{
			previousEffect = auroras[0].effects().getCurrentEffectName();
			if (previousEffect != null && previousEffect.equals("*Solid*"))
			{
				previousHue = auroras[0].state().getHue();
				previousSat = auroras[0].state().getSaturation();
				previousBri = auroras[0].state().getBrightness();
			}
		}
		catch (StatusCodeException sce)
		{
			// ignore this exception for now, handle it
			// later in the loadPreviousEffect() method
		}
	}
	
	private void loadPreviousEffect()
	{
		try
		{
			if (previousEffect != null && !previousEffect.equals("*Dynamic*") && !previousEffect.equals("*Solid*"))
			{
				auroras[0].effects().setEffect(previousEffect);
			}
			else if (previousEffect.equals("*Solid*"))
			{
				auroras[0].state().setHue(previousHue);
				auroras[0].state().setSaturation(previousSat);
				auroras[0].state().setBrightness(previousBri);
			}
		}
		catch (StatusCodeException | NullPointerException e)
		{
			e.printStackTrace();
			if (panel != null)
			{
				new TextDialog(panel.getFocusCycleRootAncestor(),
						"The previous effect could not be loaded.").setVisible(true);
			}
		}
	}
	
	private void init()
	{
		try
		{
			System.out.println("init");
			initEffect();
			CurrentlyPlaying current = getCurrentlyPlaying();
			currentTrack = current.getItem();
			currentAlbum = currentTrack.getAlbum();
			currentTrackAnalysis = getTrackAnalysis(currentTrack.getId());
			progress = current.getProgress_ms();
			playing = true;
			updateTrackInfoText();
			updateTrackProgressText();
			
//			java.awt.Color[] newpalette = getAlbumImagePalette();
//			setPalette(convertPalette(newpalette));
//			System.out.println(Arrays.asList(newpalette));
//			panel.setPalette(newpalette);
		}
		catch (SpotifyWebApiException swe)
		{
			swe.printStackTrace();
		}
		catch (NullPointerException npe)
		{
			npe.printStackTrace();
			init();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
		}
	}
	
	private io.github.rowak.nanoleafapi.Color[] convertPalette(java.awt.Color[] awtPalette)
	{
		io.github.rowak.nanoleafapi.Color[] palette =
				new io.github.rowak.nanoleafapi.Color[awtPalette.length];
		for (int i = 0; i < awtPalette.length; i++)
		{
			java.awt.Color c = awtPalette[i];
			palette[i] = io.github.rowak.nanoleafapi.Color.fromRGB(c.getRed(),
					c.getGreen(), c.getBlue());
		}
		return palette;
	}
	
	public void initEffect()
			throws StatusCodeException, IOException
	{
		clearDisplay();
		if (effect.requiresExtControl())
		{
			enableExternalStreaming();
		}
		effect.init();
	}
	
	private void clearDisplay() throws StatusCodeException
	{
		for (Aurora aurora : auroras)
		{
			Effect clear = new CustomEffectBuilder(aurora)
					.addFrameToAllPanels(new Frame(0, 0, 0, 0, 1))
					.build("", false);
			aurora.effects().displayEffect(clear);
		}
	}
	
	private void update() throws UnauthorizedException,
		StatusCodeException, IOException
	{
		if (playing)
		{
			try
			{
				updateTrackProgressText();
				SpecificAudioAnalysis analysis = SpecificAudioAnalysis
						.getAnalysis(currentTrackAnalysis,
								progress+audioOffset, sensitivity);
				effect.run(analysis);
				progress += 100;
			}
			catch (NullPointerException npe)
			{
				npe.printStackTrace();
			}
		}
	}
	
	private void enableExternalStreaming() throws StatusCodeException
	{
		for (Aurora aurora : auroras)
		{
			String deviceType = getDeviceType(aurora);
			if (deviceType.equals("aurora"))
			{
				aurora.externalStreaming().enable();
			}
			else if (deviceType.equals("canvas"))
			{
				CanvasExtStreaming.enable(aurora);
			}
		}
	}
	
	private String getDeviceType(Aurora aurora)
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
	
	private void updateTrackProgressText()
	{
		if (panel != null)
		{
			Date d = new Date((int)((progress+audioOffset)/1000f) * 1000L);
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));
			panel.setTrackProgressText(df.format(d));
		}
	}
	
	private void updateTrackInfoText()
	{
		if (panel != null)
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
	
//	private BufferedImage getAlbumImage() throws IOException
//	{
//		BufferedImage img = null;
//		if (currentAlbum.getImages().length > 0)
//		{
//			Image raw = currentAlbum.getImages()[1];
//			img = ImageIO.read(new URL(raw.getUrl()));
//		}
//		return img;
//	}
//	
//	private java.awt.Color[] getAlbumImagePalette() throws StatusCodeException, IOException
//	{
//		Panel[][] rows = PanelTableSort.getRows(auroras[0].panelLayout().getPanelsRotated());
//		BufferedImage img = getAlbumImage();
//		Set<java.awt.Color> colors = new HashSet<java.awt.Color>();
//		if (img != null)
//		{
//			final int VERTICAL_SEPARATOR = img.getHeight()/rows.length;
//			for (int i = 0; i < rows.length; i++)
//			{
//				int captureY = VERTICAL_SEPARATOR*i + VERTICAL_SEPARATOR/2;
//				
//				for (int j = 0; j < rows[i].length; j++)
//				{
//					final int HORIZONTAL_SEPARATOR = img.getWidth()/rows[i].length;
//					int captureX = HORIZONTAL_SEPARATOR*j + HORIZONTAL_SEPARATOR/2;
//					
//					try
//					{
//						if (img.getSubimage(captureX, captureY, 1, 1) != null)
//						{
//							java.awt.Color c = new java.awt.Color(img.getRGB(captureX, captureY));
//							if (!c.equals(java.awt.Color.BLACK))
//							{
//								colors.add(c);
//							}
//						}
//					}
//					catch (RasterFormatException rfe)
//					{
//						// catch, but ignore
//					}
//				}
//			}
//		}
//		else
//		{
//			System.out.println("ERROR: Invalid album image");
//		}
//		return colors.toArray(new java.awt.Color[]{});
//	}
	
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
			lastTrack = currentTrack;
			currentTrack = current.getItem();
			currentAlbum = currentTrack.getAlbum();
			currentTrackAnalysis = getTrackAnalysis(currentTrack.getId());
			progress = current.getProgress_ms();
			effect.reset();
			updateTrackInfoText();
			updateTrackProgressText();
			if (lastTrack != currentTrack)
			{
				System.out.println("Now playing: " + currentTrack.getName());
			}
			
//			if (usingDefaultPalette)
//			{
//				try
//				{
//					java.awt.Color[] newpalette = getAlbumImagePalette();
//					setPalette(convertPalette(newpalette));
//					panel.setPalette(newpalette);
//				}
//				catch (StatusCodeException e) 
//				{
//					e.printStackTrace();
//				}
//			}
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
			effect.reset();
			updateTrackInfoText();
			updateTrackProgressText();
		}
		// Detect if the user pauses a track
		else if (!current.getIs_playing() && playing)
		{
			playing = false;
			progress = current.getProgress_ms();
			effect.reset();
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
		CurrentlyPlaying curr = trackRequest.execute();
		return curr;
	}
	
	/*
	 * This is a very hacky solution to a very annoying problem. The Spotify Java
	 * API library I'm using doesn't properly handle JSON exceptions, so when
	 * Spotify made a very small change to the JSON format, it caused the
	 * AudioAnalysis object creation to fail.
	 * 
	 * This solution handles the JSON format change.
	 */
	private AudioAnalysis getTrackAnalysis(String trackId)
	{
		HttpRequest req = HttpRequest.get("https://api.spotify.com/v1/audio-analysis/" + trackId);
		req.header("Content-Type", "application/json");
		req.header("Accept", "application/json");
		req.header("Authorization", "Bearer " + spotifyApi.getAccessToken());
		JSONObject json = new JSONObject(req.body());
		json.getJSONObject("meta").put("status_code", req.code());
		AudioAnalysisMeta aamet = new AudioAnalysisMeta.JsonUtil().createModelObject(json.getJSONObject("meta").toString());
		AudioAnalysisTrack aat = new AudioAnalysisTrack.JsonUtil().createModelObject(json.getJSONObject("track").toString());
		AudioAnalysisMeasure[] aamba = new AudioAnalysisMeasure.JsonUtil().createModelObjectArray(json.getJSONArray("bars").toString());
		AudioAnalysisMeasure[] aambe = new AudioAnalysisMeasure.JsonUtil().createModelObjectArray(json.getJSONArray("beats").toString());
		AudioAnalysisSection[] aasec = new AudioAnalysisSection.JsonUtil().createModelObjectArray(json.getJSONArray("sections").toString());
		AudioAnalysisSegment[] aaseg = new AudioAnalysisSegment.JsonUtil().createModelObjectArray(json.getJSONArray("segments").toString());
		AudioAnalysisMeasure[] aamta = new AudioAnalysisMeasure.JsonUtil().createModelObjectArray(json.getJSONArray("tatums").toString());
		return new AudioAnalysis.Builder()
				.setMeta(aamet)
				.setTrack(aat)
				.setBars(aamba)
				.setBeats(aambe)
				.setSections(aasec)
				.setSegments(aaseg)
				.setTatums(aamta)
				.build();
	}
	
//	private AudioAnalysis getTrackAnalysis(String trackId)
//			throws SpotifyWebApiException, IOException
//	{
//		final GetAudioAnalysisForTrackRequest trackAnalysisRequest = spotifyApi
//				.getAudioAnalysisForTrack(trackId)
//				.build();
//		return trackAnalysisRequest.execute();
//	}
}
