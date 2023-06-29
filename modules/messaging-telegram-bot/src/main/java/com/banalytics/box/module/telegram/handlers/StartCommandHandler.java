package com.banalytics.box.module.telegram.handlers;

import com.pengrad.telegrambot.TelegramBot;

public class StartCommandHandler extends HomeCommandHandler {
    public static final String COMMAND_START = "/start";

    public StartCommandHandler(TelegramBot bot) {
        super(bot);
    }

    @Override
    public String getCommand() {
        return COMMAND_START;
    }

}
