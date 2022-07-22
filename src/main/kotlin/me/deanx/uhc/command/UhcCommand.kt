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
            "config" -> changeConfig(args)
            else -> return false
        }
        ret?.let { sender.sendMessage(ret) }
        return true
    }

    private fun startGame(sender: CommandSender): String? {
        if (sender is Player) {
            val location = sender.location
            location.x = location.blockX + 0.5
            location.y = location.blockY + 0.5
            location.z = location.blockZ + 0.5
            if (!plugin.startGame(location)) {
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

    private fun changeConfig(args: Array<out String>): String {
        if (args.size <= 1) {
            return "The config name is needed."
        }
        if (!plugin.config.CONFIGS.contains(args[1])) {
            return "Cannot find the config " + args[1]
        }
        if (args.size > 2) {
            plugin.config.set(args[1], args[2])
        }
        return args[1] + " = " + plugin.config.get(args[1])
    }
}