package me.deanx.uhc.listener

import me.deanx.uhc.Plugin
import me.deanx.uhc.game.UhcGame
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.permissions.PermissionAttachment

class DeathListener(private val plugin: Plugin, private val uhcGame: UhcGame) : Listener {
    private val permissionMap = HashMap<Player, PermissionAttachment>()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
        permissionMap.values.forEach { it.remove() }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        uhcGame.playerDeath(event.entity)
    }

    @EventHandler
    fun onRespawn(event: PlayerRespawnEvent) {
        val player = event.player
        if (!uhcGame.isSurvival(player)) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable { player.gameMode = GameMode.SPECTATOR }, 1)
            player.teleport(uhcGame.center.add(0.0, 10.0, 0.0))
            if (player !in permissionMap) {
                val permission = player.addAttachment(plugin, "minecraft.command.teleport", true)
                permissionMap[player] = permission
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable { player.gameMode = plugin.config.gameMode }, 1)
            permissionMap.remove(player)?.remove()
            uhcGame.teleportPlayer(player)
        }
    }
}