import com.banalytics.box.module.telegram.handlers.HomeCommandHandler;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScope;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeDefault;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChat;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChatMember;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetMyCommandsResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.banalytics.box.module.telegram.handlers.system.RebootActionCommandHandler.COMMAND_REBOOT_ACTION;
import static com.banalytics.box.module.telegram.handlers.system.ReloadFirmwareActionCommandHandler.COMMAND_RELOAD_FIRMWARE_ACTION;
import static com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL;

/**
 * https://sendpulse.com/ru/knowledge-base/chatbot/create-telegram-chatbot
 * <p>
 * Done! Congratulations on your new bot. You will find it at t.me/my_x123_bot. You can now add a description, about section and profile picture for your bot, see /help for a list of commands. By the way, when you've finished creating your cool bot, ping our Bot Support if you want a better username for it. Just make sure the bot is fully operational before you do this.
 * <p>
 * Use this token to access the HTTP API:
 * 5302470203:AAGuFYb_T5FAIOyMfOKSNzogwqbfW5Qbfdk
 * Keep your token secure and store it safely, it can be used by anyone to control your bot.
 * <p>
 * For a description of the Bot API, see this page: https://core.telegram.org/bots/api
 */

/*
    https://tdlib.github.io/td/build.html?language=Java
    TDLib
    https://trueconf.ru/webrtc.html

    https://www.baeldung.com/webrtc

    https://ourcodeworld.com/articles/read/1175/how-to-create-and-configure-your-own-stun-turn-server-with-coturn-in-ubuntu-18-04
 */
public class TestTelega {
    public static void main(String[] args) throws Exception {
        TelegramBot bot = new TelegramBot("5476393223:AAEkaBjZ8tdCVSMHeQgcP822OruD4Bk1w9o");

//        bot.execute(new GetMyCommands(), new Callback<GetMyCommands, GetMyCommandsResponse>() {
//            @Override
//            public void onResponse(GetMyCommands request, GetMyCommandsResponse response) {
//                System.out.println();
//            }
//
//            @Override
//            public void onFailure(GetMyCommands request, IOException e) {
//                System.out.println();
//            }
//        });


        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                Message msg = update.message();

                long chatId = msg.chat().id();

//                bot.execute(
//                        new SetMyCommands().scope(new BotCommandsScopeChat(chatId))
//                );

//                bot.execute(new SetChatMenuButton().chatId(chatId).menuButton(new MenuButtonDefault()));
//                bot.execute(
//                        new SetMyCommands(
//                                new BotCommand(COMMAND_REBOOT_ACTION, "Reboot server"),
//                                new BotCommand(COMMAND_RELOAD_FIRMWARE_ACTION, "Reload firmware")
//                        ).scope(new BotCommandsScopeChat(chatId))
//                );

                bot.execute(HomeCommandHandler.homeMenu(chatId, "Test buttons"));

                bot.execute(new SetChatMenuButton().chatId(chatId).menuButton(new MenuButtonDefault()));
//                bot.execute(
////                        new SetChatMenuButton().chatId(chatId).menuButton(new MenuButtonWebApp("External Web app", new WebAppInfo("https://google.com"))),
//                        new SetChatMenuButton().chatId(chatId).menuButton(new MenuButtonDefault()),
//                        new Callback<SetChatMenuButton, BaseResponse>() {
//                            @Override
//                            public void onResponse(SetChatMenuButton request, BaseResponse response) {
//                                System.out.println();
//                            }
//
//                            @Override
//                            public void onFailure(SetChatMenuButton request, IOException e) {
//                                System.out.println();
//                            }
//                        }
//                );

                bot.execute(new SetMyCommands(
                                new BotCommand("/reboot", "Reboot server"),
                                new BotCommand("/reload_firmware", "Reload firmware")
                        ).scope(new BotCommandsScopeChat(chatId)),
                        new Callback<SetMyCommands, BaseResponse>() {
                            @Override
                            public void onResponse(SetMyCommands request, BaseResponse response) {
                                System.out.println();
                            }

                            @Override
                            public void onFailure(SetMyCommands request, IOException e) {
                                System.out.printf("");
                            }
                        }
                );


//                SendMessage message = new SendMessage(chatId, "")
//                        .parseMode(ParseMode.Markdown)
//                        .disableWebPagePreview(false)
//                        .replyMarkup(
//                                new ReplyKeyboardMarkup(
//                                        new KeyboardButton("/Home")
//                                )
//                                        .addRow(new KeyboardButton("/Quick Actions"), new KeyboardButton("/Video Shot"))
//                                        .addRow(new KeyboardButton("/Storage Events"), new KeyboardButton("/Browse File Storage"))
//                        );
//                SendMessage message = new SendMessage(chatId, "Choose action")
//                        .parseMode(ParseMode.Markdown)
//                        .disableWebPagePreview(false)
//                        .replyMarkup(new InlineKeyboardMarkup(
//                                new InlineKeyboardButton("Quick Actions")/*.requestContact(true)*/,
//                                new InlineKeyboardButton("Capture")/*.requestLocation(true)*/,
//                                new InlineKeyboardButton("Motion Notifications"),
//                                new InlineKeyboardButton("File Storage Notifications"),
//                                new InlineKeyboardButton("Browse File Storage")
//                        ));

//                bot.execute(message);


//                SetChatMenuButton menu = new SetChatMenuButton();
//                menu.menuButton(new MenuButtonCommands());
//                menu.chatId(chatId);
//                bot.execute(menu, new Callback<SetChatMenuButton, BaseResponse>() {
//                    @Override
//                    public void onResponse(SetChatMenuButton request, BaseResponse response) {
//                        System.out.println();
//                    }
//
//                    @Override
//                    public void onFailure(SetChatMenuButton request, IOException e) {
//                        System.out.println();
//                    }
//                });


//                bot.execute(new SendMessage(chatId, "Send video"));

//                SendVideo videoMsg = new SendVideo(chatId, new File("E:\\out\\motion\\484e2f23-459b-4577-9ce7-a69a15160b68\\2022-05-30\\05-05-49.mp4"));
//                bot.execute(videoMsg);

//                bot.execute(new SendChatAction(chatId, ChatAction.record_video)); //set chat status
//                bot.execute(new SendPoll(chatId, "Что сделать ?", "Камеры", "Файловые хранилища")); //set chat status
            });
            return CONFIRMED_UPDATES_ALL;
        });
        Thread.sleep(60000);
        System.out.println("1234");

        bot.removeGetUpdatesListener();

        bot.shutdown();
    }
}
