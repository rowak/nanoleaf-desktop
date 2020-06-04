package io.github.rowak.nanoleafdesktop.ui.dialog;

public class ErrorMessage extends Message {
    public static final MessageType ERROR = MessageType.ERROR;

    public ErrorMessage(String message) {
        super(message, ERROR);
    }

}
