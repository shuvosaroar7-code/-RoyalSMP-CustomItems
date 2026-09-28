import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.enchantments.Enchantment
import kotlin.random.Random
import org.bukkit.persistence.PersistentDataType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.UUID


// ============================================================
// ROYAL SMP RARE PICKAXE
// ============================================================

val UNARMED_KEY = NamespacedKey("kite_custom", "mhs_unarmed")

val EXPIRY_KEY = NamespacedKey("kite_custom", "mhs_pickaxe_expiry")

val ITEM_ID_KEY = NamespacedKey("kite_custom", "mhs_pickaxe_id")

val PICKAXE_DURATION =
    24L * 60L * 60L * 1000L


// ============================================================
// CHECK ROYAL SMP PICKAXE
// ============================================================

fun isRoyalRarePickaxe(item: ItemStack?): Boolean {

    if (item == null) return false
    if (item.type == Material.AIR) return false
    if (!item.hasItemMeta()) return false

    return item.itemMeta
        .persistentDataContainer
        .has(
            ITEM_ID_KEY,
            PersistentDataType.STRING
        )
}


// ============================================================
// CREATE COUNTDOWN LORE
// ============================================================

fun createCountdownLore(
    timeLeft: Long
): List<Component> {

    val totalSeconds =
        timeLeft / 1000L

    val hours =
        totalSeconds / 3600L

    val minutes =
        (totalSeconds % 3600L) / 60L

    val seconds =
        totalSeconds % 60L

    return listOf(

        Component.text(
            "✦ Ability: 3x3 Mining Area",
            NamedTextColor.GOLD
        ),

        Component.text(
            "✦ Feature: Auto Smelt Ores & Direct Pickup",
            NamedTextColor.YELLOW
        ),

        Component.empty(),

        Component.text(
            "⏳ Expires in: ",
            NamedTextColor.RED
        ).append(
            Component.text(
                "${hours}h ${minutes}m ${seconds}s",
                NamedTextColor.WHITE
            ).decorate(
                TextDecoration.BOLD
            )
        )
    )
}


// ============================================================
// UPDATE LORE
// ============================================================

fun updatePickaxeLore(
    item: ItemStack
): Boolean {

    if (!isRoyalRarePickaxe(item)) {
        return false
    }

    val meta =
        item.itemMeta

    val expiry =
        meta.persistentDataContainer.get(
            EXPIRY_KEY,
            PersistentDataType.LONG
        )
        ?: return false

    val timeLeft =
        expiry - System.currentTimeMillis()


    // --------------------------------------------------------
    // EXPIRED
    // --------------------------------------------------------

    if (timeLeft <= 0L) {
        return true
    }


    // --------------------------------------------------------
    // CREATE NEW LORE
    // --------------------------------------------------------

    val newLore =
        createCountdownLore(timeLeft)


    // --------------------------------------------------------
    // DON'T UPDATE IF NOTHING CHANGED
    // --------------------------------------------------------

    if (meta.lore() == newLore) {
        return false
    }


    // --------------------------------------------------------
    // UPDATE ONLY LORE
    // --------------------------------------------------------

    meta.lore(newLore)

    item.itemMeta = meta

    return false
}


// ============================================================
// ACTIVATE PICKAXE
// ============================================================

fun activateRoyalRarePickaxe(
    player: Player,
    item: ItemStack
) {

    if (!isRoyalRarePickaxe(item)) {
        return
    }

    val meta =
        item.itemMeta

    val pdc =
        meta.persistentDataContainer


    // Already activated
    if (
        pdc.has(
            EXPIRY_KEY,
            PersistentDataType.LONG
        )
    ) {
        return
    }


    // Only unused pickaxe can activate
    if (
        !pdc.has(
            UNARMED_KEY,
            PersistentDataType.BYTE
        )
    ) {
        return
    }


    // --------------------------------------------------------
    // REMOVE UNUSED FLAG
    // --------------------------------------------------------

    pdc.remove(
        UNARMED_KEY
    )


    // --------------------------------------------------------
    // START 24 HOUR TIMER
    // --------------------------------------------------------

    val expiry =
        System.currentTimeMillis() +
        PICKAXE_DURATION

    pdc.set(
        EXPIRY_KEY,
        PersistentDataType.LONG,
        expiry
    )

    item.itemMeta = meta


    // --------------------------------------------------------
    // INITIAL LORE
    // --------------------------------------------------------

    updatePickaxeLore(item)


    player.sendMessage(
        Component.text(
            "⏰ Royal SMP Rare Pickaxe Activated!",
            NamedTextColor.GOLD
        )
    )

    player.sendMessage(
        Component.text(
            "24-hour countdown started.",
            NamedTextColor.GRAY
        )
    )
}


