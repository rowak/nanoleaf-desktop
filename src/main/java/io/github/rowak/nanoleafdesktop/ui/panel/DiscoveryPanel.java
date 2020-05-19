package io.github.rowak.nanoleafdesktop.ui.panel;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Effect;
import io.github.rowak.nanoleafapi.StatusCodeException;
import io.github.rowak.nanoleafdesktop.Main;
import io.github.rowak.nanoleafdesktop.discovery.Discovery;
import io.github.rowak.nanoleafdesktop.discovery.EffectMetadata;
import io.github.rowak.nanoleafdesktop.tools.UIConstants;
import io.github.rowak.nanoleafdesktop.ui.combobox.ModernComboBox;
import io.github.rowak.nanoleafdesktop.ui.combobox.ModernComboCheckBox;
import io.github.rowak.nanoleafdesktop.ui.dialog.LoadingSpinner;
import io.github.rowak.nanoleafdesktop.ui.dialog.OptionDialog;
import io.github.rowak.nanoleafdesktop.ui.dialog.TextDialog;
import io.github.rowak.nanoleafdesktop.ui.list.DiscoveryCellRenderer;
import io.github.rowak.nanoleafdesktop.ui.scrollbar.ModernScrollBarUI;
import net.miginfocom.swing.MigLayout;

public class DiscoveryPanel extends JPanel
{
	private final String[] SORT_TYPES = {"Sort Type...", "Top", "Recent", "Plugin"};
	private final String[] TAGS = {"Tags...", "Party", "Chill", "Wellness", "Holiday",
								   "Nature", "Zen", "Happy", "Romantic", "Sexy",
								   "Bright", "Dim", "Ambient", "Warm", "Cool",
								   "Mellow", "Vibrant", "White", "Pop", "Jazz",
								   "Classical", "Rock", "Rap", "Hiphop",
								   "Electronic", "Kpop", "Reggae", "Metal",
								   "Acoustic", "Vocal", "Mb"};
	
	private boolean isUpdating;
	private int topPage = 0, recentPage = 0;
	private Aurora[] auroras;
	private DefaultListModel<EffectMetadata> discoveryEffects;
	private JComboBox<String> cmbxSortType;
	private ModernComboCheckBox<String> cmbxTags;
	private JList<EffectMetadata> list;
	private JScrollPane scrollPane;
	private OptionDialog activeDialog;
	
