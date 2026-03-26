package com.charlie.gui;

import com.charlie.Config;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends Screen {
    private Tab currentTab = Tab.GLOWNE;
    private final List<TextFieldWidget> commandFields = new ArrayList<>();

    public MainScreen() {
        super(Text.literal("BK-KOPACZ"));
    }

    @Override
    protected void init() {
        refreshGui();
    }

    protected void refreshGui() {
        this.clearChildren();
        int centerX = width / 2;
        int centerY = height / 2;

        // X Button (Close)
        addDrawableChild(ButtonWidget.builder(Text.literal("X"), button -> this.close())
                .dimensions(centerX + 145, centerY - 125, 15, 15).build());

        // Header Links
        addDrawableChild(ButtonWidget.builder(Text.literal("GITHUB"), button -> Util.getOperatingSystem().open("https://github.com/"))
                .dimensions(centerX - 60, centerY - 125, 45, 15).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("DISCORD"), button -> Util.getOperatingSystem().open("https://discord.gg/"))
                .dimensions(centerX - 5, centerY - 125, 50, 15).build());

        // Tabs
        addDrawableChild(createTabButton("GLOWNE", Tab.GLOWNE, centerX - 150, centerY - 105));
        addDrawableChild(createTabButton("WYRZUCANIE", Tab.WYRZUCANIE, centerX - 50, centerY - 105));
        addDrawableChild(createTabButton("KOMENDY", Tab.KOMENDY, centerX + 55, centerY - 105));

        // Footer buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("▶ URUCHOM"), button -> {
            Config.enabled = true;
            Config.startTime = System.currentTimeMillis();
        }).dimensions(centerX - 150, centerY + 80, 145, 25).build());
        
        addDrawableChild(ButtonWidget.builder(Text.literal("■ ZATRZYMAJ"), button -> Config.enabled = false)
                .dimensions(centerX + 5, centerY + 80, 145, 25).build());

        switch (currentTab) {
            case GLOWNE -> initGlowne(centerX, centerY);
            case WYRZUCANIE -> initWyrzucanie(centerX, centerY);
            case KOMENDY -> initKomendy(centerX, centerY);
        }
    }

    private ButtonWidget createTabButton(String label, Tab tab, int x, int y) {
        return ButtonWidget.builder(Text.literal(label), button -> switchTab(tab))
                .dimensions(x, y, 95, 20).build();
    }

    private void switchTab(Tab tab) {
        currentTab = tab;
        refreshGui();
    }

    private void initGlowne(int centerX, int centerY) {
        // Block Limit
        addDrawableChild(ButtonWidget.builder(Text.literal("-"), button -> Config.blockLimit = Math.max(1, Config.blockLimit - 1))
                .dimensions(centerX - 40, centerY - 65, 20, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> Config.blockLimit++)
                .dimensions(centerX + 20, centerY - 65, 20, 20).build());

        // Tryb Kopania
        addDrawableChild(ButtonWidget.builder(Text.literal(Config.miningMode.name()), button -> {
            Config.miningMode = Config.miningMode == Config.MiningMode.LINIA ? Config.MiningMode.KWADRAT : Config.MiningMode.LINIA;
            refreshGui();
        }).dimensions(centerX + 60, centerY - 65, 80, 20).build());

        // Toggles
        addDrawableChild(CheckboxWidget.builder(Text.literal("RUCH GŁOWY"), textRenderer)
                .pos(centerX - 140, centerY - 30).checked(Config.headMovement)
                .callback((cb, checked) -> Config.headMovement = checked).build());

        addDrawableChild(CheckboxWidget.builder(Text.literal("AUTO DROP"), textRenderer)
                .pos(centerX + 10, centerY - 30).checked(Config.autoDrop)
                .callback((cb, checked) -> Config.autoDrop = checked).build());

        addDrawableChild(CheckboxWidget.builder(Text.literal("AUTO REJOIN"), textRenderer)
                .pos(centerX - 140, centerY + 5).checked(Config.autoRejoin)
                .callback((cb, checked) -> Config.autoRejoin = checked).build());
    }

    private void initWyrzucanie(int centerX, int centerY) {
        addDrawableChild(ButtonWidget.builder(Text.literal("Mniej (-1m)"), button -> Config.dropIntervalMinutes = Math.max(1, Config.dropIntervalMinutes - 1))
                .dimensions(centerX - 140, centerY - 75, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Więcej (+1m)"), button -> Config.dropIntervalMinutes++)
                .dimensions(centerX + 60, centerY - 75, 80, 20).build());

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = row * 9 + col + 9;
                boolean isSelected = Config.slotsToDrop.contains(slotIndex);
                addDrawableChild(ButtonWidget.builder(Text.literal(isSelected ? "X" : ""), button -> {
                    if (Config.slotsToDrop.contains(slotIndex)) Config.slotsToDrop.remove(slotIndex);
                    else Config.slotsToDrop.add(slotIndex);
                    refreshGui();
                }).dimensions(centerX - 100 + col * 22, centerY - 30 + row * 22, 20, 20).build());
            }
        }
    }

    private void initKomendy(int centerX, int centerY) {
        commandFields.clear();
        for (int i = 0; i < 5; i++) {
            Config.AutoCommand cmd = Config.autoCommands.get(i);
            TextFieldWidget field = new TextFieldWidget(textRenderer, centerX - 140, centerY - 70 + i * 25, 180, 20, Text.literal(""));
            field.setText(cmd.command);
            field.setChangedListener(text -> cmd.command = text);
            addDrawableChild(field);
            commandFields.add(field);

            addDrawableChild(ButtonWidget.builder(Text.literal(cmd.intervalMinutes + "m"), button -> {
                cmd.intervalMinutes = (cmd.intervalMinutes % 10) + 1;
                refreshGui();
            }).dimensions(centerX + 50, centerY - 70 + i * 25, 40, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int centerX = width / 2;
        int centerY = height / 2;

        // Main Window Frame
        context.fill(centerX - 165, centerY - 130, centerX + 165, centerY + 115, 0xEE121212);
        context.drawBorder(centerX - 165, centerY - 130, 330, 245, 0xFF333333);

        super.render(context, mouseX, mouseY, delta);

        // Header Title
        context.drawText(textRenderer, "BK-KOPACZ", centerX - 150, centerY - 125, 0xAAAAAA, false);
        context.drawText(textRenderer, "USER: CHARLIEMAJSTER", centerX - 150, centerY - 115, 0x666666, false);

        // Tab Underline
        int underlineX = currentTab == Tab.GLOWNE ? centerX - 150 : (currentTab == Tab.WYRZUCANIE ? centerX - 50 : centerX + 55);
        context.fill(underlineX, centerY - 85, underlineX + 95, centerY - 83, 0xFFFFFFFF);

        if (currentTab == Tab.GLOWNE) {
            renderGlowneLabels(context, centerX, centerY);
        } else if (currentTab == Tab.WYRZUCANIE) {
            context.drawText(textRenderer, "INTERVAL CZASOWY: " + Config.dropIntervalMinutes + " min", centerX - 140, centerY - 90, 0xFFFFFF, false);
            context.drawText(textRenderer, "WYBÓR SLOTÓW (Plecak)", centerX - 140, centerY - 45, 0xAAAAAA, false);
        }
    }

    private void renderGlowneLabels(DrawContext context, int centerX, int centerY) {
        context.drawText(textRenderer, "BLOCK LIMIT", centerX - 140, centerY - 70, 0xFFFFFF, false);
        context.drawText(textRenderer, "Zasięg kratek", centerX - 140, centerY - 60, 0x666666, false);
        context.drawText(textRenderer, String.valueOf(Config.blockLimit), centerX - 5, centerY - 60, 0x00FF00, false);
        
        context.drawText(textRenderer, "TRYB KOPANIA", centerX + 60, centerY - 80, 0xFFFFFF, false);
        context.drawText(textRenderer, "LINIA vs KWADRAT", centerX + 60, centerY - 70, 0x666666, false);

        // Statistics
        context.drawText(textRenderer, "STATYSTYKI", centerX + 10, centerY + 10, 0xFFFFFF, false);
        context.drawText(textRenderer, "Cykle: " + Config.cycles, centerX + 10, centerY + 25, 0xAAAAAA, false);
        long timeElapsed = Config.enabled ? (System.currentTimeMillis() - Config.startTime) : Config.totalTimeMillis;
        context.drawText(textRenderer, "Czas: " + formatTime(timeElapsed), centerX + 10, centerY + 40, 0xAAAAAA, false);
    }

    private String formatTime(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    enum Tab { GLOWNE, WYRZUCANIE, KOMENDY }
}
