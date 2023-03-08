package nl.thomasbrants.mineroverview.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.thomasbrants.mineroverview.MinerOverviewMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * World block update mixin.
 */
@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "onBlockChanged", at = @At("RETURN"))
    public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock,
                               CallbackInfo ci) {
        if (MinerOverviewMod.getOverviewHud() == null) return;
        MinerOverviewMod.getOverviewHud().handleBlockUpdate(pos, oldBlock, newBlock);
    }
}
