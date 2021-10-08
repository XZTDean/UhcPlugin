package me.deanx.uhc.command

import me.deanx.uhc.Plugin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class UhcCommand(private val plugin: Plugin) : CommandExecutor {
    init {
        plugin.getCommand("uhc")!!.setExecutor(this)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }
        val ret: String? = when (args[0].lowercase()) {
            "start" -> startGame()
            else -> null
        }
        ret?.let { sender.sendMessage(ret) }
        return true
    }

    private fun startGame(): String {
        TODO("Not yet implemented")
    }
}