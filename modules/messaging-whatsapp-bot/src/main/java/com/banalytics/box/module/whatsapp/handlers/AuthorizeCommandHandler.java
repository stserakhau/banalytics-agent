package com.banalytics.box.module.whatsapp.handlers;

import com.banalytics.box.module.whatsapp.WhatsAppBotThing;
import org.apache.commons.lang3.StringUtils;

public class AuthorizeCommandHandler extends AbstractCommandHandler {
    public static final String COMMAND_AUTHORIZE = "/login";

    public AuthorizeCommandHandler(WhatsAppBotThing bot) {
        super(bot);
    }

    @Override
    public void handle(String chatId) {
        bot.sendMessage(chatId, "Input authorization code");
    }

    @Override
    public void handleArgs(String chatId, String... args) {
        if (args.length == 0) {
            return;
        }
        boolean pinSuccess = StringUtils.isEmpty(bot.getConfiguration().pinCode) || bot.getConfiguration().pinCode.equals(args[0]);
        if (pinSuccess) {
            bot.botConfig.authorizeChat(chatId, chatId);
            bot.botConfig.fireUpdate();
            bot.sendMessage(chatId, "Chat authorization success");
        }
    }

    @Override
    public String getCommand() {
        return COMMAND_AUTHORIZE;
    }
}
