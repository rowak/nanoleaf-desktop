package io.github.rowak.nanoleafdesktop.tools;

import org.json.JSONObject;

import java.util.Comparator;

/**
 * A local version interface for the GitHub REST api.
 */
public class Version implements Comparable<Version> {
    int semVer;
    boolean preRelease;
    String name;

    public Version(String name, boolean preRelease) {
        this.name = name;
        this.preRelease = preRelease;
    }

    public Version(JSONObject json) {
        name = json.getString("name");
        preRelease = json.getBoolean("prerelease");
    }

    public String getName() {
        return this.name;
    }

    public boolean getPreRelease() {
        return this.preRelease;
    }

    @Override
    public int compareTo(Version otherVersion) {
        return Comparator.comparing((Version p) -> p.name)
                         .thenComparing(p -> p.preRelease).compare(this, otherVersion);

    }
}
