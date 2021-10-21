package me.deanx.uhc.game

import me.deanx.uhc.Plugin
import me.deanx.uhc.listener.DeathListener
import me.deanx.uhc.listener.DisconnectionListener
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import kotlin.random.Random

class UhcGame(private val plugin: Plugin, val center: Location) {
    private val initBorderSize = plugin.config.initBorderSize
    private val endBorderSize = plugin.config.endBorderSize
    private val survivals = Bukkit.getOnlinePlayers().toHashSet()
    private val deathListener = DeathListener(plugin, this)
    private val disconnectionListener = DisconnectionListener(plugin, this)
    private val worldBorder: WorldBorder

    init {
        val world = center.world ?: Bukkit.getWorlds()[0]
        worldBorder = world.worldBorder
        if (survivals.size <= 1) {
            deathListener.unregister()
            disconnectionListener.unregister()
            plugin.removeGame()
            Bukkit.broadcastMessage("There is not enough player for UHC")
        } else {
            worldBorder.reset()
            teleportPlayersRandomly()
            setPlayerState()
            world.difficulty = plugin.config.difficulty
            world.time = 0
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
    }

    fun gameEnd() {
        worldBorder.size = worldBorder.size
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { worldBorder.reset() }, 200)
        deathListener.unregister()
        disconnectionListener.unregister()
        congratulationDisplay()
        val world = center.world ?: Bukkit.getWorlds()[0]
        world.setGameRule(GameRule.NATURAL_REGENERATION, true)
        plugin.removeGame()
    }

    private fun displayTitleStart() {
        for (player in survivals) {
            player.sendTitle("UHC Start", null, 5, 30, 5)
        }
    }

    private fun congratulationDisplay () {
        for (player in survivals) {
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.sendTitle("You Win!", null, 10, 40, 10)
        }
    }

    private fun setPlayerState() {
        for (player in survivals) {
            player.gameMode = plugin.config.gameMode
            player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue ?: 20.0
            player.foodLevel = 20
            player.saturation = 5f
            player.inventory.clear()
        }
    }

    fun playerDeath(player: Player) {
        survivals.remove(player)
        if (survivals.size > 1) {
            Bukkit.broadcastMessage(survivals.size.toString() + " players are remaining.")
        } else {
            gameEnd()
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
        if (center.world == null) {
            return center
        }
        var location: Location
        do {
            val x = center.blockX + Random.nextInt(-radius, radius)
            val z = center.blockZ + Random.nextInt(-radius, radius)
            location = center.world!!.getHighestBlockAt(x, z).location
            location.y += 1
        } while (!isSafeLocation(location))
        return location
    }

    private fun isSafeLocation(location: Location): Boolean {
        val upCheckLocation = location.clone()
        for (i in 0..2) {
            if (upCheckLocation.block.type != Material.AIR) {
                return false
            }
            upCheckLocation.y += 1
        }
        val downCheckLocation = location.clone()
        for (i in 0..2) {
            downCheckLocation.y -= 1
            val type: Material = downCheckLocation.block.type
            if (type != Material.AIR) {
                return type != Material.WATER && type != Material.LAVA
            }
        }
        return false
    }

}