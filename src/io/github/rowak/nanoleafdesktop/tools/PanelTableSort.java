package io.github.rowak.nanoleafdesktop.tools;

import java.util.ArrayList;
import java.util.List;

import io.github.rowak.Panel;

public class PanelTableSort
{
	/*
	 * Arrange all the panels ordered as a 2d matrix that can be read
	 * like a book (right to left, top to bottom). Uses bubblesort
	 * because it is easy to implement and this doesn't really
	 * need efficiency.
	 */
	public static void sortPanelsAsTable(Panel[] panels)
	{
		while (!panelsSortedAsTable(panels))
		{
			for (int i = 0; i < panels.length; i++)
			{
				/*
				 * When comparing two panels in the iteration, the panels will swap only if
				 * the panel at i+1 is located above the panel at i, or if the panel at i+1
				 * is located to the left of the panel at i in the same row.
				 */
				if (i < panels.length-1 && (panelBelowIsGreater(panels[i+1], panels[i]) ||
						panelBesideIsGreater(panels[i+1], panels[i])))
				{
					// Swap the panels at i and i+1
					Panel temp = panels[i];
					panels[i] = panels[i+1];
					panels[i+1] = temp;
				}
			}
		}
	}
	
	/*
	 * Arrange the panels into an actual 2d array using ONLY an array
	 * of panels that has been sorted using the sortPanelsAsTable method.
	 */
	public static Panel[][] getRows(Panel[] sortedPanels)
	{
		List<Panel[]> rows = new ArrayList<Panel[]>();
		int lastRowIndex = 0;
		
		/*
		 * Split each row in the panels array by checking for the next
		 * panel that is not on the same row as the previous panel. Then
		 * group the previous panels as a row and add it to the rows list.
		 */
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
		
		// Add the remaining panels to the final row
		Panel[] row = new Panel[sortedPanels.length - lastRowIndex];
		for (int i = lastRowIndex; i < sortedPanels.length; i++)
		{
			row[i - lastRowIndex] = sortedPanels[i];
		}
		rows.add(row);
		return rows.toArray(new Panel[][]{});
	}
	
	/*
	 * Check if a panel BELOW another panel (in the current iteration)
	 * has a greater Y value.
	 */
	private static boolean panelBelowIsGreater(Panel p1, Panel p2)
	{
		return p1.getY() > p2.getY() && p1.getY() - p2.getY() > 44;
	}
	
	/*
	 * Check if a panel BESIDE another panel (in the current iteration)
	 * has a greater X value.
	 */
	private static boolean panelBesideIsGreater(Panel p1, Panel p2)
	{
		return p1.getX() < p2.getX() && p2.getY() - p1.getY() <= 44;
	}
	
	/*
	 * Check if an array of panels is fully sorted as a table/ 2d matrix.
	 */
	private static boolean panelsSortedAsTable(Panel[] panels)
	{
		for (int i = 0; i < panels.length; i++)
		{
			if (i < panels.length-1 &&
					panelBelowIsGreater(panels[i+1], panels[i]))
			{
				return false;
			}
			else if (i < panels.length-1 &&
					panelBesideIsGreater(panels[i+1], panels[i]))
			{
				return false;
			}
		}
		return true;
	}
}
