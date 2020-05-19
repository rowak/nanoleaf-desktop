package io.github.rowak.nanoleafdesktop.ui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import io.github.rowak.nanoleafdesktop.ui.button.CloseButton;
import io.github.rowak.nanoleafdesktop.ui.listener.WindowDragListener;
import net.miginfocom.swing.MigLayout;

public class BasicDialog extends JDialog
{
	protected static final Font FONT = new Font("Tahoma", Font.PLAIN, 20);
	
	protected JPanel contentPanel = new JPanel();
	
	public BasicDialog()
	{
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setUndecorated(true);
		contentPanel.setLayout(new MigLayout("", "[432.00,grow]", "[][]"));
		contentPanel.setBackground(Color.DARK_GRAY);
		contentPanel.setBorder(new LineBorder(new Color(128, 128, 128), 2));
		setContentPane(contentPanel);
		
		WindowDragListener wdl = new WindowDragListener(50);
		addMouseListener(wdl);
		addMouseMotionListener(wdl);
		
		CloseButton btnClose = new CloseButton(this, JFrame.DISPOSE_ON_CLOSE);
		contentPanel.add(btnClose, "cell 0 0,alignx right,gapx 0 15");
	}
	
	protected void finalize(Component parent)
	{
		pack();
		
		setSize(getWidth() + 15, getHeight());
		setLocationRelativeTo(parent);
	}
}
