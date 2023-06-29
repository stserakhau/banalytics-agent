package com.banalytics.box.module.telegram.handlers;

import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ITask;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.standard.FileStorage;
import com.banalytics.box.module.storage.FileVO;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVideo;

import java.io.File;
import java.util.*;

import static com.banalytics.box.module.telegram.handlers.HomeCommandHandler.COMMAND_HOME;

public class BrowseFileStorageCommandHandler extends AbstractCommandHandler {
    public final static String COMMAND_BROWSE_FILE_STORAGES = "/Browse File Storages";

    private final BoxEngine engine;

    public BrowseFileStorageCommandHandler(TelegramBot bot, BoxEngine engine) {
        super(bot);
        this.engine = engine;
    }

    @Override
    public String getCommand() {
        return COMMAND_BROWSE_FILE_STORAGES;
    }

    @Override
    public void handle(long chatId) {
        chatSelectedStorage.remove(chatId);
        chatPathContext.remove(chatId);
        List<Thing<?>> things = engine.findThingsByStandard(FileStorage.class);

        int colsPerRow = 2;

        ReplyKeyboardMarkup keyb = new ReplyKeyboardMarkup(new KeyboardButton(COMMAND_HOME));
        List<KeyboardButton> row = new ArrayList<>();
        for (Thing<?> fsThing : things) {
            if (row.size() == colsPerRow) {
                keyb.addRow(row.toArray(new KeyboardButton[0]));
                row.clear();
            }
            row.add(new KeyboardButton(fsThing.getTitle()));
        }
        if (!row.isEmpty()) {
            keyb.addRow(row.toArray(new KeyboardButton[0]));
        }

        bot.execute(new SendMessage(chatId, COMMAND_BROWSE_FILE_STORAGES)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(keyb));
    }

    Map<Long, FileStorage> chatSelectedStorage = new HashMap<>();
    Map<Long, LinkedList<String>> chatPathContext = new HashMap<>();

    @Override
    public void handleArgs(long chatId, String... args) {
        String pathPart;
        if (!chatSelectedStorage.containsKey(chatId)) {
            String storageTitle = args[0];
            List<Thing<?>> things = engine.findThingsByStandard(FileStorage.class);
            FileStorage fileStorage = null;
            for (Thing<?> thing : things) {
                if (thing.getTitle().equals(storageTitle)) {
                    fileStorage = (FileStorage) thing;
                    break;
                }
            }
            pathPart = "";
            if (fileStorage != null) {
                chatSelectedStorage.put(chatId, fileStorage);
            } else {
                bot.execute(new SendMessage(chatId, storageTitle + " unavailable"));
                return;
            }
        } else {
            pathPart = args[0];
        }

        if(!pathPart.isEmpty()){
            String[] parts = pathPart.split("_");
            if(parts.length>1){
                pathPart = parts[1];
            }
        }

        LinkedList<String> path = chatPathContext.computeIfAbsent(chatId, k -> new LinkedList<>());

        FileStorage fileStorage = chatSelectedStorage.get(chatId);

        try {
            String contextPath;
            boolean goToParentForlder = "[..]".equals(pathPart);
            if (goToParentForlder) {
                path.removeLast();
                contextPath = String.join("/", path);
            } else {
                contextPath = String.join("/", path) + "/" + pathPart;
            }
            boolean isFile = contextPath.lastIndexOf('.') >= 4 && (contextPath.length() - contextPath.lastIndexOf('.') < 5);
            if (isFile) {
                File file = fileStorage.file(contextPath);
                boolean isVideo = contextPath.endsWith("mp4");
                if(isVideo) {
                    bot.execute(
                            new SendVideo(chatId, file)
                            .caption(contextPath)
                    );
                } else {
                    bot.execute(
                            new SendDocument(chatId, file)
                                    .caption(contextPath)
                    );
                }
            } else {
                if (!goToParentForlder) {
                    path.add(pathPart);
                }
                contextPath = String.join("/", path);
                int colsPerRow = 4;

                ReplyKeyboardMarkup keyb;
                if (path.size() > 1) {
                    keyb = new ReplyKeyboardMarkup(new KeyboardButton(COMMAND_HOME), new KeyboardButton("[..]"));
                } else {
                    keyb = new ReplyKeyboardMarkup(new KeyboardButton(COMMAND_HOME), new KeyboardButton(COMMAND_BROWSE_FILE_STORAGES));
                }
                List<KeyboardButton> row = new ArrayList<>();
                for (FileVO file : fileStorage.list(contextPath)) {
                    if (row.size() == colsPerRow) {
                        keyb.addRow(row.toArray(new KeyboardButton[0]));
                        row.clear();
                    }
                    String fileName = file.getName();
                    try {
                        UUID thingUUID = UUID.fromString(fileName); //if uuid detected
                        ITask<?> t = engine.findTask(thingUUID);    //try to get thing
                        if (t != null) {                            //if thing exists
                            Thing<?> th = t.getSourceThing();
                            if (th != null) {
                                fileName = th.getTitle() + "_" + fileName;                //replace path part to title
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        //if file name is not uuid, using the original value
                    }
                    row.add(new KeyboardButton(fileName));
                }
                if (!row.isEmpty()) {
                    keyb.addRow(row.toArray(new KeyboardButton[0]));
                }

                bot.execute(new SendMessage(chatId, contextPath.length() == 0 ? "/" : contextPath)
                        .parseMode(ParseMode.Markdown)
                        .disableWebPagePreview(false)
                        .replyMarkup(keyb));
            }
        } catch (Exception e) {
            bot.execute(new SendMessage(chatId, "Can't read storage content. Cause: " + e.getMessage()));
        }
    }
}
