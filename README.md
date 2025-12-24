# mc-MenuAPI

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.7-blue.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21+-green.svg)
![Java](https://img.shields.io/badge/java-21-orange.svg)
![License](https://img.shields.io/badge/license-MIT-purple.svg)

*A powerful, context-aware menu system for Minecraft Paper servers with automatic refresh and player inventory interaction*

[Features](#-features) • [Installation](#-installation) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Examples](#-examples)

</div>

---

## 🌟 What is mc-MenuAPI?

mc-MenuAPI is a comprehensive GUI menu library designed for Paper/Spigot plugin developers who want to create beautiful, interactive, **context-aware** inventory menus without the hassle. Whether you're building a simple shop, a complex paginated player list, or dynamic item browsers with real-time updates, mc-MenuAPI has you covered with an intuitive API and powerful YAML configuration system.

### Why mc-MenuAPI?

- **🎯 Context-Aware Menus** - Bind data objects to menus and access them anywhere
- **🔄 Auto-Refresh** - Real-time updates without manual intervention
- **📦 Player Inventory Interaction** - Custom handling for clicks in player's inventory
- **🔍 Auto-Discovery Placeholders** - Automatic placeholder generation from context objects
- **⚡ Zero Boilerplate** - Focus on your menu logic, not inventory management
- **📝 YAML-First Design** - Define menus in configuration files with hot-reload support
- **🎨 Dynamic Content** - Build menus programmatically with full placeholder support
- **📄 Smart Pagination** - Automatic page handling with built-in navigation
- **🔌 Custom Item Support** - Seamless integration with Nexo, ItemsAdder, and Oraxen
- **🚀 Performance Optimized** - Thread-safe, concurrent operations with minimal overhead
- **✨ Conditional Actions** - Execute different actions based on runtime conditions
- **🎭 Visibility Conditions** - Show/hide items based on context state
- **🔧 Modern API** - Fluent builders, lambdas, and clean method chaining

---

## 🎯 Features

### Core Capabilities

#### Context-Aware Menus
Bind any data object to a menu and access it throughout the menu lifecycle. Perfect for chest management, player profiles, shop systems, and more.

```java
// Open menu with context
menuAPI.openMenu(player, "chest-menu.yml", sellChest);

// Access context in action handlers
ContextActionRegistry.register("chest-menu.yml", "TOGGLE_AUTO_SELL", ctx -> {
    SellChest chest = ctx.requireContext(SellChest.class);
    chest.setAutoSellEnabled(!chest.isAutoSellEnabled());
    ctx.refresh();
});
```

#### Auto-Refresh
Automatically update menu content at specified intervals. No more manual refresh calls!

```yaml
auto-refresh:
  enabled: true
  interval: 20  # Update every second
  slots: [10, 11, 12]  # Only refresh specific slots (or omit for all slots)
```

#### Player Inventory Interaction
Handle clicks in the player's inventory while a menu is open. Perfect for item charging, upgrades, or custom interactions.

```yaml
player-inventory:
  enabled: true
  handler: "CHARGE_HANDLER"
```

```java
PlayerInventoryHandlerRegistry.register("charge-menu.yml", context -> {
    ItemStack clicked = context.getClickedItem();
    SellChest chest = context.getMenuContext(SellChest.class).orElse(null);
    
    if (chargeService.isChargeItem(clicked)) {
        chargeService.addCharge(chest, clicked);
        context.refreshMenu();
    }
    
    return ClickResult.CANCEL;
});
```

#### Auto-Discovery Placeholders
Automatically generate placeholders from context objects using reflection. No manual registration needed!

```java
// Your context class
public class SellChest {
    public String getOwnerName() { return "Steve"; }
    public int getTotalEarned() { return 1000; }
    public boolean isAutoSellEnabled() { return true; }
}

// Automatically available in YAML:
// {context.ownerName} → "Steve"
// {context.totalEarned} → "1000"
// {context.autoSellEnabled} → "true"
```

#### Visibility Conditions
Show or hide items based on context state:

```yaml
auto-sell-toggle:
  material: LEVER
  name: "&eAuto-Sell: {context.autoSellEnabled}"
  visible-if: "{context.ownerUUID} == {player_uuid}"
  slot: 13
```

#### Simple Menus
Create static menus with ease using YAML configuration or programmatic builders. Perfect for shops, information displays, and navigation menus.

#### Advanced Pagination
Automatically handle large datasets with smart pagination. The system manages page navigation, item distribution, and player state tracking automatically.

#### Dynamic Content
Generate menu content at runtime based on player data, permissions, database queries, or any custom logic you need.

#### Custom Items
Full support for custom item plugins:
- **Nexo** - `nexo:item_id`
- **ItemsAdder** - `itemsadder:item_id`
- **Oraxen** - `oraxen:item_id`

#### Flexible Actions
Execute powerful actions on item clicks:
- Command execution (player/console)
- Menu navigation (open/close)
- Page changes (next/previous/goto)
- Sound effects
- Messages and broadcasts
- Conditional logic
- **Context actions** (custom handlers with full context access)
- **Refresh action** (update menu content)

#### Rich Placeholders
- Global menu placeholders
- Per-item placeholders
- Dynamic runtime placeholders
- **Context placeholders** (from bound data objects)
- **Auto-discovery placeholders** (automatically generated)
- Automatic placeholder resolution

---

## 📦 Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.mongenscave.com/releases")
}

dependencies {
    implementation("com.mongenscave:mc-MenuAPI:1.0.7")
}
```

### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://repo.mongenscave.com/releases' }
}

dependencies {
    implementation 'com.mongenscave:mc-MenuAPI:1.0.7'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>mongenscave</id>
        <url>https://repo.mongenscave.com/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.mongenscave</groupId>
        <artifactId>mc-MenuAPI</artifactId>
        <version>1.0.7</version>
    </dependency>
</dependencies>
```

---

## 🚀 Quick Start

### 1. Initialize the API

```java
public class MyPlugin extends JavaPlugin {
    private McMenuAPI menuAPI;

    @Override
    public void onEnable() {
        // Create your menus folder
        File menusFolder = new File(getDataFolder(), "menus");
        
        // Initialize the API
        menuAPI = new McMenuAPI(this, menusFolder);
        
        getLogger().info("MenuAPI initialized!");
    }
    
    @Override
    public void onDisable() {
        // Shutdown the API
        if (menuAPI != null) {
            menuAPI.shutdown();
        }
    }
}
```

### 2. Create Your First Context-Aware Menu (YAML)

Create `menus/chest-menu.yml`:

```yaml
title: "&6&l{context.ownerName}'s Chest"
size: 54
context-aware: true

# Auto-refresh every second
auto-refresh:
  enabled: true
  interval: 20
  slots: [10, 13, 16]

# Enable player inventory interaction
player-inventory:
  enabled: true
  handler: "CHARGE_HANDLER"

items:
  info:
    material: CHEST
    name: "&eChest Information"
    lore:
      - "&7Owner: &f{context.ownerName}"
      - "&7Total Earned: &a${context.totalEarned}"
      - "&7Items Sold: &e{context.totalSold}"
      - ""
      - "&7Auto-Sell: {context.autoSellEnabled}"
    slot: 10
  
  auto-sell-toggle:
    material: LEVER
    name: "&eToggle Auto-Sell"
    lore:
      - "&7Current: {context.autoSellEnabled}"
      - ""
      - "&eClick to toggle"
    slot: 13
    actions:
      - "[ACTION] TOGGLE_AUTO_SELL"
      - "[SOUND] UI_BUTTON_CLICK"
      - "[REFRESH]"
  
  upgrade:
    material: DIAMOND
    name: "&bUpgrade Capacity"
    lore:
      - "&7Current: &e{context.capacity}"
      - "&7Cost: &a${context.upgradeCost}"
      - ""
      - "&eClick to upgrade"
    slot: 16
    visible-if: "{context.capacity} < 1000"
    actions:
      - "[ACTION] UPGRADE_CAPACITY"
  
  close:
    material: BARRIER
    name: "&c&lClose"
    slot: 49
    actions:
      - "[CLOSE]"
```

### 3. Register Context Actions

```java
// Register the toggle action
ContextActionRegistry.register("chest-menu.yml", "TOGGLE_AUTO_SELL", ctx -> {
    SellChest chest = ctx.requireContext(SellChest.class);
    
    chest.setAutoSellEnabled(!chest.isAutoSellEnabled());
    
    ctx.sendMessage("&aAuto-sell " + (chest.isAutoSellEnabled() ? "enabled" : "disabled"));
    ctx.playSound("UI_BUTTON_CLICK");
    // Menu will auto-refresh due to [REFRESH] action
});

// Register the upgrade action
ContextActionRegistry.register("chest-menu.yml", "UPGRADE_CAPACITY", ctx -> {
    SellChest chest = ctx.requireContext(SellChest.class);
    Player player = ctx.getPlayer();
    
    if (hasEnoughMoney(player, chest.getUpgradeCost())) {
        takeMoney(player, chest.getUpgradeCost());
        chest.upgradeCapacity();
        ctx.sendMessage("&aChest upgraded!");
        ctx.playSound("ENTITY_PLAYER_LEVELUP");
        ctx.refresh();
    } else {
        ctx.sendMessage("&cNot enough money!");
        ctx.playSound("ENTITY_VILLAGER_NO");
    }
});

// Register player inventory handler for charging
PlayerInventoryHandlerRegistry.register("chest-menu.yml", context -> {
    ItemStack clicked = context.getClickedItem();
    if (clicked == null || clicked.getType().isAir()) {
        return ClickResult.CANCEL;
    }
    
    SellChest chest = context.getMenuContext(SellChest.class).orElse(null);
    if (chest == null) return ClickResult.CANCEL;
    
    if (chargeService.isChargeItem(clicked)) {
        int chargeAmount = chargeService.getChargeAmount(clicked);
        chest.addCharge(chargeAmount);
        
        clicked.setAmount(0);
        
        context.sendMessage("&aAdded " + chargeAmount + " charge!");
        context.playSound("ENTITY_EXPERIENCE_ORB_PICKUP");
        context.refreshMenu();
        
        return ClickResult.CANCEL;
    }
    
    return ClickResult.CANCEL;
});
```

### 4. Open the Menu with Context

```java
// Open the menu with a SellChest context
menuAPI.openMenu(player, "chest-menu.yml", sellChest);

// The menu will now:
// - Display data from the SellChest object
// - Auto-refresh every second
// - Handle player inventory clicks
// - Execute context-aware actions
```

That's it! You've created a fully context-aware, auto-refreshing menu with player inventory interaction.

---

## 📚 Documentation

### Core Concepts

#### McMenuAPI - The Main Class

The `McMenuAPI` class is your entry point to the entire system. It manages menu loading, player states, context binding, and provides all core functionality.

```java
// Initialize
McMenuAPI api = new McMenuAPI(plugin, menusFolder);

// Open a menu without context
api.openMenu(player, "menu.yml");

// Open a menu with context
api.openMenu(player, "menu.yml", contextObject);

// Open a menu preserving existing context
api.openMenuPreserveContext(player, "other-menu.yml");

// Reload all menus
api.reloadMenus();

// Get a loaded menu
Optional<Menu> menu = api.getMenu("menu.yml");

// Close a player's menu
api.closeMenu(player);

// Refresh the current menu
api.refreshMenu(player);

// Register a menu programmatically
api.registerMenu("custom.yml", customMenu);

// Get the current open menu for a player
Menu currentMenu = api.getOpenMenu(player);

// Get menu context for a player
Optional<SellChest> context = api.getMenuContext(player, SellChest.class);

// Update context
api.updateContext(player, updatedContextObject);

// Shutdown the API (call in onDisable)
api.shutdown();
```

#### Menu Context System

The context system allows you to bind any data object to a menu session:

```java
// Setting context (automatically done when opening menu with context)
MenuContext.set(player, sellChest);

// Getting context with type checking
Optional<SellChest> context = MenuContext.get(player, SellChest.class);

// Getting raw context
Object rawContext = MenuContext.getRaw(player);

// Checking if context exists
boolean hasContext = MenuContext.has(player);
boolean hasSellChest = MenuContext.has(player, SellChest.class);

// Clearing context
MenuContext.clear(player);
```

#### ActionContext - Full Context Access

The `ActionContext` provides complete information about a click event:

```java
ContextActionRegistry.register("menu.yml", "MY_ACTION", ctx -> {
    // Player information
    Player player = ctx.getPlayer();
    
    // Menu information
    Menu menu = ctx.getMenu();
    String menuFileName = ctx.getMenuFileName();
    
    // Click information
    int slot = ctx.getSlot();
    ItemStack clickedItem = ctx.getClickedItem();
    ClickType clickType = ctx.getClickType();
    
    // Click type helpers
    boolean isLeft = ctx.isLeftClick();
    boolean isRight = ctx.isRightClick();
    boolean isShift = ctx.isShiftClick();
    
    // Context access
    SellChest chest = ctx.requireContext(SellChest.class);
    Optional<SellChest> optChest = ctx.getContext(SellChest.class);
    boolean hasChest = ctx.hasContext(SellChest.class);
    
    // Menu control
    ctx.refresh();
    ctx.close();
    ctx.open("other-menu.yml");
    ctx.openPreserveContext("other-menu.yml");
    
    // Communication
    ctx.sendMessage("&aMessage");
    ctx.playSound("UI_BUTTON_CLICK");
    ctx.playSound("ENTITY_PLAYER_LEVELUP", 1.0f, 1.5f);
    
    // Update context and refresh
    ctx.updateContext(updatedChest);
});
```

#### Auto-Discovery Placeholders

Automatically discover placeholders from context objects:

```java
public class SellChest {
    private String ownerName;
    private int totalEarned;
    private boolean autoSellEnabled;
    
    // Automatically creates: {context.ownerName}
    public String getOwnerName() { return ownerName; }
    
    // Automatically creates: {context.totalEarned}
    public int getTotalEarned() { return totalEarned; }
    
    // Automatically creates: {context.autoSellEnabled}
    public boolean isAutoSellEnabled() { return autoSellEnabled; }
}

// Use in YAML:
name: "&e{context.ownerName}'s Chest"
lore:
  - "&7Earned: &a${context.totalEarned}"
  - "&7Auto-Sell: {context.autoSellEnabled}"
```

Supported types:
- Primitives (int, boolean, etc.)
- Primitive wrappers (Integer, Boolean, etc.)
- String
- UUID
- Date (formatted as "yyyy-MM-dd HH:mm:ss")
- Enums (name)

#### Menu Interface

All menus implement the `Menu` interface, providing a consistent API:

```java
menu.open(player);          // Open for a player
menu.close(player);         // Close for a player
menu.refresh(player);       // Refresh content
menu.refreshSlots(player, slots); // Refresh specific slots
menu.setItem(key, item);    // Add an item
menu.removeItem(key);       // Remove an item
menu.setPlaceholders(map);  // Set global placeholders

// Pagination methods
menu.getCurrentPage(player); // Get current page
menu.setPage(player, page);  // Set page
menu.getTotalPages();        // Get total pages
menu.isPaginated();          // Check if paginated

// Event handlers
menu.onOpen(player -> {...});   // Run when opened
menu.onClose(player -> {...});  // Run when closed
menu.onRefresh(player -> {...}); // Run when refreshed

// Auto-refresh
menu.setRefreshConfig(RefreshConfig.all(20)); // Refresh all slots every second
menu.setRefreshConfig(RefreshConfig.slots(40, List.of(10, 11, 12))); // Specific slots

// Player inventory interaction
menu.setPlayerInventoryInteraction(true);
menu.setPlayerInventoryHandlerName("MY_HANDLER");

// Context awareness
menu.setContextAware(true);
boolean isContextAware = menu.isContextAware();
```

---

### YAML Configuration

#### Basic Structure

```yaml
# Menu title with color support and placeholders
title: "&6&l{context.ownerName}'s Menu"

# Size (must be multiple of 9, between 9-54)
size: 54

# Context awareness flag
context-aware: true

# Auto-refresh configuration
auto-refresh:
  enabled: true
  interval: 20  # Ticks (20 = 1 second)
  slots: [10, 11, 12]  # Optional: specific slots (omit for all)

# Player inventory interaction
player-inventory:
  enabled: true
  handler: "MY_HANDLER"  # Optional: handler name

# Placeable slots (slots where players can place items)
placeable-slots: "0-8"

# Items definition
items:
  item_key:
    material: MATERIAL_NAME
    name: "&aDisplay Name"
    lore:
      - "&7Line 1"
      - "&7Line 2"
    slot: 0
    amount: 1
    visible-if: "{context.someValue} == true"
    actions:
      - "[ACTION_TYPE] value"
```

#### Visibility Conditions

Control item visibility based on context:

```yaml
premium-item:
  material: DIAMOND
  name: "&bPremium Item"
  visible-if: "{context.isPremium} == true"
  slot: 13

owner-only-button:
  material: LEVER
  name: "&eSettings"
  visible-if: "{context.ownerUUID} == {player_uuid}"
  slot: 16
```

Supported operators:
- `==` or `equals` - Equal to
- `!=` - Not equal to

#### Auto-Refresh Configuration

```yaml
# Refresh all slots every second
auto-refresh:
  enabled: true
  interval: 20

# Refresh specific slots every 2 seconds
auto-refresh:
  enabled: true
  interval: 40
  slots: [10, 11, 12, 13, 14, 15, 16]

# Disabled (default)
auto-refresh:
  enabled: false
```

#### Player Inventory Interaction

```yaml
# Enable with handler
player-inventory:
  enabled: true
  handler: "CHARGE_HANDLER"

# Enable without handler (cancel by default)
player-inventory:
  enabled: true

# Disabled (default)
player-inventory:
  enabled: false
```

#### Material Types

**Standard Materials:**
```yaml
material: DIAMOND
```

**Custom Items (Nexo):**
```yaml
material: nexo:custom_sword
```

**Custom Items (ItemsAdder):**
```yaml
material: itemsadder:magic_staff
```

**Custom Items (Oraxen):**
```yaml
material: oraxen:ruby_sword
```

#### Slot Configurations

**Single Slot:**
```yaml
slot: 13
```

**Multiple Slots:**
```yaml
slot: [10, 11, 12, 13]
```

**Slot Ranges:**
```yaml
slot: "10-16"  # Slots 10 through 16
```

**Mixed:**
```yaml
slot: "0-8, 17, 26, 35-44"
```

#### Item Properties

```yaml
item:
  material: DIAMOND_SWORD
  name: "&b&lLegendary Sword"
  amount: 1
  
  # Lore (supports colors and hex)
  lore:
    - "&7A powerful weapon"
    - "&#FF5555Deals massive damage"
  
  # Enchantments
  enchantments:
    - "sharpness:5"
    - "unbreaking:3"
  
  # Custom model data
  modeldata: 1001
  
  # Model key (1.21+)
  modelkey: "custom_sword"
  
  # Unbreakable
  unbreakable: true
  
  # Clickable (false = no actions)
  clickable: true
  
  # Priority (higher = placed last)
  priority: 10
  
  # Visibility condition
  visible-if: "{context.level} >= 10"
```

---

### Actions System

Actions execute when players click menu items. They're powerful, chainable, and support conditional logic.

#### Available Actions

**Context Actions (NEW):**
```yaml
actions:
  - "[ACTION] CUSTOM_ACTION_NAME"  # Executes registered context action
  - "[REFRESH]"                     # Refreshes the menu
```

**Command Execution:**
```yaml
actions:
  - "[CONSOLE] give {player} diamond 1"  # Run as console
  - "[PLAYER] spawn"                      # Run as player
  - "[COMMAND] say Hello"                 # Alias for CONSOLE
```

**Menu Navigation:**
```yaml
actions:
  - "[OPEN] shop.yml"    # Open another menu
  - "[CLOSE]"            # Close current menu
```

**Communication:**
```yaml
actions:
  - "[MESSAGE] &aYou clicked an item!"
  - "[BROADCAST] &e{player} &7bought an item!"
```

**Audio:**
```yaml
actions:
  - "[SOUND] ENTITY_EXPERIENCE_ORB_PICKUP"
  - "[SOUND] UI_BUTTON_CLICK 1.0 1.5"  # With volume and pitch
```

**Pagination:**
```yaml
actions:
  - "[PAGE] +1"    # Next page
  - "[PAGE] -1"    # Previous page
  - "[PAGE] 0"     # First page
  - "[PAGE] 5"     # Go to page 5
```

#### Context Action Handlers

Register custom action handlers with full context access:

```java
// Global handler (available in all menus)
ContextActionRegistry.registerGlobal("CLOSE_WITH_SOUND", ctx -> {
    ctx.playSound("UI_BUTTON_CLICK");
    ctx.close();
});

// Menu-specific handler
ContextActionRegistry.register("chest-menu.yml", "TOGGLE_AUTO_SELL", ctx -> {
    SellChest chest = ctx.requireContext(SellChest.class);
    
    chest.setAutoSellEnabled(!chest.isAutoSellEnabled());
    
    ctx.sendMessage(chest.isAutoSellEnabled() 
        ? "&aAuto-sell enabled!" 
        : "&cAuto-sell disabled!");
    
    ctx.playSound("UI_BUTTON_CLICK");
    ctx.refresh();
});

// Check if handler exists
boolean exists = ContextActionRegistry.hasHandler("menu.yml", "MY_ACTION");

// Execute handler programmatically
boolean executed = ContextActionRegistry.execute(actionContext, "MY_ACTION");
```

#### Player Inventory Handlers

Handle clicks in the player's inventory while a menu is open:

```java
PlayerInventoryHandlerRegistry.register("charge-menu.yml", context -> {
    ItemStack clicked = context.getClickedItem();
    if (clicked == null || clicked.getType().isAir()) {
        return ClickResult.CANCEL;
    }
    
    // Get menu context
    SellChest chest = context.getMenuContext(SellChest.class).orElse(null);
    if (chest == null) return ClickResult.CANCEL;
    
    // Check if it's a charge item
    if (chargeService.isChargeItem(clicked)) {
        int chargeAmount = chargeService.getChargeAmount(clicked);
        chest.addCharge(chargeAmount);
        
        // Remove the item
        clicked.setAmount(0);
        
        // Send feedback
        context.sendMessage("&aAdded " + chargeAmount + " charge!");
        context.playSound("ENTITY_EXPERIENCE_ORB_PICKUP");
        
        // Refresh menu
        context.refreshMenu();
        
        return ClickResult.CANCEL;
    }
    
    // Don't allow other items
    return ClickResult.CANCEL;
});
```

Click results:
- `CANCEL` - Cancel the event (default)
- `ALLOW` - Allow the event to proceed
- `CANCEL_SILENT` - Cancel but don't send update to client

#### Conditional Actions

Execute different actions based on runtime conditions:

```yaml
actions:
  # Page-based conditions
  - "[IF] {page} == 0 [THEN] [OPEN] main.yml [ELSE] [PAGE] -1"
  
  # Level-based conditions
  - "[IF] {level} >= 10 [THEN] [MESSAGE] &aUnlocked! [ELSE] [MESSAGE] &cLevel 10 required"
  
  # Health-based conditions
  - "[IF] {health} < 5 [THEN] [SOUND] ENTITY_PLAYER_HURT [ELSE] [SOUND] UI_BUTTON_CLICK"
  
  # Permission-based conditions
  - "[IF] {permission} has vip.access [THEN] [OPEN] vip.yml [ELSE] [MESSAGE] &cVIP required!"
  
  # Name-based conditions
  - "[IF] {name} equals Notch [THEN] [MESSAGE] &eWelcome, creator! [ELSE] [MESSAGE] &7Hello!"
```

---

### Context Placeholders

#### Auto-Discovery (Recommended)

Automatically generate placeholders from any context object:

```java
// Your context class
public class SellChest {
    private String ownerName;
    private int totalEarned;
    private int totalSold;
    private boolean autoSellEnabled;
    private UUID ownerUUID;
    
    // All getters automatically create placeholders!
    public String getOwnerName() { return ownerName; }
    public int getTotalEarned() { return totalEarned; }
    public int getTotalSold() { return totalSold; }
    public boolean isAutoSellEnabled() { return autoSellEnabled; }
    public UUID getOwnerUUID() { return ownerUUID; }
}

// Open menu with context - placeholders automatically available!
menuAPI.openMenu(player, "chest-menu.yml", sellChest);
```

Available placeholders:
- `{context.ownerName}` → "Steve"
- `{context.totalEarned}` → "1000"
- `{context.totalSold}` → "50"
- `{context.autoSellEnabled}` → "true"
- `{context.ownerUUID}` → "uuid-string"

#### Manual Registration (Advanced)

For custom formatting or complex logic:

```java
// Register for a specific context type
ContextPlaceholderRegistry.register(SellChest.class, "{earned_formatted}",
    (player, chest) -> FormatUtil.formatMoney(chest.getTotalEarned())
);

// Register multiple placeholders with a prefix
ContextPlaceholderRegistry.registerWithPrefix(SellChest.class, "chest",
    Map.of(
        "earned", (p, c) -> String.valueOf(c.getTotalEarned()),
        "sold", (p, c) -> String.valueOf(c.getTotalSold()),
        "owner", (p, c) -> c.getOwnerName()
    )
);
// Creates: {chest.earned}, {chest.sold}, {chest.owner}

// Resolve placeholders
Map<String, String> resolved = ContextPlaceholderRegistry.resolveAll(player);
String value = ContextPlaceholderRegistry.resolve(player, "{earned_formatted}");
String text = ContextPlaceholderRegistry.apply(player, "Earned: {earned_formatted}");
```

#### Global Placeholders

```java
menu.setPlaceholders(Map.of(
    "{server}", "MyServer",
    "{online}", String.valueOf(Bukkit.getOnlinePlayers().size()),
    "{max}", String.valueOf(Bukkit.getMaxPlayers())
));
```

#### Per-Item Placeholders

```java
MenuItem item = MenuItem.builder()
    .itemStack(itemStack)
    .slot(13)
    .placeholder("{price}", "100")
    .placeholder("{discount}", "20%")
    .build();
```

#### Dynamic Placeholders

```java
MenuItem item = MenuItem.builder()
    .itemStack(itemStack)
    .slot(13)
    .dynamicPlaceholder("{balance}", player -> 
        String.valueOf(getEconomy().getBalance(player))
    )
    .dynamicPlaceholder("{time}", player -> 
        new SimpleDateFormat("HH:mm").format(new Date())
    )
    .build();
```

#### Built-in Placeholders

- `{player}` - Player name
- `{player_uuid}` - Player UUID
- `{page}` - Current page (pagination)
- `{total_pages}` - Total pages (pagination)
- `{context.*}` - All getters from context object (auto-discovery)

---

### Pagination

#### YAML Configuration

```yaml
title: "&6&lPlayers Online"
size: 54

# Enable pagination
pagination:
  enabled: true
  pages: 5  # Total pages (can be updated dynamically)

items:
  # Navigation items
  previous:
    material: ARROW
    name: "&e← Previous Page"
    slot: 48
    actions:
      - "[PAGE] -1"
  
  next:
    material: ARROW
    name: "&eNext Page →"
    slot: 50
    actions:
      - "[PAGE] +1"
  
  # Static border items, etc.
  border:
    material: GRAY_STAINED_GLASS_PANE
    name: " "
    slot: "0-8, 45-53"
```

#### Programmatic Pagination

```java
// Define page slots
int[] pageSlots = new int[]{
    10, 11, 12, 13, 14, 15, 16,
    19, 20, 21, 22, 23, 24, 25,
    28, 29, 30, 31, 32, 33, 34
};

PaginatedMenu menu = new PaginatedMenu("&6Player List", 54, pageSlots);

// Add static items (borders, buttons)
MenuItem border = MenuItem.builder()
    .itemStack(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
    .slots(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8))
    .clickable(false)
    .build();
menu.setItem("border", border);

// Add page items
for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
    ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta meta = (SkullMeta) skull.getItemMeta();
    meta.setOwningPlayer(onlinePlayer);
    meta.setDisplayName("§e" + onlinePlayer.getName());
    skull.setItemMeta(meta);
    
    MenuItem playerItem = MenuItem.builder()
        .itemStack(skull)
        .action(Action.message("§7Clicked " + onlinePlayer.getName()))
        .build();
    
    menu.addPageItem(playerItem);
}

// Set navigation buttons
MenuItem prevButton = MenuItem.builder()
    .itemStack(new ItemStack(Material.ARROW))
    .slot(48)
    .action(Action.page("-1"))
    .build();
menu.setPreviousPageItem(prevButton);

MenuItem nextButton = MenuItem.builder()
    .itemStack(new ItemStack(Material.ARROW))
    .slot(50)
    .action(Action.page("+1"))
    .build();
menu.setNextPageItem(nextButton);

// Open the menu
menu.open(player);
```

#### Smart Paginated Menu

```java
SmartPaginatedMenu.builder()
    .fromYAML("players.yml")
    .titlePlaceholder("{total}", String.valueOf(Bukkit.getOnlinePlayers().size()))
    .dynamicItems(menu -> {
        for (Player p : Bukkit.getOnlinePlayers()) {
            menu.addPageItem("player_template", template -> {
                template.setPlayerHead(p);
                template.placeholder("{player_name}", p.getName());
                template.placeholder("{player_health}", String.valueOf(p.getHealth()));
                template.onClick(clicker -> {
                    clicker.sendMessage("§aYou clicked " + p.getName());
                });
            });
        }
    })
    .open(player);
```

---

### Auto-Refresh System

The auto-refresh system automatically updates menu content at specified intervals.

#### Configuration

```yaml
# Refresh all slots every second
auto-refresh:
  enabled: true
  interval: 20  # Ticks (20 = 1 second)

# Refresh specific slots every 2 seconds
auto-refresh:
  enabled: true
  interval: 40
  slots: [10, 11, 12, 13]
```

#### Programmatic Configuration

```java
// Refresh all slots every second
menu.setRefreshConfig(RefreshConfig.all(20));

// Refresh specific slots every 2 seconds
menu.setRefreshConfig(RefreshConfig.slots(40, List.of(10, 11, 12)));

// Disable refresh
menu.setRefreshConfig(RefreshConfig.disabled());
```

#### Manual Refresh

```java
// Refresh entire menu
menuAPI.refreshMenu(player);

// Or through the menu instance
menu.refresh(player);

// Refresh specific slots
menu.refreshSlots(player, List.of(10, 11, 12));
```

#### Refresh Manager

```java
// Access the refresh manager
MenuRefreshManager refreshManager = menuAPI.getRefreshManager();

// Force refresh all open menus
refreshManager.forceRefreshAll();

// Force refresh for specific player
refreshManager.forceRefresh(player);

// Get refresh statistics
long currentTick = refreshManager.getCurrentTick();
long lastRefresh = refreshManager.getLastRefreshTick(player);

// Stop/start refresh system
refreshManager.stop();
refreshManager.start();
```

---

## 🎯 Examples

### Example 1: Context-Aware Chest Menu

**YAML (`menus/chest-menu.yml`):**
```yaml
title: "&6&l{context.ownerName}'s Chest"
size: 54
context-aware: true

auto-refresh:
  enabled: true
  interval: 20
  slots: [10, 11, 12, 13, 16, 22]

player-inventory:
  enabled: true
  handler: "CHARGE_HANDLER"

items:
  border:
    material: BLACK_STAINED_GLASS_PANE
    name: " "
    slot: "0-8, 9, 17, 18, 26, 27, 35, 36-44, 45-53"
    clickable: false
  
  info:
    material: CHEST
    name: "&eChest Information"
    lore:
      - "&7Owner: &f{context.ownerName}"
      - "&7Total Earned: &a${context.totalEarned}"
      - "&7Items Sold: &e{context.totalSold}"
      - "&7Capacity: &b{context.capacity}/1000"
      - ""
      - "&7Charge: &d{context.charge}/{context.maxCharge}"
    slot: 10
  
  auto-sell:
    material: LEVER
    name: "&eAuto-Sell"
    lore:
      - "&7Status: {context.autoSellEnabled}"
      - ""
      - "&eClick to toggle"
    slot: 11
    actions:
      - "[ACTION] TOGGLE_AUTO_SELL"
      - "[SOUND] UI_BUTTON_CLICK"
      - "[REFRESH]"
  
  auto-craft:
    material: CRAFTING_TABLE
    name: "&eAuto-Craft"
    lore:
      - "&7Status: {context.autoCraftEnabled}"
      - ""
      - "&eClick to toggle"
    slot: 12
    actions:
      - "[ACTION] TOGGLE_AUTO_CRAFT"
      - "[SOUND] UI_BUTTON_CLICK"
      - "[REFRESH]"
  
  filter:
    material: HOPPER
    name: "&eItem Filter"
    lore:
      - "&7Click to configure"
    slot: 13
    actions:
      - "[OPEN] filter-menu.yml"
  
  upgrade:
    material: DIAMOND
    name: "&bUpgrade Capacity"
    lore:
      - "&7Current: &e{context.capacity}"
      - "&7Next: &e{context.nextCapacity}"
      - "&7Cost: &a${context.upgradeCost}"
      - ""
      - "&eClick to upgrade"
    slot: 16
    visible-if: "{context.capacity} < 1000"
    actions:
      - "[ACTION] UPGRADE_CAPACITY"
  
  charge-info:
    material: REDSTONE
    name: "&dCharge System"
    lore:
      - "&7Current: &d{context.charge}/{context.maxCharge}"
      - ""
      - "&7Place charge items in your inventory"
      - "&7to add charge to this chest"
    slot: 22
  
  settings:
    material: COMPARATOR
    name: "&eSettings"
    lore:
      - "&7Click to configure settings"
    slot: 40
    visible-if: "{context.ownerUUID} == {player_uuid}"
    actions:
      - "[OPEN] settings-menu.yml"
  
  close:
    material: BARRIER
    name: "&c&lClose"
    slot: 49
    actions:
      - "[SOUND] UI_BUTTON_CLICK"
      - "[CLOSE]"
```

**Java Implementation:**
```java
public class ChestMenuHandler {
    
    public void register(McMenuAPI menuAPI) {
        // Register toggle actions
        ContextActionRegistry.register("chest-menu.yml", "TOGGLE_AUTO_SELL", ctx -> {
            SellChest chest = ctx.requireContext(SellChest.class);
            
            chest.setAutoSellEnabled(!chest.isAutoSellEnabled());
            chest.save();
            
            ctx.sendMessage(chest.isAutoSellEnabled() 
                ? "&aAuto-sell enabled!" 
                : "&cAuto-sell disabled!");
        });
        
        ContextActionRegistry.register("chest-menu.yml", "TOGGLE_AUTO_CRAFT", ctx -> {
            SellChest chest = ctx.requireContext(SellChest.class);
            
            chest.setAutoCraftEnabled(!chest.isAutoCraftEnabled());
            chest.save();
            
            ctx.sendMessage(chest.isAutoCraftEnabled() 
                ? "&aAuto-craft enabled!" 
                : "&cAuto-craft disabled!");
        });
        
        // Register upgrade action
        ContextActionRegistry.register("chest-menu.yml", "UPGRADE_CAPACITY", ctx -> {
            SellChest chest = ctx.requireContext(SellChest.class);
            Player player = ctx.getPlayer();
            
            double cost = chest.getUpgradeCost();
            
            if (economy.getBalance(player) >= cost) {
                economy.withdrawPlayer(player, cost);
                chest.upgradeCapacity();
                chest.save();
                
                ctx.sendMessage("&aChest upgraded to " + chest.getCapacity() + " capacity!");
                ctx.playSound("ENTITY_PLAYER_LEVELUP");
                ctx.refresh();
            } else {
                ctx.sendMessage("&cNot enough money! Need $" + cost);
                ctx.playSound("ENTITY_VILLAGER_NO");
            }
        });
        
        // Register player inventory handler for charging
        PlayerInventoryHandlerRegistry.register("chest-menu.yml", context -> {
            ItemStack clicked = context.getClickedItem();
            if (clicked == null || clicked.getType().isAir()) {
                return ClickResult.CANCEL;
            }
            
            SellChest chest = context.getMenuContext(SellChest.class).orElse(null);
            if (chest == null) return ClickResult.CANCEL;
            
            // Check if it's a charge item (e.g., redstone, lapis)
            if (isChargeItem(clicked)) {
                int chargeAmount = getChargeAmount(clicked);
                int added = chest.addCharge(chargeAmount);
                
                if (added > 0) {
                    clicked.setAmount(clicked.getAmount() - 1);
                    
                    context.sendMessage("&aAdded " + added + " charge!");
                    context.playSound("ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.5f);
                    context.refreshMenu();
                } else {
                    context.sendMessage("&cChest is fully charged!");
                    context.playSound("ENTITY_VILLAGER_NO");
                }
                
                return ClickResult.CANCEL;
            }
            
            // Don't allow other items
            return ClickResult.CANCEL;
        });
    }
    
    private boolean isChargeItem(ItemStack item) {
        return item.getType() == Material.REDSTONE || 
               item.getType() == Material.LAPIS_LAZULI;
    }
    
    private int getChargeAmount(ItemStack item) {
        return switch (item.getType()) {
            case REDSTONE -> 10;
            case LAPIS_LAZULI -> 25;
            default -> 0;
        };
    }
}
```

### Example 2: Real-Time Player List with Auto-Refresh

**Java:**
```java
public void openPlayerList(Player viewer) {
    // Register dynamic builder
    DynamicMenuRegistry.register("players.yml", context -> {
        // This runs every time the menu opens or refreshes
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        
        int slot = 10;
        for (Player online : onlinePlayers) {
            if (slot > 43) break;  // Don't overflow
            
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(online);
            meta.setDisplayName("§e" + online.getName());
            meta.setLore(List.of(
                "§7Health: §c" + (int)online.getHealth() + "❤",
                "§7Level: §a" + online.getLevel(),
                "§7Ping: §e" + getPing(online) + "ms",
                "",
                "§eClick to teleport"
            ));
            skull.setItemMeta(meta);
            
            context.inventory().setItem(slot, skull);
            
            // Register click handler
            context.registerClickHandler(slot, (p, item, clickType) -> {
                if (p.hasPermission("admin.teleport")) {
                    p.teleport(online);
                    p.sendMessage("§aTeleported to " + online.getName());
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                } else {
                    p.sendMessage("§cNo permission!");
                }
            });
            
            slot++;
        }
    });
    
    // Open with auto-refresh every 2 seconds
    Optional<Menu> menuOpt = menuAPI.getMenu("players.yml");
    if (menuOpt.isPresent()) {
        Menu menu = menuOpt.get();
        menu.setRefreshConfig(RefreshConfig.all(40));  // 2 seconds
        menu.open(viewer);
    }
}
```

### Example 3: Shop with Context and Permissions

**YAML (`menus/shop.yml`):**
```yaml
title: "&6&l✦ Shop Menu ✦"
size: 54
context-aware: true

items:
  border:
    material: BLACK_STAINED_GLASS_PANE
    name: " "
    slot: "0-8, 9, 17, 18-26, 27, 35, 36-44, 45-53"
    clickable: false
  
  balance:
    material: GOLD_INGOT
    name: "&eYour Balance"
    lore:
      - "&7Balance: &a${context.balance}"
    slot: 4
  
  diamond:
    material: DIAMOND
    name: "&b&lDiamond"
    lore:
      - "&7Price: &e$100"
      - ""
      - "&aClick to purchase"
    slot: 20
    actions:
      - "[ACTION] PURCHASE_ITEM"
  
  emerald:
    material: EMERALD
    name: "&a&lEmerald"
    lore:
      - "&7Price: &e$250"
      - ""
      - "&aClick to purchase"
    slot: 22
    actions:
      - "[ACTION] PURCHASE_ITEM"
  
  netherite:
    material: NETHERITE_INGOT
    name: "&4&lNetherite"
    lore:
      - "&7Price: &e$1000"
      - "&7VIP Only"
      - ""
      - "&aClick to purchase"
    slot: 24
    visible-if: "{context.hasVIP} == true"
    actions:
      - "[ACTION] PURCHASE_ITEM"
  
  close:
    material: BARRIER
    name: "&c&lClose"
    slot: 49
    actions:
      - "[CLOSE]"
```

**Java:**
```java
public class ShopMenuHandler {
    
    public static class ShopContext {
        private final Player player;
        
        public ShopContext(Player player) {
            this.player = player;
        }
        
        // Auto-discovery placeholders
        public double getBalance() {
            return economy.getBalance(player);
        }
        
        public boolean getHasVIP() {
            return player.hasPermission("shop.vip");
        }
    }
    
    public void openShop(Player player) {
        ShopContext context = new ShopContext(player);
        menuAPI.openMenu(player, "shop.yml", context);
    }
    
    public void register(McMenuAPI menuAPI) {
        ContextActionRegistry.register("shop.yml", "PURCHASE_ITEM", ctx -> {
            Player player = ctx.getPlayer();
            ItemStack clickedItem = ctx.getClickedItem();
            
            if (clickedItem == null) return;
            
            ShopContext shopContext = ctx.requireContext(ShopContext.class);
            
            // Determine price based on item
            double price = switch (clickedItem.getType()) {
                case DIAMOND -> 100;
                case EMERALD -> 250;
                case NETHERITE_INGOT -> 1000;
                default -> 0;
            };
            
            if (shopContext.getBalance() >= price) {
                economy.withdrawPlayer(player, price);
                player.getInventory().addItem(new ItemStack(clickedItem.getType(), 1));
                
                ctx.sendMessage("&aPurchased " + clickedItem.getType().name() + " for $" + price);
                ctx.playSound("ENTITY_EXPERIENCE_ORB_PICKUP", 1.0f, 1.0f);
                
                // Update context and refresh
                ctx.updateContext(new ShopContext(player));
            } else {
                ctx.sendMessage("&cNot enough money! Need $" + price);
                ctx.playSound("ENTITY_VILLAGER_NO");
            }
        });
    }
}
```

### Example 4: Settings Menu with Multiple Pages

**Java:**
```java
public void openSettings(Player player, GameSettings settings) {
    // Register context action for toggle
    ContextActionRegistry.register("settings.yml", "TOGGLE_SETTING", ctx -> {
        GameSettings gameSettings = ctx.requireContext(GameSettings.class);
        ItemStack clicked = ctx.getClickedItem();
        
        if (clicked != null && clicked.hasItemMeta()) {
            String displayName = clicked.getItemMeta().getDisplayName();
            
            if (displayName.contains("PvP")) {
                gameSettings.setPvpEnabled(!gameSettings.isPvpEnabled());
            } else if (displayName.contains("Flight")) {
                gameSettings.setFlightEnabled(!gameSettings.isFlightEnabled());
            } else if (displayName.contains("Weather")) {
                gameSettings.setWeatherEnabled(!gameSettings.isWeatherEnabled());
            }
            
            gameSettings.save();
            ctx.refresh();
            ctx.playSound("UI_BUTTON_CLICK");
        }
    });
    
    // Open with context
    menuAPI.openMenu(player, "settings.yml", settings);
}
```

---

## 🔧 Advanced Topics

### Thread Safety

mc-MenuAPI is designed with thread safety in mind:

- All internal maps use `ConcurrentHashMap`
- Collections are synchronized where necessary
- Safe to modify menus from async tasks
- Menu updates should be done on the main thread

```java
// Safe to load data async
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    List<Data> data = loadFromDatabase();
    
    // Update menu on main thread
    Bukkit.getScheduler().runTask(plugin, () -> {
        menu.clearPageItems();
        data.forEach(d -> menu.addPageItem(createItem(d)));
        menu.refresh(player);
    });
});
```

### Performance Optimization

**Lazy Loading:**
```java
// Don't create menus until needed
private Menu getOrCreateMenu(String id) {
    return menuCache.computeIfAbsent(id, k -> createMenu(k));
}
```

**Efficient Pagination:**
```java
// Use appropriate page slot arrays
// Smaller = better performance for large datasets
int[] efficientSlots = IntStream.range(0, 28).toArray();
```

**Item Reuse:**
```java
// Cache commonly used items
private static final ItemStack BORDER = createBorder();
```

**Auto-Refresh Optimization:**
```java
// Only refresh slots that need updates
menu.setRefreshConfig(RefreshConfig.slots(20, List.of(10, 11, 12)));

// Instead of
menu.setRefreshConfig(RefreshConfig.all(20));  // Refreshes all slots
```

### Integration with Other Plugins

**PlaceholderAPI:**
```java
if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
    String parsed = PlaceholderAPI.setPlaceholders(player, text);
}
```

**Vault Economy:**
```java
MenuItem item = MenuItem.builder()
    .itemStack(itemStack)
    .dynamicPlaceholder("{balance}", player -> 
        String.format("$%.2f", economy.getBalance(player))
    )
    .build();
```

**LuckPerms:**
```java
DynamicMenuRegistry.register("permissions.yml", context -> {
    User user = luckPerms.getUserManager().getUser(context.player().getUniqueId());
    // Add permission nodes dynamically
});
```

---

## 📖 API Reference

### McMenuAPI

| Method | Description |
|--------|-------------|
| `openMenu(Player, String)` | Open a menu by filename |
| `openMenu(Player, String, T)` | Open a menu with context |
| `openMenuPreserveContext(Player, String)` | Open menu preserving context |
| `closeMenu(Player)` | Close player's current menu |
| `refreshMenu(Player)` | Refresh player's current menu |
| `reloadMenus()` | Reload all menus from disk |
| `getMenu(String)` | Get a loaded menu |
| `getOpenMenu(Player)` | Get player's current menu |
| `registerMenu(String, Menu)` | Register a menu programmatically |
| `unregisterMenu(String)` | Unregister a menu |
| `getMenuContext(Player, Class<T>)` | Get menu context for player |
| `updateContext(Player, T)` | Update context for player |
| `shutdown()` | Shutdown the API |

### Menu Interface

| Method | Description |
|--------|-------------|
| `open(Player)` | Open menu for player |
| `close(Player)` | Close menu for player |
| `refresh(Player)` | Refresh menu for player |
| `refreshSlots(Player, List<Integer>)` | Refresh specific slots |
| `getTitle()` | Get menu title |
| `getSize()` | Get menu size |
| `getItem(String)` | Get item by key |
| `setItem(String, MenuItem)` | Set item |
| `removeItem(String)` | Remove item |
| `setPlaceholders(Map)` | Set global placeholders |
| `getCurrentPage(Player)` | Get current page |
| `setPage(Player, int)` | Set page |
| `getTotalPages()` | Get total pages |
| `onOpen(Consumer)` | Add open handler |
| `onClose(Consumer)` | Add close handler |
| `onRefresh(Consumer)` | Add refresh handler |
| `setRefreshConfig(RefreshConfig)` | Set auto-refresh config |
| `setPlayerInventoryInteraction(boolean)` | Enable player inventory clicks |
| `setContextAware(boolean)` | Mark as context-aware |

### Action Factory Methods

| Method | Description |
|--------|-------------|
| `Action.command(String)` | Console command |
| `Action.playerCommand(String)` | Player command |
| `Action.sound(String)` | Play sound |
| `Action.message(String)` | Send message |
| `Action.broadcast(String)` | Broadcast message |
| `Action.close()` | Close menu |
| `Action.open(String)` | Open another menu |
| `Action.page(String)` | Change page |
| `Action.refresh()` | Refresh menu |

### ContextActionRegistry

| Method | Description |
|--------|-------------|
| `registerGlobal(String, Handler)` | Register global action |
| `register(String, String, Handler)` | Register menu-specific action |
| `getHandler(String, String)` | Get action handler |
| `execute(ActionContext, String)` | Execute action handler |
| `unregisterGlobal(String)` | Unregister global action |
| `unregister(String, String)` | Unregister menu action |
| `clearAll()` | Clear all handlers |

### PlayerInventoryHandlerRegistry

| Method | Description |
|--------|-------------|
| `register(String, Handler)` | Register inventory handler |
| `getHandler(String)` | Get handler for menu |
| `hasHandler(String)` | Check if handler exists |
| `unregister(String)` | Unregister handler |
| `clearAll()` | Clear all handlers |

### MenuContext

| Method | Description |
|--------|-------------|
| `set(Player, T)` | Set context for player |
| `get(Player, Class<T>)` | Get context with type check |
| `getRaw(Player)` | Get raw context object |
| `has(Player)` | Check if context exists |
| `has(Player, Class)` | Check if specific type exists |
| `clear(Player)` | Clear player context |
| `clearAll()` | Clear all contexts |

### RefreshConfig

| Method | Description |
|--------|-------------|
| `RefreshConfig.all(int)` | Refresh all slots |
| `RefreshConfig.slots(int, List)` | Refresh specific slots |
| `RefreshConfig.disabled()` | Disable refresh |
| `isEnabled()` | Check if enabled |
| `getIntervalTicks()` | Get interval |
| `getSlots()` | Get slots to refresh |
| `isRefreshAll()` | Check if refreshing all |

---

## 🤝 Support & Contributing

### Getting Help

- Check the [examples](#-examples) section
- Review the [documentation](#-documentation)
- Open an issue on GitHub
- Create a ticket on our [Discord](https://discord.gg/HMvs8hsdvu) server!

### Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Submit a pull request

---

## 📄 License

This project is licensed under the MIT License.

---

## 🙏 Credits

**Author:** coma112  
**Organization:** MonGens-Cave

Built with ❤️ for the Minecraft Development & Configurator community.

---

<div align="center">

**[⬆ Back to Top](#mc-menuapi)**

Made with Java 21 • Powered by Paper API • Built for Performance

**Version 1.0.7** - Context-Aware • Auto-Refresh • Player Inventory Interaction

</div>