/**
 * Miner Overview © 2023 by Thomas (DJ1TJOO) is licensed under CC BY-NC 4.0. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc/4.0/
 */

package nl.thomasbrants.mineroverview.light;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class LightHighlightRenderer {
    private static final LightHighlightRenderer INSTANCE = new LightHighlightRenderer();

    public static final float PLANE_DEPTH = 0.4999f;
    public static final float PLANE_OFFSET = 0.5f;
    
    private final MinecraftClient client;
    private final List<Long> highlightedBlocks;
    public LightHighlightRenderer() {
        this.client = MinecraftClient.getInstance();
        this.highlightedBlocks = new ArrayList<>();
    }

    public void addHighlightedBlock(long pos) {
        if (highlightedBlocks.contains(pos)) return;
        highlightedBlocks.add(pos);
    }

    public void removeHighlightedBlock(long pos) {
        highlightedBlocks.remove(pos);
    }

    public void clearHighlightedBlocks() {
        highlightedBlocks.clear();
    }

    public void render(WorldRenderContext context) {
        Camera camera = context.camera();
        World world = context.world();
        if (!camera.isReady() || client.getEntityRenderDispatcher().gameOptions == null) return;

        for (long pos : highlightedBlocks) {
            renderBlockHighlight(camera, world, BlockPos.fromLong(pos));
        }
    }

    private void renderBlockHighlight(Camera camera, World world, BlockPos pos) {
        List<Direction> highlightDirections = getHighlightDirections(world, pos);

        // Setup
        float x = pos.getX() + 0.5f;
        float y = pos.getY() + 0.5f;
        float z = pos.getZ() + 0.5f;
        float size = 1f;

        double d = camera.getPos().x;
        double e = camera.getPos().y;
        double f = camera.getPos().z;

        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate((float)(x - d), (float)(y - e), (float)(z - f));
        matrixStack.scale(size, -size, size);
        matrixStack.scale(-1.0F, 1.0F, 1.0F);

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        RenderSystem.depthMask(true);

        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.setShaderColor(0, 1, 0, 0.1F);

        // Render
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        createHighlightVertices(highlightDirections, buffer);

        RenderSystem.applyModelViewMatrix();
        tessellator.draw();

        // Reset
        matrixStack.pop();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.applyModelViewMatrix();
    }

    private void createHighlightVertices(List<Direction> highlightDirections, BufferBuilder buffer) {
        for (Direction direction : highlightDirections) {
            switch (direction) {
                case UP -> {
                    buffer.vertex(-PLANE_OFFSET, -PLANE_DEPTH, PLANE_OFFSET).color(0).next();
                    buffer.vertex(PLANE_OFFSET, -PLANE_DEPTH, PLANE_OFFSET).color(0).next();
                    buffer.vertex(PLANE_OFFSET, -PLANE_DEPTH, -PLANE_OFFSET).color(0).next();
                    buffer.vertex(-PLANE_OFFSET, -PLANE_DEPTH, -PLANE_OFFSET).color(0).next();
                }
                case DOWN -> {
                    buffer.vertex(PLANE_OFFSET, PLANE_DEPTH, PLANE_OFFSET).color(0).next();
                    buffer.vertex(-PLANE_OFFSET, PLANE_DEPTH, PLANE_OFFSET).color(0).next();
                    buffer.vertex(-PLANE_OFFSET, PLANE_DEPTH, -PLANE_OFFSET).color(0).next();
                    buffer.vertex(PLANE_OFFSET, PLANE_DEPTH, -PLANE_OFFSET).color(0).next();
                }
                case NORTH -> {
                    buffer.vertex(-PLANE_OFFSET, -PLANE_OFFSET, -PLANE_DEPTH).color(0).next();
                    buffer.vertex(PLANE_OFFSET, -PLANE_OFFSET, -PLANE_DEPTH).color(0).next();
                    buffer.vertex(PLANE_OFFSET, PLANE_OFFSET, -PLANE_DEPTH).color(0).next();
                    buffer.vertex(-PLANE_OFFSET, PLANE_OFFSET, -PLANE_DEPTH).color(0).next();
                }
                case EAST -> {
                    buffer.vertex(-PLANE_DEPTH, -PLANE_OFFSET, PLANE_OFFSET).color(0).next();
                    buffer.vertex(-PLANE_DEPTH, -PLANE_OFFSET, -PLANE_OFFSET).color(0).next();
                    buffer.vertex(-PLANE_DEPTH, PLANE_OFFSET, -PLANE_OFFSET).color(0).next();
                    buffer.vertex(-PLANE_DEPTH, PLANE_OFFSET, PLANE_OFFSET).color(0).next();
                }
                case SOUTH -> {
                    buffer.vertex(-PLANE_OFFSET, -PLANE_OFFSET, PLANE_DEPTH).color(0).next();
                    buffer.vertex(-PLANE_OFFSET, PLANE_OFFSET, PLANE_DEPTH).color(0).next();
                    buffer.vertex(PLANE_OFFSET, PLANE_OFFSET, PLANE_DEPTH).color(0).next();
                    buffer.vertex(PLANE_OFFSET, -PLANE_OFFSET, PLANE_DEPTH).color(0).next();
                }
                case WEST -> {
                    buffer.vertex(PLANE_DEPTH, -PLANE_OFFSET, -PLANE_OFFSET).color(0).next();
                    buffer.vertex(PLANE_DEPTH, -PLANE_OFFSET, PLANE_OFFSET).color(0).next();
                    buffer.vertex(PLANE_DEPTH, PLANE_OFFSET, PLANE_OFFSET).color(0).next();
                    buffer.vertex(PLANE_DEPTH, PLANE_OFFSET, -PLANE_OFFSET).color(0).next();
                }
            }
        }
    }

    private List<Direction> getHighlightDirections(World world, BlockPos pos) {
        List<Direction> placeableDirections = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            BlockPos neighbourPos = pos.offset(direction);

            VoxelShape collisionShapeNeighbour = world.getBlockState(neighbourPos).getCollisionShape(
                world, neighbourPos);
            if (!VoxelShapes.isSideCovered(VoxelShapes.fullCube(), collisionShapeNeighbour, direction)) continue;

            placeableDirections.add(direction);
        }

        return placeableDirections;
    }

    public static LightHighlightRenderer getInstance() {
        return INSTANCE;
    }
}
