package me.deanx.uhc.command

import me.deanx.uhc.Plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment

class CenterCommand(private val plugin: Plugin) : CommandExecutor {
    init {
        plugin.getCommand("center")!!.setExecutor(this)
    }

    private val lastExecutionTime = HashMap<Player, Long>()
    private var center: Location? = null
    private var enabled: Boolean = false
    private val permissionList = mutableListOf<PermissionAttachment>()
    private var delay = plugin.config.centerDistanceDelay

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!enabled || !sender.hasPermission("uhc.center") || sender !is Player) {
            sender.sendMessage("You do not have permission to use this command.")
            return false
        }
        val currentTime = center!!.world!!.fullTime
        lastExecutionTime[sender]?.let {
            val timeFromLastQuery = currentTime - it
            if (timeFromLastQuery < delay) {
                val waitingTime = delay / 20 - timeFromLastQuery / 20
                sender.sendMessage("You cannot query the distance to center now, please wait ${waitingTime}s")
                return true
            }
        }
        val distance = sender.location.distance(center!!)
        sender.sendMessage("The distance to center is %.2f".format(distance))
        lastExecutionTime[sender] = currentTime
        return true
    }

    fun startGame(center: Location) {
        if (plugin.config.enableCenterDistance) {
            this.center = center
            enabled = true
            delay = plugin.config.centerDistanceDelay
            Bukkit.getOnlinePlayers().forEach { permissionList.add(it.addAttachment(plugin, "uhc.center", true)) }
        }
    }

    fun endGame() {
        enabled = false
        center = null
        permissionList.forEach { it.remove() }
        permissionList.clear()
        lastExecutionTime.clear()
    }
}