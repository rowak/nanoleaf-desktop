package io.github.rowak.nanoleafdesktop.spotify;

import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.json.JSONObject;

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
import io.github.rowak.nanoleafapi.CustomEffect;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.Direction;
import io.github.rowak.nanoleafapi.Frame;
import io.github.rowak.nanoleafapi.NanoleafCallback;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.NanoleafGroup;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.StaticEffect;
import io.github.rowak.nanoleafapi.util.HttpUtil;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyFireworksEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyPartyMixEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyPulseBeatsEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifySoundBarEffect;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyStreakingNotesEffect;
import io.github.rowak.nanoleafdesktop.tools.CanvasExtStreaming;
import io.github.rowak.nanoleafdesktop.tools.PanelTableSort;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.panel.SpotifyPanel;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpotifyPlayer {
	
	private boolean running, playing, usingDefaultPalette;
	private int progress, sensitivity, audioOffset;
	private Map<NanoleafDevice, String> previousEffects;
	private Timer effectTimer, spotifyActionTimer;
	private SpotifyApi spotifyApi;
	private Track currentTrack;
	private AlbumSimplified currentAlbum;
	private AudioAnalysis currentTrackAnalysis;
	private NanoleafGroup group;
	private SpotifyEffect effect;
	private Color[] palette;
	private Color[] defaultPalette;
	private SpotifyPanel panel;
	private PanelCanvas canvas;
	
	public SpotifyPlayer(SpotifyApi spotifyApi, SpotifyEffectType defaultEffect,
			Color[] defaultPalette, NanoleafGroup group, SpotifyPanel panel,
			PanelCanvas canvas) throws NanoleafException, IOException {
		this.spotifyApi = spotifyApi;
		this.defaultPalette = defaultPalette.clone();
		palette = defaultPalette.clone();
		usingDefaultPalette = true;
		this.group = group;
		this.panel = panel;
		this.canvas = canvas;
		setEffect(defaultEffect);
		if (group != null) {
			enableExternalStreaming();
			start();
		}
	}
	
	public void start() {
		if (!running) {
			running = true;
			saveCurrentEffect();
			init();
			effectTimer = new Timer();
			effectTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						update();
					}
					catch (NanoleafException e) {
						e.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 0, 100);
			
			spotifyActionTimer = new Timer();
			spotifyActionTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						checkTrackStateChange();
					}
					catch (SpotifyWebApiException e) {
						e.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 0, 2000);
		}
	}
	
	public void stop() {
		if (running) {
			running = false;
			effectTimer.cancel();
			effectTimer.purge();
			spotifyActionTimer.cancel();
			spotifyActionTimer.purge();
			loadPreviousEffect();
		}
	}
	
	public void setAuroras(NanoleafGroup group) {
		this.group = group;
		if (group != null) {
			try {
				enableExternalStreaming();
			}
			catch (NanoleafException | IOException e) {
				e.printStackTrace();
			}
			if (!running) {
				start();
			}
		}
	}
	
	public void setUsingDefaultPalette(boolean usingDefaultPalette) {
		this.usingDefaultPalette = usingDefaultPalette;
	}
	
	public SpotifyEffect getEffect() {
		return effect;
	}
	
	public void setEffect(SpotifyEffectType effectType) throws NanoleafException, IOException {
		switch (effectType) {
			case PULSE_BEATS:
				effect = new SpotifyPulseBeatsEffect(palette, group);
				break;
//			case SOUNDBAR:
//				if (palette.length > 1 &&
//						(Arrays.asList(palette).equals(Arrays.asList(defaultPalette)) ||
//								usingDefaultPalette)) {
//					palette[0] = Color.fromRGB(0, 0, 0);
//				}
//				String directionStr = (String)getUserOptionArgs().get("direction");
//				Direction direction = getDirectionFromStr(directionStr);
//				effect = new SpotifySoundBarEffect(palette, direction, group, canvas);
//				break;
			case FIREWORKS:
				effect = new SpotifyFireworksEffect(palette, group);
				break;
			case STREAKING_NOTES:
				effect = new SpotifyStreakingNotesEffect(palette, group, canvas);
				break;
			case PARTY_MIX:
				effect = new SpotifyPartyMixEffect(palette, group);
				break;
		}
	}
	
	public void setPalette(Color[] palette)
			throws IOException, NanoleafException {
		this.palette = palette;
		effect.setPalette(palette);
	}
	
	public void setSensitivity(int sensitivity) {
		this.sensitivity = sensitivity;
	}
	
	public void setAudioOffset(int audioOffset) {
		this.audioOffset = audioOffset;
	}
	
	private boolean saveCurrentEffect() {
    	for (NanoleafDevice device : group.getDevices().values()) {
    		try {
    			previousEffects.put(device, device.getCurrentEffectName());
    		}
    		catch (NanoleafException | IOException e) {
    			return false;
    		}
    	}
    	return true;
    }

    private void loadPreviousEffect() {
        for (NanoleafDevice device : group.getDevices().values()) {
			device.setEffectAsync(previousEffects.get(device), (status, data, caller) -> {
				if (status != NanoleafCallback.SUCCESS) {
					new TextDialog(panel.getFocusCycleRootAncestor(),
							"The previous effect could not be loaded (Error " + status + ").").setVisible(true);
				}
			});
    	}
    }
	
	private void init() {
		try {
			System.out.println("init");
			initEffect();
			CurrentlyPlaying current = getCurrentlyPlaying();
			currentTrack = (Track)current.getItem();
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
		catch (SpotifyWebApiException swe) {
			swe.printStackTrace();
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
			init();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		catch (NanoleafException e) {
			e.printStackTrace();
		}
	}
	
	private io.github.rowak.nanoleafapi.Color[] convertPalette(java.awt.Color[] awtPalette) {
		io.github.rowak.nanoleafapi.Color[] palette =
				new io.github.rowak.nanoleafapi.Color[awtPalette.length];
		for (int i = 0; i < awtPalette.length; i++) {
			java.awt.Color c = awtPalette[i];
			palette[i] = io.github.rowak.nanoleafapi.Color.fromRGB(c.getRed(),
					c.getGreen(), c.getBlue());
		}
		return palette;
	}
	
	public void initEffect()
			throws NanoleafException, IOException {
		clearDisplay();
		if (effect.requiresExtControl()) {
			enableExternalStreaming();
		}
		effect.init();
	}
	
	private void clearDisplay() throws NanoleafException, IOException {
		group.forEach((d) -> {
			try {
				Effect clear = new CustomEffect.Builder(d)
						.addFrameToAllPanels(new Frame(0, 0, 0, 1))
						.build("", false);
				d.displayEffect(clear);
			}
			catch (NanoleafException | IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	private void update() throws NanoleafException, IOException {
		if (playing) {
			try {
				updateTrackProgressText();
				SpecificAudioAnalysis analysis = SpecificAudioAnalysis
						.getAnalysis(currentTrackAnalysis,
								progress+audioOffset, sensitivity);
				effect.run(analysis);
				progress += 100;
			}
			catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
	}
	
	private void enableExternalStreaming() throws NanoleafException, IOException {
		group.enableExternalStreaming();
	}
	
	private void updateTrackProgressText() {
		Date d = new Date((int)((progress+audioOffset)/1000f) * 1000L);
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		panel.setTrackProgressText(df.format(d));
	}
	
	private void updateTrackInfoText() {
		if (playing) {
			String title = currentTrack.getName();
			String artists = getArtists();
			panel.setTrackInfoText(title + " | " + artists);
		}
		else {
			panel.setTrackInfoText("No song playing");
		}
	}
	
	private String getArtists() {
		ArtistSimplified[] artists = currentTrack.getArtists();
		String str = "";
		for (int i = 0; i < artists.length; i++) {
			str += artists[i].getName();
			if (i < artists.length-1) {
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
	
	private Direction getDirectionFromStr(String str) {
		if (str != null) {
			str = str.toLowerCase();
			for (Direction dir : Direction.values()) {
				if (dir.name().toLowerCase().replace('_', ' ').equals(str)) {
					return dir;
				}
			}
		}
		return null;
	}
	
	private Map<String, Object> getUserOptionArgs() {
		return panel.getUserOptionArgs();
	}
	
	private void checkTrackStateChange()
			throws SpotifyWebApiException, IOException {
		CurrentlyPlaying current = getCurrentlyPlaying();
		if (current == null) {
			checkTrackStateChange();
			return;
		}
		if (current != null && !currentTrack.getId().equals(current.getItem().getId())) {
			currentTrack = (Track)current.getItem();
			currentAlbum = currentTrack.getAlbum();
			currentTrackAnalysis = getTrackAnalysis(currentTrack.getId());
			progress = current.getProgress_ms();
			effect.reset();
			updateTrackInfoText();
			updateTrackProgressText();
			
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
		if (current.getIs_playing() && !playing) {
			playing = true;
			progress = current.getProgress_ms()+500;
			effect.reset();
			updateTrackInfoText();
			updateTrackProgressText();
		}
		// Detect if the user pauses a track
		else if (!current.getIs_playing() && playing) {
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
		else if (current.getIs_playing() && progressDiff >= 10) {
			progress = current.getProgress_ms();
			updateTrackInfoText();
			updateTrackProgressText();
		}
	}
	
	private CurrentlyPlaying getCurrentlyPlaying()
			throws SpotifyWebApiException, IOException {
		final GetUsersCurrentlyPlayingTrackRequest trackRequest = spotifyApi
				.getUsersCurrentlyPlayingTrack()
				.build();
		CurrentlyPlaying curr = null;
		try {
			curr = trackRequest.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
	private AudioAnalysis getTrackAnalysis(String trackId) {
		String url = "https://api.spotify.com/v1/audio-analysis/" + trackId;
		OkHttpClient client = new OkHttpClient();
		Request request = new okhttp3.Request.Builder()
				.url(url)
				.addHeader("Content-Type", "application/json")
				.addHeader("Accept", "application/json")
				.addHeader("Authorization", "Bearer " + spotifyApi.getAccessToken())
				.get()
				.build();
		Response resp = null;
		String body = "";
		try {
			resp = client.newCall(request).execute();
			body = resp.body().string();
		}
		catch (IOException e) {
			return null;
		}
		JSONObject json = new JSONObject(body);
		json.getJSONObject("meta").put("status_code", resp.code());
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
