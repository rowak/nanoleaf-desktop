package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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

import org.json.JSONArray;

import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.models.DeviceGroup;
import io.github.rowak.nanoleafdesktop.tools.PropertyManager;
import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowDragListener;
import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;
import net.miginfocom.swing.MigLayout;

public class GroupDeleterDialog extends JDialog
{
	private List<DeviceGroup> groups;
	private DefaultListModel<String> groupsModel;
	
	public GroupDeleterDialog(Component parent)
	{
		initUI(parent);
		loadGroups();
	}
	
	private void loadGroups()
	{
		groups = getDeviceGroups();
		for (DeviceGroup group : groups)
		{
			groupsModel.addElement(group.getName());
		}
	}
	
	private void deleteGroup(String name)
	{
		for (DeviceGroup group : groups)
		{
			if (group.getName().equals(name))
			{
				groups.remove(group);
				groupsModel.removeElement(name);
				PropertyManager manager =
						new PropertyManager(Main.PROPERTIES_FILEPATH);
				manager.setProperty("deviceGroups",
						new JSONArray(groups).toString());
				break;
			}
		}
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
			return groups;
		}
		return new ArrayList<DeviceGroup>();
	}
	
	private void initUI(Component parent)
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(474, 225);
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
		
		JLabel lblTitle = new JLabel("Select a Group");
		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 22));
		lblTitle.setForeground(Color.WHITE);
		contentPane.add(lblTitle, "gapx 15 0, cell 0 0");
		
		CloseButton btnClose = new CloseButton(this, JFrame.DISPOSE_ON_CLOSE);
		contentPane.add(btnClose, "cell 2 0,alignx right,gapx 0 15");
		
		JScrollPane devicesScrollPane = new JScrollPane();
		devicesScrollPane.setBorder(null);
		devicesScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
		devicesScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
		contentPane.add(devicesScrollPane, "cell 0 1 3 1,grow");
		
		groupsModel = new DefaultListModel<String>();
		JList<String> listGroups = new JList<String>(groupsModel);
		listGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listGroups.setFont(new Font("Tahoma", Font.PLAIN, 20));
		listGroups.setBackground(Color.DARK_GRAY);
		listGroups.setBorder(new LineBorder(Color.GRAY));
		listGroups.setForeground(Color.WHITE);
		devicesScrollPane.setViewportView(listGroups);
		
		JButton btnCreateGroup = new ModernButton("Delete Group");
		btnCreateGroup.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				deleteGroup(listGroups.getSelectedValue());
			}
		});
		contentPane.add(btnCreateGroup, "cell 2 2");
	}
}
