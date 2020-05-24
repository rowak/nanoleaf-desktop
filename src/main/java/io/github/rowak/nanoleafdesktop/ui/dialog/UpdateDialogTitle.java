package io.github.rowak.nanoleafdesktop.ui.dialog;

import javax.swing.*;
import java.awt.*;

public class UpdateDialogTitle extends JLabel {

    public UpdateDialogTitle(String title, JPanel contentPanel) {
        super(title);

        setFont(UpdateOptionDialog.FONT);
        setForeground(Color.WHITE);
        contentPanel.add(this, "gapx 15 0, cell 0 1,alignx center,aligny bottom");
    }
}