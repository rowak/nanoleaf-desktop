package io.github.rowak.nanoleafdesktop.spotify;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;

public class SpotifyAuthenticator
{
	private final String CLIENT_ID = "6c3ccee4f84446ccb656e50adb975123";
	private final String CLIENT_SECRET = "123e4ee42d1744b686895564f2fbe2c9";
	private final URI REDIRECT_URI = SpotifyHttpManager.makeUri("http://localhost:7142");
	private final String SCOPES = "user-read-playback-state";
	
	private final SpotifyApi spotifyApi = new SpotifyApi.Builder()
			.setClientId(CLIENT_ID)
			.setClientSecret(CLIENT_SECRET)
			.setRedirectUri(REDIRECT_URI)
			.build();
	private final AuthorizationCodeUriRequest authCodeUriRequest =
			spotifyApi.authorizationCodeUri()
			.state("x4xkmn9pu3j6ukrs8n")
			.scope(SCOPES)
			.show_dialog(true)
			.build();
	
	public SpotifyAuthenticator()
			throws SpotifyWebApiException, IOException, InterruptedException
	{
		String savedToken = getSavedAccessToken();
		if (savedToken != null)
		{
			AuthorizationCodeCredentials credentials =
					new AuthorizationCodeCredentials.Builder()
					.setAccessToken(savedToken).build();
			spotifyApi.setAccessToken(credentials.getAccessToken());
			spotifyApi.setRefreshToken(credentials.getRefreshToken());
		}
		else
		{
			String authCode = getAuthCode();
			AuthorizationCodeRequest authCodeRequest = spotifyApi.authorizationCode(authCode).build();
			AuthorizationCodeCredentials credentials = authCodeRequest.execute();
			spotifyApi.setAccessToken(credentials.getAccessToken());
			spotifyApi.setRefreshToken(credentials.getRefreshToken());
			writeAccessToken(credentials.getAccessToken());
		}
	}
	
	public SpotifyApi getSpotifyApi()
	{
		return spotifyApi;
	}
	
	private String getAuthCode()
			throws IOException, InterruptedException
	{
		URI uri = authCodeUriRequest.execute();
		
		if (Desktop.isDesktopSupported() &&
				Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			Desktop.getDesktop().browse(uri);
		}
		
		AuthCallbackServer server = new AuthCallbackServer();
		
		while (server.getAccessToken() == null)
		{
			Thread.sleep(1000);
		}
		
		if (!server.getAccessToken().equals("error"))
		{
			startRefreshTokenTimer();
		}
		stopServer(server);
		
		return server.getAccessToken();
	}
	
	private void stopServer(AuthCallbackServer server)
	{
		// Wait for server to server client, then close the server
		new Timer().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				server.stop();
			}
		}, 3000);
	}
	
	private void startRefreshTokenTimer()
	{
		/*
		 * Start the refresh token timer to request a new token when
		 * the previous one expires (every 3500 seconds)
		 */
		new Timer().scheduleAtFixedRate(new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					refreshToken();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}, 3500*1000, 3500*1000);
	}
	
	private void refreshToken()
			throws SpotifyWebApiException, IOException
	{
		AuthorizationCodeRefreshRequest authCodeRefreshRequest =
				spotifyApi.authorizationCodeRefresh().build();
		AuthorizationCodeCredentials credentials = authCodeRefreshRequest.execute();
		spotifyApi.setAccessToken(credentials.getAccessToken());
		spotifyApi.setRefreshToken(credentials.getRefreshToken());
		System.out.println("Expires in: " + credentials.getExpiresIn());
		writeAccessToken(credentials.getAccessToken());
	}
	
	private void writeAccessToken(String token)
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		manager.setProperty("spotifyToken", token);
		
		Date now = Calendar.getInstance().getTime();
		manager.setProperty("spotifyTokenCreated", now.getTime());
	}
	
	public static String getSavedAccessToken()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		String dateStr = manager.getProperty("spotifyTokenCreated");
		if (dateStr != null)
		{
			try
			{
				Date time = new Date(Long.parseLong(dateStr));
				Date now = Calendar.getInstance().getTime();
				String token = manager.getProperty("spotifyToken");
				if (now.getTime() - time.getTime() < 3550000 && token != null)
				{
					return token;
				}
			}
			catch (NumberFormatException nfe)
			{
				nfe.printStackTrace();
			}
		}
		return null;
	}
}
