package com.banalytics.box.module.telegram.handlers.system;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.telegram.TelegramBotThing;
import com.banalytics.box.module.telegram.handlers.AbstractCommandHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChat;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.extern.slf4j.Slf4j;

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
        bot.execute(new SetMyCommands().scope(new BotCommandsScopeChat(chatId)));//clear menu

        bot.execute(new SendMessage(chatId, "Bye")
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(new ReplyKeyboardMarkup(new KeyboardButton("/start"))));

        botConfig.logoutChat(chatId);
    }

    @Override
    public void handleArgs(long chatId, String... args) {
    }
}
