package haicheng.mckookchat;

import net.fabricmc.api.ClientModInitializer;

public class McKookChatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Client-side initialization
		// /kook-link command is registered server-side so it works in both
		// single-player and multiplayer scenarios
	}
}
