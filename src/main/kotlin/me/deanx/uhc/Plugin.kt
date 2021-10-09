package me.deanx.uhc

import me.deanx.uhc.command.UhcCommand
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {
    val config = Config(this)

    override fun onEnable() {
        UhcCommand(this)
    }

    override fun onDisable() {
        config.saveConfig()
    }
}