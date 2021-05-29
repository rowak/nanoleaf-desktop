package io.github.rowak.nanoleafdesktop.ui.combobox;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import io.github.rowak.nanoleafdesktop.ui.checkbox.ModernCheckBox;
import io.github.rowak.nanoleafdesktop.ui.label.LargeModernLabel;
import net.miginfocom.swing.MigLayout;

public class ModernComboCheckBoxRenderer<T> implements ListCellRenderer<T> {
	
	private String[] items;
	private boolean[] selectedItems;
	
	public ModernComboCheckBoxRenderer(String[] items, boolean[] selectedItems) {
		this.items = items;
		this.selectedItems = selectedItems;
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (index == -1) {
			index = 0;
		}
		String item = (String)list.getModel().getElementAt(index);
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[][]", "[][]"));
		panel.setBackground(Color.DARK_GRAY);
		JLabel text = new LargeModernLabel(item);
		panel.add(text, "cell 1 0");
		if (!cellHasFocus && index > 0) {
			JCheckBox checkbox = new ModernCheckBox();
			checkbox.setSelected(selectedItems[index]);
			panel.add(checkbox, "cell 0 0");
		}
		if (isSelected) {
			panel.setBackground(new Color(153, 180, 209));
		}
		return panel;
	}
	
	public void setSelected(int index, boolean selected) {
		selectedItems[index] = selected;
	}
}
