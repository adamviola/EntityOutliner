package net.entityoutliner.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.entityoutliner.EntityOutliner;
import net.entityoutliner.ui.ColorWidget.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.registry.Registries;

public class EntitySelector extends Screen {
    protected final Screen parent;

    private TextFieldWidget searchField;
    private EntityListWidget list;
    public static boolean groupByCategory = true;
    private static String searchText = "";
    public static HashMap<String, List<EntityType<?>>> searcher; // Prefix -> arr of results
    public static HashMap<EntityType<?>, Color> outlinedEntityTypes = new HashMap<>();
 
    public EntitySelector(Screen parent) {
       super(Text.translatable("title.entity-outliner.selector"));
       this.parent = parent;
    }
 
    public void onClose() {
        this.client.setScreen(this.parent);
    }

    protected void init() {
        if (searcher == null) {
            initializePrefixTree();
        }

        this.list = new EntityListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        this.addSelectableChild(list);

        // Create search field
        this.searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 6, 200, 20, Text.of(searchText));
        this.searchField.setText(searchText);
        this.searchField.setChangedListener(this::onSearchFieldUpdate);
        this.addSelectableChild(searchField);

        // Create buttons
        int buttonWidth = 80;
        int buttonHeight = 20;
        int buttonInterval = (this.width - 4 * buttonWidth) / 5;
        int buttonOffset = buttonInterval;
        int buttonY = this.height - 16 - (buttonHeight / 2);

