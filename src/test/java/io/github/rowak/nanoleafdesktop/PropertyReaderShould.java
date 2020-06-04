package io.github.rowak.nanoleafdesktop;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyReaderShould {

    public static final String appData = System.getenv("APPDATA");

    @Test
    public void determine_property_file_path_for_windows() {
        String propertyFilePathForWindows = new TestablePropertyReaderForWindows().getPropertiesFilePath();

        assertThat(propertyFilePathForWindows).isEqualTo(appData + File.separator + "Nanoleaf for Desktop" + File.separator + "preferences.txt");
    }

    private class TestablePropertyReaderForWindows extends PropertyReader {
        @Override
        protected String getOs() {
            return "win";
        }
    }
}
