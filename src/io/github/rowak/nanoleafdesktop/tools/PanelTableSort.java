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
	public static void sortPanelsAsRows(Panel[] panels)
	{
		while (!panelsSortedAsRows(panels))
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
		// Sort the panels if they aren't already sorted
		if (!panelsSortedAsRows(sortedPanels))
		{
			sortPanelsAsRows(sortedPanels);
		}
		
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
	 * Arrange all the panels ordered as a 2d matrix that can be read
	 * from top to bottom, right to left (in terms of columns).
	 * Uses bubblesort because it is easy to implement and this
	 * doesn't really need efficiency.
	 */
	public static void sortPanelsAsColumns(Panel[] panels)
	{
		while (!panelsSortedAsColumns(panels))
		{
			for (int i = 0; i < panels.length; i++)
			{
				/*
				 * When comparing two panels in the iteration, the panels will swap only if
				 * the panel at i+1 is located to the left the panel at i, or if the panel at i+1
				 * is located at the same x value as the panel at i and the panel at i+1 is above
				 * the panel at i.
				 */
				if (i < panels.length-1 && (panelHasGreaterX(panels[i+1], panels[i]) ||
						panelIsAbove(panels[i+1], panels[i])))
				{
					// Swap the panels at i and i+1
					Panel temp = panels[i];
					panels[i] = panels[i+1];
					panels[i+1] = temp;
				}
			}
		}
	}
	
	public static Panel[][] getColumns(Panel[] sortedPanels)
	{
		// Sort the panels if they aren't already sorted
		if (!panelsSortedAsColumns(sortedPanels))
		{
			sortPanelsAsColumns(sortedPanels);
		}
		
		List<Panel[]> columns = new ArrayList<Panel[]>();
		
		for (int i = 0; i < sortedPanels.length; i++)
		{
			/*
			 * Check if the panel at i is in the same column as the
			 * panel at i+1 (same x value).
			 */
			if (i < sortedPanels.length-1 &&
					sortedPanels[i].getX() == sortedPanels[i+1].getX())
			{
				/*
				 * Determine the number of panels in the column (number
				 * of following panels with same x value).
				 */
				int max = 0;
				for (int j = i; j < sortedPanels.length; j++)
				{
					if (j < sortedPanels.length-1 &&
							sortedPanels[j].getX() != sortedPanels[j+1].getX())
					{
						max = j+1;
						break;
					}
				}
				/*
				 * Add the panels in this column to the columns
				 * list as an array.
				 */
				Panel[] column = new Panel[max-i];
				for (int j = i; j < max; j++)
				{
					column[j-i] = sortedPanels[j];
				}
				columns.add(column);
				i = max-1;
			}
			else
			{
				/*
				 * If only one panel is in the column, add the panel at i
				 * to the columns list as a column with a size of 1.
				 */
				Panel[] column = new Panel[]{sortedPanels[i]};
				columns.add(column);
			}
		}
		return columns.toArray(new Panel[][]{});
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
	 * Check if an array of panels is fully sorted in terms of rows.
	 */
	private static boolean panelsSortedAsRows(Panel[] panels)
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
	
	/*
	 * Check if panel 2 is located to the left of panel 1.
	 */
	private static boolean panelHasGreaterX(Panel p1, Panel p2)
	{
		return p1.getX() < p2.getX();
	}
	
	/*
	 * Check if panel 1 and panel 2 are at the same x value and if
	 * panel 1 is above panel 2.
	 */
	private static boolean panelIsAbove(Panel p1, Panel p2)
	{
		return p1.getX() == p2.getX() && p1.getY() > p2.getY();
	}
	
	/*
	 * Check if an array of panels is fully sorted in terms of columns.
	 */
	private static boolean panelsSortedAsColumns(Panel[] panels)
	{
		for (int i = 0; i < panels.length; i++)
		{
			if (i < panels.length-1 && panelHasGreaterX(panels[i+1], panels[i]))
			{
				return false;
			}
			else if (i < panels.length-1 && panelIsAbove(panels[i+1], panels[i]))
			{
				return false;
			}
		}
		return true;
	}
}
