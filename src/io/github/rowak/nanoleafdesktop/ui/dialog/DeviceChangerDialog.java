package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import io.github.rowak.Aurora;
import io.github.rowak.Setup;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowDragListener;
import net.miginfocom.swing.MigLayout;

public class DeviceChangerDialog extends JDialog
{
	private Aurora device;
	private DefaultListModel<String> listModel;
	private Main parent;
	private JPanel contentPane;
	private JLabel lblTitle;
	
	public DeviceChangerDialog(Main parent)
	{
		this.parent = parent;
		listModel = new DefaultListModel<String>();
		initUI(parent);
		
		new Thread(() ->
		{
			findAuroras();
		}).start();
	}
	
	private void findAuroras()
	{
		if (!findAurorasMethod1() && !findAurorasMethod2())
		{
			// Only show this message if both connect methods fail
			new TextDialog(this, "Couldn't locate any devices. " +
					"Please try again or create an issue on GitHub.")
					.setVisible(true);
		}
	}
	
	// The first device search method uses the nanoleaf backend
	// api to find the devices on the local network
	private boolean findAurorasMethod1()
	{
		List<InetSocketAddress> auroras = Setup.quickFindAuroras();
		
		for (InetSocketAddress address : auroras)
		{
			listModel.addElement(address.getHostName() + ":" + address.getPort());
			
			if (lblTitle.getText().equals("Searching for Devices..."))
			{
				lblTitle.setText("Select a Device");
			}
		}
		return !auroras.isEmpty();
	}
	
	// The second device search method uses ssdp to find
	// devices on the local network
	private boolean findAurorasMethod2()
	{
		List<InetSocketAddress> auroras = new ArrayList<InetSocketAddress>();
		try
		{
			auroras = Setup.findAuroras(5000);
		}
		catch (Exception e)
		{
			// do nothing
		}
		
		for (InetSocketAddress address : auroras)
		{
			listModel.addElement(address.getHostName() + ":" + address.getPort());
		}
		return !auroras.isEmpty();
	}
	
	private Aurora connectToAurora(String host)
	{
		String text = "Press the power button on your " +
				  "Aurora for 5-7 seconds until the LED starts flashing.";
		TextDialog info = new TextDialog(this, text);
		info.setVisible(true);
		
		DeviceChangerDialog dialog = this;
	
		String[] hostArr = host.split(":");
		String hostName = hostArr[0];
		int port = Integer.parseInt(hostArr[1]);
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				try
				{
					String accessToken = Setup.createAccessToken(hostName, port, "v1");
					System.out.println(accessToken);
					Aurora device = new Aurora(hostName, port, "v1", accessToken);
					if (device != null)
					{
						parent.setDevice(device);
					}
					this.cancel();
					info.dispose();
					dialog.dispose();
				}
				catch (Exception e)
				{
					// This will be called every second until an api key
					// can be generated (403 forbidden)
				}
			}
		}, 1000, 1000);
		return device;
	}
	
	private Aurora connectToExternalAurora(String ip, int port, String accessToken)
	{
		try
		{
			return new Aurora(ip, port, "v1", accessToken);
		}
		catch (StatusCodeException | HttpRequestException schre)
		{
			return null;
		}
	}
	
	private void initUI(Component parent)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(400, 200);
		setLocationRelativeTo(parent);
		setUndecorated(true);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new LineBorder(new Color(128, 128, 128), 2));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[grow][244.00,grow]", "[][grow][]"));
		
		WindowDragListener wdl = new WindowDragListener(50);
		addMouseListener(wdl);
		addMouseMotionListener(wdl);
		
		lblTitle = new JLabel("Searching for Devices...");
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblTitle.setForeground(Color.WHITE);
		contentPane.add(lblTitle, "gapx 15 0, cell 0 0");
		
		CloseButton btnClose = new CloseButton(this, JFrame.DISPOSE_ON_CLOSE);
		contentPane.add(btnClose, "gapx 0 15, cell 1 0,alignx right");
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		contentPane.add(scrollPane, "cell 0 1 2 1,grow");
		
		JList<String> listAuroras = new JList<String>(listModel);
		listAuroras.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listAuroras.setFont(new Font("Tahoma", Font.PLAIN, 20));
		listAuroras.setBackground(Color.DARK_GRAY);
		listAuroras.setBorder(new LineBorder(Color.GRAY));
		listAuroras.setForeground(Color.WHITE);
		scrollPane.setViewportView(listAuroras);
		
		JButton btnConnect = new ModernButton("Connect");
		btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnConnect.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (listAuroras.getSelectedValue() != null)
				{
					connectToAurora(listAuroras.getSelectedValue());
				}
			}
		});
		contentPane.add(btnConnect, "cell 1 2,alignx right");
		
		JButton btnAddExternalDevice = new ModernButton("Add External Device");
		btnAddExternalDevice.setText("Setup New Device");
		btnAddExternalDevice.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnAddExternalDevice.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				new TripleEntryDialog(DeviceChangerDialog.this, "IP Address",
						"Port (Default is 16021)", "Access Token", "Add Device", new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent e)
							{
								JButton button = (JButton)e.getSource();
								TripleEntryDialog thisDialog =
										(TripleEntryDialog)button.getFocusCycleRootAncestor();
								String entry1Text = thisDialog.getEntry1().getText();
								String entry2Text = thisDialog.getEntry2().getText();
								String entry3Text = thisDialog.getEntry3().getText();
								if (!entry1Text.equals("IP Address") &&
										!entry2Text.equals("Port (Default is 16021)"))
								{
									String ip = entry1Text;
									int port = -1;
									String accessToken = entry3Text;
									try
									{
										port = Integer.parseInt(entry2Text);
										Aurora aurora = connectToExternalAurora(ip, port, accessToken);
										if (aurora != null)
										{
											DeviceChangerDialog.this.parent.setDevice(aurora);
											thisDialog.dispose();
											DeviceChangerDialog.this.dispose();
										}
										else
										{
											new TextDialog(DeviceChangerDialog.this,
													"Couldn't connect to the external device.")
													.setVisible(true);
										}
									}
									catch (NumberFormatException nfe)
									{
										new TextDialog(DeviceChangerDialog.this,
												"The port can only consist of numbers.")
												.setVisible(true);
									}
								}
								else
								{
									new TextDialog(DeviceChangerDialog.this,
											"You must fill out all entry fields.")
											.setVisible(true);
								}
							}
						}).setVisible(true);
			}
		});
		contentPane.add(btnAddExternalDevice, "cell 0 2,alignx left");
	}
}