// ============================================================
// GIVE COMMAND
//
// /givemhs
// /givemhs <player>
// ============================================================

command("givemhs") {

    description =
        "Give Royal SMP Rare Pickaxe"

    permission =
        "kite.admin"

    usage =
        "/givemhs [player]"


    execute { sender, args ->

        val target: Player?


        // ----------------------------------------------------
        // TARGET PLAYER
        // ----------------------------------------------------

        if (args.isNotEmpty()) {

            target =
                Bukkit.getPlayer(
                    args[0]
                )

        } else {

            target =
                sender as? Player
        }


        // ----------------------------------------------------
        // TARGET NOT FOUND
        // ----------------------------------------------------

        if (target == null) {

            sender.sendMessage(
                Component.text(
                    "Player not found!",
                    NamedTextColor.RED
                )
            )

            return@execute
        }


        // ----------------------------------------------------
        // CREATE PICKAXE
        // ----------------------------------------------------

        val item =
            ItemStack(
                Material.NETHERITE_PICKAXE
            )

        val meta =
            item.itemMeta


        // ----------------------------------------------------
        // PICKAXE NAME
        // ----------------------------------------------------

        meta.displayName(
            Component.text(
                "Royal SMP Rare Pickaxe",
                NamedTextColor.GOLD
            ).decorate(
                TextDecoration.BOLD
            )
        )

        // Keep it breakable like a normal Netherite Pickaxe.
        meta.isUnbreakable = false


        // ----------------------------------------------------
        // FIRST USE FLAG
        // ----------------------------------------------------

        meta.persistentDataContainer.set(
            UNARMED_KEY,
            PersistentDataType.BYTE,
            1.toByte()
        )


        // ----------------------------------------------------
        // UNIQUE PICKAXE ID
        // ----------------------------------------------------

        meta.persistentDataContainer.set(
            ITEM_ID_KEY,
            PersistentDataType.STRING,
            UUID.randomUUID().toString()
        )


        // ----------------------------------------------------
        // INITIAL LORE
        // ----------------------------------------------------

        val lore =
            listOf(

                Component.text(
                    "✦ Ability: 3x3 Mining Area",
                    NamedTextColor.GOLD
                ),

                Component.text(
                    "✦ Feature: Auto Smelt Ores & Direct Pickup",
                    NamedTextColor.YELLOW
                ),

                Component.empty(),

                Component.text(
                    "⏳ Status: ",
                    NamedTextColor.RED
                ).append(
                    Component.text(
                        "Hold in hand to activate (24h)",
                        NamedTextColor.GRAY
                    ).decorate(
                        TextDecoration.ITALIC
                    )
                )
            )


        meta.lore(lore)

        item.itemMeta = meta


        // ----------------------------------------------------
        // GIVE ITEM
        // ----------------------------------------------------

        target.inventory.addItem(
            item
        )


        target.sendMessage(
            Component.text(
                "🎉 You received a Royal SMP Rare Pickaxe!",
                NamedTextColor.GREEN
            )
        )

        target.sendMessage(
            Component.text(
                "Hold it in your hand to activate the 24-hour timer.",
                NamedTextColor.GRAY
            )
        )
    }
}


// ============================================================
// FIRST TIME HOLDING PICKAXE
// ============================================================

on<PlayerItemHeldEvent> { event ->

    val player =
        event.player

    val item =
        player.inventory.getItem(
            event.newSlot
        ) ?: return@on


    if (
        !isRoyalRarePickaxe(item)
    ) {
        return@on
    }


    val meta =
        item.itemMeta

    val pdc =
        meta.persistentDataContainer


    // --------------------------------------------------------
    // FIRST ACTIVATION
    // --------------------------------------------------------

    if (
        pdc.has(
            UNARMED_KEY,
            PersistentDataType.BYTE
        )
    ) {

        activateRoyalRarePickaxe(
            player,
            item
        )

        return@on
    }


    // --------------------------------------------------------
    // CHECK ACTIVE PICKAXE
    // --------------------------------------------------------

    if (
        pdc.has(
            EXPIRY_KEY,
            PersistentDataType.LONG
        )
    ) {

        val expired =
            updatePickaxeLore(item)


        if (expired) {

            player.inventory.setItem(
                event.newSlot,
                ItemStack(Material.AIR)
            )

            player.sendMessage(
                Component.text(
                    "⌛ Your Royal SMP Rare Pickaxe has expired!",
                    NamedTextColor.RED
                )
            )
        }
    }
}


// ============================================================
// ACTIVATE IF PLAYER JOINS WITH PICKAXE IN MAIN HAND
// ============================================================

