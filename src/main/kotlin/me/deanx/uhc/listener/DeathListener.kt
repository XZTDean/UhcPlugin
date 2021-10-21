package me.deanx.uhc.listener

import me.deanx.uhc.Plugin
import me.deanx.uhc.game.UhcGame
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent

class DeathListener(private val plugin: Plugin, private val uhcGame: UhcGame) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        uhcGame.playerDeath(event.entity)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        if (!uhcGame.isSurvival(player)) {
            player.gameMode = GameMode.SPECTATOR
            player.teleport(uhcGame.center.add(0.0, 10.0, 0.0))
        } else {
            player.gameMode = plugin.config.gameMode
            uhcGame.teleportPlayer(player)
        }
    }
}