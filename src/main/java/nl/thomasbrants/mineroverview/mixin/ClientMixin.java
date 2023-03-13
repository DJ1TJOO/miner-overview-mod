/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import nl.thomasbrants.mineroverview.light.LightHighlightRenderer;
import nl.thomasbrants.mineroverview.light.LightLevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientMixin {
    @Inject(method = "joinWorld", at = @At("RETURN"))
    private void joinWorld(ClientWorld world, CallbackInfo ci) {
        // Reset light levels
        LightLevelStorage.LIGHT_LEVELS.clear();
        LightHighlightRenderer.getInstance().clearHighlightedBlocks();
    }
}
