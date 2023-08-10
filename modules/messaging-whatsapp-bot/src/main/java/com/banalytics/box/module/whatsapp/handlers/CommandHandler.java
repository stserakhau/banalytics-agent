package com.banalytics.box.module.whatsapp.handlers;

public interface CommandHandler {
    void handle(String chatId);

    void handleArgs(String chatId, String... args);

    String getCommand();

    default boolean isAuthRequired() {
        return true;
    }
}
