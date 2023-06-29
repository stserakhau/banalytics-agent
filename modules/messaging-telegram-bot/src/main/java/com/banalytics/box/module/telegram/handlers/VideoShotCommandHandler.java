package com.banalytics.box.module.telegram.handlers;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.InitShutdownSupport;
import com.banalytics.box.module.MediaCaptureCallbackSupport;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.standard.LocalMediaStream;
import com.banalytics.box.module.standard.Onvif;
import com.banalytics.box.module.standard.UrlMediaStream;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChat;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VideoShotCommandHandler extends AbstractCommandHandler {
    public static final String COMMAND_VIDEO_SHOT = "/Video Shot";
    final BoxEngine engine;

    public VideoShotCommandHandler(TelegramBot bot, BoxEngine engine) {
        super(bot);
        this.engine = engine;
    }

    @Override
    public String getCommand() {
        return COMMAND_VIDEO_SHOT;
    }

    @Override
    public void handle(long chatId) {
        List<Thing<?>> things = engine.findThingsByStandard(Onvif.class, LocalMediaStream.class, UrlMediaStream.class);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(HomeCommandHandler.COMMAND_HOME);
        List<String> keyboardTitles = new ArrayList<>();
        for (int i = 0; i < things.size(); i++) {
            Thing<?> t = things.get(i);
            if (t.getSubscribers().isEmpty()) {//if no related tasks with thing - skip
                continue;
            }
            keyboardTitles.add(t.getTitle());
        }
        keyboardTitles.sort(String::compareToIgnoreCase);

        for (String keybTitle : keyboardTitles) {
            keyboard.addRow(new KeyboardButton(keybTitle));
        }

        SendMessage message = new SendMessage(chatId, "Choose Camera")
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(keyboard);

        bot.execute(message);
    }

    @Override
    public void handleArgs(long chatId, String... args) {
        if (args.length == 0) {
            return;
        }
        List<Thing<?>> things = engine.findThingsByStandard(Onvif.class, LocalMediaStream.class, UrlMediaStream.class);
        for (Thing<?> thing : things) {
            if (thing.getTitle().equals(args[0])) {
                for (InitShutdownSupport subscriber : thing.getSubscribers()) {
                    if (subscriber instanceof MediaCaptureCallbackSupport mcb) { // first is always grabber
                        mcb.screenShot(
                                mediaResult -> sendMediaResult(thing.getTitle(), chatId, mediaResult)
                        );
                        return;
                    }
                }
            }
        }
    }
}
