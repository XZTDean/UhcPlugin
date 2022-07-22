package me.deanx.uhc.command

import me.deanx.uhc.Plugin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.util.StringUtil

class CenterCommandCompleter(private val plugin: Plugin) : TabCompleter {
    init {
        plugin.getCommand("center")!!.tabCompleter = this;
        plugin.getCommand("CenterDistance")!!.tabCompleter = this;
    }

    private val ARG_0 = listOf("AutoQuery")
    private val ARG_1 = listOf("on", "off")

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        val hint = mutableListOf<String>()
        when (args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], ARG_0, hint)
            2 -> {
                if (args[0].equals(ARG_0[0], true)) {
                    StringUtil.copyPartialMatches(args[1], ARG_1, hint)
                }
            }
        }
        return hint
    }
}