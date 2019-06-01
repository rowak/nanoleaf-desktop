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

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.rowak.Aurora;
import io.github.rowak.AuroraMetadata;
import io.github.rowak.Setup;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.models.DeviceGroup;
import io.github.rowak.nanoleafdesktop.models.DeviceInfo;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.label.SmallModernLabel;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowDragListener;
import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;
import net.miginfocom.swing.MigLayout;
import javax.swing.JButton;

public class GroupCreatorDialog extends JDialog
{
	private static List<AuroraMetadata> devices;
	private DefaultListModel<String> devicesModel;
	private DefaultListModel<String> groupDevicesModel;
	private JLabel lblTitle;
	
	public GroupCreatorDialog(Component parent)
	{
		initUI(parent);
		new Thread(() ->
		{
			getDevices();
		}).start();
//		devices = new ArrayList<AuroraMetadata>();
//		devices.add(new AuroraMetadata("1.2.3.4", 16021, "123", "Aurora 1"));
//		devices.add(new AuroraMetadata("5.6.7.8", 16021, "456", "Aurora 2"));
//		devices.add(new AuroraMetadata("9.10.11.12", 16021, "789", "Aurora 3"));
//		fillDevicesModel();
	}
	
	private void getDevices()
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
		lblTitle.setText("Create a Group");
	}
	
	private boolean findMethod1()
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
	
	private boolean findMethod2()
	{
		devices = new ArrayList<AuroraMetadata>();
		try
		{
			List<InetSocketAddress> devicesOld = Setup.quickFindAuroras();
			for (InetSocketAddress addr : devicesOld)
			{
				AuroraMetadata metadata = new AuroraMetadata(addr.getHostName(),
						addr.getPort(), "", "");
				devices.add(metadata);
			}
		}
		catch (Exception e)
		{
			// do nothing
		}
		return !devices.isEmpty();
	}
	
