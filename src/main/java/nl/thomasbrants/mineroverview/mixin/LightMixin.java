/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.DimensionType;
import nl.thomasbrants.mineroverview.light.LightLevelManger;
import nl.thomasbrants.mineroverview.light.LightLevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(World.class)
public abstract class LightMixin implements BlockView, WorldAccess {
    @Shadow @Final private RegistryKey<DimensionType> dimension;

    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    @Inject(method = "onBlockChanged", at = @At("RETURN"))
    public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock,
                               CallbackInfo ci) {
        if (!this.dimension.getValue().getPath().contains("overworld") || !LightLevelStorage.PLAYER_MOVED) return;
        if (newBlock.getFluidState() != Fluids.EMPTY.getDefaultState()) return;
        if (oldBlock.getLuminance() == newBlock.getLuminance() && oldBlock.getOpacity(this, pos) == newBlock.getOpacity(this, pos)) return;

        LightLevelManger.getInstance().updateBlockLight(this, pos.asLong(), getLuminance(pos));
    }
}
