package nl.thomasbrants.mineroverview;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinerOverviewMod implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("miner_overview");

	/**
	 * Runs the mod initializer on the client environment.
	 */
	@Override
	public void onInitializeClient() {
		LOGGER.info("Loaded!");

		KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.miner_overview.hud_toggle",
			GLFW.GLFW_KEY_M,
			"category.miner_overview.keybindings"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			if (keyBinding.wasPressed()) {
				client.player.sendMessage(Text.literal("Key 1 was pressed!"), false);
			}
		});
	}
}
