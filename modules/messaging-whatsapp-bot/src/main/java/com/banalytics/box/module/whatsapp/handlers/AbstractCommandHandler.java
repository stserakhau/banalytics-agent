package com.banalytics.box.module.whatsapp.handlers;

import com.banalytics.box.module.MediaCaptureCallbackSupport;
import com.banalytics.box.module.whatsapp.WhatsAppBotThing;
import it.auties.whatsapp.model.message.standard.DocumentMessage;
import it.auties.whatsapp.model.message.standard.ImageMessage;
import it.auties.whatsapp.model.message.standard.VideoMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static com.banalytics.box.module.MediaCaptureCallbackSupport.MediaResult.MediaType.*;

@Slf4j
public abstract class AbstractCommandHandler implements CommandHandler {
    protected final WhatsAppBotThing bot;

    public AbstractCommandHandler(WhatsAppBotThing bot) {
        this.bot = bot;
    }

    @Override
    public void handleArgs(String chatId, String... args) {
//        bot.sendMessage(chatId, "Context doesn't support parameters");
    }

    protected void sendMediaResult(String caption, String chatId, MediaCaptureCallbackSupport.MediaResult mediaResult) {
        if (mediaResult.data != null) {
            if (mediaResult.mediaType == image) {
//                DocumentMessage dm = DocumentMessage.simpleBuilder()
//                        .media(mediaResult.data)
//                        .title(mediaResult.sourceUuid.toString())
//                        .fileName(mediaResult.sourceUuid.toString())
//                        .thumbnail(mediaResult.data)
//                        .build();
//                bot.sendMessage(chatId, dm);

                ImageMessage message = ImageMessage.builder()
                        .caption(caption)
                        .decodedMedia(mediaResult.data)
                        .width(mediaResult.width)
                        .height(mediaResult.height)
                        .mimetype("image/jpeg")
                        .build();

                bot.sendMessage(chatId, message);
            } else if (mediaResult.mediaType == video) {
                VideoMessage.VideoMessageBuilder<?, ?> messageBuilder = VideoMessage.builder()
                        .decodedMedia(mediaResult.data)
                        .caption(caption)
                        .width(mediaResult.width)
                        .height(mediaResult.height)
                        .duration(mediaResult.duration / 1000)
                        .mimetype("video/mp4");
                VideoMessage message = messageBuilder.build();
                bot.sendMessage(chatId, message);
            } else if (mediaResult.mediaType == audio) {
//                SendAudio videoMsg = new SendAudio(responseChatId, mediaResult.data)
//                        .caption(caption);
//                bot.execute(videoMsg);
            }
        } else if (mediaResult.file != null) {
            if (mediaResult.mediaType == image) {
                try {
                    byte[] data = FileUtils.readFileToByteArray(mediaResult.file);
                    ImageMessage message = ImageMessage.builder()
                            .caption(caption)
                            .decodedMedia(data)
                            .width(mediaResult.width)
                            .height(mediaResult.height)
                            .mimetype("image/jpeg")
                            .build();
                    bot.sendMessage(chatId, message);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            } else if (mediaResult.mediaType == video) {
                try {
                    File videoFile = mediaResult.file;
                    File thumbFile = new File(videoFile.getParent(), videoFile.getName() + ".jpg");
                    byte[] data = FileUtils.readFileToByteArray(videoFile);
                    byte[] thumbnail = null;
                    if (thumbFile.exists()) {
                        thumbnail = FileUtils.readFileToByteArray(mediaResult.file);
                    }

                    VideoMessage.VideoMessageBuilder<?, ?> messageBuilder = VideoMessage.builder()
                            .decodedMedia(data)
                            .caption(mediaResult.sourceUuid.toString())
                            .width(mediaResult.width)
                            .height(mediaResult.height)
                            .duration(mediaResult.duration / 1000)
                            .mimetype("video/mp4");
                    if (thumbnail != null) {
                        messageBuilder.thumbnail(thumbnail);
                    }

                    VideoMessage message = messageBuilder.build();

                    bot.sendMessage(chatId, message);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            } else if (mediaResult.mediaType == audio) {
//                SendAudio videoMsg = new SendAudio(responseChatId, mediaResult.file);
//                videoMsg.caption(caption);
//                bot.execute(videoMsg);
            }
        }
    }
}
