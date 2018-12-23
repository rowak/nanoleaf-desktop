package io.github.rowak.tools;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;

import io.github.rowak.ui.dialog.OptionDialog;
import io.github.rowak.ui.dialog.TextDialog;

public class UpdateManager
{
	private final String HOST = "";
	
	public boolean updateAvailable(String currentVersion)
	{
//		String[] currentVersionData = currentVersion.split("\\.");
//		String[] latestVersion = new String[]{"1", "0", "0"};        // GET FROM DOWNLOAD SERVER
//		for (int i = 0; i < latestVersion.length; i++)
//		{
//			int current = Integer.parseInt(currentVersionData[i]);
//			int latest = Integer.parseInt(latestVersion[i]);
//			if (latest > current)
//			{
//				return true;
//			}
//		}
		return false;
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
								Desktop.getDesktop().browse(new URI(HOST));
							}
							catch (IOException e1)
							{
								TextDialog error = new TextDialog(parent,
										"Failed to automatically redirect. Go to " +
										HOST + " to download the update.");
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
									HOST + " to download the update.");
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
