package io.github.rowak.nanoleafdesktop;

import java.io.File;
import java.io.Serializable;

public class PropertyReader implements Serializable {
    public String getPropertiesFilePath() {
        String userHome = "";
        String infix = "";
        String applicationName = "Nanoleaf for Desktop";
        String propertyFileName = "preferences.txt";

        String dir = "";
        final String os = getOS();

        if (os.contains("win")) {
            userHome = System.getenv("APPDATA") + File.separator;
        } else if (os.contains("mac")) {
            userHome = System.getProperty("user.home") + File.separator;
            infix = "Library" + File.separator + "Application Support" + File.separator;
        } else if (os.contains("nux")) {
            userHome = System.getProperty("user.home") + File.separator;
            infix = ".";
        }

        dir = userHome + infix + applicationName;

        create(dir);

        return dir + File.separator + propertyFileName;
    }

    protected void create(String dir) {
        File dirFile = new File(dir);

        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
    }

    protected String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }
}
