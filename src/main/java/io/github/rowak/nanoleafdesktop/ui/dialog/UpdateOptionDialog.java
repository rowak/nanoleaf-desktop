package io.github.rowak.nanoleafdesktop.ui.dialog;

import io.github.rowak.nanoleafdesktop.IListenToMessages;

import javax.swing.*;
import java.awt.*;

public class UpdateOptionDialog extends BasicDialog {

    private final IListenToMessages parent;
    private final String title = "An update is available! Would you like to download it now?";
    private final String confirmationButtonText = "Yes";
    private final String cancellationButtonText = "No";
    private final ConfirmationActionListener confirmationActionListener;
    private final CancellationActionListener cancellationActionListener = new CancellationActionListener();

    public UpdateOptionDialog(IListenToMessages parent, String repo) {
        this.parent = parent;
        confirmationActionListener = new ConfirmationActionListener(this.parent, repo);

        createDialog();
    }

    private void createDialog() {
        new UpdateDialogTitle(title, contentPanel);

        new UpdateButton(confirmationButtonText, confirmationActionListener, contentPanel,
                         "gapx 0 30, flowx,cell 0 2,alignx center,aligny bottom");
        new UpdateButton(cancellationButtonText, cancellationActionListener, contentPanel, "cell 0 2,alignx center");

        JLabel spacer = new JLabel(" ");
        contentPanel.add(spacer, "cell 0 3");
    }

    public void finalizeDialog(Component parent) {
        super.finalize(parent);
    }
}

