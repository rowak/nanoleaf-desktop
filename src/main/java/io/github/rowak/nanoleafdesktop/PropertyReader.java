package io.github.rowak.nanoleafdesktop;

import java.io.File;
import java.io.Serializable;

public class PropertyReader implements Serializable {
    public String getPropertiesFilePath() {
        String applicationName = "Nanoleaf for Desktop";
        String propertyFileName = "preferences.txt";

        String prefix = getOSDependentPrefix();
        String dir = prefix + applicationName;

        create(dir);

        return dir + File.separator + propertyFileName;
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

    private void create(String dir) {
        File dirFile = new File(dir);

        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
    }

    protected String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }
}
