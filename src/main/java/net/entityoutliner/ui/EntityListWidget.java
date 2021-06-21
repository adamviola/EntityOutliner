package net.entityoutliner.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;

@Environment(EnvType.CLIENT)
public class EntityListWidget extends ElementListWidget<EntityListWidget.Entry> {

    private BiConsumer<EntityType<?>, Boolean> clickCallback;

    public EntityListWidget(MinecraftClient client, int width, int height, int top, int bottom,  int itemHeight, BiConsumer<EntityType<?>, Boolean> clickCallback) {
        super(client, width, height, top, bottom, itemHeight);
        this.clickCallback = clickCallback;
        this.centerListVertically = false;
    }
    
    public void addListEntry(EntityListWidget.Entry entry) {
        super.addEntry(entry);
    }

    public void clearListEntries() {
        super.clearEntries();
    }

    public int getRowWidth() {
        return 400;
    }

    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 32;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);

        // Calls click callback to inform the config about which checkbox was clicked
        Entry clickedEntry = this.getEntryAtPosition(mouseX, mouseY);

        if (clickedEntry != null && clickedEntry.getClass() == EntityListWidget.CheckboxEntry.class) {
            CheckboxEntry clickedCheckbox = (CheckboxEntry) clickedEntry;
            clickCallback.accept(clickedCheckbox.getEntityType(), clickedCheckbox.getCheckbox().isChecked());
        }
        
        return result;
    }

    // Need this because DrawableHelper's drawCenteredString is not static (???)
    private static class Drawer extends DrawableHelper {}

    @Environment(EnvType.CLIENT)
    public static abstract class Entry extends ElementListWidget.Entry<EntityListWidget.Entry> { }

    @Environment(EnvType.CLIENT)
    public static class CheckboxEntry extends EntityListWidget.Entry {

        private final CheckboxWidget checkbox;
        private final EntityType<?> entityType;

        private CheckboxEntry(CheckboxWidget checkbox, EntityType<?> entityType) {
            this.checkbox = checkbox;
            this.entityType = entityType;
        }

        public static EntityListWidget.CheckboxEntry create(EntityType<?> entityType, boolean checked, int width) {
            return new EntityListWidget.CheckboxEntry(new CheckboxWidget(width/2 - 155, 0, 310, 20, entityType.getName(), checked), entityType);
        }

        public void render(MatrixStack matrices, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.checkbox.y = j;
            this.checkbox.render(matrices, n, o, f);
        }

        public List<? extends Element> children() {
            return List.of(this.checkbox);
        }

        public EntityType<?> getEntityType() {
            return this.entityType;
        }

        public CheckboxWidget getCheckbox() {
            return this.checkbox;
        }

        public List<? extends Selectable> method_37025() {
            return List.of(this.checkbox);
        }
    }

    @Environment(EnvType.CLIENT)
    public static class HeaderEntry extends EntityListWidget.Entry {

        private final TextRenderer font;
        private final String title;
        private final int width;
        private final int height;

        // private final Drawer drawer;

        private HeaderEntry(TextRenderer font, String title, int width, int height) {
            this.font = font;
            this.title = title;
            this.width = width;
            this.height = height;

            // this.drawer = new Drawer();
        }

        public static EntityListWidget.HeaderEntry create(TextRenderer font, String title, int width, int height) {
            return new EntityListWidget.HeaderEntry(font, title, width, height);
        }

        public void render(MatrixStack matrices, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            Drawer.drawCenteredText(matrices, this.font, this.title, this.width / 2, j + (this.height / 2) - (this.font.fontHeight / 2), 16777215);
        }

        public List<? extends Element> children() {
            return new ArrayList<>();
        }

        public String toString() {
            return this.title;
        }

        @Override
        public List<? extends Selectable> method_37025() {
            return new ArrayList<>();
        }
    }
}
