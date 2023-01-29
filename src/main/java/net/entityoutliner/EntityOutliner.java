package net.entityoutliner;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.lwjgl.glfw.GLFW;

import net.entityoutliner.ui.EntitySelector;
import net.entityoutliner.ui.ColorWidget.Color;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

public class EntityOutliner implements ClientModInitializer {
    private static final Gson GSON = new Gson();
    public static boolean outliningEntities;

    private static final KeyBinding CONFIG_BIND = new KeyBinding(
        "key.entity-outliner.selector",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_SEMICOLON,
        "title.entity-outliner.title"
    );

    private static final KeyBinding OUTLINE_BIND = new KeyBinding(
        "key.entity-outliner.outline",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_O,
        "title.entity-outliner.title"
    );

	@Override
	public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(CONFIG_BIND);
        KeyBindingHelper.registerKeyBinding(OUTLINE_BIND);

        loadConfig();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("entityoutliner.json");
    }

    public static void saveConfig() {
        JsonObject config = new JsonObject();

        List<List<String>> outlinedEntityNames = EntitySelector.outlinedEntityTypes.entrySet().stream()
            .map(entry -> List.of(EntityType.getId(entry.getKey()).toString(), entry.getValue().name()))
            .collect(Collectors.toList());

        config.add("outlinedEntities", GSON.toJsonTree(outlinedEntityNames));

        try {
            Files.write(getConfigPath(), GSON.toJson(config).getBytes());
        }
        catch (IOException ex) {
            logException(ex, "Failed to save EntityOutliner config");
        }
    }

    private void loadConfig() {
        try {
            JsonObject config = GSON.fromJson(new String(Files.readAllBytes(getConfigPath())), JsonObject.class);
            if (config.has("outlinedEntities")) {
                Type setType = new TypeToken<List<List<String>>>(){}.getType();
                List<List<String>> outlinedEntityNames = GSON.fromJson(config.get("outlinedEntities"), setType);

                Map<EntityType<?>, Color> outlinedEntityTypes = outlinedEntityNames.stream()
                    .collect(Collectors.toMap(list -> EntityType.get(list.get(0)).get(), list -> Color.valueOf(list.get(1))));;

                for (EntityType<?> entityType : Registries.ENTITY_TYPE)
                    if (outlinedEntityTypes.containsKey(entityType))
                        EntitySelector.outlinedEntityTypes.put(entityType, outlinedEntityTypes.get(entityType));
            }
        }
        catch (IOException | JsonSyntaxException ex) {
            logException(ex, "Failed to load EntityOutliner config");
        }
    }

    private void onEndTick(MinecraftClient client) {
        while (OUTLINE_BIND.wasPressed()) {
            outliningEntities = !outliningEntities;
        }

        if (CONFIG_BIND.isPressed()) {           
            client.setScreen(new EntitySelector(null));
        }
    }

    public static void logException(Exception ex, String message) {
        System.err.printf("[EntityOutliner] %s (%s: %s)", message, ex.getClass().getSimpleName(), ex.getLocalizedMessage());
    }
}