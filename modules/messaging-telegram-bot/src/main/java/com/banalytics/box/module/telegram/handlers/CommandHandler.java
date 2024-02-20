package com.banalytics.box.module.telegram.handlers;

import com.pengrad.telegrambot.model.Location;

public interface CommandHandler {
    void handle(long chatId);

    void handleArgs(long chatId, String... args);

    String getCommand();

    default void handlePhotoArgs(long chatId, String fileUri) {
        throw new UnsupportedOperationException();
    }

    default void handleVideoArg(long chatId, String fileUri) {
        throw new UnsupportedOperationException();
    }

    default void handleAudioArg(long chatId, String fileUri) {
        throw new UnsupportedOperationException();
    }

    default void handleDocumentArg(long chatId, String fileUri) {
        throw new UnsupportedOperationException();
    }

    default void handleLocationArgs(long chatId, Location location) {
        throw new UnsupportedOperationException();
    }
}
