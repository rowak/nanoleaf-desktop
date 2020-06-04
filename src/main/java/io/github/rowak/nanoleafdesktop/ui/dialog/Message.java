package io.github.rowak.nanoleafdesktop.ui.dialog;

public class Message implements IDeliverMessages {
    protected final String message;
    protected final MessageType messageType;

    public Message(String message, MessageType messageType) {
        this.message = message;
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }
}
