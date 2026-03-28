package haicheng.mckookchat.chat;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.CopyOnWriteArrayList;

public class ChatInterceptor {

    private final ChatBuffer chatBuffer;
    private final CopyOnWriteArrayList<ChatMessageListener> listeners = new CopyOnWriteArrayList<>();

    public ChatInterceptor(ChatBuffer chatBuffer) {
        this.chatBuffer = chatBuffer;
    }

    public void register() {
        ServerMessageEvents.CHAT_MESSAGE.register(this::onChatMessage);
    }

    private void onChatMessage(SignedMessage signedMessage, ServerPlayerEntity sender, MessageType.Parameters parameters) {
        String playerName = sender.getName().getString();
        String message = signedMessage.getContent().getString();

        ChatMessage chatMessage = new ChatMessage(playerName, message, "minecraft");
        chatBuffer.addMessage(chatMessage);

        for (ChatMessageListener listener : listeners) {
            listener.onMessage(chatMessage);
        }
    }

    public void addListener(ChatMessageListener listener) {
        listeners.add(listener);
    }

    public interface ChatMessageListener {
        void onMessage(ChatMessage message);
    }
}
