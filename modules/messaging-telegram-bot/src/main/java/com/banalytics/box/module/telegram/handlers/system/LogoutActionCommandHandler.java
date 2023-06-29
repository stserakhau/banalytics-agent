package com.banalytics.box.module.telegram.handlers.system;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.telegram.TelegramBotThing;
import com.banalytics.box.module.telegram.handlers.AbstractCommandHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

import static com.banalytics.box.module.telegram.handlers.HomeCommandHandler.homeMenu;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
public class LogoutActionCommandHandler extends AbstractCommandHandler {
    public final static String COMMAND_LOGOUT_ACTION = "/logout";
    final BoxEngine engine;

    final TelegramBotThing.BotConfig botConfig;

    long commandAvailableTime;

    public LogoutActionCommandHandler(TelegramBot bot, BoxEngine engine, TelegramBotThing.BotConfig botConfig) {
        super(bot);
        this.engine = engine;
        this.botConfig = botConfig;
        commandAvailableTime = System.currentTimeMillis() + 60000;
    }

    @Override
    public String getCommand() {
        return COMMAND_LOGOUT_ACTION;
    }


    @Override
    public void handle(long chatId) {
        if (System.currentTimeMillis() < commandAvailableTime) {
            return;
        }
        botConfig.logoutChat(chatId);
    }

    @Override
    public void handleArgs(long chatId, String... args) {
    }
}
