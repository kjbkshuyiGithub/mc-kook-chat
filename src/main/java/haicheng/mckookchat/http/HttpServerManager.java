package haicheng.mckookchat.http;

import com.sun.net.httpserver.HttpServer;
import haicheng.mckookchat.chat.ChatBuffer;
import haicheng.mckookchat.config.ModConfig;
import haicheng.mckookchat.http.handlers.ApiKookMessageHandler;
import haicheng.mckookchat.http.handlers.ApiMessagesHandler;
import haicheng.mckookchat.http.handlers.ApiStatusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpServerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("mc-kook-chat");
    private final ModConfig config;
    private final ChatBuffer chatBuffer;
    private HttpServer server;

    public HttpServerManager(ModConfig config, ChatBuffer chatBuffer) {
        this.config = config;
        this.chatBuffer = chatBuffer;
    }

    public void start() throws IOException {
        int port = config.getHttpPort();
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/messages", new ApiMessagesHandler(chatBuffer, config));
        server.createContext("/api/status", new ApiStatusHandler());
        server.createContext("/api/kook/message", new ApiKookMessageHandler(chatBuffer));

        server.setExecutor(null);
        server.start();

        LOGGER.info("[McKookChat] HTTP server started on port {}", port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            LOGGER.info("[McKookChat] HTTP server stopped");
        }
    }
}
