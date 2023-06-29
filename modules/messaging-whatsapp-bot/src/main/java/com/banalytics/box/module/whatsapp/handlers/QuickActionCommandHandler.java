package com.banalytics.box.module.whatsapp.handlers;

import com.banalytics.box.module.whatsapp.WhatsAppBotThing;
import it.auties.whatsapp.model.button.base.Button;
import it.auties.whatsapp.model.button.base.ButtonBody;
import it.auties.whatsapp.model.button.base.ButtonText;
import it.auties.whatsapp.model.message.button.ButtonsMessage;

import java.util.List;

import static com.banalytics.box.module.whatsapp.handlers.HomeCommandHandler.COMMAND_HOME;
import static com.banalytics.box.module.whatsapp.handlers.VideoShotAllCommandHandler.COMMAND_VIDEO_SHOT_ALL;

public class QuickActionCommandHandler extends AbstractCommandHandler {
    public final static String COMMAND_QUICK_ACTIONS = "/quickActions";

    public static final ButtonsMessage BUTTONS = ButtonsMessage.simpleBuilder()
            .body("Choose action")
            .buttons(List.of(
                    Button.of(COMMAND_HOME, ButtonText.of("Home")),
                    Button.of(COMMAND_VIDEO_SHOT_ALL, ButtonText.of("Video shot all"))
            ))
            .build();

    public QuickActionCommandHandler(WhatsAppBotThing bot) {
        super(bot);
    }

    @Override
    public String getCommand() {
        return COMMAND_QUICK_ACTIONS;
    }

    @Override
    public void handle(String chatId) {
        bot.sendMessage(chatId, BUTTONS);
    }
}
