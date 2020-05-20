package io.github.rowak.nanoleafdesktop.ui.dialog;

import io.github.rowak.nanoleafdesktop.IListenToMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ConfirmationActionListener implements ActionListener {
    private final IListenToMessages parent;
    private final String repo;
    private boolean hasError = false;
    private String message;

    public ConfirmationActionListener(IListenToMessages parent, String repo) {
        this.parent = parent;
        this.repo = repo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (desktopIsSupported()) {
            actOnEvent(e);

            try {
                callRepo();
            } catch (IOException e1) {
                //TODO this has to be decoupled; again visitor or observer pattern shoudl do the trick
                //TODO simply have an error message, which will be passed to a global listener (text dialog)
                hasError = true;
                message = "Failed to automatically redirect. Go to " +
                        repo + " to download the update.";

                parent.createDialog(message, hasError);
            } catch (URISyntaxException e1) {
                hasError = true;
                message = "An internal error occurred. The update cannot be completed.";
                parent.createDialog(message, hasError);
            }
        } else {
            hasError = true;
            message = "Failed to automatically redirect. Go to " +
                    repo + " to download the update.";

            parent.createDialog(message, hasError);
        }
    }

    //TODO seam for testing; will be removed after refactoring
    protected void actOnEvent(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        OptionDialog dialog = (OptionDialog) btn.getFocusCycleRootAncestor();
        dialog.dispose();
    }

    //TODO seam for testing; will be removed after refactoring
    protected void callRepo() throws IOException, URISyntaxException {
        Desktop.getDesktop().browse(new URI(repo));
    }

    //TODO seam for testing; will be removed after refactoring
    protected boolean desktopIsSupported() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
    }

    public String getMessage() {
        return message;
    }

    public boolean hasError() {
        return hasError;
    }
}