/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import nl.thomasbrants.mineroverview.light.LightLevelManger;
import nl.thomasbrants.mineroverview.light.LightLevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class PlayerMixin extends AbstractClientPlayerEntity {
    public PlayerMixin(ClientWorld world,
                       GameProfile profile) {
        super(world, profile);
    }

    private BlockPos lastUpdatedPos = null;

    @Inject(method = "move", at = @At("RETURN"))
    private void move(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        BlockPos playerPos = this.getBlockPos();

        if (lastUpdatedPos == playerPos) return;
        lastUpdatedPos = playerPos;

        World world = this.getWorld();
        int radius = world.getMaxLightLevel() * 2;
        for (int x = playerPos.getX() - radius; x < playerPos.getX() + radius; x++) {
            for (int y = playerPos.getY() - radius; y < playerPos.getY() + radius; y++) {
                for (int z = playerPos.getZ() - radius; z < playerPos.getZ() + radius; z++) {
                    int storedLightLevel = 0;
                    if (LightLevelStorage.LIGHT_LEVELS.containsKey(BlockPos.asLong(x,y,z))) {
                        storedLightLevel = LightLevelStorage.LIGHT_LEVELS.get(BlockPos.asLong(x,y,z)).value;
                    }

                    BlockPos pos = new BlockPos(x, y, z);
                    int luminance = world.getLuminance(pos);
                    if (luminance <= 0 || luminance <= storedLightLevel) continue;

                    LightLevelManger.getInstance().updateBlockLight(world, pos.asLong(), luminance);
                }
            }
        }
    }
}
