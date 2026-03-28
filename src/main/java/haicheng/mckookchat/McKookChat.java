package haicheng.mckookchat;

import com.mojang.brigadier.context.CommandContext;
import haicheng.mckookchat.chat.ChatBuffer;
import haicheng.mckookchat.chat.ChatInterceptor;
import haicheng.mckookchat.config.ModConfig;
import haicheng.mckookchat.http.HttpServerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McKookChat implements ModInitializer {
    public static final String MOD_ID = "mc-kook-chat";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile MinecraftServer server;
    private static ModConfig config;
    private static ChatBuffer chatBuffer;
    private static HttpServerManager httpServerManager;

    @Override
    public void onInitialize() {
        LOGGER.info("[McKookChat] {} initializing...", MOD_VERSION);

        config = ModConfig.load();
        chatBuffer = new ChatBuffer(config.getMaxMessages());

        ChatInterceptor chatInterceptor = new ChatInterceptor(chatBuffer);
        chatInterceptor.register();

        httpServerManager = new HttpServerManager(config, chatBuffer);

        ServerLifecycleEvents.SERVER_STARTING.register(s -> {
            server = s;
            LOGGER.info("[McKookChat] Server starting, chat sync active");
        });

        ServerLifecycleEvents.SERVER_STARTED.register(s -> {
            try {
                httpServerManager.start();
                LOGGER.info("[McKookChat] Chat sync HTTP server started on port {}", config.getHttpPort());
            } catch (Exception e) {
                LOGGER.error("[McKookChat] Failed to start HTTP server", e);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(s -> {
            httpServerManager.stop();
            LOGGER.info("[McKookChat] Server stopping, chat sync deactivated");
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
            server = null;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("kook-link")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(McKookChat::executeKookLink));
        });

        LOGGER.info("[McKookChat] {} loaded.", MOD_VERSION);
    }

    private static int executeKookLink(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(() -> Text.literal("\u00a7a[McKookChat] \u00a7f\u7248\u672c: \u00a7e" + MOD_VERSION), false);

        if (server != null) {
            source.sendFeedback(() -> Text.literal("\u00a7a[McKookChat] \u00a7f\u72b6\u6001: \u00a7b\u804a\u5929\u540c\u6b65\u670d\u52a1\u8fd0\u884c\u4e2d"), false);
            source.sendFeedback(() -> Text.literal("\u00a7a[McKookChat] \u00a7f\u7aef\u53e3: \u00a7e" + config.getHttpPort()), false);
            source.sendFeedback(() -> Text.literal("\u00a7a[McKookChat] \u00a7f\u52a0\u5bc6: " + (config.isEncryptionEnabled() ? "\u00a7a\u5df2\u542f\u7528" : "\u00a7c\u672a\u542f\u7528")), false);
            source.sendFeedback(() -> Text.literal("\u00a7a[McKookChat] \u00a7f\u6d88\u606f\u7f13\u51b2: \u00a7e" + chatBuffer.size() + "/" + config.getMaxMessages()), false);
        } else {
            source.sendFeedback(() -> Text.literal("\u00a7a[McKookChat] \u00a7f\u72b6\u6001: \u00a7c\u804a\u5929\u540c\u6b65\u670d\u52a1\u672a\u542f\u52a8"), false);
        }
        return 1;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static boolean isServerRunning() {
        return server != null && server.isRunning();
    }

    public static int getPlayerCount() {
        if (server == null) return 0;
        return server.getCurrentPlayerCount();
    }

    public static int getMessageCount() {
        if (chatBuffer == null) return 0;
        return chatBuffer.size();
    }
}
