package com.banalytics.box.module.telegram.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;

import static com.banalytics.box.module.telegram.handlers.VideoShotAllCommandHandler.COMMAND_VIDEO_SHOT_ALL;

public class QuickActionCommandHandler extends AbstractCommandHandler {
    public final static String COMMAND_QUICK_ACTIONS = "/Quick Actions";

    public QuickActionCommandHandler(TelegramBot bot) {
        super(bot);
    }

    @Override
    public String getCommand() {
        return COMMAND_QUICK_ACTIONS;
    }

    @Override
    public void handle(long chatId) {
        SendMessage message = new SendMessage(chatId, COMMAND_QUICK_ACTIONS)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(
                        new ReplyKeyboardMarkup(new KeyboardButton(HomeCommandHandler.COMMAND_HOME))
                                .addRow(new KeyboardButton(COMMAND_VIDEO_SHOT_ALL))
                );
        bot.execute(message);
    }
}
