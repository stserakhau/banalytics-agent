package com.banalytics.box.module.whatsapp.handlers;

import com.banalytics.box.module.whatsapp.WhatsAppBotThing;
import it.auties.whatsapp.model.button.base.Button;
import it.auties.whatsapp.model.button.base.ButtonText;
import it.auties.whatsapp.model.message.button.ButtonsMessage;

import java.util.List;

import static com.banalytics.box.module.whatsapp.handlers.QuickActionCommandHandler.COMMAND_QUICK_ACTIONS;

public class HomeCommandHandler extends AbstractCommandHandler {
    public static final String COMMAND_HOME = "/home";

    public HomeCommandHandler(WhatsAppBotThing bot) {
        super(bot);
    }

    @Override
    public String getCommand() {
        return COMMAND_HOME;
    }

    public static final ButtonsMessage HOME_BUTTONS = ButtonsMessage.builder()
            .body("Choose action")
            .buttons(List.of(
                    Button.of(COMMAND_HOME, ButtonText.of("Home")),
                    Button.of(COMMAND_QUICK_ACTIONS, ButtonText.of("Quick action")),
                    Button.of("/videoShot", ButtonText.of("Video shot"))
            ))
            .build();

    @Override
    public void handle(String chatId) {
        bot.sendMessage(chatId, HOME_BUTTONS);
    }
}
