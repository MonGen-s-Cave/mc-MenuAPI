# mc-MenuAPI

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.3-blue.svg)
![Minecraft](https://img.shields.io/badge/minecraft-1.21+-green.svg)
![Java](https://img.shields.io/badge/java-21-orange.svg)
![License](https://img.shields.io/badge/license-MIT-purple.svg)

*A powerful, flexible, and developer-friendly menu system for Minecraft Paper servers*

[Features](#-features) • [Installation](#-installation) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Examples](#-examples)

</div>

---

## 🌟 What is mc-MenuAPI?

mc-MenuAPI is a comprehensive GUI menu library designed for Paper/Spigot plugin developers who want to create beautiful, interactive inventory menus without the hassle. Whether you're building a simple shop, a complex paginated player list, or dynamic item browsers, mc-MenuAPI has you covered with an intuitive API and powerful YAML configuration system.

### Why mc-MenuAPI?

- **Zero Boilerplate** - Focus on your menu logic, not inventory management
- **YAML-First Design** - Define menus in configuration files with hot-reload support
- **Dynamic Content** - Build menus programmatically with full placeholder support
- **Smart Pagination** - Automatic page handling with built-in navigation
- **Custom Item Support** - Seamless integration with Nexo, ItemsAdder, and Oraxen
- **Performance Optimized** - Thread-safe, concurrent operations with minimal overhead
- **Conditional Actions** - Execute different actions based on runtime conditions
- **Modern API** - Fluent builders, lambdas, and clean method chaining

---

## Features

### Core Capabilities

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

#### Rich Placeholders
- Global menu placeholders
- Per-item placeholders
- Dynamic runtime placeholders
- Automatic placeholder resolution

---

## 📦 Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://repo.mongenscave.com/releases")
}

dependencies {
    implementation("com.mongenscave:mc-MenuAPI:1.0.3")
}
```

### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://repo.mongenscave.com/releases' }
}

dependencies {
    implementation 'com.mongenscave:mc-MenuAPI:1.0.3'
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
        <version>1.0.3</version>
    </dependency>
</dependencies>
```

---

## Quick Start

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
}
```

### 2. Create Your First Menu (YAML)

Create `menus/shop.yml`:

```yaml
title: "&6&lShop Menu"
size: 27

items:
  diamond:
    material: DIAMOND
    name: "&b&lDiamond"
    lore:
      - "&7Click to buy"
      - "&7Price: &e$100"
    slot: 13
    actions:
      - "[SOUND] ENTITY_EXPERIENCE_ORB_PICKUP"
      - "[CONSOLE] eco take {player} 100"
      - "[MESSAGE] &aYou bought a diamond!"
      - "[CLOSE]"
  
  close:
    material: BARRIER
    name: "&c&lClose"
    slot: 22
    actions:
      - "[CLOSE]"
```

### 3. Open the Menu

```java
menuAPI.openMenu(player, "shop.yml");
```

That's it! You've created your first interactive menu.

---

## Documentation

### Core Concepts

#### McMenuAPI - The Main Class

The `McMenuAPI` class is your entry point to the entire system. It manages menu loading, player states, and provides all core functionality.

```java
// Initialize
McMenuAPI api = new McMenuAPI(plugin, menusFolder);

// Open a menu for a player
api.openMenu(player, "menu.yml");

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
```

#### Menu Interface

All menus implement the `Menu` interface, providing a consistent API:

```java
menu.open(player);          // Open for a player
menu.close(player);         // Close for a player
menu.refresh(player);       // Refresh content
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
```

---

### YAML Configuration

#### Basic Structure

```yaml
# Menu title with color support
title: "&6&lMy Menu"

# Size (must be multiple of 9, between 9-54)
size: 54

# Items definition
items:
  item_key:
    material: MATERIAL_NAME
    name: "&aDisplay Name"
    lore:
      - "&7Line 1"
      - "&7Line 2"
    slot: 0  # or multiple slots
    amount: 1
    actions:
      - "[ACTION_TYPE] value"
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
```

#### Color Support

mc-MenuAPI supports multiple color formats:

**Legacy Colors:**
```yaml
name: "&a&lGreen Text"
```

**Hex Colors:**
```yaml
name: "&#FF5555Red Text"
lore:
  - "&#00FF00Green"
  - "&#0000FFBlue"
```

---

### Actions System

Actions execute when players click menu items. They're powerful, chainable, and support conditional logic.

#### Available Actions

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

**Supported Conditions:**
- `{page}` - Current menu page (pagination)
- `{health}` - Player health
- `{level}` - Player experience level
- `{permission}` - Permission check
- `{name}` - Player name check

**Operators:**
- `==` or `equals` - Equal to
- `!=` - Not equal to
- `>` - Greater than
- `<` - Less than
- `>=` - Greater than or equal
- `<=` - Less than or equal
- `has` - Has permission (for {permission})

**Multiple Actions in Conditions:**
```yaml
actions:
  - "[IF] {level} >= 50 [THEN] [MESSAGE] &aMax level! [SOUND] ENTITY_PLAYER_LEVELUP 1.0 2.0 [ELSE] [MESSAGE] &7Keep grinding!"
```

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

**Basic Paginated Menu:**
```java
// Define which slots will contain paginated items
int[] pageSlots = new int[]{
    10, 11, 12, 13, 14, 15, 16,
    19, 20, 21, 22, 23, 24, 25,
    28, 29, 30, 31, 32, 33, 34
};

PaginatedMenu menu = new PaginatedMenu("&6Player List", 54, pageSlots);

// Add static items (borders, buttons, etc.)
MenuItem border = MenuItem.builder()
    .itemStack(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
    .slots(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8))
    .clickable(false)
    .build();
menu.setItem("border", border);

// Add page items (these will be paginated automatically)
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

The `SmartPaginatedMenu` provides an even easier way to create paginated menus:

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

### Dynamic Menu Builders

Create menus with runtime-generated content using the dynamic builder system.

#### Registering a Dynamic Builder

```java
DynamicMenuRegistry.register("boxes.yml", context -> {
    Player player = context.player();
    Inventory inv = context.inventory();
    
    // Add items dynamically
    for (int i = 0; i < 10; i++) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Box #" + i);
        meta.setLore(List.of("§7Click to open"));
        item.setItemMeta(meta);
        
        inv.setItem(10 + i, item);
        
        // Register click handler for this slot
        context.registerClickHandler(10 + i, (p, clickedItem, clickType) -> {
            p.sendMessage("§aYou opened box #" + i);
            p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        });
    }
});
```

#### Dynamic Click Handlers

Register click handlers for specific slots:

```java
// Single slot handler
context.registerClickHandler(13, (player, item, clickType) -> {
    if (clickType == ClickType.LEFT) {
        player.sendMessage("§aLeft click!");
    } else if (clickType == ClickType.RIGHT) {
        player.sendMessage("§eRight click!");
    }
});

// Multiple slots with same handler
int[] slots = {10, 11, 12, 19, 20, 21};
context.registerClickHandler(slots, (player, item, clickType) -> {
    player.sendMessage("§7You clicked a slot!");
});
```

---

### Programmatic Menu Building

Create menus entirely in code using the fluent builder API.

#### MenuBuilder

```java
SimpleMenu menu = MenuBuilder.create("&6&lCustom Shop", 27)
    .item("diamond", builder -> builder
        .itemStack(new ItemStack(Material.DIAMOND))
        .slot(13)
        .action(Action.consoleCommand("give {player} diamond 1"))
        .action(Action.sound("ENTITY_EXPERIENCE_ORB_PICKUP"))
        .action(Action.message("&aYou received a diamond!"))
    )
    .item("close", builder -> builder
        .itemStack(new ItemStack(Material.BARRIER))
        .slot(22)
        .action(Action.close())
    )
    .placeholder("{server}", "MyServer")
    .onOpen(player -> player.sendMessage("§eWelcome to the shop!"))
    .onClose(player -> player.sendMessage("§7Thanks for visiting!"))
    .build();

// Register and open
McMenuAPI.getInstance().registerMenu("custom-shop.yml", menu);
menu.open(player);
```

#### MenuItem Builder

```java
MenuItem item = MenuItem.builder()
    .itemStack(createCustomItem())
    .slot(13)
    .slots(List.of(10, 11, 12)) // Multiple slots
    .action(Action.message("§aClicked!"))
    .action(Action.sound("UI_BUTTON_CLICK"))
    .priority(10)
    .clickable(true)
    .placeholder("{price}", "100")
    .dynamicPlaceholder("{time}", player -> 
        String.valueOf(System.currentTimeMillis())
    )
    .customClickHandler((player, menuItem) -> {
        // Custom logic
        player.sendMessage("§eCustom handler executed!");
    })
    .build();
```

---

### ItemFactory and ItemBuilder

Create items programmatically with a fluent API.

#### Basic Item Creation

```java
ItemStack item = ItemFactory.create(Material.DIAMOND_SWORD)
    .setName("&b&lLegendary Sword")
    .addLore("&7A powerful weapon", "&#FF5555Deals 50 damage")
    .addEnchantment(Enchantment.SHARPNESS, 5)
    .addEnchantment(Enchantment.UNBREAKING, 3)
    .setUnbreakable()
    .addFlag(ItemFlag.HIDE_ENCHANTS)
    .finish();
```

#### Building from YAML Section

```java
YamlDocument config = YamlDocument.create(file);
Section itemSection = config.getSection("items.diamond");

Optional<ItemStack> item = ItemFactory.buildItem(itemSection);
item.ifPresent(itemStack -> {
    // Use the item
});
```

#### Custom Items

```java
// Build a custom item from a plugin
Optional<ItemStack> nexoItem = ItemFactory.buildCustomItem(
    section, 
    "nexo:custom_sword"
);

Optional<ItemStack> iaItem = ItemFactory.buildCustomItem(
    section, 
    "itemsadder:magic_staff"
);

Optional<ItemStack> oraxenItem = ItemFactory.buildCustomItem(
    section, 
    "oraxen:ruby_pickaxe"
);
```

---

### Placeholders

#### Global Menu Placeholders

Set placeholders that apply to all items in a menu:

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

Evaluate placeholders at runtime:

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

#### Built-in Placeholder

The `{player}` placeholder is automatically replaced with the player's name in all actions:

```yaml
actions:
  - "[CONSOLE] give {player} diamond 1"
  - "[MESSAGE] Welcome, {player}!"
  - "[BROADCAST] {player} just bought an item!"
```

---

### Event Handlers

Add custom logic when menus are opened or closed.

```java
menu.onOpen(player -> {
    player.sendMessage("§eWelcome to the menu!");
    player.playSound(player.getLocation(), Sound.UI_TOAST_IN, 1.0f, 1.0f);
});

menu.onClose(player -> {
    player.sendMessage("§7Menu closed.");
    // Save player data, cleanup, etc.
});
```

**Multiple Handlers:**
```java
menu.onOpen(player -> player.sendMessage("§eOpening..."))
    .onOpen(player -> player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f))
    .onClose(player -> savePlayerData(player))
    .onClose(player -> player.sendMessage("§7Goodbye!"));
```

---

## Examples

### Example 1: Simple Shop Menu

**YAML (`menus/shop.yml`):**
```yaml
title: "&6&l✦ Shop Menu ✦"
size: 27

items:
  border:
    material: BLACK_STAINED_GLASS_PANE
    name: " "
    slot: "0-8, 9, 17, 18-26"
    clickable: false
  
  diamond:
    material: DIAMOND
    name: "&b&lDiamond"
    lore:
      - "&7Price: &e$100"
      - ""
      - "&aClick to purchase"
    slot: 11
    actions:
      - "[IF] {level} >= 5 [THEN] [CONSOLE] eco take {player} 100 [CONSOLE] give {player} diamond 1 [MESSAGE] &aPurchased! [SOUND] ENTITY_EXPERIENCE_ORB_PICKUP [ELSE] [MESSAGE] &cYou need level 5!"
  
  emerald:
    material: EMERALD
    name: "&a&lEmerald"
    lore:
      - "&7Price: &e$250"
      - ""
      - "&aClick to purchase"
    slot: 13
    actions:
      - "[CONSOLE] eco take {player} 250"
      - "[CONSOLE] give {player} emerald 1"
      - "[MESSAGE] &aPurchased an emerald!"
      - "[SOUND] ENTITY_EXPERIENCE_ORB_PICKUP"
  
  netherite:
    material: NETHERITE_INGOT
    name: "&4&lNetherite"
    lore:
      - "&7Price: &e$1000"
      - ""
      - "&aClick to purchase"
    slot: 15
    actions:
      - "[CONSOLE] eco take {player} 1000"
      - "[CONSOLE] give {player} netherite_ingot 1"
      - "[MESSAGE] &aPurchased netherite!"
      - "[SOUND] ENTITY_EXPERIENCE_ORB_PICKUP 1.0 0.5"
  
  close:
    material: BARRIER
    name: "&c&lClose"
    slot: 22
    actions:
      - "[SOUND] UI_BUTTON_CLICK"
      - "[CLOSE]"
```

### Example 2: Player List (Paginated)

**Java:**
```java
public void openPlayerList(Player viewer) {
    int[] pageSlots = IntStream.range(10, 44)
        .filter(i -> i % 9 != 0 && i % 9 != 8)
        .toArray();
    
    PaginatedMenu menu = new PaginatedMenu(
        "§6§lOnline Players", 
        54, 
        pageSlots
    );
    
    // Add border
    MenuItem border = MenuItem.builder()
        .itemStack(new ItemStack(Material.GRAY_STAINED_GLASS_PANE))
        .slots(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 
                       45, 46, 47, 48, 49, 50, 51, 52, 53))
        .clickable(false)
        .build();
    menu.setItem("border", border);
    
    // Add player heads
    for (Player online : Bukkit.getOnlinePlayers()) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(online);
        meta.setDisplayName("§e" + online.getName());
        meta.setLore(List.of(
            "§7Health: §c" + online.getHealth() + "❤",
            "§7Level: §a" + online.getLevel(),
            "",
            "§eClick to teleport"
        ));
        skull.setItemMeta(meta);
        
        MenuItem playerItem = MenuItem.builder()
            .itemStack(skull)
            .action(Action.consoleCommand("tp " + viewer.getName() + " " + online.getName()))
            .action(Action.message("§aTeleporting to " + online.getName()))
            .action(Action.close())
            .build();
        
        menu.addPageItem(playerItem);
    }
    
    // Navigation buttons
    MenuItem prev = MenuItem.builder()
        .itemStack(createArrow("§e← Previous"))
        .slot(48)
        .action(Action.page("-1"))
        .build();
    menu.setPreviousPageItem(prev);
    
    MenuItem next = MenuItem.builder()
        .itemStack(createArrow("§eNext →"))
        .slot(50)
        .action(Action.page("+1"))
        .build();
    menu.setNextPageItem(next);
    
    menu.open(viewer);
}
```

### Example 3: Dynamic Warps Menu

**Java:**
```java
public class WarpsMenu {
    
    public void openWarpsMenu(Player player) {
        // Register dynamic builder
        DynamicMenuRegistry.register("warps.yml", context -> {
            List<Warp> warps = loadWarps(); // Your warp loading logic
            
            int slot = 10;
            for (Warp warp : warps) {
                ItemStack item = new ItemStack(Material.ENDER_PEARL);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName("§d" + warp.getName());
                meta.setLore(List.of(
                    "§7Location: §e" + formatLocation(warp.getLocation()),
                    "§7Created by: §a" + warp.getCreator(),
                    "",
                    "§eClick to teleport"
                ));
                item.setItemMeta(meta);
                
                context.inventory().setItem(slot, item);
                
                context.registerClickHandler(slot, (p, clickedItem, clickType) -> {
                    p.teleport(warp.getLocation());
                    p.sendMessage("§aTeleported to " + warp.getName());
                    p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    p.closeInventory();
                });
                
                slot++;
            }
        });
        
        // Open the menu
        McMenuAPI.getInstance().openMenu(player, "warps.yml");
    }
}
```

### Example 4: Confirmation Menu

**Java:**
```java
public void openConfirmation(Player player, Runnable onConfirm) {
    SimpleMenu menu = MenuBuilder.create("§c§lAre you sure?", 27)
        .item("confirm", builder -> builder
            .itemStack(createItem(Material.GREEN_WOOL, "§a§lCONFIRM"))
            .slot(11)
            .customClickHandler((p, item) -> {
                onConfirm.run();
                p.closeInventory();
            })
        )
        .item("cancel", builder -> builder
            .itemStack(createItem(Material.RED_WOOL, "§c§lCANCEL"))
            .slot(15)
            .action(Action.message("§7Cancelled."))
            .action(Action.close())
        )
        .build();
    
    menu.open(player);
}

// Usage
openConfirmation(player, () -> {
    player.sendMessage("§aAction confirmed!");
    // Do something
});
```

### Example 5: Settings Menu with Toggle

**YAML (`menus/settings.yml`):**
```yaml
title: "&e&lSettings"
size: 27

items:
  flight:
    material: FEATHER
    name: "&bFlight Mode"
    lore:
      - "&7Click to toggle"
      - "&7Status: {flight_status}"
    slot: 11
    # Handled dynamically in code
  
  pvp:
    material: DIAMOND_SWORD
    name: "&cPvP Mode"
    lore:
      - "&7Click to toggle"
      - "&7Status: {pvp_status}"
    slot: 13
    # Handled dynamically in code
```

**Java:**
```java
public void openSettings(Player player) {
    Optional<Menu> menuOpt = McMenuAPI.getInstance().getMenu("settings.yml");
    if (menuOpt.isEmpty()) return;
    
    Menu menu = menuOpt.get();
    
    // Set dynamic placeholders
    menu.setPlaceholders(Map.of(
        "{flight_status}", player.getAllowFlight() ? "§aEnabled" : "§cDisabled",
        "{pvp_status}", !player.hasMetadata("pvp_disabled") ? "§aEnabled" : "§cDisabled"
    ));
    
    // Register dynamic click handlers
    DynamicMenuRegistry.register("settings.yml", context -> {
        // Flight toggle
        context.registerClickHandler(11, (p, item, clickType) -> {
            p.setAllowFlight(!p.getAllowFlight());
            p.sendMessage(p.getAllowFlight() ? "§aFlight enabled" : "§cFlight disabled");
            McMenuAPI.getInstance().refreshMenu(p);
        });
        
        // PvP toggle
        context.registerClickHandler(13, (p, item, clickType) -> {
            if (p.hasMetadata("pvp_disabled")) {
                p.removeMetadata("pvp_disabled", plugin);
                p.sendMessage("§aPvP enabled");
            } else {
                p.setMetadata("pvp_disabled", new FixedMetadataValue(plugin, true));
                p.sendMessage("§cPvP disabled");
            }
            McMenuAPI.getInstance().refreshMenu(p);
        });
    });
    
    menu.open(player);
}
```

### Example 6: Multi-Page Shop with Categories

**Java:**
```java
public void openCategoryShop(Player player, ShopCategory category) {
    SmartPaginatedMenu.builder()
        .fromYAML("category-shop.yml")
        .titlePlaceholder("{category}", category.getName())
        .dynamicItems(menu -> {
            for (ShopItem shopItem : category.getItems()) {
                menu.addPageItem("item_template", template -> {
                    template.setItemStack(shopItem.getDisplayItem());
                    template.placeholder("{name}", shopItem.getName());
                    template.placeholder("{price}", String.valueOf(shopItem.getPrice()));
                    template.placeholder("{stock}", String.valueOf(shopItem.getStock()));
                    
                    template.onClick(buyer -> {
                        if (shopItem.purchase(buyer)) {
                            buyer.sendMessage("§aPurchased " + shopItem.getName());
                            menu.refresh(buyer);
                        } else {
                            buyer.sendMessage("§cCannot afford this item!");
                        }
                    });
                });
            }
        })
        .open(player);
}
```

---

## Advanced Topics

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

## API Reference

### McMenuAPI

| Method | Description |
|--------|-------------|
| `openMenu(Player, String)` | Open a menu by filename |
| `closeMenu(Player)` | Close player's current menu |
| `refreshMenu(Player)` | Refresh player's current menu |
| `reloadMenus()` | Reload all menus from disk |
| `getMenu(String)` | Get a loaded menu |
| `getOpenMenu(Player)` | Get player's current menu |
| `registerMenu(String, Menu)` | Register a menu programmatically |
| `unregisterMenu(String)` | Unregister a menu |

### Menu Interface

| Method | Description |
|--------|-------------|
| `open(Player)` | Open menu for player |
| `close(Player)` | Close menu for player |
| `refresh(Player)` | Refresh menu for player |
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

## License

This project is licensed under the MIT License.

---

## 🙏 Credits

**Author:** coma112  
**Organization:** MonGens-Cave

Built with LOVE for the Minecraft Development & Configurator community.

---

<div align="center">

**[⬆ Back to Top](#-mc-menuapi)**

Made with Java 21 • Powered by Paper API • Built for Performance

</div>