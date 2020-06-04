package io.github.rowak.nanoleafdesktop;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyReaderShould {

    public final String applicationName = "Nanoleaf for Desktop";
    private String userHome;
    private String propertyFileName = "preferences.txt";

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

    private class TestablePropertyReaderForWindows extends PropertyReader {

        @Override
        protected String getOs() {
            return "win";
        }
    }

    private class TestablePropertyReaderForMac extends PropertyReader {
        @Override
        protected String getOs() {
            return "mac";
        }
    }

    private class TestablePropertyReaderForLinux extends PropertyReader {
        @Override
        protected String getOs() {
            return "nux";
        }
    }
}
