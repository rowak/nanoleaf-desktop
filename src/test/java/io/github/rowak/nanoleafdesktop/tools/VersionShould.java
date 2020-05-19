package io.github.rowak.nanoleafdesktop.tools;

import org.json.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class VersionShould {

    @Test
    public void compare_versionNumber_for_prereleases() {
        Version currentVersion = createPreRelease("v0.8.6");
        Version newerVersion = createPreRelease("v0.9.0");

        int isNewer = newerVersion.compareTo(currentVersion);

        assertThat(isNewer).isGreaterThan(0);
    }

    private Version createPreRelease(String version) {
        JSONObject newerVersionJson = new JSONObject().put("name", version).put("prerelease", true);
        return new Version(newerVersionJson);
    }

    @Test
    public void compare_versionNumber_first_even_for_releases() {
        Version releaseVersion = createRelease("v0.8.6");
        Version preReleaseVersion = createPreRelease("v0.9.0");

        int isNewer = preReleaseVersion.compareTo(releaseVersion);

        assertThat(isNewer).isGreaterThan(0);
    }

    private Version createRelease(String version) {
        JSONObject releaseWithMinorVersionNumber = new JSONObject().put("name", version).put("prerelease", false);
        return new Version(releaseWithMinorVersionNumber);
    }

    @Test
    public void compare_prerelease_status_when_same_version() {
        Version currentVersion = createRelease("v0.8.6");
        Version newerVersion = createPreRelease("v0.8.6");

        int isNewer = newerVersion.compareTo(currentVersion);

        assertThat(isNewer).isGreaterThan(0);
    }
}