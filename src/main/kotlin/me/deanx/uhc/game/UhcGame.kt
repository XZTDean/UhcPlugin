package me.deanx.uhc.game

import me.deanx.uhc.Plugin
import me.deanx.uhc.listener.DeathListener
import me.deanx.uhc.listener.DisconnectionListener
import org.bukkit.*
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
        if (survivals.size <= 1) {
            survivals.clear()
            gameEnd()
            Bukkit.broadcastMessage("There is not enough player for UHC")
        }

        val world = center.world ?: Bukkit.getWorlds()[0]
        worldBorder = world.worldBorder
        teleportPlayersRandomly()
        setPlayerMode()
        world.difficulty = plugin.config.difficulty
        world.time = 0
        worldBorder.center = center
        worldBorder.size = initBorderSize
        Bukkit.getScheduler().runTaskLater(plugin,
            Runnable { worldBorder.setSize(endBorderSize, plugin.config.timeToShrink) },
            plugin.config.timeBeforeShrink * 20)
        displayTitleStart()
    }

    fun gameEnd() {
        worldBorder.size = worldBorder.size
        Bukkit.getScheduler().runTaskLater(plugin, Runnable { worldBorder.reset() }, 200)
        deathListener.unregister()
        disconnectionListener.unregister()
        congrulationDisplay()
        plugin.removeGame()
    }

    private fun displayTitleStart() {
        for (player in survivals) {
            player.sendTitle("UHC Start", null, 5, 30, 5)
        }
    }

    private fun congrulationDisplay () {
        for (player in survivals) {
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)
            player.sendTitle("You Win!", null, 10, 40, 10)
        }
    }

    private fun setPlayerMode() {
        for (player in survivals) {
            player.gameMode = plugin.config.gameMode
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
        val location = findSafeLocation(center, worldBorder.size.toInt())
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
            val location = findSafeLocation(center, plugin.config.initBorderSize.toInt())
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
        for (i in 0..2) {
            val checkLocation = location.add(0.0, i.toDouble(), 0.0)
            if (checkLocation.block.type != Material.AIR) {
                return false
            }
        }
        location.y -= 1
        for (i in 0..2) {
            val type: Material = location.block.type
            if (type != Material.AIR) {
                return type != Material.WATER && type != Material.LAVA
            }
        }
        return false
    }

}