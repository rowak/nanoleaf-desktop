package io.github.rowak.nanoleafdesktop.ui.dialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CancellationActionListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        OptionDialog dialog = (OptionDialog) btn.getFocusCycleRootAncestor();
        dialog.dispose();
    }
}