package com.charlie;

import net.fabricmc.api.ClientModInitializer;

public class CharlieKopaczClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CommandHandler.register();
		MiningLogic.register();
		RejoinLogic.register();
	}
}
