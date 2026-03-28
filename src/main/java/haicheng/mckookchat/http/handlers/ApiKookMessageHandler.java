package haicheng.mckookchat.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import haicheng.mckookchat.McKookChat;
import haicheng.mckookchat.chat.ChatBuffer;
import haicheng.mckookchat.chat.ChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ApiKookMessageHandler implements HttpHandler {

    private static final Gson GSON = new Gson();
    private final ChatBuffer chatBuffer;

    public ApiKookMessageHandler(ChatBuffer chatBuffer) {
        this.chatBuffer = chatBuffer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String body;
        try (InputStream is = exchange.getRequestBody()) {
            body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        String username;
        String content;
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            username = json.has("username") ? json.get("username").getAsString() : "KooK";
            content = json.has("content") ? json.get("content").getAsString() : "";
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid JSON body. Required: {\\\"username\\\":\\\"...\\\",\\\"content\\\":\\\"...\\\"}\"}");
            return;
        }

        if (content.isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"content field is required\"}");
            return;
        }

        ChatMessage chatMessage = new ChatMessage(username, content, "kook");
        chatBuffer.addMessage(chatMessage);

        MinecraftServer server = McKookChat.getServer();
        if (server != null) {
            String chatPrefix = "\u00a7a[KooK] \u00a7r";
            String finalMessage = chatPrefix + "\u00a7b" + username + "\u00a7r: " + content;
            server.execute(() -> {
                server.getPlayerManager().broadcast(Text.literal(finalMessage), false);
            });
        }

        sendResponse(exchange, 200, "{\"status\":\"ok\",\"message\":\"Message broadcast to server\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
