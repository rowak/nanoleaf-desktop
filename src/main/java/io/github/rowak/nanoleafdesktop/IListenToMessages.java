package io.github.rowak.nanoleafdesktop;

import io.github.rowak.nanoleafdesktop.ui.dialog.UpdateOptionDialog;

public interface IListenToMessages {
    void render(UpdateOptionDialog updateDialog);

    void createDialog(String message, boolean hasError);
}
