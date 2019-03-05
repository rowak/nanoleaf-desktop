package io.github.rowak.nanoleafdesktop.ui.panel.spotify;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

public class AuthCallbackServer extends NanoHTTPD
{
	private final static int PORT = 7142;
	private String accessToken;
	
	public AuthCallbackServer() throws IOException
	{
		super(PORT);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
	}
	
	public String getAccessToken()
	{
		return accessToken;
	}
	
	@Override
    public Response serve(IHTTPSession session)
	{
		Map<String, String> parms = session.getParms();
		String msg = "<html>";
		if (parms.get("code") != null)
		{
			accessToken = parms.get("code");
			msg = "<html><center><body bgcolor=\"#1DB954\" text=\"#FFFFFF\"><center>" +
					"<font size=6 face=\"Arial Black\"><h1>Authentication complete.</h1>" +
					"You can now close this window.</center>";
		}
		else if (accessToken == null)
		{
			accessToken = "error";
			msg = "<body bgcolor=\"#1DB954\" text=\"#FF0000\">" +
					"<font size=6 face=\"Arial Black\"><h1>" +
					"Authentication failed.</h1>Please try again.";
		}
        return newFixedLengthResponse(msg + "</body></html>\n");
    }
}
