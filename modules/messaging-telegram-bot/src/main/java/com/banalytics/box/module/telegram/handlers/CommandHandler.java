package com.banalytics.box.module.telegram.handlers;

public interface CommandHandler {
    void handle(long chatId);

    void handleArgs(long chatId, String... args);

    String getCommand();
}
