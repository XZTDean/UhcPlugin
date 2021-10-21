package me.deanx.uhc.command

import me.deanx.uhc.Plugin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.util.StringUtil

class UhcCommandCompleter(private val plugin: Plugin) : TabCompleter {
    init {
        plugin.getCommand("uhc")!!.tabCompleter = this;
    }

    private val COMMANDS = listOf<String>("start", "stop", "config")

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        val hint = mutableListOf<String>()
        when (args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], COMMANDS, hint)
            2 -> {
                if (args[0].equals("config", true)) {
                    StringUtil.copyPartialMatches(args[1], plugin.config.CONFIGS, hint)
                }
            }
        }
        return hint
    }
}