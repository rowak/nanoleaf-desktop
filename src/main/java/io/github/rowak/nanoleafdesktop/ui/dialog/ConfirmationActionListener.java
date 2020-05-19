package io.github.rowak.nanoleafdesktop.ui.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ConfirmationActionListener implements ActionListener {
    private final Component parent;
    private final String repo;
    private boolean hasError = false;

    public ConfirmationActionListener(Component parent, String repo) {
        this.parent = parent;
        this.repo = repo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Desktop.isDesktopSupported() &&
                Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            JButton btn = (JButton) e.getSource();
            OptionDialog dialog = (OptionDialog) btn.getFocusCycleRootAncestor();
            dialog.dispose();

            try {
                Desktop.getDesktop().browse(new URI(repo));
            } catch (IOException e1) {
                //TODO this has to be decoupled; again visitor or observer pattern shoudl do the trick
                //TODO simply have an error message, which will be passed to a global listener (text dialog)
                TextDialog error = new TextDialog(parent,
                                                  "Failed to automatically redirect. Go to " +
                                                          repo + " to download the update.");
                error.setVisible(true);
            } catch (URISyntaxException e1) {
                TextDialog error = new TextDialog(parent,
                                                  "An internal error occurred. " +
                                                          "The update cannot be completed.");
                error.setVisible(true);
            }
        } else {
            TextDialog error = new TextDialog(parent,
                                              "Failed to automatically redirect. Go to " +
                                                      repo + " to download the update.");
            error.setVisible(true);
        }
    }
}