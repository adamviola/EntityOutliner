package net.entityoutliner;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;

public class EntityOutliner implements ClientModInitializer {

    public static boolean outliningEntities;

    private static KeyBinding configBind;
    private static KeyBinding outlineBind;

	@Override
	public void onInitializeClient() {
        configBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.entity-outliner.selector", // Keybind name
            InputUtil.Type.KEYSYM,          // Keybind type
            GLFW.GLFW_KEY_P,                // Keycode
            "title.entity-outliner.title"   // Keybind category
        ));

        outlineBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.entity-outliner.outline",  // Keybind name
            InputUtil.Type.KEYSYM,          // Keybind type
            GLFW.GLFW_KEY_O,                // Keycode
            "title.entity-outliner.title"   // Keybind category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (outlineBind.wasPressed()) {
                outliningEntities = !outliningEntities;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configBind.isPressed()) {
                if (client != null) {                    
                    client.openScreen(new EntitySelector(new TranslatableText("title.entity-outliner.selector")));
                }
            }
        });
    }
}