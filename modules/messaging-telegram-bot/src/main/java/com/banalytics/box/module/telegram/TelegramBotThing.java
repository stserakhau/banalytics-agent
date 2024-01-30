package com.banalytics.box.module.telegram;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.FileCreatedEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.StatusEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.peer.ConnectionStateEvent;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.standard.EventConsumer;
import com.banalytics.box.module.storage.filestorage.FileStorageThing;
import com.banalytics.box.module.telegram.handlers.*;
import com.banalytics.box.module.telegram.handlers.system.LogoutActionCommandHandler;
import com.banalytics.box.module.telegram.handlers.system.RebootActionCommandHandler;
import com.banalytics.box.module.telegram.handlers.system.ReloadFirmwareActionCommandHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.GetFileResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.banalytics.box.api.integration.utils.CommonUtils.DEFAULT_OBJECT_MAPPER;
import static com.banalytics.box.module.State.RUN;
import static com.banalytics.box.module.Thing.StarUpOrder.DATA_EXCHANGE;
import static com.banalytics.box.module.telegram.handlers.AuthorizeCommandHandler.COMMAND_AUTHORIZE;

@Slf4j
@Order(DATA_EXCHANGE)
public class TelegramBotThing extends AbstractThing<TelegramBotConfiguration> implements UpdatesListener, EventConsumer {
    private final TelegramBotThing _this = this;

    @Override
    public String getTitle() {
        return configuration.title;
    }

    public TelegramBotThing(BoxEngine engine) {
        super(engine);
    }

    private TelegramBot bot;

    private final Map<String, CommandHandler> commandHandlerMap = new HashMap<>();

    private final Map<Long, String> lastCommandMap = new HashMap<>();

    private AuthorizeCommandHandler authorizeCommandHandler;

    private void registerHandler(CommandHandler commandHandler) {
        commandHandlerMap.put(commandHandler.getCommand(), commandHandler);
    }

    @Override
    public Object uniqueness() {
        return configuration.botToken;
    }

    @Override
    public Set<String> generalPermissions() {
        return super.generalPermissions();
    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of();
    }

    @Override
    protected void doInit() throws Exception {
    }

    private long startAcceptCommandsTime;

    @Override
    public void destroy() {
        if (this.botConfigFile.exists()) {
            this.botConfigFile.delete();
        }
    }

    @Override
    protected void doStart() throws Exception {
        startAcceptCommandsTime = System.currentTimeMillis() + 10000;

        File applicationConfigFolder = engine.applicationConfigFolder();
        File instanceFolder = new File(applicationConfigFolder, "instances");
        this.botConfigFile = new File(instanceFolder, getUuid().toString() + ".telegram-bot-config");
        loadBotConfig();

        if (this.bot != null) {
            this.bot.removeGetUpdatesListener();
            this.bot.shutdown();
        }

        bot = new TelegramBot(configuration.botToken);

        authorizeCommandHandler = new AuthorizeCommandHandler(bot, botConfig, configuration);

        registerHandler(new BrowseFileStorageCommandHandler(bot, engine));
        registerHandler(new StartCommandHandler(bot));
        registerHandler(new HomeCommandHandler(bot));
        registerHandler(new QuickActionCommandHandler(bot));
//        registerHandler(new StorageRecordingsCommandHandler(bot, botConfig, engine));
//        registerHandler(new SubscribeAllMotionFileStoragesEventsCommandHandler(bot, botConfig, engine));
//        registerHandler(new UnSubscribeAllMotionEventsCommandHandler(bot, botConfig, engine));
        registerHandler(new VideoShotAllCommandHandler(bot, engine));
        registerHandler(new VideoShotCommandHandler(bot, engine));
        registerHandler(new LogoutActionCommandHandler(bot, engine, botConfig));
        registerHandler(new RebootActionCommandHandler(bot, engine));
        registerHandler(new ReloadFirmwareActionCommandHandler(bot, engine));
        registerHandler(new FileEventHandler(this, bot, engine));
        bot.setUpdatesListener(_this);
    }

    @Override
    protected void doStop() throws Exception {
        if (this.bot != null) {
            this.bot.removeGetUpdatesListener();
            this.bot.shutdown();
            this.bot = null;
        }
    }

    private static final String DEFAULT_EVENT_TEMPLATE = """
            <b>${type}</b>-<i>${nodeTitle}</i>
            """;
    private static final String STATUS_EVENT_TEMPLATE = """
            <b>Status</b>: <b>${state}: ${nodeTitle}</b> ${nodeClass} <code>${message}</code>
            """;
    private static final String CONNECTION_STATE_EVENT_TEMPLATE = """
            <b>Peer connection</b>: <code>${state}:${email}</code>
            """;

