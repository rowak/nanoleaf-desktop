package io.github.rowak.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PropertyManager
{
	File file;
	
	public PropertyManager(String filePath)
	{
		file = new File(filePath);
		try
		{
			if (!file.exists())
				file.createNewFile();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	public String getProperty(String key)
	{
		BufferedReader reader = null;
		
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] data = line.split("" + (char)28);
				String xKey = data[0];
				String xValue = data[1];
				if (key.equals(xKey))
				{
					return xValue;
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
		return null;
	}
	
	public void setProperty(String key, Object value)
	{
		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		try
		{
			String properties = "";
			reader = new BufferedReader(new FileReader(file));
			writer = new BufferedWriter(new FileWriter(file));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String xKey = line.split("" + (char)28)[0];
				if (!key.equals(xKey))
				{
					properties += line + "\n";
				}
			}
			properties += (key + (char)28 + value);
			
			writer.write(properties);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				reader.close();
				writer.flush();
				writer.close();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
	
	public void removeProperty(String key)
	{
		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		try
		{
			String properties = "";
			reader = new BufferedReader(new FileReader(file));
			writer = new BufferedWriter(new FileWriter(file));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String xKey = line.split("" + (char)28)[0];
				if (!key.equals(xKey))
				{
					properties += line + "\n";
				}
			}
			
			writer.write(properties);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				reader.close();
				writer.flush();
				writer.close();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}
}
