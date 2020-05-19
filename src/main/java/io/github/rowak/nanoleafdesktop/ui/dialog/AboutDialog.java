package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Component;
import java.awt.Desktop;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.Version;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.awt.Color;

public class AboutDialog extends BasicDialog
{
	public AboutDialog(Component parent, Version version)
	{
		super();
		
		JLabel lblTitle = new JLabel("Nanoleaf for Desktop by Ethan Rowan (rowak)");
		lblTitle.setForeground(Color.WHITE);
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 25));
		getContentPane().add(lblTitle, "flowy,cell 0 1,alignx center,gapx 5 0");
		
		JLabel lblGithubUrl = new JLabel("github.com/rowak/nanoleaf-desktop\r\n");
		lblGithubUrl.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblGithubUrl.setForeground(Color.WHITE);
		getContentPane().add(lblGithubUrl, "cell 0 1,alignx center");
		
		String versionText = "Version " + version.getName();
		versionText += !version.getPreRelease() ? " (Pre-release)" : "";
		JLabel lblVersion = new JLabel(versionText);
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblVersion.setForeground(Color.WHITE);
		getContentPane().add(lblVersion, "cell 0 1,alignx center");
		
		URL iconPath = Main.class.getResource("resources/images/icon_small.png");
		JButton btnDonate = new ModernButton("Donate");
		btnDonate.setIcon(new ImageIcon(iconPath));
		btnDonate.setFocusPainted(false);
		btnDonate.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openDonationWebpage();
			}
		});
		getContentPane().add(btnDonate, "cell 0 1,alignx center");
		
		JLabel spacer = new JLabel(" ");
		contentPanel.add(spacer, "cell 0 3");
		
		finalize(parent);
	}
	
	private void openDonationWebpage()
	{
		if (Desktop.isDesktopSupported())
		{
			Desktop desktop = Desktop.getDesktop();
		    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE))
		    {
		        try
		        {
		        	final String paypalDonationLink = "https://paypal.me/rowak";
		            desktop.browse(new URI(paypalDonationLink));
		        }
		        catch (IOException | URISyntaxException iourie)
		        {
		        	String errorMessage = "Failed to open the web browser. " +
							"Please navigate to https://paypal.me/rowak manually.";
		            new TextDialog(AboutDialog.this, errorMessage).setVisible(true);
		        }
		    }
		}
		else
		{
			String errorMessage = "Your computer doesn't seem to support this feature. " +
					"Please navigate to https://paypal.me/rowak manually.";
            new TextDialog(AboutDialog.this, errorMessage).setVisible(true);
		}
	}
}
