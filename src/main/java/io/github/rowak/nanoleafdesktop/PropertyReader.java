package io.github.rowak.nanoleafdesktop;

import java.io.*;

public class PropertyReader implements Serializable {

    private final String propertyFilePath;

    public PropertyReader() {
        propertyFilePath = getPath();
    }

    public String getPropertyFilePath() {
        return propertyFilePath;
    }

    private String getPath() {
        String applicationName = "Nanoleaf for Desktop";
        String propertyFileName = "preferences.txt";

        String prefix = getOSDependentPrefix();
        String propertyFileDirectory = prefix + applicationName;

        create(propertyFileDirectory);

        return propertyFileDirectory + File.separator + propertyFileName;
    }

    protected String getOSDependentPrefix() {
        String os = getOS();
        String prefix = "";

        if (os.contains("win")) {
            prefix = System.getenv("APPDATA") + File.separator;
        } else if (os.contains("mac")) {
            prefix = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator;
        } else if (os.contains("nux")) {
            prefix = System.getProperty("user.home") + File.separator + ".";
        }

        return prefix;
    }

    private String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }

    private void create(String dir) {
        File dirFile = new File(dir);

        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
    }

    public void migrateOldProperties() {
        String oldPropertiesFilepath = System.getProperty("user.home") + "/properties.txt";
        File oldProperties = new File(oldPropertiesFilepath);

        if (!oldProperties.exists()) {
            return;
        }

        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(oldPropertiesFilepath));
            writer = new BufferedWriter(new FileWriter(propertyFilePath));
            String data = "";
            String line = "";
            while ((line = reader.readLine()) != null) {
                data += line + "\n";
            }
            writer.write(data);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                oldProperties.renameTo(new File(
                        System.getProperty("user.home") +
                                "/propertiesOLD.txt"));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
