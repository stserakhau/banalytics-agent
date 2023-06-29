package com.banalytics.box.module.telegram.handlers;

import com.banalytics.box.module.MediaCaptureCallbackSupport;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendAudio;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;

import static com.banalytics.box.module.MediaCaptureCallbackSupport.MediaResult.MediaType.*;

public abstract class AbstractCommandHandler implements CommandHandler {
    protected final TelegramBot bot;

    public AbstractCommandHandler(TelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void handleArgs(long chatId, String... args) {
        SendMessage message = new SendMessage(chatId, "Context doesn't support parameters");
        bot.execute(message);
    }

    protected void sendMediaResult(String caption, long responseChatId, MediaCaptureCallbackSupport.MediaResult mediaResult) {
        if (mediaResult.data != null) {
            if (mediaResult.mediaType == image) {
                SendPhoto videoMsg = new SendPhoto(responseChatId, mediaResult.data);
                videoMsg.caption(caption);
                bot.execute(videoMsg);
            } else if (mediaResult.mediaType == video) {
                SendVideo videoMsg = new SendVideo(responseChatId, mediaResult.data);
                videoMsg.caption(caption);
                bot.execute(videoMsg);
            } else if (mediaResult.mediaType == audio) {
                SendAudio videoMsg = new SendAudio(responseChatId, mediaResult.data)
                        .caption(caption);
                bot.execute(videoMsg);
            }
        } else if (mediaResult.file != null) {
            if (mediaResult.mediaType == image) {
                SendPhoto videoMsg = new SendPhoto(responseChatId, mediaResult.file);
                videoMsg.caption(caption);
                bot.execute(videoMsg);
            } else if (mediaResult.mediaType == video) {
                SendVideo videoMsg = new SendVideo(responseChatId, mediaResult.file);
                videoMsg.caption(caption);
                bot.execute(videoMsg);
            } else if (mediaResult.mediaType == audio) {
                SendAudio videoMsg = new SendAudio(responseChatId, mediaResult.file);
                videoMsg.caption(caption);
                bot.execute(videoMsg);
            }
        }
    }
}
