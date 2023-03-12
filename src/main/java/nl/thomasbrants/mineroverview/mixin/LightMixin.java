package nl.thomasbrants.mineroverview.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import nl.thomasbrants.mineroverview.light.LightLevelStorage;
import nl.thomasbrants.mineroverview.light.LightLevelStorageEntry;
import nl.thomasbrants.mineroverview.light.QueueEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(World.class)
public abstract class LightMixin implements WorldAccess {
    // TODO: find new unknown sources on player move.
    // TODO: recalculate on block place/break
    // TODO: make data struct more efficient.
    // TODO: reset on world change
    private final ArrayDeque<QueueEntry> queue = new ArrayDeque<>();

    @Inject(method = "onBlockChanged", at = @At("RETURN"))
    public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock,
                               CallbackInfo ci) {
        assert MinecraftClient.getInstance().world != null;
        updateBlockLight(pos.asLong(), MinecraftClient.getInstance().world.getLuminance(pos));
    }

    private int getLocalLightLevel(long pos) {
        return LightLevelStorage.LIGHT_LEVELS.containsKey(pos) ? LightLevelStorage.LIGHT_LEVELS.get(pos).value : Integer.MIN_VALUE; //getLightingProvider().get(LightType.BLOCK).getLightLevel(pos)
    }

    private void setLocalLightLevel(long pos, int lightLevel, long sourcePos) {
        LightLevelStorage.LIGHT_LEVELS.put(pos, new LightLevelStorageEntry(lightLevel, sourcePos));
    }

    private void updateBlockLight(long pos, int value) {
        long start = System.currentTimeMillis();

        if (value < 0 || value > getMaxLightLevel()) {
            throw new IllegalArgumentException();
        }

        int existingLevel = getLocalLightLevel(pos);

        boolean needPropagate = false;
        if (value <= existingLevel) {
            for (long childPos : LightLevelStorage.LIGHT_LEVELS.entrySet().stream().filter(x -> x.getValue().sourcePos == pos).map(
                Map.Entry::getKey).toList()
            ) {
                LightLevelStorage.LIGHT_LEVELS.remove(childPos);
            }

            Map<Long, LightLevelStorageEntry> sources = LightLevelStorage.LIGHT_LEVELS.entrySet().stream().filter(x -> x.getValue().sourcePos == x.getKey() && BlockPos.fromLong(x.getKey()).isWithinDistance(BlockPos.fromLong(pos), getMaxLightLevel()*2)).collect(
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (sources.size() > 0) needPropagate = true;

            for (Map.Entry<Long, LightLevelStorageEntry> entry : sources.entrySet()) {
                queue.add(new QueueEntry(entry.getKey(), entry.getValue()));
            }
        }

        if (value > 0) {
            queue.add(new QueueEntry(pos, new LightLevelStorageEntry(value, pos)));
            setLocalLightLevel(pos, value, pos);

            needPropagate = true;
        }

        if (needPropagate) {
            this.propagateIncrease();
        }

        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println("It took: " + time + "ms");
    }

    private void propagateIncrease() {
        while (!queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            long pos = entry.pos;
            long sourcePos = entry.value.sourcePos;
            int lightValue = entry.value.value;

            for (Direction direction : Direction.stream().toList()) {
                BlockPos neighbourBlockPos = BlockPos.fromLong(pos).offset(direction);
                long neighbourPos = neighbourBlockPos.asLong();

                int currentLevel = getLocalLightLevel(neighbourPos);
                if (currentLevel >= (lightValue - 1)) {
                    continue;
                }

                int neighbourStateOpacity = getBlockState(neighbourBlockPos).getOpacity(this, neighbourBlockPos);
                if (neighbourStateOpacity == getMaxLightLevel()) {
                    setLocalLightLevel(neighbourPos, 0, sourcePos);
                    continue;
                }

                int targetLevel = lightValue - Math.max(1, neighbourStateOpacity);
                if (targetLevel < -getMaxLightLevel()) continue;
                if (targetLevel > currentLevel) {
                    setLocalLightLevel(neighbourPos, targetLevel, sourcePos);
                    queue.add(new QueueEntry(neighbourPos, new LightLevelStorageEntry(targetLevel, sourcePos)));
                }
            }
        }
    }
}
