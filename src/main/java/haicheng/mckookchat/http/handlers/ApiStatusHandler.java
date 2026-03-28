package haicheng.mckookchat.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import haicheng.mckookchat.McKookChat;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiStatusHandler implements HttpHandler {

    private static final Gson GSON = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        Map<String, Object> status = new HashMap<>();
        status.put("modId", McKookChat.MOD_ID);
        status.put("version", McKookChat.MOD_VERSION);
        status.put("running", McKookChat.isServerRunning());
        status.put("playerCount", McKookChat.getPlayerCount());
        status.put("messageCount", McKookChat.getMessageCount());
        status.put("timestamp", System.currentTimeMillis());

        sendResponse(exchange, 200, GSON.toJson(status));
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
