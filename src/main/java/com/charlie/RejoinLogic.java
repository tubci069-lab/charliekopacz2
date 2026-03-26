package com.charlie;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class RejoinLogic {
    private static long rejoinTimer = -1;
    private static LobbyState lobbyState = LobbyState.NONE;
    private static long stateTimer = -1;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!Config.enabled || !Config.autoRejoin) {
                lobbyState = LobbyState.NONE;
                rejoinTimer = -1;
                return;
            }

            // 1. Wykrywanie adresu serwera podczas gry (tylko gdy jesteśmy połączeni)
            if (client.getNetworkHandler() != null && client.getCurrentServerEntry() != null) {
                Config.lastServerAddress = client.getCurrentServerEntry().address;
            }

            // 2. Obsługa rozłączenia - sprawdzenie czy jesteśmy na ekranie DisconnectedScreen
            if (client.screen instanceof DisconnectedScreen) {
                if (rejoinTimer == -1) {
                    rejoinTimer = System.currentTimeMillis() + 5000; // Czekaj 5 sekund przed ponownym połączeniem
                }
            } else if (rejoinTimer != -1) {
                // Jeśli już nie jesteśmy na DisconnectedScreen i nie połączyliśmy się, zresetuj timer
                if (client.getNetworkHandler() != null) rejoinTimer = -1;
            }

            if (rejoinTimer != -1 && System.currentTimeMillis() > rejoinTimer) {
                rejoinTimer = -1;
                if (Config.lastServerAddress != null && !Config.lastServerAddress.isEmpty()) {
                    ServerInfo serverInfo = new ServerInfo("Auto Rejoin", Config.lastServerAddress, ServerInfo.ServerType.OTHER);
                    client.execute(() -> {
                        ConnectScreen.connect(new TitleScreen(), client, ServerAddress.parse(Config.lastServerAddress), serverInfo, false, null);
                    });
                    lobbyState = LobbyState.WAIT_FOR_JOIN;
                }
            }

            // 3. Logika Lobby (rapy.gg)
            if (client.player != null && Config.lastServerAddress != null && Config.lastServerAddress.toLowerCase().contains("rapy.gg")) {
                handleLobbyNavigation(client);
            }
        });
    }

    private static void handleLobbyNavigation(MinecraftClient client) {
        if (client.player == null) return;

        switch (lobbyState) {
            case WAIT_FOR_JOIN:
                if (client.player != null && client.getNetworkHandler() != null) {
                    lobbyState = LobbyState.SELECT_HOTBAR;
                    stateTimer = System.currentTimeMillis() + 3000; // Czekaj 3 sekundy na załadowanie świata
                }
                break;

            case SELECT_HOTBAR:
                if (System.currentTimeMillis() > stateTimer) {
                    client.player.getInventory().selectedSlot = 4; // 5 slot (index 4)
                    lobbyState = LobbyState.RIGHT_CLICK;
                    stateTimer = System.currentTimeMillis() + 1000;
                }
                break;

            case RIGHT_CLICK:
                if (System.currentTimeMillis() > stateTimer) {
                    ItemStack item = client.player.getInventory().getStack(4);
                    if (item.isOf(Items.PAPER)) {
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                        lobbyState = LobbyState.WAIT_FOR_GUI;
                        stateTimer = System.currentTimeMillis() + 2000;
                    } else {
                        // Jeśli nie ma papieru na 5 slocie, spróbuj ponownie za chwilę
                        stateTimer = System.currentTimeMillis() + 1000;
                    }
                }
                break;

            case WAIT_FOR_GUI:
                if (client.screen instanceof GenericContainerScreen) {
                    GenericContainerScreen screen = (GenericContainerScreen) client.screen;
                    String title = screen.getTitle().getString();
                    if (title.contains("WYBÓR TRYBU")) {
                        lobbyState = LobbyState.CLICK_MODE;
                    }
                } else if (System.currentTimeMillis() > stateTimer) {
                    // Timeout, spróbuj kliknąć ponownie
                    lobbyState = LobbyState.RIGHT_CLICK;
                }
                break;

            case CLICK_MODE:
                if (client.screen instanceof GenericContainerScreen) {
                    GenericContainerScreen screen = (GenericContainerScreen) client.screen;
                    // Szukamy "WOJNY GILDII" (zazwyczaj 1 slot w górnym rzędzie lub według nazwy)
                    // Na zdjęciu to drugi element od lewej (index 1 lub 10 w kontenerze)
                    for (int i = 0; i < screen.getScreenHandler().slots.size(); i++) {
                        ItemStack stack = screen.getScreenHandler().getSlot(i).getStack();
                        if (!stack.isEmpty() && stack.getName().getString().contains("WOJNY GILDII")) {
                            client.interactionManager.clickSlot(screen.getScreenHandler().syncId, i, 0, SlotActionType.PICKUP, client.player);
                            lobbyState = LobbyState.NONE; // Koniec nawigacji
                            break;
                        }
                    }
                }
                break;
                
            case NONE:
                break;
        }
    }

    enum LobbyState {
        NONE, WAIT_FOR_JOIN, SELECT_HOTBAR, RIGHT_CLICK, WAIT_FOR_GUI, CLICK_MODE
    }
}
