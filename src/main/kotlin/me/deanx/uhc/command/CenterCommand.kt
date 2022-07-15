package me.deanx.uhc.command

import me.deanx.uhc.Plugin
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CenterCommand(private val plugin: Plugin) : CommandExecutor {
    init {
        plugin.getCommand("center")!!.setExecutor(this)
    }

    private val lastExecutionTime = HashMap<Player, Long>()
    private var center: Location? = null

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("uhc.center") || sender !is Player) {
            sender.sendMessage("You do not have permission to use this command.")
            return false
        }
        if (center == null) {
            sender.sendMessage("Game is not running.")
            return false
        }
        val currentTime = center!!.world!!.fullTime
        lastExecutionTime[sender]?.let {
            if (currentTime - it < 600) {
                sender.sendMessage("You cannot query the distance to center now")
                return true
            }
        }
        val distance = sender.location.distance(center!!)
        sender.sendMessage("The distance to center is %.2f".format(distance))
        lastExecutionTime[sender] = currentTime
        return true
    }

    fun startGame(center: Location) {
        this.center = center
    }

    fun endGame() {
        center = null
        lastExecutionTime.clear()
    }
}