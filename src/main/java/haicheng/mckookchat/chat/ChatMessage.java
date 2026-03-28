package haicheng.mckookchat.chat;

public class ChatMessage {

    private final long timestamp;
    private final String playerName;
    private final String message;
    private final String source;

    public ChatMessage(String playerName, String message, String source) {
        this.timestamp = System.currentTimeMillis();
        this.playerName = playerName;
        this.message = message;
        this.source = source;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }
}
