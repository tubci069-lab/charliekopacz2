package com.charlie.mixin;

import com.charlie.Config;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(at = @At("HEAD"), method = "onWindowFocusChanged(Z)V", cancellable = true)
    private void onWindowFocusChanged(boolean focused, CallbackInfo ci) {
        // Jeśli mod jest włączony, zapobiegamy pauzowaniu po utracie fokusu
        if (Config.enabled && !focused) {
            ci.cancel();
        }
    }
}
