package me.deanx.uhc.command

import me.deanx.uhc.Plugin
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UhcCommand(private val plugin: Plugin) : CommandExecutor {
    init {
        plugin.getCommand("uhc")!!.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }
        val ret: String? = when (args[0].lowercase()) {
            "start" -> startGame(sender)
            "stop" -> stopGame()
            else -> return false
        }
        ret?.let { sender.sendMessage(ret) }
        return true
    }

    private fun startGame(sender: CommandSender): String? {
        if (sender is Player) {
            if (!plugin.startGame(sender.location)) {
                return "Cannot start, Game is running now."
            }
        } else {
            if (!plugin.startGame(Bukkit.getWorlds()[0].spawnLocation)) {
                return "Cannot start, Game is running now."
            }
        }
        return null
    }

    private fun stopGame(): String? {
        if (!plugin.stopGame()) {
            return "No exist game."
        }
        return null
    }
}