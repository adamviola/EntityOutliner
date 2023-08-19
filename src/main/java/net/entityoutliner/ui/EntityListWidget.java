package net.entityoutliner.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.entityoutliner.ui.ColorWidget.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Language;

@Environment(EnvType.CLIENT)
public class EntityListWidget extends ElementListWidget<EntityListWidget.Entry> {

    public EntityListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
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

    @Environment(EnvType.CLIENT)
    public static abstract class Entry extends ElementListWidget.Entry<EntityListWidget.Entry> { }

    @Environment(EnvType.CLIENT)
    public static class EntityEntry extends EntityListWidget.Entry {

        private final CheckboxWidget checkbox;
        private final ColorWidget color;
        private final EntityType<?> entityType;
        private final List<PressableWidget> children = new ArrayList<>();

        private EntityEntry(CheckboxWidget checkbox, ColorWidget color, EntityType<?> entityType) {
            this.checkbox = checkbox;
            this.entityType = entityType;
            this.color = color;

            this.children.add(checkbox);
            if (EntitySelector.outlinedEntityTypes.containsKey(entityType))
                this.children.add(color);
        }

        public static EntityListWidget.EntityEntry create(EntityType<?> entityType, int width) {
            return new EntityListWidget.EntityEntry(
                new CheckboxWidget(width / 2 - 155, 0, 310, 20, entityType.getName(), EntitySelector.outlinedEntityTypes.containsKey(entityType)),
                new ColorWidget(width / 2 + 130, 0, 310, 20, entityType),
                entityType
            );
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.checkbox.setY(y);
            this.checkbox.render(context, mouseX, mouseY, tickDelta);

            if (this.children.contains(this.color)) {
                this.color.setY(y);
                this.color.render(context, mouseX, mouseY, tickDelta);
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (EntitySelector.outlinedEntityTypes.containsKey(entityType)) {
                if (this.color.isMouseOver(mouseX, mouseY)) {
                    this.color.onPress();
                } else {
                    EntitySelector.outlinedEntityTypes.remove(entityType);

                    this.checkbox.onPress();
                    this.children.remove(this.color);
                }
            } else {
                EntitySelector.outlinedEntityTypes.put(entityType, Color.of(entityType.getSpawnGroup()));

                this.color.onShow();
                this.checkbox.onPress();
                this.children.add(this.color);
            }

            return true;
        }

        public List<? extends Element> children() {
            return this.children;
        }

        public EntityType<?> getEntityType() {
            return this.entityType;
        }

        public CheckboxWidget getCheckbox() {
            return this.checkbox;
        }

        public List<? extends Selectable> selectableChildren() {
            return this.children;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class HeaderEntry extends EntityListWidget.Entry {

        private final TextRenderer font;
        private final String title;
        private final int width;
        private final int height;

        private HeaderEntry(SpawnGroup category, TextRenderer font, int width, int height) {
            this.font = font;
            this.width = width;
            this.height = height;

            if (category != null) {
                String title = "";
                for (String term : category.getName().split("\\p{Punct}|\\p{Space}")) {
                    title += StringUtils.capitalize(term) + " ";
                }
                this.title = title.trim();
            } else {
                this.title = Language.getInstance().get("gui.entity-outliner.no_results");
            }

        }

        public static EntityListWidget.HeaderEntry create(SpawnGroup category, TextRenderer font, int width, int height) {
            return new EntityListWidget.HeaderEntry(category, font, width, height);
        }

        public void render(DrawContext context, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            context.drawCenteredTextWithShadow(this.font, this.title, this.width / 2, j + (this.height / 2) - (this.font.fontHeight / 2), 16777215);
        }

        public List<? extends Element> children() {
            return new ArrayList<>();
        }

        public String toString() {
            return this.title;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return new ArrayList<>();
        }
    }
}
