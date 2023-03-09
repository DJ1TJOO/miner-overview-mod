package nl.thomasbrants.mineroverview.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import nl.thomasbrants.mineroverview.MinerOverviewMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At(value = "INVOKE",
                                        target = "Lnet/minecraft/client/gui/screen/Screen;wrapScreenError(Ljava/lang/Runnable;Ljava/lang/String;Ljava/lang/String;)V",
                                        ordinal = 0))
    public void onKey(long window, int key, int scancode, int action, int modifiers,
                      CallbackInfo ci) {
           MinerOverviewMod.handleInput(client, client.currentScreen, window, key, action, scancode);
    }
}
