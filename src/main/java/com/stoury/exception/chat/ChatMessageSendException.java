package com.stoury.exception.chat;

public class ChatMessageSendException extends RuntimeException {
    public ChatMessageSendException(Throwable cause) {
        super("Failed to send chatting message.", cause);
    }
}
