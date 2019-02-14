package io.github.rowak.nanoleafdesktop.tools;

import java.util.ArrayList;
import java.util.List;

import io.github.rowak.Panel;

public class PanelTableSort
{
	public static void sortPanelsAsTable(Panel[] panels)
	{
		while (!panelsSortedAsTable(panels))
		{
			for (int i = 0; i < panels.length; i++)
			{
				if (i < panels.length-1 && (panelBelowIsGreater(panels[i+1], panels[i]) ||
						panelBesideIsGreater(panels[i+1], panels[i])))
				{
					Panel temp = panels[i];
					panels[i] = panels[i+1];
					panels[i+1] = temp;
				}
			}
		}
	}
	
	public static Panel[][] getRows(Panel[] sortedPanels)
	{
		List<Panel[]> rows = new ArrayList<Panel[]>();
		int lastRowIndex = 0;
		for (int i = 0; i < sortedPanels.length; i++)
		{
			if (i < sortedPanels.length-1 &&
					sortedPanels[i+1].getY() < sortedPanels[i].getY() &&
					(sortedPanels[i].getY() - sortedPanels[i+1].getY() > 44))
			{
				Panel[] row = new Panel[i - lastRowIndex+1];
				for (int j = lastRowIndex; j < i+1; j++)
				{
					row[j - lastRowIndex] = sortedPanels[j];
				}
				lastRowIndex = i+1;
				rows.add(row);
			}
		}
		
		// add the remaining panels to the final row
		Panel[] row = new Panel[sortedPanels.length - lastRowIndex];
		for (int i = lastRowIndex; i < sortedPanels.length; i++)
		{
			row[i - lastRowIndex] = sortedPanels[i];
		}
		rows.add(row);
		return rows.toArray(new Panel[][]{});
	}
	
	private static boolean panelBelowIsGreater(Panel p1, Panel p2)
	{
		return p1.getY() > p2.getY() && p1.getY() - p2.getY() > 44;
	}
	
	private static boolean panelBesideIsGreater(Panel p1, Panel p2)
	{
		return p1.getX() < p2.getX() && p2.getY() - p1.getY() <= 44;
	}
	
	private static boolean panelsSortedAsTable(Panel[] panels)
	{
		for (int i = 0; i < panels.length; i++)
		{
			if (i < panels.length-1 && panels[i+1].getY() > panels[i].getY() &&
					panels[i+1].getY() - panels[i].getY() > 44)
			{
				return false;
			}
			else if (i < panels.length-1 && panels[i+1].getX() < panels[i].getX() &&
					panels[i].getY() - panels[i+1].getY() <= 44)
			{
				return false;
			}
		}
		return true;
	}
}
