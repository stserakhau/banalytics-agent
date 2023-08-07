package com.banalytics.box.module.whatsapp.handlers;

import com.banalytics.box.module.whatsapp.WhatsAppBotThing;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogoutActionCommandHandler extends AbstractCommandHandler {
    public final static String COMMAND_LOGOUT_ACTION = "/logout";

    long commandAvailableTime;

    public LogoutActionCommandHandler(WhatsAppBotThing thing) {
        super(thing);
        commandAvailableTime = System.currentTimeMillis() + 60000;
    }

    @Override
    public String getCommand() {
        return COMMAND_LOGOUT_ACTION;
    }

    @Override
    public void handle(String chatId) {
        if (System.currentTimeMillis() < commandAvailableTime) {
            return;
        }
        bot.botConfig.logoutChat(chatId);
    }
}
