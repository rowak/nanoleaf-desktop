package io.github.rowak.nanoleafdesktop.tools;

import org.json.JSONObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class VersionShould {

    @Test
    public void determine_newer_version_by_release_state_when_numbers_are_equal() {
        Version preRelease = createRelease("v0.8.6");
        Version releaseVersion = createPreRelease("v0.8.6");

        assertThat(releaseVersion).isGreaterThan(preRelease);
    }

    @Test
    public void determine_newer_version_by_numbers_version_for_pre_releases() {
        Version olderVersion = createPreRelease("v0.8.6");
        Version newerVersion = createPreRelease("v0.9.0");

        assertThat(newerVersion).isGreaterThan(olderVersion);
    }

    private Version createPreRelease(String version) {
        JSONObject newerVersionJson = new JSONObject().put("name", version).put("prerelease", true);
        return new Version(newerVersionJson);
    }

    @Test
    public void determine_newer_version_by_numbers_for_release_and_pre_release_versions() {
        Version releaseVersion = createRelease("v0.8.6");
        Version preReleaseVersion = createPreRelease("v0.9.0");

        assertThat(preReleaseVersion).isGreaterThan(releaseVersion);
    }

    private Version createRelease(String version) {
        JSONObject releaseWithMinorVersionNumber = new JSONObject().put("name", version).put("prerelease", false);
        return new Version(releaseWithMinorVersionNumber);
    }
}
