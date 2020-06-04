package io.github.rowak.nanoleafdesktop.tools;

import java.io.*;

//TODO this will be replaced by PropertyReader - can be simplified quite a lot
@Deprecated
public class PropertyManager {
	private File file;

	public PropertyManager(String filePath) {
		file = new File(filePath);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public String getProperty(String key) {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] data = line.split("" + (char)28);
				String xKey = data[0];
				String xValue = data[1];
				if (key.equals(xKey)) {
					return xValue;
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return null;
	}

	public void setProperty(String key, Object value) {
		BufferedReader reader = null;
		BufferedWriter writer = null;

		try {
			String properties;
			reader = new BufferedReader(new FileReader(file));
			String line;
			StringBuilder propertiesBuilder = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				String xKey = line.split("" + (char)28)[0];
				if (!key.equals(xKey)) {
					propertiesBuilder.append(line).append("\n");
				}
			}

			properties = propertiesBuilder.toString();
			properties += (key + (char)28 + value) + "\n";

			writer = new BufferedWriter(new FileWriter(file));
			writer.write(properties);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				assert reader != null;
				reader.close();
				assert writer != null;
				writer.flush();
				writer.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public void removeProperty(String key) {
		BufferedReader reader = null;
		BufferedWriter writer = null;

		try {
			String properties = "";
			reader = new BufferedReader(new FileReader(file));
			writer = new BufferedWriter(new FileWriter(file));
			String line;
			while ((line = reader.readLine()) != null) {
				String xKey = line.split("" + (char)28)[0];
				if (!key.equals(xKey)) {
					properties += line + "\n";
				}
			}

			writer.write(properties);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				reader.close();
				writer.flush();
				writer.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public void clearProperties() {
		try {
			file.delete();
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
