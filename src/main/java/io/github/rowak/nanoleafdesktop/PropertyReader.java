package io.github.rowak.nanoleafdesktop;

import java.io.File;
import java.io.Serializable;

public class PropertyReader implements Serializable {
    public String getPropertiesFilePath() {
        String dir = "";
        final String os = getOs();
        if (os.contains("win")) {
            dir = System.getenv("APPDATA") + "/Nanoleaf for Desktop";
        } else if (os.contains("mac")) {
            dir = System.getProperty("user.home") +
                    "/Library/Application Support/Nanoleaf for Desktop";
        } else if (os.contains("nux")) {
            dir = System.getProperty("user.home") + "/.Nanoleaf for Desktop";
        }

        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }

        return dir + "/preferences.txt";
    }

    protected String getOs() {
        return System.getProperty("os.name").toLowerCase();
    }
}
