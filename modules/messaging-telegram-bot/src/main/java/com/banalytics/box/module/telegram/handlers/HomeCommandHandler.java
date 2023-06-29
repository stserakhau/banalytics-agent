package com.banalytics.box.module.telegram.handlers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.MenuButtonDefault;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChat;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetChatMenuButton;
import com.pengrad.telegrambot.request.SetMyCommands;

import static com.banalytics.box.module.telegram.handlers.BrowseFileStorageCommandHandler.COMMAND_BROWSE_FILE_STORAGES;
import static com.banalytics.box.module.telegram.handlers.QuickActionCommandHandler.COMMAND_QUICK_ACTIONS;
import static com.banalytics.box.module.telegram.handlers.VideoShotCommandHandler.COMMAND_VIDEO_SHOT;
import static com.banalytics.box.module.telegram.handlers.system.LogoutActionCommandHandler.COMMAND_LOGOUT_ACTION;
import static com.banalytics.box.module.telegram.handlers.system.RebootActionCommandHandler.COMMAND_REBOOT_ACTION;
import static com.banalytics.box.module.telegram.handlers.system.ReloadFirmwareActionCommandHandler.COMMAND_RELOAD_FIRMWARE_ACTION;

public class HomeCommandHandler extends AbstractCommandHandler {
    public static final String COMMAND_HOME = "/home";

    public HomeCommandHandler(TelegramBot bot) {
        super(bot);
    }

    @Override
    public String getCommand() {
        return COMMAND_HOME;
    }

    @Override
    public void handle(long chatId) {
        bot.execute(homeMenu(chatId, COMMAND_HOME));

        bot.execute(new SetChatMenuButton().chatId(chatId).menuButton(new MenuButtonDefault()));
        bot.execute(
                new SetMyCommands(
                        new BotCommand(COMMAND_HOME, "Home"),
                        new BotCommand(COMMAND_REBOOT_ACTION, "Reboot server"),
                        new BotCommand(COMMAND_RELOAD_FIRMWARE_ACTION, "Reload firmware"),
                        new BotCommand(COMMAND_LOGOUT_ACTION, "Logout")
                ).scope(new BotCommandsScopeChat(chatId))
        );
    }

    public static SendMessage homeMenu(long chatId, String message) {
        return new SendMessage(chatId, message)
                .parseMode(ParseMode.Markdown)
                .disableWebPagePreview(false)
                .replyMarkup(
                        new ReplyKeyboardMarkup(
                                new KeyboardButton(COMMAND_HOME),
                                new KeyboardButton(COMMAND_QUICK_ACTIONS)
                        ).addRow(
                                new KeyboardButton(COMMAND_VIDEO_SHOT),
                                new KeyboardButton(COMMAND_BROWSE_FILE_STORAGES)
                        )
                );
    }
}
