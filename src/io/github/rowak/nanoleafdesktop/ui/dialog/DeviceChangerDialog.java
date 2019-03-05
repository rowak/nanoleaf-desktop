package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import org.json.JSONObject;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import io.github.rowak.Aurora;
import io.github.rowak.AuroraMetadata;
import io.github.rowak.Setup;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowDragListener;
import net.miginfocom.swing.MigLayout;

public class DeviceChangerDialog extends JDialog
{
	private Aurora device;
	private List<AuroraMetadata> devices;
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
	
	private AuroraMetadata getMetadataFromListItem(String item)
	{
		AuroraMetadata data = null;
		String name = item.substring(0, item.indexOf("(")-1);
		String ip = item.substring(item.indexOf("(")+1, item.indexOf(")"));
		for (AuroraMetadata metadata : devices)
		{
			if (metadata.getDeviceName().equals(name) ||
					metadata.getHostName().equals(ip))
			{
				data = metadata;
				break;
			}
		}
		return data;
	}
	
	private void findAuroras()
	{
		if (!findMethod1())
		{
			findMethod2();
		}
		
		for (AuroraMetadata metadata : devices)
		{
			addDeviceToList(metadata);
		}
		
		if (devices.isEmpty())
		{
			new TextDialog(this, "Couldn't locate any devices. " +
					"Please try again or create an issue on GitHub.")
					.setVisible(true);
		}
		lblTitle.setText("Select a Device");
	}
	
	private boolean findMethod1()
	{
		devices = new ArrayList<AuroraMetadata>();
		try
		{
			List<InetSocketAddress> devicesOld = Setup.quickFindAuroras();
			for (InetSocketAddress addr : devicesOld)
			{
				AuroraMetadata metadata = new AuroraMetadata(addr.getHostName(),
						addr.getPort(), "", addr.getHostName());
				devices.add(metadata);
			}
		}
		catch (Exception e)
		{
			// do nothing
		}
		return !devices.isEmpty();
	}
	
	private boolean findMethod2()
	{
		devices = new ArrayList<AuroraMetadata>();
		try
		{
			devices = Setup.findAuroras(5000);
		}
		catch (Exception e)
		{
			// do nothing
		}
		return !devices.isEmpty();
	}
	
	private Aurora connectToAurora(String item)
	{
		String text = "Press the power button on your " +
				  "Aurora for 5-7 seconds until the LED starts flashing.";
		TextDialog info = new TextDialog(this, text);
		info.setVisible(true);
		
		DeviceChangerDialog dialog = this;
		
		AuroraMetadata metadata = getMetadataFromListItem(item);
		String hostName = metadata.getHostName();
		int port = metadata.getPort();
		
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
	
	private void addDeviceToList(AuroraMetadata metadata)
	{
		Map<String, Object> savedDevices = getDevices();
		if (savedDevices.containsKey(metadata.getHostName()))
		{
			String ip = metadata.getHostName();
			String name = String.format("%s (%s)",
					savedDevices.get(ip), ip);
			listModel.addElement(name);
		}
		else
		{
			String name = String.format("%s (%s)",
					metadata.getDeviceName(), metadata.getHostName());
			listModel.addElement(name);
		}
	}
	
	private Map<String, Object> getDevices()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		String devicesStr = manager.getProperty("devices");
		if (devicesStr != null)
		{
			JSONObject json = new JSONObject(devicesStr);
			return json.toMap();
		}
		return new HashMap<String, Object>();
	}
	
	private void initUI(Component parent)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(474, 225);
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
				new DoubleEntryDialog(DeviceChangerDialog.this, "IP Address",
						"Port (Default is 16021)", "Add Device", new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent e)
							{
								JButton button = (JButton)e.getSource();
								DoubleEntryDialog thisDialog =
										(DoubleEntryDialog)button.getFocusCycleRootAncestor();
								String entry1Text = thisDialog.getEntry1().getText();
								String entry2Text = thisDialog.getEntry2().getText();
								if (!entry1Text.equals("IP Address") &&
										!entry2Text.equals("Port (Default is 16021)"))
								{
									try
									{										
										// TODO: Clean up this code
										String ip = entry1Text;
										int port = Integer.parseInt(entry2Text);
										String text = "Press the power button on your " +
												  "Aurora for 5-7 seconds until the LED starts flashing.";
										TextDialog info = new TextDialog(DeviceChangerDialog.this, text);
										info.setVisible(true);
										Timer timer = new Timer();
										timer.scheduleAtFixedRate(new TimerTask()
										{
											public void run()
											{
												try
												{
													String accessToken = Setup.createAccessToken(ip, port, "v1");
													System.out.println(accessToken);
													this.cancel();
													Aurora aurora = new Aurora(ip, port, "v1", accessToken);
													if (aurora != null)
													{
														info.dispose();
														DeviceChangerDialog.this.parent.setDevice(aurora);
														thisDialog.dispose();
														DeviceChangerDialog.this.dispose();
													}
												}
												catch (Exception e)
												{
													// This will be called every second until an api key
													// can be generated (403 forbidden)
												}
											}
										}, 1000, 1000);
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
