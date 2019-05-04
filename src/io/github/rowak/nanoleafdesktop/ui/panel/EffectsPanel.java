package io.github.rowak.nanoleafdesktop.ui.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import io.github.rowak.Aurora;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.tools.UIConstants;
import io.github.rowak.nanoleafdesktop.ui.dialog.LoadingSpinner;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;
import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;

public class EffectsPanel extends JScrollPane
{
	private JList<String> effects;
	private DefaultListModel<String> model;
	
	public EffectsPanel(String label, Main parent,
			Aurora device, PanelCanvas canvas)
	{
		init(label, parent, device, canvas);
	}
	
	public void addEffect(String effect)
	{
		if (!model.contains(effect))
		{
			model.addElement(effect);
		}
	}
	
	public void removeEffect(String effect)
	{
		if (model.contains(effect))
		{
			model.removeElement(effect);
		}
	}
	
	public void clearEffects()
	{
		model.clear();
	}
	
	public DefaultListModel<String> getModel()
	{
		return model;
	}
	
	public JList<String> getList()
	{
		return effects;
	}
	
	private void init(String label, Main parent, Aurora device, PanelCanvas canvas)
	{
		getVerticalScrollBar().setUI(new ModernScrollBarUI());
		getHorizontalScrollBar().setUI(new ModernScrollBarUI());
		setBackground(UIConstants.darkBackground);
		setForeground(UIConstants.textPrimary);
		setBorder(new TitledBorder(new LineBorder(Color.GRAY),
				label, TitledBorder.LEFT, TitledBorder.TOP, null, Color.WHITE));
		((TitledBorder)getBorder())
			.setTitleFont(new Font("Tahoma", Font.BOLD, 22));
		
		model = new DefaultListModel<String>();
		effects = new JList<String>(model);
		effects.setBackground(UIConstants.darkBackground);
		effects.setForeground(UIConstants.textPrimary);
		effects.setFont(new Font("Tahoma", Font.PLAIN, 19));
		effects.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				JList<String> list = (JList<String>)e.getSource();
				try
				{
					device.effects().setEffect(list.getSelectedValue());
					canvas.checkAuroraState();
					parent.loadStateComponents();
				}
				catch (StatusCodeException sce)
				{
					new TextDialog(parent,
							"The requested action could not be completed. " +
							"Please try again.").setVisible(true);
				}
			}
		});
		
		LoadingSpinner regEffectsSpinner =
				new LoadingSpinner(UIConstants.darkBackground);
		setViewportView(regEffectsSpinner);
	}
}