    @Override
    public Set<String> accountNames(Set<String> accountIds) {
        Set<String> result = new HashSet<>();
        if (botConfig != null) {
            for (String accountId : accountIds) {
                BotConfig.Chat chat = botConfig.allowedChats.get(Long.parseLong(accountId));
                if (chat == null) {
// todo dont show self unauthenticated records                    result.add("???" + accountId + "???");
                } else {
                    result.add((chat.title));
                }
            }
        }
        return result;
    }

    @Override
    public void consume(Recipient recipient, AbstractEvent event) {
        if (event instanceof FileCreatedEvent fce) {
            botConfig.getAllowedChats().forEach((id, chat) -> {
                if (!recipient.isAllowed("" + chat.id)) {
                    return;
                }
                try {
                    FileStorageThing fileStorageThing = engine.getThing(fce.getStorageUuid());
                    String contextPath = fce.getContextPath();
                    File file = fileStorageThing.file(contextPath);
                    SendVideo videoMsg = new SendVideo(id, file);
                    videoMsg.caption(contextPath);
                    File thumbnailFile = new File(file.getParentFile(), "thumbnails/" + file.getName() + ".jpg");
                    if (thumbnailFile.exists()) {
                        videoMsg.thumb(thumbnailFile);
                    }
                    bot.execute(videoMsg);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            });
        } else if (event instanceof StatusEvent se) {
            botConfig.getAllowedChats().forEach((id, chat) -> {
                if (!recipient.isAllowed("" + chat.id)) {
                    return;
                }
                Map<String, Object> m = new HashMap<>();
                m.put("state", se.getState());
                m.put("nodeTitle", se.getNodeTitle());
                m.put("nodeClass", se.getNodeClassName());
                m.put("message", se.getMessage());
                String msg = StringSubstitutor.replace(STATUS_EVENT_TEMPLATE, m);
                SendMessage textMsg = new SendMessage(id, msg);
                textMsg.parseMode(ParseMode.HTML);
                bot.execute(textMsg);
            });
        } else if (event instanceof ConnectionStateEvent se) {
            botConfig.getAllowedChats().forEach((id, chat) -> {
                if (!recipient.isAllowed("" + chat.id)) {
                    return;
                }
                String msg = StringSubstitutor.replace(
                        CONNECTION_STATE_EVENT_TEMPLATE,
                        Map.of(
                                "state", se.getState(),
                                "email", se.getEmail()
                        )
                );
                SendMessage textMsg = new SendMessage(id, msg);
                textMsg.parseMode(ParseMode.HTML);
                bot.execute(textMsg);
            });
        } else {
            botConfig.getAllowedChats().forEach((id, chat) -> {
                if (!recipient.isAllowed("" + chat.id)) {
                    return;
                }
                String msg = StringSubstitutor.replace(
                        DEFAULT_EVENT_TEMPLATE,
                        Map.of(
                                "type", event.getType(),
                                "nodeTitle", event.getNodeTitle()
                        )
                );
                SendMessage textMsg = new SendMessage(id, msg);
                textMsg.parseMode(ParseMode.HTML);
                bot.execute(textMsg);
            });
        }
    }

    public void sendMessage(AbstractMessage message) {
        /*try {
            SendMessage invalidCmdMsg = new SendMessage(null, message.toJson());
            bot.execute(invalidCmdMsg);
        } catch (Throwable e) {
            SendMessage invalidCmdMsg = new SendMessage(null, "Error:" + e.getMessage());
            bot.execute(invalidCmdMsg);
        }*/
    }

    @Override
    public int process(List<Update> updates) {
        if (System.currentTimeMillis() < startAcceptCommandsTime) {
            return CONFIRMED_UPDATES_ALL;
        }

        updates.forEach(upd -> {
            Message msg = upd.message();
            if (msg == null) {
                log.warn(upd.toString());
                return;
            }
            User user = msg.from();
            if (Boolean.TRUE.equals(user.isBot())) {
                return;
            }
            Chat chat = msg.chat();
            long chatId = chat.id();
            boolean authorized = botConfig.isAuthorized(chatId);
            String message = msg.text();
            boolean botCommand = isCommand(msg.entities());
            if (botCommand) {                               // if bot command
                if (authorized) {
                    CommandHandler handler = commandHandlerMap.get(message);
                    if (handler == null) {                      // if unknown command send response message
                        SendMessage invalidCmdMsg = new SendMessage(chatId, "Invalid command");
                        bot.execute(invalidCmdMsg);
                    } else {                                    // otherwise execute
                        lastCommandMap.put(chatId, message);    // and store last chat command
                        handler.handle(chatId);
                    }
                } else {
                    lastCommandMap.put(chatId, COMMAND_AUTHORIZE);
                    this.authorizeCommandHandler.handle(chatId);
                }
            } else {                                        // if simple message
                String lastCommand = lastCommandMap.get(chatId); // get last executed command
                if (authorized) {
                    if (lastCommand != null) {
                        CommandHandler lastCommandHandler = commandHandlerMap.get(lastCommand);
                        if (lastCommandHandler != null) {
                            boolean textArg = StringUtils.isNotEmpty(message);
                            if (textArg) {
                                lastCommandHandler.handleArgs(chatId, message); // and execute it with argument
                            } else {
                                PhotoSize[] photos = msg.photo();
                                boolean photoArg = photos != null;
                                if (photoArg) {
                                    String fileUri = fileUri(photos[photos.length - 1].fileId());
                                    lastCommandHandler.handlePhotoArgs(
                                            chatId,
                                            fileUri
                                    ); // and execute it with argument
                                }

                                Video video = msg.video();
                                boolean videoArg = video != null;
                                if (videoArg) {
                                    String fileUri = fileUri(video.fileId());
                                    lastCommandHandler.handleVideoArg(
                                            chatId,
                                            fileUri
                                    ); // and execute it with argument
                                }

                                Audio audio = msg.audio();
                                boolean audioArg = audio != null;
                                if (audioArg) {
                                    String fileUri = fileUri(audio.fileId());
                                    lastCommandHandler.handleAudioArg(
                                            chatId,
                                            fileUri
                                    ); // and execute it with argument
                                }

                                Document doc = msg.document();
                                boolean documentArg = doc != null;
                                if (documentArg) {
                                    String fileUri = fileUri(doc.fileId());
                                    lastCommandHandler.handleDocumentArg(
                                            chatId,
                                            fileUri
                                    ); // and execute it with argument
                                }

                                Location location = msg.location();
                                boolean locationArg = location != null;
                                if (locationArg) {
                                    lastCommandHandler.handleLocationArgs(
                                            chatId,
                                            location
                                    ); // and execute it with argument
                                }
                            }
                        }
                    }
                } else {
                    if (COMMAND_AUTHORIZE.equals(lastCommand)) {
                        this.authorizeCommandHandler.handleArgs(chatId, message, user.username() + ":" + user.firstName() + ":" + user.lastName() + ":" + user.languageCode());
                    }
                }
            }
        });
        return CONFIRMED_UPDATES_ALL;
    }

    private String fileUri(String fileId) {
        GetFile request = new GetFile(fileId);
        GetFileResponse res = bot.execute(request);
        return bot.getFullFilePath(res.file());
    }

    private static boolean isCommand(MessageEntity[] entities) {
        if (entities == null) {
            return false;
        }
        for (MessageEntity entity : entities) {
            if (entity.type() == MessageEntity.Type.bot_command) {
                return true;
            }
        }
        return false;
    }

    private final TypeReference<BotConfig> TYPE_BOT_CONFIG = new TypeReference<>() {
    };

    private void loadBotConfig() throws Exception {
        if (!botConfigFile.exists()) {
            if (!botConfigFile.createNewFile()) {
                log.error("Can't create {} file", botConfigFile.getAbsolutePath());
            } else {
                try (FileWriter fw = new FileWriter(botConfigFile)) {
                    fw.write("{}");
                }
            }
            botConfig = new BotConfig();
            botConfig.telegramBotThing(this);
        } else {
            try {
                botConfig = DEFAULT_OBJECT_MAPPER.readValue(botConfigFile, TYPE_BOT_CONFIG);
                botConfig.telegramBotThing(this);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void persistBotConfig() {
        try {
            DEFAULT_OBJECT_MAPPER.writeValue(botConfigFile, botConfig);
        } catch (IOException e) {
            log.error("Can't persist bot configuration.", e);
        }
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        if (getState() != RUN) {
            throw new Exception("error.thing.notInitialized");
        }
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);
        switch (method) {
            case "readBotConfig" -> {
                return configuration;
            }
            case "readAccounts" -> {
                List<String> accounts = botConfig.allowedChats.values().stream().map(chat -> chat.id + "~" + chat.title).collect(Collectors.toList());
                return accounts;
            }
            default -> {
                throw new Exception("Method not supported: " + method);
            }
        }
    }

    private File botConfigFile;
    public BotConfig botConfig;

    @NoArgsConstructor
    @Getter
    @Setter
    public static class BotConfig {
        @JsonIgnore
        private TelegramBotThing telegramBotThing;

        private void telegramBotThing(TelegramBotThing telegramBotThing) {
            this.telegramBotThing = telegramBotThing;
        }

        private Map<Long, Chat> allowedChats = new HashMap<>();

        public void fireUpdate() {
            telegramBotThing.persistBotConfig();
        }

        public void authorizeChat(long chatId, String title) {
            allowedChats.put(chatId, new Chat(chatId, title));
            fireUpdate();
        }

        public void logoutChat(long chatId) {
            allowedChats.remove(chatId);
            fireUpdate();
        }

        public boolean isAuthorized(long chatId) {
            return allowedChats.containsKey(chatId);
        }

        @Getter
        @Setter
        public static class Chat {
            long id;
            String title;

            public Chat() {
            }

            public Chat(long id, String title) {
                this.id = id;
                this.title = title;
            }
        }
    }

    @Override
    public boolean billable() {
        return true;
    }
}