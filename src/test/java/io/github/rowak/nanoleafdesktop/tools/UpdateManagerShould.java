package io.github.rowak.nanoleafdesktop.tools;

import org.json.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class UpdateManagerShould {

    @Test
    public void foo() {
        TestableUpdateManager testableUpdateManager = new TestableUpdateManager(
                "https://api.github.com/repos/rowak/nanoleaf-desktop/releases",
                "https://github.com/rowak/nanoleaf-desktop");
        Version current = createRelease("v0.1");

        boolean actual = testableUpdateManager.updateAvailable(current);

        assertThat(actual).isTrue();
    }

    private Version createRelease(String version) {
        JSONObject releaseWithMinorVersionNumber = new JSONObject().put("name", version).put("prerelease", false);
        return new Version(releaseWithMinorVersionNumber);
    }

    @Test
    public void foo2() {
        TestableUpdateManager testableUpdateManager = new TestableUpdateManager(
                "https://api.github.com/repos/rowak/nanoleaf-desktop/releases",
                "https://github.com/rowak/nanoleaf-desktop");
        Version current = createRelease("v0.9.0");

        boolean actual = testableUpdateManager.updateAvailable(current);

        assertThat(actual).isFalse();
    }

    private class TestableUpdateManager extends UpdateManager {
        public TestableUpdateManager(String host, String repo) {
            super(host, repo);
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
    }
}