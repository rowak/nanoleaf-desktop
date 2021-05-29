package io.github.rowak.nanoleafdesktop.tools;

import io.github.rowak.nanoleafapi.util.HttpUtil;
import io.github.rowak.nanoleafdesktop.ui.dialog.OptionDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import okhttp3.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class UpdateManager {
	
	private String host, repo;

	public UpdateManager(String host, String repo) {
		this.host = host;
		this.repo = repo + "/releases";
	}

	public boolean updateAvailable(Version current) throws IOException {
		Version latest = getLatestVersionFromHost();
		return latest.compareTo(current) > 0;
	}

	private Version getLatestVersionFromHost() throws IOException {
		String responseFrom = getResponseFrom(host);
		JSONArray parsedResponse = new JSONArray(responseFrom);
		JSONObject versionAsJson = parsedResponse.getJSONObject(0);
		return new Version(versionAsJson);
	}

	protected JSONObject parseVersion(JSONArray json) {
		return json.getJSONObject(0);
	}

	protected String getResponseFrom(String host) throws IOException {
		OkHttpClient client = new OkHttpClient();
		return HttpUtil.getHttpSync(client, host).body().string();
	}

	public void showUpdateMessage(Component parent) {
		new OptionDialog(parent, "An update is available! Would you like to download it now?",
				"Yes", "No", new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (Desktop.isDesktopSupported() &&
								Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
							JButton btn = (JButton)e.getSource();
							OptionDialog dialog = (OptionDialog)btn.getFocusCycleRootAncestor();
							dialog.dispose();
							try {
								Desktop.getDesktop().browse(new URI(repo));
							}
							catch (IOException e1) {
								TextDialog error = new TextDialog(parent,
										"Failed to automatically redirect. Go to " +
										repo + " to download the update.");
								error.setVisible(true);
							}
							catch (URISyntaxException e1) {
								TextDialog error = new TextDialog(parent,
										"An internal error occurred. " +
										"The update cannot be completed.");
								error.setVisible(true);
							}
						}
						else {
							TextDialog error = new TextDialog(parent,
									"Failed to automatically redirect. Go to " +
									repo + " to download the update.");
							error.setVisible(true);
						}
					}
				},
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						JButton btn = (JButton)e.getSource();
						OptionDialog dialog = (OptionDialog)btn.getFocusCycleRootAncestor();
						dialog.dispose();
					}
				}).setVisible(true);
	}
}
