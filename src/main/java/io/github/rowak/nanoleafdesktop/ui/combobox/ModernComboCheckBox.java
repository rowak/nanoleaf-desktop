package io.github.rowak.nanoleafdesktop.ui.combobox;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

public class ModernComboCheckBox<T> extends ModernComboBox<T> {
	
	private String[] items;
	private boolean[] selectedItems;
	private ModernComboCheckBoxRenderer<T> renderer;
	
	public ModernComboCheckBox(String[] items) {
		initUI(items);
	}
	
	public ModernComboCheckBox(DefaultComboBoxModel<T> model) {
		setModel(model);
		initUI(modelToArray(model));
	}
	
	public List<T> getSelectedItems() {
		List<T> itemsList = new ArrayList<T>();
		for (int i = 0; i < items.length; i++) {
			if (selectedItems[i]) {
				itemsList.add((T)items[i]);
			}
		}
		return itemsList;
	}
	
	public void setSelected(int index, boolean selected) {
		renderer.setSelected(index, selected);
	}
	
	public boolean isSelected(int index) {
		return selectedItems[index];
	}
	
	private void initUI(String[] items) {
		this.items = items;
		selectedItems = new boolean[items.length];
		renderer = new ModernComboCheckBoxRenderer<T>(items, selectedItems);
		setRenderer(renderer);
	}
	
	private String[] modelToArray(DefaultComboBoxModel<T> model) {
		String[] items = new String[model.getSize()];
		for (int i = 0; i < model.getSize(); i++) {
			items[i] = (String)model.getElementAt(i);
		}
		return items;
	}
}
