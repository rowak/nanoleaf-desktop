package io.github.rowak.nanoleafdesktop.tools;

import io.github.rowak.nanoleafdesktop.IListenToMessages;
import io.github.rowak.nanoleafdesktop.ui.dialog.IDeliverMessages;
import io.github.rowak.nanoleafdesktop.ui.dialog.UpdateOptionDialog;
import org.json.JSONObject;
import org.junit.Test;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;


public class UpdateManagerShould {

    @Test
    public void create_UpdateDialog_when_newer_version_is_available() {
        TestableUpdateManager testableUpdateManager = new TestableUpdateManager(
                "https://api.github.com/repos/rowak/nanoleaf-desktop/releases",
                "https://github.com/rowak/nanoleaf-desktop");
        Version current = createRelease("v0.1");
        ParentDummy parent = new ParentDummy();

        UpdateOptionDialog updateOptionDialog = testableUpdateManager.checkForUpdate(parent, current);

        //TODO figure out how to access title, button texts
        assertThat(updateOptionDialog).isNotNull();
        assertThat(updateOptionDialog.isVisible()).isTrue();
    }

    private Version createRelease(String version) {
        JSONObject releaseWithMinorVersionNumber = new JSONObject().put("name", version).put("prerelease", false);
        return new Version(releaseWithMinorVersionNumber);
    }

    @Test
    public void show_no_dialog_if_already_latest() {
        TestableUpdateManager testableUpdateManager = new TestableUpdateManager(
                "https://api.github.com/repos/rowak/nanoleaf-desktop/releases",
                "https://github.com/rowak/nanoleaf-desktop");
        Version current = createRelease("v0.9.0");
        IListenToMessages parent = new ParentDummy();

        UpdateOptionDialog updateOptionDialog = testableUpdateManager.checkForUpdate(parent, current);

        //TODO maybe a TextDialog with appropriate message?
        assertThat(updateOptionDialog).isNull();
    }

    private class TestableUpdateManager extends UpdateManager {
        public TestableUpdateManager(String host, String repo) {
            super();
        }

        @Override
        protected String getResponseFrom(String host) {
            //language=JSON
            String body = "[\n" +
                    "  {\n" +
                    "    \"prerelease\": false,\n" +
                    "    \"name\": \"v0.8.6\"\n" +
                    "  }\n" +
                    "]";

            return body;
        }

        @Override
        protected void render(Component parent, UpdateOptionDialog updateDialog) {
            // do nothing as we do not want to render the window
        }
    }

    private class ParentDummy implements IListenToMessages {

        @Override
        public void render(UpdateOptionDialog updateDialog) {

        }

        @Override
        public void createDialog(IDeliverMessages message) {
        }
    }
}
