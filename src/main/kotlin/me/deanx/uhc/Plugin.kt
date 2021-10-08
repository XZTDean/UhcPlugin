package me.deanx.uhc

import me.deanx.uhc.command.UhcCommand
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {
    override fun onEnable() {
        UhcCommand(this)
    }

}