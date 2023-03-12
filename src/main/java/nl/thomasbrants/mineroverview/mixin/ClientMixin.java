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
