package com.banalytics.box.module.telegram.handlers;

import com.banalytics.box.module.telegram.TelegramBotConfiguration;
import com.banalytics.box.module.telegram.TelegramBotThing;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public class AuthorizeCommandHandler extends AbstractCommandHandler {
    public static final String COMMAND_AUTHORIZE = "/Login";

    private final TelegramBotThing.BotConfig botConfig;
    private final TelegramBotConfiguration configuration;

    public AuthorizeCommandHandler(TelegramBot bot, TelegramBotThing.BotConfig botConfig, TelegramBotConfiguration configuration) {
        super(bot);
        this.botConfig = botConfig;
        this.configuration = configuration;
    }

    @Override
    public void handle(long chatId) {
        SendMessage invalidCmdMsg = new SendMessage(chatId, "Input authorization code");
        bot.execute(invalidCmdMsg);
    }

    @Override
    public void handleArgs(long chatId, String... args) {
        if (args.length == 0) {
            return;
        }
        if (configuration.pinCode.equals(args[0])) {
            botConfig.authorizeChat(chatId, args[1]);
            botConfig.fireUpdate();
            bot.execute(HomeCommandHandler.homeMenu(chatId, "Authorization success"));
        }
    }

    @Override
    public String getCommand() {
        return COMMAND_AUTHORIZE;
    }
}