on<PlayerJoinEvent> { event ->

    val player =
        event.player

    val item =
        player.inventory.itemInMainHand

    if (!isRoyalRarePickaxe(item)) {
        return@on
    }

    val meta =
        item.itemMeta

    val pdc =
        meta.persistentDataContainer

    if (
        pdc.has(
            UNARMED_KEY,
            PersistentDataType.BYTE
        )
    ) {

        activateRoyalRarePickaxe(
            player,
            item
        )
    }
}


// ============================================================
// VANILLA-STYLE DURABILITY
// ============================================================

fun damageRoyalPickaxe(
    player: Player,
    tool: ItemStack
): Boolean {

    if (tool.type != Material.NETHERITE_PICKAXE) {
        return false
    }

    val meta =
        tool.itemMeta as? Damageable
        ?: return false

    if (meta.isUnbreakable) {
        return false
    }

    val unbreakingLevel =
        tool.getEnchantmentLevel(
            Enchantment.UNBREAKING
        )

    // Vanilla-style Unbreaking chance for tools:
    // each block has a 1/(level + 1) chance to consume durability.
    val shouldDamage =
        if (unbreakingLevel <= 0) {
            true
        } else {
            Random.nextInt(
                unbreakingLevel + 1
            ) == 0
        }

    if (!shouldDamage) {
        return false
    }

    val currentDamage =
        meta.damage

    val maxDurability =
        tool.type.maxDurability.toInt()

    val newDamage =
        currentDamage + 1

    if (newDamage >= maxDurability) {

        player.inventory.setItemInMainHand(
            ItemStack(Material.AIR)
        )

        player.sendMessage(
            Component.text(
                "⛏ Your Royal SMP Rare Pickaxe broke!",
                NamedTextColor.RED
            )
        )

        return true
    }

    meta.damage = newDamage
    tool.itemMeta = meta

    return false
}

// ============================================================
// 3x3 MINING
// AUTO SMELT
// DIRECT PICKUP
// ============================================================

