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
            "config" -> changeConfig(args.drop(1))
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

    private fun changeConfig(args: List<String>): String {
        if (args.isEmpty()) {
            return "The config name is needed."
        }
        val configName = args[0].lowercase()
        if (!plugin.config.CONFIGS.contains(configName)) {
            return "Cannot find the config " + args[0]
        }
        if (args.size > 1) {
            plugin.config.set(configName, args.subList(1, args.size))
        }
        return args[0] + " = " + plugin.config.get(configName)
    }
}