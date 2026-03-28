package haicheng.mckookchat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class ModConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("mc-kook-chat");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Path.of("config", "mc-kook-chat.json");

    private int httpPort = 8888;
    private String encryptionKey = "";
    private int maxMessages = 100;
    private String kookBotAddress = "";
    private boolean enableEncryption = true;

    private ModConfig() {
    }

    public static ModConfig load() {
        ModConfig config;
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                config = GSON.fromJson(json, ModConfig.class);
                if (config == null) {
                    config = new ModConfig();
                }
                LOGGER.info("[McKookChat] Configuration loaded from {}", CONFIG_PATH);
            } catch (IOException e) {
                LOGGER.error("[McKookChat] Failed to read config, using defaults", e);
                config = new ModConfig();
            }
        } else {
            config = new ModConfig();
        }

        if (config.encryptionKey == null || config.encryptionKey.isEmpty()) {
            config.encryptionKey = generateEncryptionKey();
            LOGGER.info("[McKookChat] Generated new AES encryption key");
        }

        config.save();
        return config;
    }

    private void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            LOGGER.error("[McKookChat] Failed to save config", e);
        }
    }

    private static String generateEncryptionKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32];
        random.nextBytes(key);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : key) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public String getKookBotAddress() {
        return kookBotAddress;
    }

    public boolean isEncryptionEnabled() {
        return enableEncryption;
    }
}
