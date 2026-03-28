package haicheng.mckookchat.chat;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChatBuffer {

    private final Deque<ChatMessage> messages;
    private final int maxSize;

    public ChatBuffer(int maxSize) {
        this.maxSize = maxSize;
        this.messages = new ConcurrentLinkedDeque<>();
    }

    public void addMessage(ChatMessage message) {
        messages.addLast(message);
        while (messages.size() > maxSize) {
            messages.pollFirst();
        }
    }

    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    public List<ChatMessage> getMessagesAfter(long timestamp) {
        List<ChatMessage> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            if (msg.getTimestamp() > timestamp) {
                result.add(msg);
            }
        }
        return result;
    }

    public int size() {
        return messages.size();
    }

    public void clear() {
        messages.clear();
    }
}
