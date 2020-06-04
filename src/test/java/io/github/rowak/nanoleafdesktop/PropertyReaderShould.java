package io.github.rowak.nanoleafdesktop;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyReaderShould {

    @Rule
    public TemporaryFolder folderForPreferenceFile = new TemporaryFolder();

    public final String applicationName = "Nanoleaf for Desktop";
    private String userHome;
    private String propertyFileName = "preferences.txt";
    private File tempFolder;

    @Before
    public void setUp() throws Exception {
        tempFolder = folderForPreferenceFile.newFolder();
    }

    @Test
    public void determine_property_file_path_for_windows() {
        userHome = System.getenv("APPDATA");

        String propertyFilePathForWindows = new TestablePropertyReaderForWindows().getPropertiesFilePath();

        //TODO should be preferred
        //assertThat(propertyFilePathForWindows).isEqualTo(System.getProperty("user.home") + File.separator + "Nanoleaf for Desktop" + File.separator + "preferences.txt");
        assertThat(propertyFilePathForWindows).isEqualTo(userHome + File.separator + applicationName + File.separator + propertyFileName);
    }

    @Test
    public void determine_property_file_path_for_mac() {
        userHome = System.getProperty("user.home");
        String macAppLibraryPath = "Library" + File.separator + "Application Support";

        String propertyFilePathForMac = new TestablePropertyReaderForMac().getPropertiesFilePath();

        assertThat(propertyFilePathForMac).isEqualTo(userHome + File.separator + macAppLibraryPath + File.separator + applicationName + File.separator + propertyFileName);
    }

    @Test
    public void determine_propert_file_path_for_linux() {
        userHome = System.getProperty("user.home");
        String hiddenFilePrefix = ".";

        String propertyFilePathForNix = new TestablePropertyReaderForLinux().getPropertiesFilePath();

        assertThat(propertyFilePathForNix).isEqualTo(userHome + File.separator + hiddenFilePrefix + applicationName + File.separator + propertyFileName);
    }

    @Test
    public void create_folder_for_preference_file() throws IOException {
        assertThat(tempFolder).isEmptyDirectory();

        String propertyFilePath = new TestablePropertyReader().getPropertiesFilePath();

        File propertyFile = new File(propertyFilePath);
        File propertyFileParentFolder = propertyFile.getParentFile();
        assertThat(propertyFile).doesNotExist();
        assertThat(propertyFileParentFolder).exists();
    }

    private class TestablePropertyReaderForWindows extends PropertyReader {

        @Override
        protected String getOSDependentPrefix() {
            return System.getenv("APPDATA") + File.separator;
        }
    }

    private class TestablePropertyReaderForMac extends PropertyReader {
        @Override
        protected String getOSDependentPrefix() {
            return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator;
        }
    }

    private class TestablePropertyReaderForLinux extends PropertyReader {
        @Override
        protected String getOSDependentPrefix() {
            return System.getProperty("user.home") + File.separator + ".";
        }
    }

    private class TestablePropertyReader extends PropertyReader {
        @Override
        protected String getOSDependentPrefix() {
            return tempFolder.getPath() + File.separator;
        }
    }
}
