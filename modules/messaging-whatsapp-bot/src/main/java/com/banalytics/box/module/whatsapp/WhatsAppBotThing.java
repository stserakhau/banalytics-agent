package com.banalytics.box.module.whatsapp;

import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.FileCreatedEvent;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.State;
import com.banalytics.box.module.standard.EventConsumer;
import com.banalytics.box.module.storage.filestorage.FileStorageThing;
import com.banalytics.box.module.whatsapp.handlers.*;
import com.banalytics.box.service.AppForkJoinWorkerThreadFactory;
import com.banalytics.box.service.SystemThreadsService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import it.auties.whatsapp.api.DisconnectReason;
import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.api.SocketEvent;
import it.auties.whatsapp.api.Whatsapp;
import it.auties.whatsapp.controller.DefaultControllerSerializer;
import it.auties.whatsapp.controller.Store;
import it.auties.whatsapp.listener.Listener;
import it.auties.whatsapp.model.contact.Contact;
import it.auties.whatsapp.model.contact.ContactJid;
import it.auties.whatsapp.model.info.ContextInfo;
import it.auties.whatsapp.model.info.MessageInfo;
import it.auties.whatsapp.model.message.button.ButtonsResponseMessage;
import it.auties.whatsapp.model.message.model.Message;
import it.auties.whatsapp.model.message.model.MessageContainer;
import it.auties.whatsapp.model.message.model.MessageKey;
import it.auties.whatsapp.model.message.standard.TextMessage;
import it.auties.whatsapp.model.message.standard.VideoMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.util.Base64;
import org.springframework.core.annotation.Order;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static com.banalytics.box.api.integration.utils.CommonUtils.DEFAULT_OBJECT_MAPPER;
import static com.banalytics.box.module.State.RUN;
import static com.banalytics.box.module.Thing.StarUpOrder.DATA_EXCHANGE;
import static com.banalytics.box.module.whatsapp.handlers.AuthorizeCommandHandler.COMMAND_AUTHORIZE;

/**
 * https://github.com/Auties00/Whatsapp4j
 */
@Slf4j
@Order(DATA_EXCHANGE)
public class WhatsAppBotThing extends AbstractThing<WhatsAppBotConfiguration> implements EventConsumer, Listener {
    private Whatsapp whatsapp;
    private File botConfigFile;
    private File botQrCodeFile;
    public BotConfig botConfig;
    private final Map<String, CommandHandler> commandHandlerMap = new HashMap<>();
    private final Map<String, String> lastCommandMap = new HashMap<>();
    private final AuthorizeCommandHandler authorizeCommandHandler;


    private void registerHandler(CommandHandler commandHandler) {
        commandHandlerMap.put(commandHandler.getCommand(), commandHandler);
    }

    public WhatsAppBotThing(BoxEngine engine) {
        super(engine);
        registerHandler(new WhoIsHereHandler(this));
        registerHandler(this.authorizeCommandHandler = new AuthorizeCommandHandler(this));
        registerHandler(new LogoutActionCommandHandler(this));
//        registerHandler(new HomeCommandHandler(this));
//        registerHandler(new QuickActionCommandHandler(this));
        registerHandler(new VideoShotAllCommandHandler(this, engine));
    }

    @Override
    public Object uniqueness() {
        return configuration.alias;
    }

    @Override
    protected void doInit() throws Exception {
    }

    @Override
    protected void doStart() throws Exception {
        File applicationConfigFolder = engine.applicationConfigFolder();
        File instanceFolder = new File(applicationConfigFolder, "instances");
        this.botConfigFile = new File(instanceFolder, getUuid().toString() + ".whatsup-bot-config");

        loadBotConfig();

        SystemThreadsService.execute(this, () -> {
//            Thread.currentThread().setContextClassLoader(Whatsapp.class.getClassLoader());

            AppForkJoinWorkerThreadFactory factory = new AppForkJoinWorkerThreadFactory(Whatsapp.class.getClassLoader());
            ForkJoinPool myCommonPool = new ForkJoinPool(3, factory, null, false);
            log.info("Starting");
            File homeDir = engine.applicationHomeFolder();
            File whatsappBaseDir = new File(homeDir, "/whatsapp4j/");
            var connection = Whatsapp
                    .webBuilder()
                    .serializer(new DefaultControllerSerializer(whatsappBaseDir.toPath()))
                    .newConnection(configuration.alias)
                    .name("Banalytics Bot '" + configuration.alias + "'")
                    .socketExecutor(myCommonPool);

            Optional<Whatsapp> opt = connection.registered();
            if (opt.isPresent()) {
                opt.get()
                        .addListener(this)
                        .connect()
                        .join();
            } else {
                connection
                        .unregistered(QrHandler.toFile(path -> {
                            botQrCodeFile = path.toFile();
                        }))
                        .addListener(this)
                        .connect()
                        .join();
            }

            log.info("Stopped");
        });
    }

