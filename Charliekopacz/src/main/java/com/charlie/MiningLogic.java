package com.charlie;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.screen.slot.SlotActionType;

public class MiningLogic {
    private static boolean movingRight = true;
    private static float headPitch = 0;
    private static boolean pitchIncreasing = true;
    private static long lastDropTime = 0;
    private static int currentBlockLimitSteps = 0;

    private static int forwardSteps = 0;
    private static int squareState = 0; // 0: Right, 1: Forward, 2: Left, 3: Forward

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || !Config.enabled) {
                if (!Config.enabled && Config.startTime != 0) {
                    Config.totalTimeMillis += (System.currentTimeMillis() - Config.startTime);
                    Config.startTime = 0;
                }
                return;
            }

            // 1. Automatyczne kopanie
            KeyBinding.setKeyPressed(client.options.attackKey.getDefaultKey(), true);

            // 2. Block Limit (Ruch lewo/prawo)
            handleMovement(client);

            // 3. Ruch głowy (góra/dół)
            handleHeadMovement(client);

            // 4. Auto Commands
            handleAutoCommands(client);

            // 5. Auto Drop
            handleAutoDrop(client);
        });
    }

    private static void handleMovement(MinecraftClient client) {
        if (client.player == null) return;
        
        // Reset movement keys
        KeyBinding.setKeyPressed(client.options.leftKey.getDefaultKey(), false);
        KeyBinding.setKeyPressed(client.options.rightKey.getDefaultKey(), false);
        KeyBinding.setKeyPressed(client.options.forwardKey.getDefaultKey(), false);

        if (Config.miningMode == Config.MiningMode.LINIA) {
            handleLiniaMovement(client);
        } else {
            handleKwadratMovement(client);
        }
    }

    private static void handleLiniaMovement(MinecraftClient client) {
        if (currentBlockLimitSteps < Config.blockLimit * 20) {
            if (movingRight) {
                KeyBinding.setKeyPressed(client.options.rightKey.getDefaultKey(), true);
            } else {
                KeyBinding.setKeyPressed(client.options.leftKey.getDefaultKey(), true);
            }
            currentBlockLimitSteps++;
        } else {
            currentBlockLimitSteps = 0;
            movingRight = !movingRight;
            if (movingRight) Config.cycles++;
        }
    }

    private static void handleKwadratMovement(MinecraftClient client) {
        int ticksPerBlock = 20;
        int limit = Config.blockLimit * ticksPerBlock;

        switch (squareState) {
            case 0: // Right
                KeyBinding.setKeyPressed(client.options.rightKey.getDefaultKey(), true);
                if (++currentBlockLimitSteps >= limit) {
                    currentBlockLimitSteps = 0;
                    squareState = 1;
                }
                break;
            case 1: // Forward
                KeyBinding.setKeyPressed(client.options.forwardKey.getDefaultKey(), true);
                if (++forwardSteps >= ticksPerBlock) { // Move 1 block forward
                    forwardSteps = 0;
                    squareState = 2;
                }
                break;
            case 2: // Left
                KeyBinding.setKeyPressed(client.options.leftKey.getDefaultKey(), true);
                if (++currentBlockLimitSteps >= limit) {
                    currentBlockLimitSteps = 0;
                    squareState = 3;
                }
                break;
            case 3: // Forward
                KeyBinding.setKeyPressed(client.options.forwardKey.getDefaultKey(), true);
                if (++forwardSteps >= ticksPerBlock) {
                    forwardSteps = 0;
                    squareState = 0;
                    Config.cycles++;
                }
                break;
        }
    }

    private static void handleHeadMovement(MinecraftClient client) {
        if (!Config.headMovement || client.player == null) return;

        if (pitchIncreasing) {
            headPitch += 1.0f;
            if (headPitch >= 30) pitchIncreasing = false;
        } else {
            headPitch -= 1.0f;
            if (headPitch <= -30) pitchIncreasing = true;
        }
        client.player.setPitch(headPitch);
    }

    private static void handleAutoCommands(MinecraftClient client) {
        if (client.player == null || client.player.networkHandler == null) return;
        long currentTime = System.currentTimeMillis();

        for (Config.AutoCommand cmd : Config.autoCommands) {
            if (cmd.command == null || cmd.command.trim().isEmpty()) continue;
            
            if (currentTime - cmd.lastExecutionTime >= cmd.intervalMinutes * 60000L) {
                String cmdText = cmd.command.startsWith("/") ? cmd.command.substring(1) : cmd.command;
                client.player.networkHandler.sendChatCommand(cmdText);
                cmd.lastExecutionTime = currentTime;
            }
        }
    }

    private static void handleAutoDrop(MinecraftClient client) {
        if (!Config.autoDrop || client.player == null || client.interactionManager == null) return;
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastDropTime >= Config.dropIntervalMinutes * 60000L) {
            for (int slotIndex : Config.slotsToDrop) {
                // Wyrzucanie całego stosu z danego slotu
                client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, slotIndex, 1, SlotActionType.THROW, client.player);
            }
            lastDropTime = currentTime;
        }
    }
}
