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
    private IDeliverMessages message = null;

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
                message = new ErrorMessage("Failed to automatically redirect. Go to " +
                                                   repo + " to download the update.");
            } catch (URISyntaxException e1) {
                message = new ErrorMessage("An internal error occurred. The update cannot be completed.");
            }
        } else {
            message = new ErrorMessage("Failed to automatically redirect. Go to " +
                                               repo + " to download the update.");

        }

        if (hasError()) {
            parent.createDialog(message);
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

    private boolean hasError() {
        return message != null;
    }
}
