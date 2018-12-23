package io.github.rowak.ui.panel;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import io.github.rowak.Aurora;
import io.github.rowak.Effect;
import io.github.rowak.Main;
import io.github.rowak.StatusCodeException;
import io.github.rowak.discovery.Discovery;
import io.github.rowak.discovery.EffectMetadata;
import io.github.rowak.ui.dialog.LoadingSpinner;
import io.github.rowak.ui.dialog.OptionDialog;
import io.github.rowak.ui.dialog.TextDialog;
import io.github.rowak.ui.list.DiscoveryCellRenderer;
import io.github.rowak.ui.scrollbar.ModernScrollBarUI;

public class DiscoveryPanel extends JScrollPane
{
	private boolean isUpdating;
	private int topPage = 0, recentPage = 0;
	private Aurora aurora;
	private DefaultListModel<EffectMetadata> discoveryEffects;
	private OptionDialog activeDialog;
	
	public DiscoveryPanel(Aurora aurora)
	{
		this.aurora = aurora;
		getVerticalScrollBar().setUI(new ModernScrollBarUI());
		setHorizontalScrollBar(null);
		setBackground(Color.DARK_GRAY);
		setBorder(null);
		setViewportView(new LoadingSpinner(Color.DARK_GRAY));
		
		discoveryEffects = new DefaultListModel<EffectMetadata>();
		JList<EffectMetadata> list = new JList<EffectMetadata>(discoveryEffects);
		list.setCellRenderer(new DiscoveryCellRenderer());
		list.setFont(new Font("Tahoma", Font.PLAIN, 22));
		list.setBackground(Color.DARK_GRAY);
		list.setForeground(Color.WHITE);
		setBorder(new LineBorder(Color.GRAY, 1, true));
		//setViewportView(list);
		
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
								addTopEffects(++topPage);
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
		list.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if (!e.getValueIsAdjusting())
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
											panel.aurora.effects().previewEffect(ef);
											
											updateMain(list);
										}
										catch (StatusCodeException sce)
										{
											new TextDialog(panel, "Lost connection to the Aurora. " +
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
											panel.aurora.effects().addEffect(ef);
											
											updateMain(list);
										}
										catch (StatusCodeException sce)
										{
											new TextDialog(panel, "Lost connection to the Aurora. " +
													"Please try again.").setVisible(true);
										}
									}).start();
								}
							});
					dialog.setVisible(true);
					activeDialog = dialog;
				}
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
			new TextDialog(frame, "Lost connection to the Aurora. " +
					"Please try again.").setVisible(true);
		}
	}
	
	public Aurora getAurora()
	{
		return this.aurora;
	}
	
	public void setAurora(Aurora aurora)
	{
		this.aurora = aurora;
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
	
	public void addTopEffects(int page)
	{
		addEffects(Discovery.getTopEffects(page));
	}
	
	public void addRecentEffects(int page)
	{
		addEffects(Discovery.getRecentEffects(page));
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
