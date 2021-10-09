package me.deanx.uhc.game

import me.deanx.uhc.Plugin
import org.bukkit.Bukkit
import org.bukkit.Location

class UhcGame(plugin: Plugin ,center: Location) {
    init {
        val world = center.world ?: Bukkit.getWorlds()[0]
        val worldBorder = world.worldBorder
        //todo teleport player
        worldBorder.center = center
        worldBorder.size = plugin.config.initBorderSize
        Bukkit.getScheduler().runTaskLater(plugin,
            Runnable { worldBorder.setSize(plugin.config.endBorderSize, plugin.config.timeToShrink) },
            plugin.config.timeBeforeShrink * 20)
    }

}