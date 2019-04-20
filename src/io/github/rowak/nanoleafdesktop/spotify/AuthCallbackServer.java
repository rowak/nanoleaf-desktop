package io.github.rowak.nanoleafdesktop.spotify;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

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
		String msg = "<html><center><body bgcolor=\"#1DB954\" text=\"#FFFFFF\">" +
					 "<font size=6 face=\"Arial Black\">";
		if (parms.get("code") != null)
		{
			Calendar expireCal = Calendar.getInstance();
			expireCal.add(Calendar.HOUR, 1);
			Date expireTime = expireCal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("MMM d, y HH:mm:ss");
			accessToken = parms.get("code");
			msg += "<h1>Authentication complete.</h1>" +
					"<h3></h3>Your panels will be synced in a moment. " +
					"You can now close this window.";
			msg += "<br><p id=\"countdown\">This session will expire in: </p>";
			msg += "<script>var target = new Date(\"" + sdf.format(expireTime) + "\").getTime(); " +
					"var x = setInterval(function() { var now = new Date().getTime(); " +
					"var distance = target - now; " +
					"var minutes = Math.floor((distance % (1000*60*60)) / (1000*60)); " +
					"var seconds = Math.floor((distance % (1000*60)) / 1000); " +
					"document.getElementById(\"countdown\").innerHTML = \"This session will expire in: \"" +
					"+ minutes + \"m \" + seconds + \"s\"; " + "if (distance < 0) { clearInterval(x); " +
					"document.getElementById(\"countdown\").innerHTML = \"This session will expire in: " +
					"*Expired*\"; } }, 1000);</script>";
		}
		else if (accessToken == null)
		{
			accessToken = "error";
			msg += "Authentication failed.</h1>Please try again.</center>";
		}
        return newFixedLengthResponse(msg + "</center></body></html>\n");
    }
}
