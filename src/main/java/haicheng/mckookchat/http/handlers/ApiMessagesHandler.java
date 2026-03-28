package haicheng.mckookchat.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import haicheng.mckookchat.chat.ChatBuffer;
import haicheng.mckookchat.chat.ChatMessage;
import haicheng.mckookchat.crypto.AesEncryptor;
import haicheng.mckookchat.config.ModConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiMessagesHandler implements HttpHandler {

    private static final Gson GSON = new Gson();
    private final ChatBuffer chatBuffer;
    private final AesEncryptor encryptor;
    private final boolean encryptionEnabled;

    public ApiMessagesHandler(ChatBuffer chatBuffer, ModConfig config) {
        this.chatBuffer = chatBuffer;
        this.encryptionEnabled = config.isEncryptionEnabled();
        this.encryptor = encryptionEnabled ? new AesEncryptor(config.getEncryptionKey()) : null;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        List<ChatMessage> messages;
        if (query != null && query.startsWith("after=")) {
            try {
                long after = Long.parseLong(query.substring(6));
                messages = chatBuffer.getMessagesAfter(after);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid after parameter\"}");
                return;
            }
        } else {
            messages = chatBuffer.getMessages();
        }

        String json;
        if (encryptionEnabled) {
            json = buildEncryptedResponse(messages);
        } else {
            json = buildPlainResponse(messages);
        }

        sendResponse(exchange, 200, json);
    }

    private String buildPlainResponse(List<ChatMessage> messages) {
        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("count", messages.size());
        response.put("encrypted", false);
        return GSON.toJson(response);
    }

    private String buildEncryptedResponse(List<ChatMessage> messages) {
        try {
            String plainJson = GSON.toJson(messages);
            String encrypted = encryptor.encrypt(plainJson);
            Map<String, Object> response = new HashMap<>();
            response.put("data", encrypted);
            response.put("count", messages.size());
            response.put("encrypted", true);
            return GSON.toJson(response);
        } catch (Exception e) {
            return "{\"error\":\"Encryption failed\",\"count\":0,\"encrypted\":true}";
        }
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
