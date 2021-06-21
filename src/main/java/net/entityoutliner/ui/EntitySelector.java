package net.entityoutliner.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.entityoutliner.EntityOutliner;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;

public class EntitySelector extends Screen {
    protected final Screen parent;

    private TextFieldWidget searchField;
    private EntityListWidget list;
    public static boolean groupByCategory = true;
    private static String searchText = "";
    public static Hashtable<String, List<EntityType<?>>> searcher; // Prefix -> arr of results
    public static HashSet<EntityType<?>> outlinedEntityTypes = new HashSet<>();
 
    public EntitySelector(Screen parent) {
       super(new TranslatableText("title.entity-outliner.selector"));
       this.parent = parent;
    }
 
    public void onClose() {
        this.client.openScreen(this.parent);
    }

    protected void init() {
        this.client.keyboard.setRepeatEvents(true);

        if (searcher == null) {
            initializePrefixTree();
        }

        this.list = new EntityListWidget(this.client, this.width, this.height, 32, this.height - 32, 25, this::onCheckboxToggled);
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
        this.addDrawableChild(new ButtonWidget(buttonOffset, buttonY, buttonWidth, buttonHeight, new TranslatableText(groupByCategory ? "button.entity-outliner.categories" : "button.entity-outliner.no-categories"), (button) -> {
            groupByCategory = !groupByCategory;
            this.onSearchFieldUpdate(this.searchField.getText());
            button.setMessage(new TranslatableText(groupByCategory ? "button.entity-outliner.categories" : "button.entity-outliner.no-categories"));
        }));

        // Add Deselect All button
        this.addDrawableChild(new ButtonWidget(buttonOffset + (buttonWidth + buttonInterval), buttonY, buttonWidth, buttonHeight, new TranslatableText("button.entity-outliner.deselect"), (button) -> {
            outlinedEntityTypes.clear();
            this.onSearchFieldUpdate(this.searchField.getText());
        }));

        // Add toggle outlining button
        this.addDrawableChild(new ButtonWidget(buttonOffset + (buttonWidth + buttonInterval) * 2, buttonY, buttonWidth, buttonHeight, new TranslatableText(EntityOutliner.outliningEntities ? "button.entity-outliner.on" : "button.entity-outliner.off"), (button) -> {
            EntityOutliner.outliningEntities = !EntityOutliner.outliningEntities;
            button.setMessage(new TranslatableText(EntityOutliner.outliningEntities ? "button.entity-outliner.on" : "button.entity-outliner.off"));
        }));

        // Add Done button
        this.addDrawableChild(new ButtonWidget(buttonOffset + (buttonWidth + buttonInterval) * 3, buttonY, buttonWidth, buttonHeight, new TranslatableText("button.entity-outliner.done"), (button) -> {
            this.client.openScreen(null);
        }));
        
        this.setInitialFocus(this.searchField);
        this.onSearchFieldUpdate(this.searchField.getText());
    }

    // Initializes the prefix tree used for searching in the entity selector screen
    private void initializePrefixTree() {
        EntitySelector.searcher = new Hashtable<>();

        // Initialize no-text results
        List<EntityType<?>> allResults =  new ArrayList<EntityType<?>>();
        EntitySelector.searcher.put("", allResults);

        // Get sorted list of entity types
        List<EntityType<?>> entityTypes = new ArrayList<>();
        for (EntityType<?> entityType : Registry.ENTITY_TYPE) {
            entityTypes.add(entityType);
        }
        entityTypes.sort(new Comparator<EntityType<?>>() {
            @Override
            public int compare(EntityType<?> o1, EntityType<?> o2) {
                return o1.getName().asString().compareTo(o2.getName().asString());
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

    // Callback provided to CheckboxListWidget triggered when a checkbox is checked
    private void onCheckboxToggled(EntityType<?> entityType, boolean checked) {
        if (!checked && outlinedEntityTypes.contains(entityType)) {
            outlinedEntityTypes.remove(entityType);
        } else if (checked && !outlinedEntityTypes.contains(entityType)) {
            outlinedEntityTypes.add(entityType);
        }
    }

    // Cleans up the name of each category with capitalization
    // TODO: what about localization?
    private String getCategoryName(SpawnGroup category) {
        String name = "";
        for (String term : category.getName().trim().split("\\p{Punct}|\\p{Space}")) {
            name += StringUtils.capitalize(term) + " ";
        }
        return name.trim();
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
                Hashtable<SpawnGroup, List<EntityType<?>>> resultsByCategory = new Hashtable<>();

                for (EntityType<?> entityType : results) {
                    SpawnGroup category = entityType.getSpawnGroup();
                    if (!resultsByCategory.containsKey(category)) {
                        resultsByCategory.put(category, new ArrayList<>());
                    }

                    resultsByCategory.get(category).add(entityType);
                }

                for (SpawnGroup category : SpawnGroup.values()) {
                    if (resultsByCategory.containsKey(category)) {
                        this.list.addListEntry(EntityListWidget.HeaderEntry.create(this.client.textRenderer, getCategoryName(category), this.width, 25));

                        for (EntityType<?> entityType : resultsByCategory.get(category)) {
                            this.list.addListEntry(EntityListWidget.CheckboxEntry.create(entityType, outlinedEntityTypes.contains(entityType), this.width));
                        }

                    }      
                }

            } else {
                for (EntityType<?> entityType : results) {
                    this.list.addListEntry(EntityListWidget.CheckboxEntry.create(entityType, outlinedEntityTypes.contains(entityType), this.width));
                }
            }
        } else { // If there are no results, let the user know
            this.list.addListEntry(EntityListWidget.HeaderEntry.create(this.client.textRenderer, "No results", this.width, 25));
        }

        // This prevents an overscroll when the user is already scrolled down and the results list is shortened
        this.list.setScrollAmount(this.list.getScrollAmount());
    }

    // Called when config screen is escaped
    public void removed() {
        this.client.keyboard.setRepeatEvents(false);
        EntityOutliner.saveConfig();
    }

    public void tick() {
        this.searchField.tick();
    }

    // public void render(int mouseX, int mouseY, float delta) {
    //     // Render dirt background
    //     this.renderBackground(); 

    //     // Render scrolling list
    //     this.list.render(mouseX, mouseY, delta);

    //     // Render our search bar
    //     this.setFocused(this.searchField);
    //     this.searchField.setSelected(true);
    //     this.searchField.render(mouseX, mouseY, delta);

    //     // Render buttons
    //     super.render(mouseX, mouseY, delta);
    // }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        // Render dirt background
        this.renderBackground(matrices); 

        // Render scrolling list
        this.list.render(matrices, mouseX, mouseY, delta);

        // Render our search bar
        this.setFocused(this.searchField);
        this.searchField.setTextFieldFocused(true);
        this.searchField.render(matrices, mouseX, mouseY, delta);

        // Render buttons
        super.render(matrices, mouseX, mouseY, delta);
    }

    // Sends mouseDragged event to the scrolling list
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.list.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
