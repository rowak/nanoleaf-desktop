package io.github.rowak.nanoleafdesktop.tools;

import com.github.kevinsawicki.http.HttpRequest;
import io.github.rowak.nanoleafdesktop.IListenToMessages;
import io.github.rowak.nanoleafdesktop.ui.dialog.UpdateOptionDialog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;

public class UpdateManager {
    //TODO should move to property file and be read from there
    private String host = "https://api.github.com/repos/rowak/nanoleaf-desktop/releases";
    private String repo = "https://github.com/rowak/nanoleaf-desktop/releases";

    public UpdateOptionDialog checkForUpdate(IListenToMessages parent, Version version) {
        UpdateOptionDialog updateDialog = null;
        Version latest = getLatestVersionFromHost();

        if (newVersionIsAvailable(version, latest)) {
            updateDialog = createUpdateDialog(parent);
        }

        return updateDialog;
    }

    private Version getLatestVersionFromHost() {
        String responseFrom = getResponseFrom(host);

        JSONArray parsedResponse = new JSONArray(responseFrom);
        JSONObject versionAsJson = parsedResponse.getJSONObject(0);

        return new Version(versionAsJson);
    }

    //TODO just glue code; will be removed after refactoring; HttpRequest should be injected; for easy testing
    protected String getResponseFrom(String host) {
        return HttpRequest.get(host).body();
    }

    private boolean newVersionIsAvailable(Version version, Version latest) {
        return latest.compareTo(version) > 0;
    }

    private UpdateOptionDialog createUpdateDialog(IListenToMessages parent) {
        UpdateOptionDialog updateDialog;
        updateDialog = new UpdateOptionDialog(parent, repo);
        updateDialog.setVisible(true);

        parent.render(updateDialog);
        return updateDialog;
    }

    //TODO too tight coupling to parent; will apply observer or visitor pattern; don't know right now which
    //just glue code; will be removed after refactoring
    protected void render(Component parent, UpdateOptionDialog updateDialog) {
        updateDialog.finalizeDialog(parent);
    }
}
