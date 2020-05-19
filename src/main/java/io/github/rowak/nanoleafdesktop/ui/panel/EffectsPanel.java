package io.github.rowak.nanoleafdesktop.ui.panel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.models.BasicEffect;
import io.github.rowak.nanoleafdesktop.tools.BasicEffects;
import io.github.rowak.nanoleafdesktop.tools.UIConstants;
import io.github.rowak.nanoleafdesktop.ui.dialog.LoadingSpinner;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.menu.EffectOptionsMenu;
import io.github.rowak.nanoleafdesktop.ui.panel.panelcanvas.PanelCanvas;
import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;

public class EffectsPanel extends JScrollPane
{
	private String label;
	private Aurora[] devices;
	private Main parent;
	private PanelCanvas canvas;
	private JList<String> effects;
	private DefaultListModel<String> model;
	
	public EffectsPanel(String label, Main parent,
			Aurora[] devices, PanelCanvas canvas)
	{
		this.label = label;
		this.parent = parent;
		this.devices = devices;
		this.canvas = canvas;
		init();
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
	
	public void setAuroras(Aurora[] auroras)
	{
		this.devices = auroras;
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
	
	private void init()
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
				parent.unselectAllExcept(EffectsPanel.this);
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					if (label.equals("Basic Effects"))
					{
						setBasicEffectForDevices(list.getSelectedValue());
					}
					else
					{
						setEffectForDevices(list.getSelectedValue());
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON3)
				{
					createEffectOptionsMenu();
				}
			}
		});
		
		LoadingSpinner regEffectsSpinner =
				new LoadingSpinner(UIConstants.darkBackground);
		setViewportView(regEffectsSpinner);
	}
	
	private void setEffectForDevices(String effectName)
	{
		try
		{
			for (Aurora device : devices)
			{
				device.effects().setEffect(effectName);
			}
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
	
	private void setBasicEffectForDevices(String effectName)
	{
		try
		{
			List<BasicEffect> basicEffects = BasicEffects.getBasicEffects();
			for (BasicEffect ef : basicEffects)
			{
				if (ef.getName().equals(effectName))
				{
					for (int i = 0; i < devices.length; i++)
					{
						devices[i].state().setHue(ef.getHue());
						devices[i].state().setSaturation(ef.getSaturation());
					}
					canvas.checkAuroraState();
					parent.loadStateComponents();
					break;
				}
			}
		}
		catch (StatusCodeException sce)
		{
			sce.printStackTrace();
			new TextDialog(parent,
					"The requested action could not be completed. " +
					"Please try again.").setVisible(true);
		}
	}
	
	private void createEffectOptionsMenu()
	{
		new EffectOptionsMenu(EffectsPanel.this,
				label, devices, parent);
	}
}
