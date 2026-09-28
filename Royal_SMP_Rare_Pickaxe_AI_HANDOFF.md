# Royal SMP Rare Pickaxe — AI Handoff / Project Context

## Purpose
This document explains the Royal SMP Rare Pickaxe Kite Kotlin script so that another ChatGPT/AI/developer can understand the existing implementation before editing it.

## Source Script
Current stable source:
`Royal_SMP_Rare_Pickaxe_FINAL_v4_STABLE.kite.kts`

Do NOT rewrite the whole system from scratch unless explicitly requested. Preserve the existing features and structure when making changes.

## Platform / Environment
- Minecraft server: Leaf 1.21.1
- Scripting: Kite Kotlin script
- Main namespace for PersistentDataContainer keys: `kite_custom`
- Command syntax uses Kite's `command("...") { execute { sender, args -> ... } }`
- Scheduled task uses `onLoad { server.scheduler.runTaskTimer(...) { ... } }`

## Current Pickaxe
Display name:
`Royal SMP Rare Pickaxe`

Material:
`NETHERITE_PICKAXE`

Command:
`/givemhs`
Optional:
`/givemhs <player>`

Permission:
`kite.admin`

## PersistentDataContainer Keys
```kotlin
val UNARMED_KEY = NamespacedKey("kite_custom", "mhs_unarmed")
val EXPIRY_KEY = NamespacedKey("kite_custom", "mhs_pickaxe_expiry")
val ITEM_ID_KEY = NamespacedKey("kite_custom", "mhs_pickaxe_id")
```

Meaning:
- `UNARMED_KEY`: marks a newly given/unused pickaxe that has not started its timer.
- `EXPIRY_KEY`: stores the activation expiry timestamp in milliseconds.
- `ITEM_ID_KEY`: identifies the custom Royal SMP Rare Pickaxe.

## Current Duration
The stable v4 file currently uses:
```kotlin
val PICKAXE_DURATION =
    24L * 60L * 60L * 1000L
```

This means 24 hours / 1 day.

For 7 days, use:
```kotlin
val PICKAXE_DURATION =
    7L * 24L * 60L * 60L * 1000L
```

IMPORTANT:
`L` is only Kotlin's Long suffix. It does NOT mean day/hour/minute.
In `7L * 24L * 60L * 60L * 1000L`:
- first `7L` = 7 days
- `24L` = 24 hours per day
- first `60L` = minutes per hour
- second `60L` = seconds per minute
- `1000L` = milliseconds per second

## Main Features
1. First-use activation:
   - Newly given pickaxe is marked with `UNARMED_KEY`.
   - Holding it starts the timer.
   - There is also a safety activation inside `BlockBreakEvent` because putting the item directly into the currently selected hotbar slot does not necessarily fire `PlayerItemHeldEvent`.
   - `PlayerJoinEvent` can also activate it if the player joins while holding the unused pickaxe.

2. 3x3 mining:
   - Breaks a 3x3 area based on the face/direction being mined.
   - Uses `player.getTargetBlockExact(6)` and `BlockFace`.
   - Skips AIR and BEDROCK.
   - Skips CHEST, TRAPPED_CHEST, BARREL and SHULKER_BOX.
   - Cancels the normal block break and manually processes the 3x3 blocks.

3. Drops:
   - Uses `block.getDrops(tool, player)` so normal tool/enchantment behavior is considered.
   - Iron/Gold/Copper ores are converted to ingots and directly picked up.
   - Nether Gold, Diamond, Emerald, Coal, Redstone, Lapis and Nether Quartz are directly picked up.
   - If inventory is full, leftovers are dropped naturally.
   - Other drops are dropped normally.

4. Durability:
   - Pickaxe is NOT unbreakable.
   - Uses `Damageable`.
   - Applies durability loss for each block actually broken.
   - Includes a vanilla-style Unbreaking chance.
   - Removes the pickaxe when durability is exhausted.

5. Countdown lore:
   Active lore shows:
   - `✦ Ability: 3x3 Mining Area`
   - `✦ Feature: Auto Smelt Ores & Direct Pickup`
   - `⏳ Expires in: Xh Xm Xs`

6. Expiration:
   - Expiry is stored as a timestamp.
   - Expired pickaxe is removed.
   - Player receives an expiration message.

## IMPORTANT ANIMATION FIX
This is a deliberate feature and MUST be preserved unless explicitly asked to change it.

Problem:
Updating the lore of the item currently held in the player's hand every second can cause visible hand/equip/raise-lower animation.

Current solution:
The 1-second scheduler deliberately skips the currently held hotbar slot:
```kotlin
if (slot == heldSlot) {
    continue
}
```

Therefore:
- Inventory pickaxes not currently held can have their lore updated every second.
- The currently held item is NOT rewritten every second.
- When the player switches to the pickaxe, its lore is refreshed once.
- This avoids the repeated held-item animation caused by constant item replacement/meta updates.

Do NOT remove this behavior just to make the held tooltip visually tick every second. True zero-animation live updates would require packet/NMS/client-side handling, not the current simple Bukkit/Kite item-meta approach.

## Important Existing Messages
The current v4 source contains hard-coded `24h` / `24-hour` text in the initial lore and activation messages.

If duration is changed from 24 hours to 7 days, the actual timer changes automatically, but these display strings should also be updated.

A cleaner future implementation is:
```kotlin
val PICKAXE_DAYS = 7L
val PICKAXE_DURATION =
    PICKAXE_DAYS * 24L * 60L * 60L * 1000L
```

Then use `${PICKAXE_DAYS}d` / `${PICKAXE_DAYS}-day` in user-facing text so future duration changes require changing only one number.

## Important Functions
- `isRoyalRarePickaxe(item)`
- `createCountdownLore(timeLeft)`
- `updatePickaxeLore(item)`
- `activateRoyalRarePickaxe(player, item)`
- `damageRoyalPickaxe(player, tool)`

## Event Handlers
- `PlayerItemHeldEvent`: first activation / refresh / expiration check
- `PlayerJoinEvent`: activates if joining while holding unused pickaxe
- `BlockBreakEvent`: 3x3 mining, drops, auto-smelt, direct pickup, durability, expiration
- `onLoad`: 1-second timer and expiration/lore maintenance

## Editing Rules for Future AI
When asked to modify this script:
1. Preserve all existing features unless the user explicitly asks to remove one.
2. Preserve the animation-safe scheduler behavior.
3. Preserve PDC keys unless a migration is intentionally planned.
4. Do not change Kite command syntax to Bukkit `onCommand`.
5. Do not make the pickaxe unbreakable unless explicitly requested.
6. If changing duration, check BOTH the timer constant AND any hard-coded duration text.
7. If changing mining behavior, keep normal drops/enchantments in mind.
8. After edits, check braces and Kotlin syntax carefully.
9. Prefer small targeted changes over rewriting the entire script.
10. If the user supplies a newer script, treat that supplied version as authoritative over this document.

## One-line Project Summary
Royal SMP Rare Pickaxe is a breakable Netherite custom pickaxe for Leaf 1.21.1/Kite that activates on first use, runs for a configurable duration, mines 3x3, auto-smelts selected ores, directly picks up selected valuable drops, tracks durability, expires automatically, and deliberately avoids rewriting the currently held item every second to prevent repeated hand/equip animation.