on<BlockBreakEvent> { event ->

    val player =
        event.player

    val tool =
        player.inventory.itemInMainHand


    // --------------------------------------------------------

    // --------------------------------------------------------
    // ONLY ROYAL SMP RARE PICKAXE
    // --------------------------------------------------------

    if (!isRoyalRarePickaxe(tool)) {
        return@on
    }

    val meta =
        tool.itemMeta

    val pdc =
        meta.persistentDataContainer

    // --------------------------------------------------------
    // SAFETY: ACTIVATE IF THE PICKAXE IS ALREADY HELD
    // --------------------------------------------------------
    // If /givemhs places the pickaxe directly into the currently
    // selected hotbar slot, PlayerItemHeldEvent does not fire.
    // In that case the first block break must activate it here.
    // This keeps the original "first time in hand = start 24h"
    // behavior without changing the animation-safe timer system.

    if (
        pdc.has(
            UNARMED_KEY,
            PersistentDataType.BYTE
        ) &&
        !pdc.has(
            EXPIRY_KEY,
            PersistentDataType.LONG
        )
    ) {
        activateRoyalRarePickaxe(
            player,
            tool
        )
    }

    // --------------------------------------------------------
    // EXPIRATION CHECK
    // --------------------------------------------------------

    if (
        updatePickaxeLore(tool)
    ) {

        player.inventory.setItemInMainHand(
            ItemStack(Material.AIR)
        )

        player.sendMessage(
            Component.text(
                "⌛ Your Royal SMP Rare Pickaxe has expired!",
                NamedTextColor.RED
            )
        )

        event.isCancelled = true
        return@on
    }

    // Re-read metadata after possible first activation.
    val activeMeta =
        tool.itemMeta

    val activePdc =
        activeMeta.persistentDataContainer

    if (
        !activePdc.has(
            EXPIRY_KEY,
            PersistentDataType.LONG
        )
    ) {
        return@on
    }


    // --------------------------------------------------------
    // CENTER BLOCK
    // --------------------------------------------------------

    val center =
        event.block


    // --------------------------------------------------------
    // DETERMINE MINING FACE
    // --------------------------------------------------------

    val targetBlock =
        player.getTargetBlockExact(6)

    val face =
        if (targetBlock != null) {
            targetBlock.getFace(center) ?: BlockFace.UP
        } else {
            BlockFace.UP
        }


    // --------------------------------------------------------
    // CREATE 3x3 BLOCK LIST
    // --------------------------------------------------------

    val blocks =
        mutableListOf<Block>()

    for (x in -1..1) {
        for (y in -1..1) {
            for (z in -1..1) {

                val target: Block =
                    when (face) {

                        BlockFace.UP,
                        BlockFace.DOWN -> {
                            center.getRelative(x, 0, z)
                        }

                        BlockFace.NORTH,
                        BlockFace.SOUTH -> {
                            center.getRelative(x, y, 0)
                        }

                        BlockFace.EAST,
                        BlockFace.WEST -> {
                            center.getRelative(0, y, z)
                        }

                        else -> {
                            center.getRelative(x, y, z)
                        }
                    }

                blocks.add(target)
            }
        }
    }


    // --------------------------------------------------------
    // CANCEL NORMAL BREAK
    // --------------------------------------------------------

    event.isCancelled = true


    // --------------------------------------------------------
    // BREAK 3x3
    // --------------------------------------------------------

    for (block in blocks) {

        if (block.type == Material.AIR) {
            continue
        }

        if (block.type == Material.BEDROCK) {
            continue
        }

        if (
            block.type == Material.CHEST ||
            block.type == Material.TRAPPED_CHEST ||
            block.type == Material.BARREL ||
            block.type == Material.SHULKER_BOX
        ) {
            continue
        }

        val blockMaterial =
            block.type

        val drops =
            block.getDrops(
                tool,
                player
            )

        for (drop in drops) {

            var finalDrop =
                drop.clone()

            var directPickup =
                false

            when (blockMaterial) {

                Material.IRON_ORE,
                Material.DEEPSLATE_IRON_ORE -> {

                    finalDrop =
                        ItemStack(
                            Material.IRON_INGOT,
                            drop.amount
                        )

                    directPickup = true
                }

                Material.GOLD_ORE,
                Material.DEEPSLATE_GOLD_ORE -> {

                    finalDrop =
                        ItemStack(
                            Material.GOLD_INGOT,
                            drop.amount
                        )

                    directPickup = true
                }

                Material.COPPER_ORE,
                Material.DEEPSLATE_COPPER_ORE -> {

                    finalDrop =
                        ItemStack(
                            Material.COPPER_INGOT,
                            drop.amount
                        )

                    directPickup = true
                }

                Material.NETHER_GOLD_ORE,
                Material.DIAMOND_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.COAL_ORE,
                Material.DEEPSLATE_COAL_ORE,
                Material.REDSTONE_ORE,
                Material.DEEPSLATE_REDSTONE_ORE,
                Material.LAPIS_ORE,
                Material.DEEPSLATE_LAPIS_ORE,
                Material.NETHER_QUARTZ_ORE -> {

                    directPickup = true
                }

                else -> {
                    directPickup = false
                }
            }

            if (directPickup) {

                val leftovers =
                    player.inventory.addItem(finalDrop)

                for (leftover in leftovers.values) {
                    block.world.dropItemNaturally(
                        block.location,
                        leftover
                    )
                }

            } else {

                block.world.dropItemNaturally(
                    block.location,
                    finalDrop
                )
            }
        }

        block.setType(
            Material.AIR,
            false
        )

        // One durability check for each block actually broken.
        if (damageRoyalPickaxe(player, tool)) {
            break
        }
    }
}


// ============================================================
// TIMER + EXPIRATION
// Runs every 1 second
//
// IMPORTANT:
// The currently held slot is NOT rewritten every second.
// This prevents repeated held-item refresh/equip animation.
// The lore of inventory pickaxes still updates every second.
// When the player switches to the pickaxe, its lore is refreshed once.
// ============================================================

onLoad {

    server.scheduler.runTaskTimer(
        delayTicks = 20L,
        periodTicks = 20L
    ) {

        for (player in Bukkit.getOnlinePlayers()) {

            val inventory =
                player.inventory

            val heldSlot =
                inventory.heldItemSlot

            for (slot in 0 until inventory.size) {

                // Never rewrite the currently held item every second.
                if (slot == heldSlot) {
                    continue
                }

                val item =
                    inventory.getItem(slot) ?: continue

                if (!isRoyalRarePickaxe(item)) {
                    continue
                }

                val meta =
                    item.itemMeta

                val expiry =
                    meta.persistentDataContainer.get(
                        EXPIRY_KEY,
                        PersistentDataType.LONG
                    )

                if (expiry == null) {
                    continue
                }

                if (
                    expiry <=
                    System.currentTimeMillis()
                ) {

                    inventory.setItem(
                        slot,
                        ItemStack(Material.AIR)
                    )

                    player.sendMessage(
                        Component.text(
                            "⌛ Your Royal SMP Rare Pickaxe has expired!",
                            NamedTextColor.RED
                        )
                    )

                } else {

                    updatePickaxeLore(item)
                }
            }
        }
    }
}
