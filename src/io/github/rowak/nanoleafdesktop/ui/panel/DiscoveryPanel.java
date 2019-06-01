package io.github.rowak.nanoleafdesktop.ui.panel;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import io.github.rowak.Aurora;
import io.github.rowak.Effect;
import io.github.rowak.StatusCodeException;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.discovery.Discovery;
import io.github.rowak.nanoleafdesktop.discovery.EffectMetadata;
import io.github.rowak.nanoleafdesktop.tools.UIConstants;
import io.github.rowak.nanoleafdesktop.ui.dialog.LoadingSpinner;
import io.github.rowak.nanoleafdesktop.ui.dialog.OptionDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.list.DiscoveryCellRenderer;
import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;

public class DiscoveryPanel extends JScrollPane
{
	private boolean isUpdating;
	private int topPage = 0, recentPage = 0;
	private Aurora[] auroras;
	private DefaultListModel<EffectMetadata> discoveryEffects;
	private OptionDialog activeDialog;
	
	public DiscoveryPanel(Aurora[] auroras)
	{
		this.auroras = auroras;
		getVerticalScrollBar().setUI(new ModernScrollBarUI());
		setHorizontalScrollBar(null);
		setBackground(UIConstants.darkBackground);
		setBorder(null);
		setViewportView(new LoadingSpinner(UIConstants.darkBackground));
		
		discoveryEffects = new DefaultListModel<EffectMetadata>();
		JList<EffectMetadata> list = new JList<EffectMetadata>(discoveryEffects);
		list.setCellRenderer(new DiscoveryCellRenderer());
		list.setFont(new Font("Tahoma", Font.PLAIN, 22));
		list.setBackground(UIConstants.darkBackground);
		list.setForeground(UIConstants.textPrimary);
		setBorder(new LineBorder(UIConstants.darkForeground, 1, true));
		
		getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
		{
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				JScrollBar bar = (JScrollBar)e.getSource();
				if (bar.getValue() == bar.getMaximum() - bar.getVisibleAmount() && !isUpdating)
				{
					isUpdating = true;
					EventQueue.invokeLater(new Runnable()
					{
						public void run()
						{
							new Thread(() ->
							{
								// Update the discovery effects concurrently
								// by evading the EDT
								addTopEffects(++topPage, DiscoveryPanel.this.getTopLevelAncestor());
								DefaultListModel<EffectMetadata> dlm =
										new DefaultListModel<EffectMetadata>();
								for (int i = 0; i < discoveryEffects.size(); i++)
								{
									dlm.addElement(discoveryEffects.getElementAt(i));
								}
								list.setModel(dlm);
								
								if (dlm.size() > 0)
								{
									DiscoveryPanel.this.setViewportView(list);
								}
								
								isUpdating = false;
							}).start();
						}
					});
				}
			}
		});
		
		DiscoveryPanel panel = this;
		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (panel.activeDialog != null)
				{
					panel.activeDialog.dispose();
				}
				
				JList<EffectMetadata> list = (JList<EffectMetadata>)e.getSource();
				OptionDialog dialog = new OptionDialog(list.getTopLevelAncestor(),
						"Preview or download this effect?", "Preview", "Download",
						new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								new Thread(() ->
								{
									try
									{
										JButton source = (JButton)e.getSource();
										((OptionDialog)source.getFocusCycleRootAncestor()).dispose();
										Effect ef = ((EffectMetadata)list.getSelectedValue()).getEffect();
										for (Aurora aurora : panel.auroras)
										{
											aurora.effects().displayEffect(ef);
										}
										
										updateMain(list);
									}
									catch (HttpRequestException hre)
									{
										new TextDialog(panel, "Lost connection to the device. " +
												"Please try again.").setVisible(true);
									}
									catch (StatusCodeException sce)
									{
										new TextDialog(panel,
												"The requested action could not be completed. " +
												"Please try again.").setVisible(true);
									}
								}).start();
							}
						},
						new ActionListener()
						{
							public void actionPerformed(ActionEvent e)
							{
								new Thread(() ->
								{
									try
									{
										JButton source = (JButton)e.getSource();
										((OptionDialog)source.getFocusCycleRootAncestor()).dispose();
										Effect ef = ((EffectMetadata)list.getSelectedValue()).getEffect();
										for (Aurora aurora : panel.auroras)
										{
											aurora.effects().addEffect(ef);
										}
										
										updateMain(list);
									}
									catch (StatusCodeException sce)
									{
										new TextDialog(panel, "Lost connection to the device. " +
												"Please try again.").setVisible(true);
									}
								}).start();
							}
						});
				dialog.setVisible(true);
				activeDialog = dialog;
			}
		});
	}
	
	private void updateMain(JList<?> list)
	{
		Main frame = (Main)list.getTopLevelAncestor();
		try
		{
			frame.loadActiveScene();
			frame.getCanvas().checkAuroraState();
		}
		catch (StatusCodeException sce)
		{
			new TextDialog(frame, "Lost connection to the device. " +
					"Please try again.").setVisible(true);
		}
	}
	
	public Aurora[] getAuroras()
	{
		return this.auroras;
	}
	
	public void setAuroras(Aurora[] auroras)
	{
		this.auroras = auroras;
	}
	
	public int getTopPage()
	{
		return this.topPage;
	}
	
	public void setTopPage(int page)
	{
		this.topPage = page;
	}
	
	public int getRecentPage()
	{
		return this.recentPage;
	}
	
	public void setRecentPage(int page)
	{
		this.recentPage = page;
	}
	
	public void addTopEffects(int page, Component component)
	{
		try
		{
			addEffects(Discovery.getTopEffects(page));
		}
		catch (HttpRequestException hre)
		{
			hre.printStackTrace();
			new TextDialog(component,
					"Failed to get discovery data from the Nanoleaf server.")
					.setVisible(true);
		}
	}
	
	public void addRecentEffects(int page, Component component)
	{
		try
		{
			addEffects(Discovery.getRecentEffects(page));
		}
		catch (HttpRequestException hre)
		{
			hre.printStackTrace();
			new TextDialog(component,
					"Failed to get discovery data from the Nanoleaf server.")
					.setVisible(true);
		}
	}
	
	private void addEffects(EffectMetadata[] effects)
	{
		for (EffectMetadata effect : effects)
		{
			if (!contains(discoveryEffects.toArray(), effect))
			{
				discoveryEffects.addElement(effect);
			}
		}
	}
	
	private boolean contains(Object[] list, EffectMetadata effect)
	{
		for (Object effectX : list)
		{
			if (((EffectMetadata)effectX).getKey().equals(effect.getKey()))
			{
				return true;
			}
		}
		return false;
	}
}