    @Override
    protected void doStop() throws Exception {
        if (whatsapp != null) {
            SystemThreadsService.execute(this, () -> {
                this.whatsapp.disconnect().join();
                log.info("Stopping");
            });
        }
    }

    @Override
    public void destroy() {
        if (this.whatsapp != null) {
            Store store = this.whatsapp.store();
            if (store.alias().contains(configuration.alias)) {
                store.removeAlias(configuration.alias);
            }
        }
        if (this.botConfigFile.exists()) {
            this.botConfigFile.delete();
        }
        if (this.botQrCodeFile != null && this.botQrCodeFile.exists()) {
            this.botQrCodeFile.delete();
        }
    }

    @Override
    public void onLoggedIn(Whatsapp whatsapp) {
        this.whatsapp = whatsapp;
        if (botQrCodeFile != null && botQrCodeFile.exists()) {
            botQrCodeFile.delete();
        }
        botQrCodeFile = null;
        UUID connectionId = whatsapp.store().uuid();
        log.info("Started with connecitonID={}", connectionId);
//        if (configuration.connectionId == -1) {
//            configuration.connectionId = connectionId;
//            try {
//                engine.persistPrimaryInstance();
//            } catch (Throwable e) {
//                log.error(e.getMessage(), e);
//            }
//        }
    }

    @Override
    public void onDisconnected(Whatsapp whatsapp, DisconnectReason reason) {
        log.error("Disconnected: {}", reason);
        if(DisconnectReason.LOGGED_OUT == reason) {
            this.destroy();
        }
    }

    @Override
    public void onSocketEvent(Whatsapp whatsapp, SocketEvent event) {
        Listener.super.onSocketEvent(whatsapp, event);
    }

    @Override
    public void onNewMessage(MessageInfo info) {
        MessageContainer mc = info.message();
        MessageKey key = info.key();
        boolean isFromMe = key.fromMe();
        ContactJid sender = key.senderJid().get();
        ContactJid receiver = key.chatJid();
        boolean doProcess = !isFromMe || sender.user().equals(receiver.user());

        if (mc.textMessage().isPresent()) {//if text message
            TextMessage tm = mc.textMessage().get();
            ContextInfo ci = tm.contextInfo();
            if (ci.quotedMessage().isPresent()) { // and it is the answer
                Optional<String> targetMessage = ci.quotedMessage().get().textWithNoContextMessage();
                if (targetMessage.isPresent()) {
                    String alias = targetMessage.get();
                    if (!this.configuration.alias.equals(alias)) {// check is it alias, and if alias of this bot, continue processing
                        doProcess = false;
                    }
                }
            } else {
                //if it's not answer on bot message process message by all bots
            }
        }

        if (!doProcess) {
            return;
        }

        String senderId = receiver.user();
        cache.putIfAbsent(senderId, sender);
        boolean authorized = isFromMe || StringUtils.isEmpty(configuration.pinCode) || botConfig.isAuthorized(senderId);
        String message = null;
        Message msg = mc.content();
        if (msg instanceof ButtonsResponseMessage br) {
            message = br.buttonId();
        } else if (msg instanceof TextMessage tm) {
            message = tm.text().trim();
        }
        if (message == null) {
            return;
        }
        if (isCommand(message)) {                               // if bot command
            CommandHandler handler = commandHandlerMap.get(message);
            if (handler == null) {                      // if unknown command send response message
                log.info("Invalid command: {}", message);
                sendMessage(sender, "Invalid command");
            } else {
                if (!handler.isAuthRequired() || authorized) {
                    lastCommandMap.put(senderId, message);    // and store last chat command
                    handler.handle(senderId);
                    log.info("Command '{}' executed", message);
                } else {
                    log.info("Authorization requested: {}", senderId);
                    lastCommandMap.put(senderId, COMMAND_AUTHORIZE);
                    this.authorizeCommandHandler.handle(senderId);
                }
            }
        } else {                                        // if simple message
            String lastCommand = lastCommandMap.get(senderId); // get last executed command
            if (authorized) {
                if (lastCommand != null) {
                    CommandHandler lastCommandHandler = commandHandlerMap.get(lastCommand);
                    if (lastCommandHandler != null) {
                        lastCommandHandler.handleArgs(senderId, message); // and execute it with argument
                        lastCommandMap.remove(lastCommand);
                        log.info("Command argument processed: {}({})", lastCommand, message);
                    }
                }
            } else {
                if (COMMAND_AUTHORIZE.equals(lastCommand)) {
                    String[] args = {message, sender.user() + ":" + sender.type()};
                    this.authorizeCommandHandler.handleArgs(senderId, args);
                    log.info("User authorized: {}({})", lastCommand, Arrays.toString(args));
                }
            }
        }

    }

    Map<String, ContactJid> cache = new ConcurrentHashMap<>();

