package com.banalytics.box.module.whatsapp.handlers;

import com.banalytics.box.module.whatsapp.WhatsAppBotThing;
import it.auties.whatsapp.model.button.base.Button;
import it.auties.whatsapp.model.button.base.ButtonBody;
import it.auties.whatsapp.model.button.base.ButtonText;
import it.auties.whatsapp.model.message.button.ButtonsMessage;
import it.auties.whatsapp.model.message.standard.LiveLocationMessage;
import it.auties.whatsapp.model.message.standard.LocationMessage;
import it.auties.whatsapp.model.message.standard.TextMessage;

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
//        bot.sendMessage(chatId, BUTTONS);

//        bot.sendMessage(chatId, "Test 1");
//        {
//            var message = TextMessage.builder() // Create a new text message
//                    .text("Check this video out: https://www.youtube.com/watch?v=dQw4w9WgXcQ") // Set the text of the message
//                    .canonicalUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ") // Set the url of the message
//                    .matchedText("https://www.youtube.com/watch?v=dQw4w9WgXcQ") // Set the matched text for the url in the message
//                    .title("A nice suprise") // Set the title of the url
//                    .description("Check me out") // Set the description of the url
//                    .build();
//            bot.sendMessage(chatId, message);
//        }
//        {
//            var location = LocationMessage.builder() // Create a new location message
//                    .caption("Look at this!") // Set the caption of the message, that is the text below the file
//                    .latitude(38.9193) // Set the longitude of the location to share
//                    .longitude(1183.1389) // Set the latitude of the location to share
//                    .build(); // Create the message
//            bot.sendMessage(chatId, location);
//        }

//        {
//            var location = LiveLocationMessage.builder() // Create a new live location message
//                    .caption("Look at this!") // Set the caption of the message, that is the text below the file. Not available if this message is live
//                    .latitude(38.9193) // Set the longitude of the location to share
//                    .longitude(1183.1389) // Set the latitude of the location to share
//                    .accuracy(10) // Set the accuracy of the location in meters
//                    .speed(12) // Set the speed of the device sharing the location in meter per endTimeStamp
//                    .build(); // Create the message
//            bot.sendMessage(chatId, location);
//        }

//        {
//            var button = Button.of(ButtonText.of("A nice button!")); // Create a button
//            var anotherButton = Button.of(ButtonText.of("Another button :)")); // Create another button with different text
//
//            {
//                var buttons = ButtonsMessage.simpleBuilder() // Create a new button message builder
//                        .body("A nice body") // Set the body
//                        .footer("A nice footer") // Set the footer
//                        .buttons(List.of(button, anotherButton)) // Set the buttons
//                        .build(); // Create the message
//                bot.sendMessage(chatId, buttons);
//            }
//            {
//                var buttons = ButtonsMessage.simpleBuilder() // Create a new button message builder
//                        .header(TextMessage.of("A nice header :)")) // Set the header
//                        .body("A nice body") // Set the body
//                        .footer("A nice footer") // Set the footer
//                        .buttons(List.of(button, anotherButton)) // Set the buttons
//                        .build(); // Create the message
//                bot.sendMessage(chatId, buttons);
//            }
//            {
//                var buttons = ButtonsMessage.simpleBuilder() // Create a new button message builder
//                        .header(TextMessage.of("Text message")) // Set the header
//                        .body("A nice body") // Set the body
//                        .footer("A nice footer") // Set the footer
//                        .buttons(List.of(button, anotherButton)) // Set the buttons
//                        .build(); // Create the message
//                bot.sendMessage(chatId, buttons);
//            }
//        }

    }
}
