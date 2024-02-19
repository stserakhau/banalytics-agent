package com.banalytics.box.module.telegram.handlers;

import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.events.FileCreatedEvent;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.telegram.TelegramBotThing;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.Map;

import static com.banalytics.box.module.telegram.handlers.HomeCommandHandler.COMMAND_HOME;

public class FileEventHandler extends AbstractCommandHandler {
    public final static String COMMAND_SEND_FILE = "/Send file";

    private final TelegramBotThing thing;
    private final BoxEngine engine;

    public FileEventHandler(TelegramBotThing thing, TelegramBot bot, BoxEngine engine) {
        super(bot);
        this.thing = thing;
        this.engine = engine;
    }

    @Override
    public String getCommand() {
        return COMMAND_SEND_FILE;
    }

    @Override
    public void handle(long chatId) {
        int colsPerRow = 2;

        ReplyKeyboardMarkup keyb = new ReplyKeyboardMarkup(
                new KeyboardButton(COMMAND_HOME),
                new KeyboardButton("Location").requestLocation(true)
        );

        bot.execute(new SendMessage(chatId, COMMAND_SEND_FILE)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(keyb));
    }

    private Location location;

    public void handleFileURIArg(long chatId, String fileUri) {
        FileCreatedEvent fce = new FileCreatedEvent(
                NodeDescriptor.NodeType.THING,
                this.thing.getUuid(),
                this.thing.getSelfClassName(),
                this.thing.getTitle(),
                this.thing.getUuid(),
                fileUri
        );
        if (location != null) {
            Map<String, Object> opts = fce.getOptions();
            opts.put("latitude", location.latitude());
            opts.put("longitude", location.longitude());
        }
        engine.fireEvent(fce);
    }

    @Override
    public void handlePhotoArgs(long chatId, String fileUri) {
        handleFileURIArg(chatId, fileUri);
    }

    @Override
    public void handleVideoArg(long chatId, String fileUri) {
        handleFileURIArg(chatId, fileUri);
    }

    @Override
    public void handleAudioArg(long chatId, String fileUri) {
        handleFileURIArg(chatId, fileUri);
    }

    @Override
    public void handleDocumentArg(long chatId, String fileUri) {
        handleFileURIArg(chatId, fileUri);
    }

    @Override
    public void handleLocationArgs(long chatId, Location location) {
        System.out.println(location);
    }
}
