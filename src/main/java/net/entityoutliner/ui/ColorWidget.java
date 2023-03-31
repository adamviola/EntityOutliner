package net.entityoutliner.ui;

import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ColorWidget extends PressableWidget {
    private static final Identifier TEXTURE = new Identifier("entityoutliner:textures/gui/colors.png");
    private Color color;
    private EntityType<?> entityType;

    private ColorWidget(int x, int y, int width, int height, Text message, EntityType<?> entityType) {
        super(x, y, width, height, message);
        this.entityType = entityType;

        if (EntitySelector.outlinedEntityTypes.containsKey(this.entityType))
            onShow();
    }

    public ColorWidget(int x, int y, int width, int height, EntityType<?> entityType) {
        this(x, y, width, height, Text.translatable("options.chat.color"), entityType);
    }

    public void onShow() {
        this.color = EntitySelector.outlinedEntityTypes.get(this.entityType);
    }

    public void onPress() {
        this.color = this.color.next();
        EntitySelector.outlinedEntityTypes.put(this.entityType, this.color);
    }

    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        drawTexture(matrices, this.getX(), this.getY(), this.isFocused() ? 20.0F : 0.0F, this.color.ordinal() * 20, 20, 20, 40, 180);
    }

    public enum Color {
        WHITE(255, 255, 255),
        BLACK(0, 0, 0),
        RED(255, 0, 0),
        ORANGE(255, 127, 0),
        YELLOW(255, 255, 0),
        GREEN(0, 255, 0),
        BLUE(0, 0, 255),
        PURPLE(127, 0, 127),
        PINK(255, 155, 182);

        public int red;
        public int green;
        public int blue;

        private static final Map<SpawnGroup, Color> spawnGroupColors = Map.of(
            SpawnGroup.AMBIENT, Color.PURPLE,
            SpawnGroup.AXOLOTLS, Color.PINK,
            SpawnGroup.CREATURE, Color.YELLOW,
            SpawnGroup.MISC, Color.WHITE,
            SpawnGroup.MONSTER, Color.RED,
            SpawnGroup.UNDERGROUND_WATER_CREATURE, Color.ORANGE,
            SpawnGroup.WATER_AMBIENT, Color.GREEN,
            SpawnGroup.WATER_CREATURE, Color.BLUE
        );

        private static Color[] colors = Color.values();

        private Color(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public static Color of(SpawnGroup group) {
            return spawnGroupColors.get(group);
        }

        public Color next() {
            return get((this.ordinal() + 1) % colors.length);
        }

        public Color get(int index) {
            return colors[index];
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
