package io.github.rowak.ui.dialog;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;

import javax.swing.border.LineBorder;

import io.github.rowak.Aurora;
import io.github.rowak.Main;
import io.github.rowak.Setup;
import io.github.rowak.ui.button.CloseButton;
import io.github.rowak.ui.listener.WindowDragListener;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;
import net.miginfocom.swing.MigLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ListSelectionModel;
import javax.swing.JButton;

public class AuroraFinder extends JDialog
{
	private String hostName, accessToken;
	private int port;
	private Aurora aurora;
	private DefaultListModel<String> listModel;
	private JPanel contentPane;

	public AuroraFinder(Component parent)
	{
		listModel = new DefaultListModel<String>();
		initUI(parent);
		
		new Thread(() ->
		{
			findAuroras();
		}).start();
	}
	
	public String getHostName()
	{
		return this.hostName;
	}
	
	public int getPort()
	{
		return this.port;
	}
	
	public String getAccessToken()
	{
		return this.accessToken;
	}
	
	private void findAuroras()
	{
		List<InetSocketAddress> auroras = new ArrayList<InetSocketAddress>();
		try
		{
			auroras = Setup.findAuroras(5000);
		}
		catch (SocketTimeoutException sto)
		{
			new TextDialog(this, "Timed out while searching for Auroras." +
					"Please try again.").setVisible(true);
		}
		catch (IOException ioe)
		{
			new TextDialog(this, "An unknown error has occurred." +
					"Please try again.").setVisible(true);
		}
		
		for (InetSocketAddress address : auroras)
		{
			listModel.addElement(address.getHostName() + ":" + address.getPort());
		}
	}
	
	private Aurora connectToAurora(String host)
	{
		String text = "Press the power button on your " +
				  "Aurora for 5-7 seconds until the LED starts flashing.";
		TextDialog info = new TextDialog(this, text);
		info.setVisible(true);
		
		AuroraFinder finder = this;
	
		String[] hostArr = host.split(":");
		hostName = hostArr[0];
		port = Integer.parseInt(hostArr[1]);
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				try
				{
					accessToken = Setup.createAccessToken(hostName, port, "v1");
					System.out.println(accessToken);
					this.cancel();
					info.dispose();
					finder.dispose();
				}
				catch (Exception e)
				{
					// This will be called every second until an api key
					// can be generated (403 forbidden)
				}
			}
		}, 1000, 1000);
		return aurora;
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
		
		JLabel lblTitle = new JLabel("Searching for Auroras...");
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
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.setContentAreaFilled(false);
		btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnConnect.setForeground(Color.WHITE);
		btnConnect.setBackground(Color.DARK_GRAY);
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
	}
}
