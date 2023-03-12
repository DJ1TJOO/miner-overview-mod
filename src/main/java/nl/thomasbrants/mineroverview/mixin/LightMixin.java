package nl.thomasbrants.mineroverview.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import nl.thomasbrants.mineroverview.light.LightLevelManger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class LightMixin implements BlockView {
    @Shadow @Final private RegistryKey<DimensionType> dimension;

    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    @Inject(method = "onBlockChanged", at = @At("RETURN"))
    public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock,
                               CallbackInfo ci) {
        if (!this.dimension.getValue().getPath().contains("overworld")) return;
        LightLevelManger.getInstance().updateBlockLight(this, pos.asLong(), getLuminance(pos));
    }
}
