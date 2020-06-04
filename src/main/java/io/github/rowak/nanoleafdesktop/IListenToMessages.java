package io.github.rowak.nanoleafdesktop;

import io.github.rowak.nanoleafdesktop.ui.dialog.IDeliverMessages;
import io.github.rowak.nanoleafdesktop.ui.dialog.UpdateOptionDialog;

public interface IListenToMessages {
    void render(UpdateOptionDialog updateDialog);

    void createDialog(IDeliverMessages message);
}
