package io.github.rowak.tools;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;

import org.json.JSONArray;

import com.github.kevinsawicki.http.HttpRequest;

import io.github.rowak.ui.dialog.OptionDialog;
import io.github.rowak.ui.dialog.TextDialog;

public class UpdateManager
{
	private String host, repo;
	
	public UpdateManager(String host, String repo)
	{
		this.host = host;
		this.repo = repo + "/releases";
	}
	
	public boolean updateAvailable(Version current)
	{
		JSONArray json = new JSONArray(HttpRequest.get(host).body());
		Version latest = new Version(json.getJSONObject(0));
		return latest.greater(current);
	}
	
	public void showUpdateMessage(Component parent)
	{
		new OptionDialog(parent,
				"An update is available! Would you like to download it now?",
				"Yes", "No",
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						if (Desktop.isDesktopSupported() &&
								Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
						{
							JButton btn = (JButton)e.getSource();
							OptionDialog dialog = (OptionDialog)btn.getFocusCycleRootAncestor();
							dialog.dispose();
							try
							{
								Desktop.getDesktop().browse(new URI(repo));
							}
							catch (IOException e1)
							{
								TextDialog error = new TextDialog(parent,
										"Failed to automatically redirect. Go to " +
										repo + " to download the update.");
								error.setVisible(true);
							}
							catch (URISyntaxException e1)
							{
								TextDialog error = new TextDialog(parent,
										"An internal error occurred. " +
										"The update cannot be completed.");
								error.setVisible(true);
							}
						}
						else
						{
							TextDialog error = new TextDialog(parent,
									"Failed to automatically redirect. Go to " +
									repo + " to download the update.");
							error.setVisible(true);
						}
					}
				},
				new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						JButton btn = (JButton)e.getSource();
						OptionDialog dialog = (OptionDialog)btn.getFocusCycleRootAncestor();
						dialog.dispose();
					}
				}).setVisible(true);
	}
}
