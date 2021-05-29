package io.github.rowak.nanoleafdesktop.tools;

import org.json.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;


public class UpdateManagerShould {

    @Test
    public void foo() {
        TestableUpdateManager testableUpdateManager = new TestableUpdateManager(
                "https://api.github.com/repos/rowak/nanoleaf-desktop/releases",
                "https://github.com/rowak/nanoleaf-desktop");
        Version current = createRelease("v0.1");

        boolean actual = false;
        try {
        	actual = testableUpdateManager.updateAvailable(current);
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
        	
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

        boolean actual = true;
        try {
        	actual = testableUpdateManager.updateAvailable(current);
        }
        catch (IOException e) {
        	e.printStackTrace();
        }

        assertThat(actual).isFalse();
    }
    
    @Test
    public void foo3() {
    	TestableUpdateManager testableUpdateManager = new TestableUpdateManager(
                "https://api.github.com/repos/rowak/nanoleaf-desktop/releases",
                "https://github.com/rowak/nanoleaf-desktop");
    	Version v = new Version("v0.9.0", true);
    	
    	boolean result = false;
    	try {
    		result = testableUpdateManager.updateAvailable(v);
    	}
    	catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    	assertThat(result).isTrue();
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