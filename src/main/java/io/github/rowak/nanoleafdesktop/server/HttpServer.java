package io.github.rowak.nanoleafdesktop.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.wrapper.spotify.SpotifyApi;

import fi.iki.elonen.NanoHTTPD;
import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Color;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyAuthenticator;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyEffectType;
import io.github.rowak.nanoleafdesktop.spotify.SpotifyPlayer;
import io.github.rowak.nanoleafdesktop.spotify.effect.SpotifyPulseBeatsEffect;
import io.github.rowak.nanoleafdesktop.tools.PanelLocations;

public class HttpServer extends NanoHTTPD {
	public static final String SPOTIFY_ENDPOINT = "/spotify";
	public static final String SPOTIFY_AUTHENTICATE_ENDPOINT = SPOTIFY_ENDPOINT + "/authenticate";
	public static final String SPOTIFY_STATE_ENDPOINT = SPOTIFY_ENDPOINT + "/state";
	public static final String SPOTIFY_STATE_AUTHENTICATED_ENDPOINT = SPOTIFY_STATE_ENDPOINT + "/authenticated";
	public static final String SPOTIFY_STATE_ENABLED_ENDPOINT = SPOTIFY_STATE_ENDPOINT + "/enabled";
	public static final String SPOTIFY_STATE_EFFECT_ENDPOINT = SPOTIFY_STATE_ENDPOINT + "/effect";
	
	public static final int DEFAULT_PORT = 7145;
	
