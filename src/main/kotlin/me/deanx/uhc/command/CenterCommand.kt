package me.deanx.uhc.command

import me.deanx.uhc.Plugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CenterCommand(private val plugin: Plugin) : CommandExecutor {
    init {
        plugin.getCommand("center")!!.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("uhc.center")) {
            sender.sendMessage("You do not have permission to use this command.")
            return false
        }
        // todo: implement
        return true
    }
}