//	private void fillDevicesModel()
//	{
//		for (AuroraMetadata metadata : devices)
//		{
//			addDeviceToList(metadata);
//			System.out.println(metadata.getHostName());
//		}
//	}
	
	private void addDeviceToList(AuroraMetadata metadata)
	{
		Map<String, Object> savedDevices = getLocalDeviceData();
		if (savedDevices.containsKey(metadata.getHostName()))
		{
			String ip = metadata.getHostName();
			String name = String.format("%s (%s)",
					savedDevices.get(ip), ip);
			devicesModel.addElement(name);
		}
		else
		{
			String deviceName = metadata.getDeviceName();
			if (deviceName.isEmpty())
			{
				deviceName = "Unknown device";
			}
			String name = String.format("%s (%s)",
					deviceName, metadata.getHostName());
			devicesModel.addElement(name);
		}
	}
	
	private Map<String, Object> getLocalDeviceData()
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
	
	private void getGroupName()
	{
		new SingleEntryDialog(GroupCreatorDialog.this,
				"Group Name", "Ok", new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						JButton button = (JButton)e.getSource();
						SingleEntryDialog thisDialog =
								(SingleEntryDialog)button.getFocusCycleRootAncestor();
						String name = thisDialog.getEntryField().getText();
						if (name.equals("Group Name"))
						{
							new TextDialog(thisDialog, "The group name can't be empty.")
								.setVisible(true);
						}
						else
						{
							thisDialog.dispose();
							createGroup(name);
						}
					}
				})
			.setVisible(true);
	}
	
	private void createGroup(String name)
	{
		new Thread(() ->
		{
			Aurora[] connectedDevices = new Aurora[groupDevicesModel.size()];
			for (int i = 0; i < groupDevicesModel.size(); i++)
			{
				connectedDevices[i] = connectToDevice(
						groupDevicesModel.getElementAt(i));
			}
			
			PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
			List<DeviceGroup> deviceGroups = getDeviceGroups();
			DeviceInfo[] info = new DeviceInfo[groupDevicesModel.size()];
			for (int i = 0; i < info.length; i++)
			{
				info[i] = new DeviceInfo(connectedDevices[i].getHostName(),
						connectedDevices[i].getPort(), connectedDevices[i].getAccessToken());
			}
			deviceGroups.add(new DeviceGroup(name, info));
			manager.setProperty("deviceGroups",
					new JSONArray(deviceGroups).toString());
			
			new TextDialog(GroupCreatorDialog.this, name + " was created.").setVisible(true);
		}).start();
	}
	
	private List<DeviceGroup> getDeviceGroups()
	{
		PropertyManager manager = new PropertyManager(Main.PROPERTIES_FILEPATH);
		String devicesStr = manager.getProperty("deviceGroups");
		if (devicesStr != null)
		{
			List<DeviceGroup> groups = new ArrayList<DeviceGroup>();
			JSONArray json = new JSONArray(devicesStr);
			for (int i = 0; i < json.length(); i++)
			{
				groups.add(DeviceGroup.fromJSON(json.getJSONObject(i).toString()));
			}
		}
		return new ArrayList<DeviceGroup>();
	}
	
	private Aurora connectToDevice(String item)
	{
		AuroraMetadata metadata = getMetadataFromListItem(item);
		String hostName = metadata.getHostName();
		int port = metadata.getPort();
		String accessToken = "";
		
		String text = "On ";
		Map<String, Object> localDevices = getLocalDeviceData();
		if (localDevices.containsKey(hostName))
		{
			text += localDevices.get(hostName);
		}
		else
		{
			text += hostName;
		}
		
		text += " Press the power button on your " +
				  "device for 5-7 seconds until the LED starts flashing.";
		TextDialog info = new TextDialog(GroupCreatorDialog.this, text);
		info.setVisible(true);
				
		while (accessToken == "")
		{
			try
			{
				Thread.sleep(1000);
				accessToken = Setup.createAccessToken(hostName, port, "v1");
				System.out.println(accessToken);
				info.dispose();
			}
			catch (Exception e)
			{
				// This will be called every second until an api key
				// can be generated (403 forbidden)
			}
		}
		try
		{
			return new Aurora(hostName, port, "v1", accessToken);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private AuroraMetadata getMetadataFromListItem(String item)
	{
		AuroraMetadata data = null;
		String ip = item.substring(item.indexOf("(")+1, item.indexOf(")"));
		for (AuroraMetadata metadata : devices)
		{
			if (metadata.getHostName().equals(ip))
			{
				data = metadata;
				break;
			}
		}
		return data;
	}

	private void initUI(Component parent)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(500, 225);
		setLocationRelativeTo(parent);
		setUndecorated(true);
		JPanel contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new LineBorder(new Color(128, 128, 128), 2));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[255.00,grow][106.00,grow][grow]", "[][grow][]"));
		
		WindowDragListener wdl = new WindowDragListener(50);
		addMouseListener(wdl);
		addMouseMotionListener(wdl);
		
		lblTitle = new JLabel("Searching for Devices...");
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblTitle.setForeground(Color.WHITE);
		contentPane.add(lblTitle, "gapx 15 0, cell 0 0");
		
		CloseButton btnClose = new CloseButton(this, JFrame.DISPOSE_ON_CLOSE);
		contentPane.add(btnClose, "cell 2 0,alignx right,gapx 0 15");
		
		JScrollPane devicesScrollPane = new JScrollPane();
		devicesScrollPane.setBorder(null);
		devicesScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
		devicesScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
		contentPane.add(devicesScrollPane, "cell 0 1,grow");
		
		devicesModel = new DefaultListModel<String>();
		JList<String> listAuroras = new JList<String>(devicesModel);
		listAuroras.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listAuroras.setFont(new Font("Tahoma", Font.PLAIN, 20));
		listAuroras.setBackground(Color.DARK_GRAY);
		listAuroras.setBorder(new LineBorder(Color.GRAY));
		listAuroras.setForeground(Color.WHITE);
		devicesScrollPane.setViewportView(listAuroras);
		
		JLabel lblAvailableDevices = new SmallModernLabel("Available Devices");
		devicesScrollPane.setColumnHeaderView(lblAvailableDevices);
		devicesScrollPane.getColumnHeader().setBackground(Color.DARK_GRAY);
		
		JScrollPane groupScrollPane = new JScrollPane();
		groupScrollPane.setBorder(null);
		groupScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
		groupScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
		contentPane.add(groupScrollPane, "cell 1 1 2 1,grow");
		
		groupDevicesModel = new DefaultListModel<String>();
		JList<String> groupDevices = new JList<String>(groupDevicesModel);
		groupDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupDevices.setFont(new Font("Tahoma", Font.PLAIN, 20));
		groupDevices.setBackground(Color.DARK_GRAY);
		groupDevices.setBorder(new LineBorder(Color.GRAY));
		groupDevices.setForeground(Color.WHITE);
		groupScrollPane.setViewportView(groupDevices);
		
		JLabel lblGroupDevices = new SmallModernLabel("Group Devices");
		groupScrollPane.setColumnHeaderView(lblGroupDevices);
		
		JButton btnAddSelected = new ModernButton("Add Selected");
		btnAddSelected.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (!groupDevicesModel.contains(listAuroras.getSelectedValue()) &&
						devicesModel.size() > 0)
				{
					groupDevicesModel.addElement(listAuroras.getSelectedValue());
					devicesModel.removeElement(listAuroras.getSelectedValue());
				}
			}
		});
		contentPane.add(btnAddSelected, "flowx,cell 0 2");
		
		JButton btnCreateGroup = new ModernButton("Create Group");
		btnCreateGroup.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (groupDevicesModel.size() > 1)
				{
					getGroupName();
				}
				else
				{
					new TextDialog(GroupCreatorDialog.this,
							"You must select at least two devices.")
							.setVisible(true);
				}
			}
		});
		contentPane.add(btnCreateGroup, "cell 2 2");
		groupScrollPane.getColumnHeader().setBackground(Color.DARK_GRAY);
	}
}
