package net.entityoutliner;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;

public class EntityOutliner implements ClientModInitializer {

    private FabricKeyBinding configBind;
    private FabricKeyBinding outlineBind;

    private boolean prevPressed;
    public static boolean outliningEntities;

	@Override
	public void onInitializeClient() {
        setupKeybinds(); 
    }

    private void setupKeybinds() {
        KeyBindingRegistry.INSTANCE.addCategory("Entity Outliner");

        // Uses fabric's keybinding system to add the binds
        configBind = FabricKeyBinding.Builder.create(
            new Identifier("entity-outliner", "selector"),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "Entity Outliner"
        ).build();

        outlineBind = FabricKeyBinding.Builder.create(
            new Identifier("entity-outliner", "outline"),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "Entity Outliner"
        ).build();

        KeyBindingRegistry.INSTANCE.register(configBind);
        KeyBindingRegistry.INSTANCE.register(outlineBind);
        
        // Register callback that opens entity selector
        ClientTickCallback.EVENT.register(e -> {
            if (configBind.isPressed()) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client != null) {                    
                    client.openScreen(new EntitySelector(new TranslatableText("title.entity-outliner.selector")));
                }
            }
        });

        // Register callback that toggles entity outlining
        ClientTickCallback.EVENT.register(e -> {
            if (outlineBind.isPressed()) {
                if (!prevPressed) {
                    outliningEntities = !outliningEntities;
                    prevPressed = true;
                }
            } else {
                prevPressed = false;
            } // The ClientTickCallback is called every tick, so this logic prevents rapid toggling when key is held >1 tick
        });
    }
}