	public DiscoveryPanel(Aurora[] auroras)
	{
		this.auroras = auroras;
		setBackground(UIConstants.darkBackground);
		setBorder(null);
		
		scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
		setLayout(new MigLayout("", "[-1.00px,grow][grow]", "[263.00px,grow][grow]"));
		scrollPane.setHorizontalScrollBar(null);
		scrollPane.setViewportView(new LoadingSpinner(UIConstants.darkBackground));
		scrollPane.setBorder(null);
		add(scrollPane, "cell 0 0 2 1,grow");
		
		cmbxSortType = new ModernComboBox<String>(
				new DefaultComboBoxModel<String>(SORT_TYPES));
		cmbxSortType.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if (cmbxSortType.getSelectedIndex() > 0 &&
						e.getStateChange() == ItemEvent.SELECTED)
				{
					clearEffects();
					updateEffects();
				}
			}
		});
		add(cmbxSortType, "cell 0 1,grow");
		
		cmbxTags = new ModernComboCheckBox<String>(
				new DefaultComboBoxModel<String>(TAGS));
		cmbxTags.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int index = cmbxTags.getSelectedIndex();
				cmbxTags.setSelected(index, !cmbxTags.isSelected(index));
				
				EventQueue.invokeLater(() ->
				{
					cmbxTags.showPopup();
				});
			}
		});
		cmbxTags.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{
				clearEffects();
				updateEffects();
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e){}
		});
		cmbxTags.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if (!cmbxTags.isPopupVisible())
				{
					clearEffects();
					updateEffects();
				}
			}
		});
		add(cmbxTags, "cell 1 1,grow");
		
		discoveryEffects = new DefaultListModel<EffectMetadata>();
		list = new JList<EffectMetadata>(discoveryEffects);
		list.setCellRenderer(new DiscoveryCellRenderer());
		list.setFont(new Font("Tahoma", Font.PLAIN, 22));
		list.setBackground(UIConstants.darkBackground);
		list.setForeground(UIConstants.textPrimary);
		setBorder(new LineBorder(UIConstants.darkForeground, 1, true));
		
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener()
		{
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				JScrollBar bar = (JScrollBar)e.getSource();
				updateEffectsByScroll(bar);
			}
		});
		
		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (DiscoveryPanel.this.activeDialog != null)
				{
					DiscoveryPanel.this.activeDialog.dispose();
				}
				
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
										for (Aurora aurora : DiscoveryPanel.this.auroras)
										{
											aurora.effects().displayEffect(ef);
										}
										
										updateMain(list);
									}
									catch (HttpRequestException hre)
									{
										new TextDialog(DiscoveryPanel.this,
												"Lost connection to the device. " +
												"Please try again.").setVisible(true);
									}
									catch (StatusCodeException sce)
									{
										new TextDialog(DiscoveryPanel.this,
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
										for (Aurora aurora : DiscoveryPanel.this.auroras)
										{
											aurora.effects().addEffect(ef);
										}
										
										updateMain(list);
									}
									catch (StatusCodeException sce)
									{
										new TextDialog(DiscoveryPanel.this,
												"Lost connection to the device. " +
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
	
	private void updateEffectsByScroll(JScrollBar bar)
	{
		if (bar.getValue() == bar.getMaximum() - bar.getVisibleAmount() && !isUpdating)
		{
			topPage++;
			updateEffects();
		}
	}
	
	private void updateEffects()
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
					addRequestedEffects(topPage);
					DefaultListModel<EffectMetadata> dlm =
							new DefaultListModel<EffectMetadata>();
					for (int i = 0; i < discoveryEffects.size(); i++)
					{
						dlm.addElement(discoveryEffects.getElementAt(i));
					}
					list.setModel(dlm);
					
					if (dlm.size() > 0)
					{
						scrollPane.setViewportView(list);
					}
					
					isUpdating = false;
				}).start();
			}
		});
	}
	
	private void clearEffects()
	{
		discoveryEffects.clear();
		DefaultListModel<EffectMetadata> dlm =
				new DefaultListModel<EffectMetadata>();
		list.setModel(dlm);
		topPage = 1;
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
	
	private void addRequestedEffects(int pageIndex)
	{
		String sortType = ((String)cmbxSortType
				.getSelectedItem()).toLowerCase();
		List<String> tags = cmbxTags.getSelectedItems();
		if (cmbxSortType.getSelectedIndex() == 0)
		{
			sortType = SORT_TYPES[1].toLowerCase();
		}
		try
		{
			addEffects(Discovery.getEffectsByType(
					sortType, pageIndex, tags));
		}
		catch (HttpRequestException hre)
		{
			hre.printStackTrace();
			System.err.println("INFO: Failed to get discovery " +
					"data from the Nanoleaf server.\n");
		}
	}
	
	public void addTopEffects(int page, List<String> tags)
	{
		try
		{
			addEffects(Discovery.getTopEffects(page, tags));
		}
		catch (HttpRequestException hre)
		{
			hre.printStackTrace();
			System.err.println("INFO: Failed to get discovery " +
					"data from the Nanoleaf server.\n");
		}
	}
	
	public void addRecentEffects(int page, List<String> tags)
	{
		try
		{
			addEffects(Discovery.getRecentEffects(page, tags));
		}
		catch (HttpRequestException hre)
		{
			hre.printStackTrace();
			System.err.println("INFO: Failed to get discovery " +
					"data from the Nanoleaf server.\n");
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
