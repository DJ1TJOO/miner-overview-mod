package nl.thomasbrants.mineroverview;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import nl.thomasbrants.mineroverview.config.ModConfig;
import nl.thomasbrants.mineroverview.hud.GameMinerHud;
import nl.thomasbrants.mineroverview.light.LightHighlightRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MinerOverviewMod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("miner_overview");
	private static GameMinerHud overviewHud;
	private static KeyBinding toggleSlotKeyBinding;
	private static boolean toggleSlotPressed = false;
	private static ItemGroup createInventoryTab;

	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		LOGGER.info("Loaded!");

		LOGGER.info("Registering config");
		ConfigHolder<ModConfig>
			configHolder = AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
		ModConfig config = configHolder.getConfig();

		LOGGER.info("Registering keybindings");
		KeyBinding hudToggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.miner_overview.hud_toggle",
			GLFW.GLFW_KEY_M,
			"category.miner_overview.keybindings"
		));

		toggleSlotKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.miner_overview.toggle_slot",
			GLFW.GLFW_KEY_LEFT_ALT,
			"category.miner_overview.keybindings"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			if (hudToggleKeyBinding.wasPressed()) {
				config.toggleHud = !config.toggleHud;
				configHolder.save();
			}
		});

		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
			LightHighlightRenderer.getInstance().render(context);
		});
	}

	public static GameMinerHud getOverviewHud() {
		return overviewHud;
	}

	public static void setOverviewHud(GameMinerHud overviewHud) {
		MinerOverviewMod.overviewHud = overviewHud;
	}

	public static boolean isToggleSlotPressed() {
		return toggleSlotPressed;
	}

	public static void handleInput(MinecraftClient client, Screen currentScreen, long window,
								   int key, int action, int scancode) {
		if (toggleSlotKeyBinding == null) return;
		toggleSlotPressed = toggleSlotKeyBinding.wasPressed() || action != 0 && toggleSlotKeyBinding.matchesKey(key, scancode);
	}

	public static ItemGroup getCreateInventoryTab() {
		return createInventoryTab;
	}

	public static void setCreateInventoryTab(ItemGroup createInventoryTab) {
		MinerOverviewMod.createInventoryTab = createInventoryTab;
	}
}
