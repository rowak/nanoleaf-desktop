package io.github.rowak.nanoleafdesktop;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class PropertyReader implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(PropertyReader.class.getName());

    private final String propertyFilePathAsString;
    private Path propertyFilePath;

    public PropertyReader() {
        propertyFilePathAsString = createPropertyFilePath();

        try {
            propertyFilePath = Files.createFile(Paths.get(this.propertyFilePathAsString));
        } catch (IOException e) {
            LOGGER.atError().log("Could not create file", e);
        }
    }

    public String getPropertyFilePathAsString() {
        return propertyFilePathAsString;
    }

    public Path getPropertyFilePath() {
        return propertyFilePath;
    }

    private String createPropertyFilePath() {
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
        String oldPropertiesFilepath = getOldPropertiesFilepath();
        File oldProperties = new File(oldPropertiesFilepath);

        if (!oldProperties.exists()) {
            return;
        }

        //TODO - please verify that this is the desired output
        copyOldPropertyFileToNewPath(oldProperties);
        backUpOldPropertyFile(oldProperties);
    }

    private void copyOldPropertyFileToNewPath(File oldProperties) {
        try {
            Files.copy(oldProperties.toPath(), propertyFilePath, REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.atError().log("Could not copy property file", e);
        }
    }

    private void backUpOldPropertyFile(File oldProperties) {
        Path backupOldPathToNewDirectory = Path.of(propertyFilePath.getParent() + File.separator + "properties-OLD.txt");
        try {
            Files.move(oldProperties.toPath(), backupOldPathToNewDirectory);
        } catch (IOException e) {
            LOGGER.atError().log("Could not move old property file", e);
        }
    }

    //TODO glue code to be able to test - has to be removed
    protected String getOldPropertiesFilepath() {
        return System.getProperty("user.home") + "/properties.txt";
    }
}