    public void sendMessage(String chatId, String message) {
        if (whatsapp == null) {
            log.info("Message rejected. not initialized yet");
            return;
        }
        ContactJid contact = cache.computeIfAbsent(chatId, cid -> {
            for (Contact c1 : whatsapp.store().contacts()) {
                if (chatId.equals(c1.jid().user())) {
                    return c1.jid();
                }
            }
            throw new RuntimeException("Contact not found: " + chatId);
        });

        sendMessage(contact, message);
    }

    public void sendMessage(String chatId, Message message) {
        if (whatsapp == null) {
            log.info("Message rejected. not initialized yet");
            return;
        }
        ContactJid contact = cache.computeIfAbsent(chatId, cid -> {
            for (Contact c1 : whatsapp.store().contacts()) {
                if (chatId.equals(c1.jid().user())) {
                    return c1.jid();
                }
            }
            throw new RuntimeException("Contact not found: " + chatId);
        });
        this.whatsapp.sendMessage(contact, message);
    }

    public void sendMessage(ContactJid contact, String message) {
        this.whatsapp.sendMessage(contact, message);
    }

    private static boolean isCommand(String message) {
        return message.startsWith("/");
    }

    @Override
    public String getTitle() {
        return configuration.alias;
    }

    @Override
    public Set<String> accountNames(Set<String> accountIds) {
        Set<String> result = new HashSet<>();
        for (String accountId : accountIds) {
            BotConfig.Chat chat = botConfig.allowedChats.get(accountId);
            if (chat == null) {
// todo dont show self unauthenticated records            result.add("???" + accountId + "???");
            } else {
                result.add((chat.title));
            }
        }
        return result;
    }

    @Override
    public void consume(Recipient recipient, AbstractEvent event) {
        //todo ignore recipients, sends to all authorized
        if (state != State.RUN) {
            return;
        }

        if (event instanceof FileCreatedEvent fce) {
            botConfig.getAllowedChats().forEach((id, chat) -> {
                if (!recipient.isAllowed(chat.id)) {
                    return;
                }
                try {
                    FileStorageThing fileStorageThing = engine.getThing(fce.getStorageUuid());
                    String contextPath = fce.getContextPath();
                    File file = fileStorageThing.file(contextPath);
                    File parentFile = file.getParentFile();


                    File thumbnailFile = new File(parentFile, "thumbnails/" + file.getName() + ".jpg");
                    byte[] thumbnail = null;
                    if (thumbnailFile.exists()) {
                        thumbnail = FileUtils.readFileToByteArray(thumbnailFile);
                    }
//                DocumentMessage message = DocumentMessage.simpleBuilder()
//                        .media(FileUtils.readFileToByteArray(file))
//                        .title(fce.getContextPath())
//                        .fileName(fce.getContextPath())
//                        .thumbnail(thumbnail)
//                        .build();

                    int duration = fce.option("duration");
                    VideoMessage.VideoMessageBuilder<?, ?> messageBuilder = VideoMessage.builder()
                            .decodedMedia(FileUtils.readFileToByteArray(file))
                            .caption(fce.getContextPath())
                            .width(fce.option("width"))
                            .height(fce.option("height"))
                            .duration(duration / 1000)
                            .mimetype("video/mp4");
                    if (thumbnail != null) {
                        messageBuilder.thumbnail(thumbnail);
                    }

                    VideoMessage message = messageBuilder.build();

                    sendMessage(chat.id, message);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            });
        } else {
            botConfig.getAllowedChats().forEach((id, chat) -> {
                if (!recipient.isAllowed(chat.id)) {
                    return;
                }
                String textView = event.textView();
                sendMessage(chat.id, textView);
            });
        }
    }

    @Override
    public boolean billable() {
        return false;
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        if (getState() != RUN) {
            throw new Exception("error.thing.notInitialized");
        }
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);
        switch (method) {
            case "qrCode" -> {
                if (botQrCodeFile != null && botQrCodeFile.exists()) {
                    byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(botQrCodeFile));
                    return new String(encoded, StandardCharsets.US_ASCII);
                } else {
                    if (whatsapp == null) {
                        return null;
                    } else {
                        Store store = whatsapp.store();
                        return "linked:" + store.name() + " " + store.locale();
                    }
                }
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

    @NoArgsConstructor
    @Getter
    @Setter
    public static class BotConfig {
        @JsonIgnore
        private WhatsAppBotThing botThing;

        private void telegramBotThing(WhatsAppBotThing telegramBotThing) {
            this.botThing = telegramBotThing;
        }

        private Map<String, Chat> allowedChats = new HashMap<>();

        public void fireUpdate() {
            botThing.persistBotConfig();
        }

        public void authorizeChat(String chatId, String title) {
            allowedChats.put(chatId, new Chat(chatId, title));
            fireUpdate();
        }

        public void logoutChat(String chatId) {
            allowedChats.remove(chatId);
            fireUpdate();
        }

        public boolean isAuthorized(String chatId) {
            return allowedChats.containsKey(chatId);
        }

        @Getter
        @Setter
        public static class Chat {
            String id;
            String title;

            public Chat() {
            }

            public Chat(String id, String title) {
                this.id = id;
                this.title = title;
            }
        }
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
}