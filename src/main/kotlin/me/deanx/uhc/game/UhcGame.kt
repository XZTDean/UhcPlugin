package me.deanx.uhc.game

import me.deanx.uhc.Plugin
import me.deanx.uhc.listener.DeathListener
import me.deanx.uhc.listener.DisconnectionListener
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class UhcGame private constructor(private val plugin: Plugin, val center: Location) {
    private val initBorderSize = plugin.config.initBorderSize
    private val endBorderSize = plugin.config.endBorderSize
    private val survivals = Bukkit.getOnlinePlayers().toHashSet()
    private val deathListener = DeathListener(plugin, this)
    private val disconnectionListener = DisconnectionListener(plugin, this)
    private val worldBorder: WorldBorder
    private val permissionMap = HashMap<Player, PermissionAttachment>()

    init {
        val world = center.world!!
        worldBorder = world.worldBorder
        worldBorder.reset()
        teleportPlayersRandomly()
        setPlayerState()
        world.setWorldDifficulty(plugin.config.difficulty)
        world.time = 0
        world.setStorm(false)
        world.setGameRule(GameRule.NATURAL_REGENERATION, false)
        worldBorder.center = center
        worldBorder.size = initBorderSize
        Bukkit.getScheduler().runTaskLater(
            plugin,
            Runnable { worldBorder.setSize(endBorderSize, plugin.config.timeToShrink) },
            plugin.config.timeBeforeShrink * 20
        )
        displayTitleStart()
    }

    companion object {
        fun newGame(plugin: Plugin, center: Location): UhcGame? {
            if (Bukkit.getOnlinePlayers().size <= 1) {
                Bukkit.broadcastMessage("There is not enough player for UHC")
                return null
            }
            if (center.world == null) {
                Bukkit.broadcastMessage("Center Location is not allowed for UHC")
                return null
            }
            return UhcGame(plugin, center)
        }
    }

    fun gameEnd() {
        worldBorder.size = worldBorder.size
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { if (!plugin.hasGame()) worldBorder.reset() }, 200)
        deathListener.unregister()
        disconnectionListener.unregister()
        congratulationDisplay()
        val world = center.world!!
        world.setGameRule(GameRule.NATURAL_REGENERATION, true)
        permissionMap.forEach {
            it.value.remove()
            if (!it.key.isOp) {
                it.key.isOp = true
                it.key.isOp = false
            }
        }
        plugin.removeGame()
    }

    private fun displayTitleStart() {
        survivals.forEach { player ->
            player.sendTitle("UHC Start", null, 5, 30, 5)
        }
    }

    private fun congratulationDisplay () {
        survivals.forEach { player ->
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.sendTitle("You Win!", null, 10, 40, 10)
        }
    }

    private fun setPlayerState() {
        survivals.forEach { player ->
            player.gameMode = plugin.config.gameMode
            player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: 20.0
            player.foodLevel = 20
            player.saturation = 5f
            setPlayerInventory(player)
            player.removePotionEffect()
            player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 500, 5))
            val iterator = Bukkit.getServer().advancementIterator()
            while (iterator.hasNext()) {
                val progress = player.getAdvancementProgress(iterator.next())
                for (criteria in progress.awardedCriteria) progress.revokeCriteria(criteria!!)
            }
            if (plugin.config.enableCenterDistance) {
                val permissionAttachment = player.addAttachment(plugin, "uhc.center", true)
                permissionMap[player] = permissionAttachment
                if (!player.isOp) { // used to refresh tab auto-completer
                    player.isOp = true
                    player.isOp = false
                }
            }
        }
    }

    private fun setPlayerInventory(player: Player) {
        val inventory = player.inventory
        inventory.clear()
        inventory.helmet = plugin.config.helmat
        inventory.chestplate = plugin.config.chestplate
        inventory.leggings = plugin.config.leggings
        inventory.boots = plugin.config.boots
        plugin.config.inventory.forEach {
            inventory.addItem(it)
        }
    }

    private fun LivingEntity.removePotionEffect() {
        val potionEffectList: Collection<PotionEffect> = this.activePotionEffects
        for (potion in potionEffectList) {
            this.removePotionEffect(potion.type)
        }
    }

    fun playerDeath(player: Player) {
        survivals.remove(player)
        permissionMap[player]?.setPermission("uhc.center", false)
        if (!player.isOp) {
            player.isOp = true
            player.isOp = false
        }
        if (survivals.size > 1) {
            killReward(player)
            Bukkit.broadcastMessage(survivals.size.toString() + " players are remaining.")
        } else {
            gameEnd()
        }
    }

    private fun killReward(player: Player) {
        player.killer?.let { killer ->
            plugin.config.killReward?.let { item -> killer.inventory.addItem(item) }
        }
    }

    fun isSurvival(player: Player): Boolean {
        return survivals.contains(player)
    }

    fun teleportPlayer(player: Player) {
        val location = findSafeLocation(center, worldBorder.size.toInt() / 2)
        player.teleport(location)
    }

    private fun teleportPlayersRandomly() {
        val map = selectPlayerStartLocation()
        for ((player, location) in map) {
            player.teleport(location)
        }
    }

    private fun selectPlayerStartLocation(): Map<Player, Location> {
        val playerList = survivals
        val locationSet = HashSet<Location>()
        var maxIter = playerList.size * 4
        while (locationSet.size < playerList.size && maxIter > 0) {
            val location = findSafeLocation(center, plugin.config.initBorderSize.toInt() / 2)
            locationSet.add(location)
            --maxIter
        }
        if (locationSet.size == playerList.size) {
            return playerList.zip(locationSet.shuffled()).toMap()
        } else {
            val locationMap = HashMap<Player, Location>()
            for (player in playerList) {
                locationMap[player] = locationSet.random()
            }
            return locationMap
        }
    }

    private fun findSafeLocation(center: Location, radius: Int): Location {
        var location: Location
        do {
            val x = center.blockX + Random.nextInt(-radius, radius)
            val z = center.blockZ + Random.nextInt(-radius, radius)
            location = center.world!!.getHighestBlockAt(x, z).location
            location.y += 1
            location.x += 0.5
            location.z += 0.5
        } while (!isSafeLocation(location))
        return location
    }

    private fun isSafeLocation(location: Location): Boolean {
        val safeBlock = listOf(Material.AIR, Material.SNOW, Material.GRASS, Material.TALL_GRASS, Material.FERN,
            Material.LARGE_FERN, Material.LILY_PAD, Material.TORCH, Material.SUGAR_CANE, Material.POPPY,
            Material.DANDELION, Material.ROSE_BUSH, Material.LILAC, Material.PEONY, Material.SUNFLOWER,
            Material.BLUE_ORCHID, Material.ALLIUM, Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP,
            Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY)
        val unsafeBlock = listOf(Material.WATER, Material.LAVA, Material.MAGMA_BLOCK, Material.CACTUS, Material.CAMPFIRE)
        val upCheckLocation = location.clone()
        for (i in 0..2) {
            if (upCheckLocation.block.type !in safeBlock) {
                return false
            }
            upCheckLocation.y += 1
        }
        val downCheckLocation = location.clone()
        for (i in 0..2) {
            downCheckLocation.y -= 1
            val type: Material = downCheckLocation.block.type
            if (type != Material.AIR) {
                return type !in unsafeBlock
            }
        }
        return false
    }

    private fun World.setWorldDifficulty(difficulty: Difficulty) {
        this.difficulty = difficulty
        val allowMonster = (difficulty != Difficulty.PEACEFUL)
        this.setSpawnFlags(allowMonster, allowAnimals)
    }
}