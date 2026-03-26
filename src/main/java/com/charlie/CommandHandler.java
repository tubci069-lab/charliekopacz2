package com.charlie;

import com.charlie.gui.MainScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandHandler {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("chk")
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal("§6Dostępne komendy:\n" +
                            "§e/chk gui §7- otwiera menu moda\n" +
                            "§e/chk start §7- uruchamia kopanie\n" +
                            "§e/chk stop §7- zatrzymuje kopanie"));
                    return 1;
                })
                .then(literal("gui").executes(context -> {
                    MinecraftClient.getInstance().execute(() -> {
                        MinecraftClient.getInstance().setScreen(new MainScreen());
                    });
                    return 1;
                }))
                .then(literal("start").executes(context -> {
                    Config.enabled = true;
                    context.getSource().sendFeedback(Text.literal("§aKopanie uruchomione!"));
                    return 1;
                }))
                .then(literal("stop").executes(context -> {
                    Config.enabled = false;
                    context.getSource().sendFeedback(Text.literal("§cKopanie zatrzymane!"));
                    return 1;
                }))
            );
        });
    }
}
