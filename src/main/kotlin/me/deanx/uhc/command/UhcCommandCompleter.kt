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
    private val BOOL = listOf("true", "false")
    private val itemList = plugin.config.itemList.map { it.name }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        val hint = mutableListOf<String>()
        when (args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], COMMANDS, hint)
            2 -> {
                if (args[0].equals("config", true)) {
                    StringUtil.copyPartialMatches(args[1], plugin.config.CONFIGS, hint)
                }
            }
            3 -> {
                if (args[0].equals("config", true)) {
                    if (args[1].lowercase() in plugin.config.BOOLEAN_CONFIGS) {
                        StringUtil.copyPartialMatches(args[2], BOOL, hint)
                    } else if (args[1].lowercase() in plugin.config.ITEM_CONFIGS) {
                        StringUtil.copyPartialMatches(args[2], itemList, hint)
                    }
                }
            }
        }
        return hint
    }
}