	public static final int DEFAULT_SENSITIVITY = 9;
	public static final int DEFAULT_AUDIO_OFFSET = 0;
	public static final Color[] DEFAULT_PALETTE = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE};
	public static final SpotifyEffectType DEFAULT_EFFECT_TYPE = SpotifyEffectType.FIREWORKS;
	
	private Aurora[] devices;
	private PanelLocations panelLocations;
	private Color[] palette = DEFAULT_PALETTE;
	
	private SpotifyAuthenticator spotifyAuthenticator;
	private SpotifyPlayer spotifyPlayer;
	
	/*
	 * endpoints:
	 *   - /spotify
	 *     - /authenticate
	 *       - POST authenticate with spotify (redirect to login page)
	 *     - /state
	 *       - GET
	 *       - /authenticated
	 *         - GET
	 *       - /enabled
	 *         - GET
	 *         - PUT
	 *       - /effect
	 *         - GET
	 *         - PUT
	 *       - /palette
	 *         - GET
	 *         - PUT
	 *       - /sensitivity
	 *         - GET
	 *         - PUT
	 *       - /audio_offset
	 *         - GET
	 *         - PUT
	 */
	public HttpServer(Aurora[] devices) throws IOException {
		super(DEFAULT_PORT);
		this.devices = devices;
		try {
			spotifyAuthenticator = new SpotifyAuthenticator(true);
			SpotifyApi api = spotifyAuthenticator.getSpotifyApi();
			if (api != null) {
				spotifyPlayer = new SpotifyPlayer(api, DEFAULT_EFFECT_TYPE, palette,
						devices, null, panelLocations);
				spotifyPlayer.setSensitivity(DEFAULT_SENSITIVITY);
				spotifyPlayer.setAudioOffset(DEFAULT_AUDIO_OFFSET);
			}
		}
		catch (Exception e) {
			
		}
		System.out.println("Starting HTTP server on port " + DEFAULT_PORT + "...");
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("Server started.");
	}
	
	@Override
    public Response serve(IHTTPSession session) {
		String endpoint = session.getUri();
		byte[] buffer = new byte[0];
		if (session.getMethod() == Method.PUT) { 
			Integer contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
			buffer = new byte[contentLength];
			try {
				session.getInputStream().read(buffer, 0, contentLength);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return getResponse(session.getMethod(), endpoint, new String(buffer));
    }
	
	private Response getResponse(Method method, String endpoint, String body) {
		switch (endpoint) {
			case SPOTIFY_ENDPOINT:
				if (method == Method.GET) {
					return newFixedLengthResponse(Response.Status.OK,
							"application/json", "{\"state\":" + getSpotifyStateJson() + "}");
				}
				return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED,
						"application/json", "");
			case SPOTIFY_STATE_ENDPOINT:
				if (method == Method.GET) {
					return newFixedLengthResponse(Response.Status.OK,
							"application/json", getSpotifyStateJson());
				}
				else if (method == Method.PUT) {
					System.out.println(body);
					JSONObject obj = new JSONObject(body);
					JSONObject enabled = obj.has("enabled") ? obj.getJSONObject("enabled") : null;
					JSONObject effect = obj.has("effect") ? obj.getJSONObject("effect") : null;
					if (enabled != null && enabled.has("value")) {
						boolean value = enabled.getBoolean("value");
						System.out.println(spotifyPlayer);
						if (value && spotifyPlayer != null && !spotifyPlayer.isRunning()) {
							spotifyPlayer.start();
						}
						else if (spotifyPlayer != null && spotifyPlayer.isRunning()) {
							spotifyPlayer.stop();
						}
					}
					if (effect != null && effect.has("value")) {
						String value = effect.getString("effect");
						if (spotifyPlayer != null) {
							try {
								if (value.equalsIgnoreCase("pulse_beats")) {
									spotifyPlayer.setEffect(SpotifyEffectType.PULSE_BEATS);
								}
								else if (value.equalsIgnoreCase("soundbar")) {
									spotifyPlayer.setEffect(SpotifyEffectType.SOUNDBAR);
								}
								else if (value.equalsIgnoreCase("fireworks")) {
									spotifyPlayer.setEffect(SpotifyEffectType.FIREWORKS);
								}
								else if (value.equalsIgnoreCase("streaking_notes")) {
									spotifyPlayer.setEffect(SpotifyEffectType.STREAKING_NOTES);
								}
								return newFixedLengthResponse(Response.Status.OK,
										"application/json", "");
							}
							catch (StatusCodeException e) {
								e.printStackTrace();
							}
						}
					}
				}
				return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED,
						"application/json", "");
			case SPOTIFY_STATE_AUTHENTICATED_ENDPOINT:
				if (method == Method.GET) {
					return newFixedLengthResponse(Response.Status.OK,
							"application/json", "{\"value\":" + isSpotifyAuthenticated() + "}");
				}
				return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED,
						"application/json", "");
			case SPOTIFY_STATE_ENABLED_ENDPOINT:
				if (method == Method.GET) {
					return newFixedLengthResponse(Response.Status.OK,
							"application/json", "{\"value\":" + isSpotifyEnabled() + "}");
				}
				return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED,
						"application/json", "");
			case SPOTIFY_STATE_EFFECT_ENDPOINT:
				if (method == Method.GET) {
					return newFixedLengthResponse(Response.Status.OK,
							"application/json", "{\"value\":" + getSpotifyEffect() + "}");
				}
				else if (method == Method.PUT) {
					
				}
				return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED,
						"application/json", "");
			case SPOTIFY_AUTHENTICATE_ENDPOINT:
				if (method == Method.GET) {
					Response response = newFixedLengthResponse(
							Response.Status.REDIRECT, MIME_HTML, "");
					URI authURI = null;
					try {
						spotifyAuthenticator = new SpotifyAuthenticator(true);
						authURI = spotifyAuthenticator.getAuthCodeASync();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					response.addHeader("Location", authURI.toString());
					return response;
				}
			default:
				return newFixedLengthResponse(Response.Status.BAD_REQUEST,
						"application/json", "");
		}
	}
	
	private String getSpotifyStateJson() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"authenticated\":{\"value\":" + isSpotifyAuthenticated() + "},");
		sb.append("\"enabled\":{\"value\":" + isSpotifyEnabled() + "}");
		sb.append("\"effect\":{\"value\":" + getSpotifyEffect() + "}");
		sb.append("}");
		return sb.toString();
	}
	
	private boolean isSpotifyAuthenticated() {
		return spotifyAuthenticator != null &&
				spotifyAuthenticator.getSpotifyApi() != null;
	}
	
	private boolean isSpotifyEnabled() {
		return spotifyPlayer != null && spotifyPlayer.isRunning();
	}
	
	private String getSpotifyEffect() {
		return spotifyPlayer != null ? spotifyPlayer.getEffect().getType().toString() : null;
	}
}
