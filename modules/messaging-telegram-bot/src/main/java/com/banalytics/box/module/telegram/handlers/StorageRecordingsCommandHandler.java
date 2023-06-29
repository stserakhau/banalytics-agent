package com.banalytics.box.module.telegram.handlers;

import com.banalytics.box.module.*;
import com.banalytics.box.module.standard.FileStorage;
import com.banalytics.box.module.telegram.TelegramBotThing;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.List;

import static com.banalytics.box.module.telegram.handlers.Utils.subscriptionCode;

public class StorageRecordingsCommandHandler extends AbstractCommandHandler {
    public final static String COMMAND_STORAGE_EVENT = "/Storage Recordings";

    private final BoxEngine engine;

    private final TelegramBotThing.BotConfig botConfig;

    public StorageRecordingsCommandHandler(TelegramBot bot, TelegramBotThing.BotConfig botConfig, BoxEngine engine) {
        super(bot);
        this.botConfig = botConfig;
        this.engine = engine;
    }

    @Override
    public String getCommand() {
        return COMMAND_STORAGE_EVENT;
    }

    @Override
    public void handle(long chatId) {
        List<Thing<?>> things = engine.findThingsByStandard(FileStorage.class);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(HomeCommandHandler.COMMAND_HOME);
        List<String> keyboardTitles = new ArrayList<>();
        for (Thing<?> t : things) {
            if (t.getSubscribers().isEmpty()) {//if no related tasks with thing - skip
                continue;
            }

            for (InitShutdownSupport subscriber : t.getSubscribers()) {
                if (subscriber instanceof FileStorageSupport && subscriber instanceof MediaCaptureCallbackSupport mcb) {
                    String target = subscriptionCode(t.getUuid(), chatId);
                    boolean subscribed = false;//mcb.hasTargetCallback(subscription, target);

                    String title = buildTitle(t, mcb);

                    String keyboardTitle = (subscribed ? "[-] " : "[+] ") + title;

                    keyboardTitles.add(keyboardTitle);
                }
            }
        }
        keyboardTitles.sort(String::compareToIgnoreCase);

        for (String keybTitle : keyboardTitles) {
            keyboard.addRow(new KeyboardButton(keybTitle));
        }

        SendMessage message = new SendMessage(chatId, COMMAND_STORAGE_EVENT)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(keyboard);

        bot.execute(message);
    }

    private String buildTitle(Thing<?> t, MediaCaptureCallbackSupport subscriber) {
        StringBuilder title = new StringBuilder();
        Thing<?> sourceThing = subscriber.getSourceThing();
        if (sourceThing != null) {
            title.append(sourceThing.getTitle()).append(" / ");
        }
        title.append(t.getTitle());
        return title.toString();
    }

    @Override
    public void handleArgs(long chatId, String... args) {
        if (args.length == 0) {
            return;
        }
        List<Thing<?>> storages = engine.findThingsByStandard(FileStorage.class);
//        for (Thing<?> thing : storages) {
//            if (StringUtils.isEmpty(args[0])) {
//                bot.execute(HomeCommandHandler.homeMenu(chatId, "Empty argument. Going to Home."));
//            }
//            for (InitShutdownSupport subscriber : thing.getSubscribers()) {
//                if (subscriber instanceof FileStorageSupport && subscriber instanceof MediaCaptureCallbackSupport mcb) {//attach to all who uses file storage and can produce media
//                    String title = buildTitle(thing, mcb);
//                    if (args[0].endsWith(title)) {
//                        String target = subscriptionCode(thing.getUuid(), chatId);
//                        boolean subscribed = mcb.hasTargetCallback(subscription, target);
//                        if (subscribed) {
//                            botConfig.chatUnSubscribeMediaRecorder(chatId, subscriber.getUuid());
//                            mcb.removeCallback(subscription, target);
//                        } else {
//                            botConfig.chatSubscribeMediaRecorder(chatId, subscriber.getUuid());
//                            mcb.addCallback(
//                                    subscription,
//                                    new MediaCaptureCallbackSupport.Callback() {
//                                        @Override
//                                        public void callback(MediaCaptureCallbackSupport.MediaResult mediaResult) {
//                                            sendMediaResult(title, chatId, mediaResult);
//                                        }
//
//                                        @Override
//                                        public Object target() {
//                                            return target;
//                                        }
//
//                                        @Override
//                                        public MediaFormat acceptableFormat() {
//                                            return MediaFormat.mp4;
//                                        }
//                                    }
//                            );
//                        }
//                    }
//                }
//            }
//        }
        handle(chatId);
    }
}
