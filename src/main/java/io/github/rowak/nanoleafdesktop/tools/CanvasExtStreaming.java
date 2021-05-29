package io.github.rowak.nanoleafdesktop.tools;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.NanoleafException;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.util.HttpUtil;
import okhttp3.OkHttpClient;

@Deprecated
public class CanvasExtStreaming {
	
	private static InetSocketAddress getAddress(Aurora aurora) {
		final int DEFAULT_CANVAS_PORT = 60222;
		return new InetSocketAddress(aurora.getHostname(),
				DEFAULT_CANVAS_PORT);
	}
	
	public static void enable(Aurora aurora) throws NanoleafException {
		try {
			String body = "{\"write\": {\"command\": \"display\", \"animType\": " +
					"\"extControl\", \"extControlVersion\": \"v2\"}}";
			String url = String.format("http://%s:%d/api/%s/%s/%s",
					aurora.getHostname(), aurora.getPort(),
					"v1", aurora.getAccessToken(), "effects");
			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(2, TimeUnit.SECONDS)
					.build();
			HttpUtil.putHttpSync(client, url, body);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendAnimData(String animData, Aurora aurora) throws NanoleafException,
				SocketException, IOException {
		byte[] data = animDataToBytes(animData);
		
		InetSocketAddress address = getAddress(aurora);
		DatagramPacket packet = new DatagramPacket(data,
				data.length, address.getAddress(), address.getPort());
		
		try {
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
			socket.close();
		}
		catch (SocketException se) {
			throw new SocketException("Failed to connect to the target device.");
		}
		catch (IOException ioe) {
			throw new IOException("I/O error.");
		}
	}
	
	public static void setPanel(int panelId, int red, int green, int blue,
			int transitionTime, Aurora aurora) throws NanoleafException,
				SocketException, IOException {
		String frame = String.format("%s %s %d %d %d 0 %s",
				intToBigEndian(1), intToBigEndian(panelId), red, green,
				blue, intToBigEndian(transitionTime));
		sendAnimData(frame, aurora);
	}
	
	public static void setPanel(Panel panel, int red, int green, int blue,
			int transitionTime, Aurora aurora) throws NanoleafException,
				SocketException, IOException {
		setPanel(panel.getId(), red, green, blue,
				transitionTime, aurora);
	}
	
	private static byte[] animDataToBytes(String animData) {
		String[] dataStr = animData.split(" ");
		byte[] dataBytes = new byte[dataStr.length];
		for (int i = 0; i < dataStr.length; i++)
			dataBytes[i] = (byte)Integer.parseInt(dataStr[i]);
		return dataBytes;
	}
	
	private static String intToBigEndian(int num) {
		final int BYTE_SIZE = 256;
		int times = Math.floorDiv(num, BYTE_SIZE);
		return String.format("%s %s", times, num-(BYTE_SIZE*times));
	}
}