        // Add sort type button
        // this.addDrawableChild(new ButtonWidget(buttonOffset, buttonY, buttonWidth, buttonHeight, Text.translatable(groupByCategory ? "button.entity-outliner.categories" : "button.entity-outliner.no-categories"), (button) -> {
        //     groupByCategory = !groupByCategory;
        //     this.onSearchFieldUpdate(this.searchField.getText());
        //     button.setMessage(Text.translatable(groupByCategory ? "button.entity-outliner.categories" : "button.entity-outliner.no-categories"));
        // }));

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.translatable(groupByCategory ? "button.entity-outliner.categories" : "button.entity-outliner.no-categories"),
                (button) -> {
                    groupByCategory = !groupByCategory;
                    this.onSearchFieldUpdate(this.searchField.getText());
                    button.setMessage(Text.translatable(groupByCategory ? "button.entity-outliner.categories" : "button.entity-outliner.no-categories"));
                }
            ).size(buttonWidth, buttonHeight).position(buttonOffset, buttonY).build()
        );

        // Add Deselect All button
        // this.addDrawableChild(new ButtonWidget(buttonOffset + (buttonWidth + buttonInterval), buttonY, buttonWidth, buttonHeight, Text.translatable("button.entity-outliner.deselect"), (button) -> {
        //     outlinedEntityTypes.clear();
        //     this.onSearchFieldUpdate(this.searchField.getText());
        // }));

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.translatable("button.entity-outliner.deselect"),
                (button) -> {
                    outlinedEntityTypes.clear();
                    this.onSearchFieldUpdate(this.searchField.getText());
                }
            ).size(buttonWidth, buttonHeight).position(buttonOffset + (buttonWidth + buttonInterval), buttonY).build()
        );

        // Add toggle outlining button
        // this.addDrawableChild(new ButtonWidget(buttonOffset + (buttonWidth + buttonInterval) * 2, buttonY, buttonWidth, buttonHeight, Text.translatable(EntityOutliner.outliningEntities ? "button.entity-outliner.on" : "button.entity-outliner.off"), (button) -> {
        //     EntityOutliner.outliningEntities = !EntityOutliner.outliningEntities;
        //     button.setMessage(Text.translatable(EntityOutliner.outliningEntities ? "button.entity-outliner.on" : "button.entity-outliner.off"));
        // }));

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.translatable(EntityOutliner.outliningEntities ? "button.entity-outliner.on" : "button.entity-outliner.off"),
                (button) -> {
                    EntityOutliner.outliningEntities = !EntityOutliner.outliningEntities;
                    button.setMessage(Text.translatable(EntityOutliner.outliningEntities ? "button.entity-outliner.on" : "button.entity-outliner.off"));
                }
            ).size(buttonWidth, buttonHeight).position(buttonOffset + (buttonWidth + buttonInterval) * 2, buttonY).build()
        );

        // Add Done button
        // this.addDrawableChild(new ButtonWidget(buttonOffset + (buttonWidth + buttonInterval) * 3, buttonY, buttonWidth, buttonHeight, Text.translatable("button.entity-outliner.done"), (button) -> {
        //     this.client.setScreen(null);
        // }));

        this.addDrawableChild(
            ButtonWidget.builder(
                Text.translatable("button.entity-outliner.done"),
                (button) -> { this.client.setScreen(null); }
            ).size(buttonWidth, buttonHeight).position(buttonOffset + (buttonWidth + buttonInterval) * 3, buttonY).build()
        );
        
        this.setInitialFocus(this.searchField);
        this.onSearchFieldUpdate(this.searchField.getText());
    }

    // Initializes the prefix tree used for searching in the entity selector screen
    private void initializePrefixTree() {
        EntitySelector.searcher = new HashMap<>();

        // Initialize no-text results
        List<EntityType<?>> allResults =  new ArrayList<EntityType<?>>();
        EntitySelector.searcher.put("", allResults);

        // Get sorted list of entity types
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
            entityTypes.add(entityType);
        }
        entityTypes.sort(new Comparator<EntityType<?>>() {
            @Override
            public int compare(EntityType<?> o1, EntityType<?> o2) {
                return o1.getName().getString().compareTo(o2.getName().getString());
            }
        });
        
        // Add each entity type to everywhere it belongs in the prefix "tree"
        for (EntityType<?> entityType : entityTypes) {

            String name = entityType.getName().getString().toLowerCase();
            allResults.add(entityType);

            List<String> prefixes = new ArrayList<>();
            prefixes.add("");

            // By looping over the name's length, we add to every possible prefix
            for (int i = 0; i < name.length(); i++) {
                char character = name.charAt(i);

                // Loop over every prefix
                for (int p = 0; p < prefixes.size(); p++) {
                    String prefix = prefixes.get(p) + character;
                    prefixes.set(p, prefix);

                    // Get results for current prefix
                    List<EntityType<?>> results;
                    if (EntitySelector.searcher.containsKey(prefix)) {
                        results = EntitySelector.searcher.get(prefix);
                    } else {
                        results = new ArrayList<EntityType<?>>();
                        EntitySelector.searcher.put(prefix, results);
                    }

                    results.add(entityType);
                }

                // Add another prefix to allow searching by second/third/... word
                if (Character.isWhitespace(character)) {
                    prefixes.add("");
                }
            }
        }
    }

    // Callback provided to TextFieldWidget triggered when its text updates
    private void onSearchFieldUpdate(String text) {
        searchText = text;
        text = text.toLowerCase().trim();

        this.list.clearListEntries();

        if (searcher.containsKey(text)) {
            List<EntityType<?>> results = searcher.get(text);
            
            // Splits results into categories and separates them with headers
            if (groupByCategory) {
                HashMap<SpawnGroup, List<EntityType<?>>> resultsByCategory = new HashMap<>();

                for (EntityType<?> entityType : results) {
                    SpawnGroup category = entityType.getSpawnGroup();
                    if (!resultsByCategory.containsKey(category)) {
                        resultsByCategory.put(category, new ArrayList<>());
                    }

                    resultsByCategory.get(category).add(entityType);
                }

                for (SpawnGroup category : SpawnGroup.values()) {
                    if (resultsByCategory.containsKey(category)) {
                        this.list.addListEntry(EntityListWidget.HeaderEntry.create(category, this.client.textRenderer, this.width, 25));

                        for (EntityType<?> entityType : resultsByCategory.get(category)) {
                            this.list.addListEntry(EntityListWidget.EntityEntry.create(entityType, this.width));
                        }

                    }      
                }

            } else {
                for (EntityType<?> entityType : results) {
                    this.list.addListEntry(EntityListWidget.EntityEntry.create(entityType, this.width));
                }
            }
        } else { // If there are no results, let the user know
            this.list.addListEntry(EntityListWidget.HeaderEntry.create(null, this.client.textRenderer, this.width, 25));
        }

        // This prevents an overscroll when the user is already scrolled down and the results list is shortened
        this.list.setScrollAmount(this.list.getScrollAmount());
    }

    // Called when config screen is escaped
    public void removed() {
        EntityOutliner.saveConfig();
    }

    public void tick() {
        this.searchField.tick();
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render dirt background
        this.renderBackground(context); 

        // Render scrolling list
        this.list.render(context, mouseX, mouseY, delta);

        // Render our search bar
        this.setFocused(this.searchField);
        //this.searchField.setTextFieldFocused(true);
        this.searchField.render(context, mouseX, mouseY, delta);

        // Render buttons
        super.render(context, mouseX, mouseY, delta);
    }

    // Sends mouseDragged event to the scrolling list
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.list.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
