package io.github.rowak.nanoleafdesktop.ui.dialog;

import io.github.rowak.nanoleafdesktop.ui.button.ModernButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class UpdateButton extends ModernButton {

    public UpdateButton(String buttonText, ActionListener actionListener, JPanel contentPanel, String constraints) {
        super(buttonText);

        setFont(new Font("Tahoma", Font.PLAIN, 18));
        addActionListener(actionListener);
        contentPanel.add(this, constraints);
    }
}