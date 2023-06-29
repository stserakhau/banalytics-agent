package com.banalytics.box.module.telegram.handlers.system;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.telegram.handlers.AbstractCommandHandler;
import com.banalytics.box.module.telegram.handlers.HomeCommandHandler;
import com.banalytics.box.service.SystemThreadsService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChat;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;

import static com.banalytics.box.module.telegram.handlers.HomeCommandHandler.homeMenu;
import static com.banalytics.box.module.telegram.handlers.VideoShotAllCommandHandler.COMMAND_VIDEO_SHOT_ALL;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
public class RebootActionCommandHandler extends AbstractCommandHandler {
    public final static String COMMAND_REBOOT_ACTION = "/reboot";
    final BoxEngine engine;

    long commandAvailableTime;

    public RebootActionCommandHandler(TelegramBot bot, BoxEngine engine) {
        super(bot);
        this.engine = engine;
        commandAvailableTime = System.currentTimeMillis() + 60000;
    }

    @Override
    public String getCommand() {
        return COMMAND_REBOOT_ACTION;
    }


    @Override
    public void handle(long chatId) {
        if (System.currentTimeMillis() < commandAvailableTime) {
            return;
        }

        bot.execute(new SendMessage(chatId, "To reboot server type 'reboot' and send"));
    }

    @Override
    public void handleArgs(long chatId, String... args) {
        if (args.length == 0) {
            return;
        }
        log.info("Reboot arg: '{}'", args[0]);
        if("reboot".equals(args[0])) {
            bot.execute(homeMenu(chatId, "Server rebooting"));
            log.info("Reboot initiated via telegram");
            SystemThreadsService.reboot();
        }
    }
}
