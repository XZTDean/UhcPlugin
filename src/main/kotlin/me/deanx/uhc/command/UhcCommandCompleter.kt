package me.deanx.uhc.command

import me.deanx.uhc.Config.Configs
import me.deanx.uhc.Plugin
import org.bukkit.Difficulty
import org.bukkit.GameMode
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
    private val itemList = plugin.config.itemSet.map { it.name.lowercase() }

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
                    } else if (args[1].lowercase() == Configs.Inventory.key) {
                        StringUtil.copyPartialMatches(args[2], listOf("add", "remove", "clear"), hint)
                    } else if (args[1].lowercase() == Configs.Difficulty.key) {
                        StringUtil.copyPartialMatches(args[2], Difficulty.entries.map { it.name.lowercase() }, hint)
                    } else if (args[1].lowercase() == Configs.Gamemode.key) {
                        StringUtil.copyPartialMatches(args[2], GameMode.entries.map { it.name.lowercase() }, hint)
                    }
                }
            }
            4 -> {
                if (args[1].lowercase() == Configs.Inventory.key && args[2].lowercase() in arrayOf("add", "remove")) {
                    StringUtil.copyPartialMatches(args[3], itemList, hint)
                }
            }
        }
        return hint
    }
}