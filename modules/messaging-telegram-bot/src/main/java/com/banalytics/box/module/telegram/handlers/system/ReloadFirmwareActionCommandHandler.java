package com.banalytics.box.module.telegram.handlers.system;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.cloud.portal.suc.SoftwareUpgradeCenterConfiguration;
import com.banalytics.box.module.cloud.portal.suc.SoftwareUpgradeCenterThing;
import com.banalytics.box.module.telegram.handlers.AbstractCommandHandler;
import com.banalytics.box.module.telegram.handlers.HomeCommandHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChat;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

import static com.banalytics.box.module.telegram.handlers.HomeCommandHandler.homeMenu;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
public class ReloadFirmwareActionCommandHandler extends AbstractCommandHandler {
    public final static String COMMAND_RELOAD_FIRMWARE_ACTION = "/reload_firmware";
    final BoxEngine engine;

    public ReloadFirmwareActionCommandHandler(TelegramBot bot, BoxEngine engine) {
        super(bot);
        this.engine = engine;
    }

    @Override
    public String getCommand() {
        return COMMAND_RELOAD_FIRMWARE_ACTION;
    }

    @Override
    public void handle(long chatId) {
        bot.execute(new SendMessage(chatId, "To reload firmware type 'reload' and send"));
    }

    @Override
    public void handleArgs(long chatId, String... args) {
        if (args.length == 0) {
            return;
        }

        if("reload".equals(args[0])) {
            bot.execute(homeMenu(chatId, "Firmware reloading"));
            SoftwareUpgradeCenterThing suc = engine.getThing(SoftwareUpgradeCenterConfiguration.THING_UUID);
            suc.initiateSoftwareUpgrade();
        }
    }
}
