package me.deanx.uhc.listener

import me.deanx.uhc.Plugin
import me.deanx.uhc.game.UhcGame
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask

class DisconnectionListener(private val plugin: Plugin, private val uhcGame: UhcGame) : Listener {
    private val playerMap = HashMap<Player, BukkitTask>()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        val player = event.player
        if (uhcGame.isSurvival(player)) {
            val task = Bukkit.getScheduler().runTaskLater(plugin, Runnable { uhcGame.playerDeath(player) }, 1200)
            playerMap[player] = task
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        if (playerMap.containsKey(player)) {
            playerMap[player]!!.cancel()
            playerMap.remove(player)
        }
        if (!uhcGame.isSurvival(player)) {
            player.gameMode = GameMode.SPECTATOR
            player.teleport(uhcGame.center.add(0.0, 10.0, 0.0))
        }
    }
}