package com.banalytics.box.module.whatsapp.handlers;

import com.banalytics.box.module.whatsapp.WhatsAppBotThing;
import it.auties.whatsapp.model.message.standard.TextMessage;

public class WhoIsHereHandler extends AbstractCommandHandler {
    public final static String COMMAND_WHO_IS_HERE = "/wih";

    public WhoIsHereHandler(WhatsAppBotThing bot) {
        super(bot);
    }

    @Override
    public boolean isAuthRequired() {
        return false;
    }

    @Override
    public String getCommand() {
        return COMMAND_WHO_IS_HERE;
    }

    @Override
    public void handle(String chatId) {
        var message = TextMessage.builder() // Create a new text message
                .text(bot.getConfiguration().alias)
                .title("Bot here")
                .description("Bot here")
                .build();
        bot.sendMessage(chatId, message);
    }
}
