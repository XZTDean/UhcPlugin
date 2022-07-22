package me.deanx.uhc.command

import me.deanx.uhc.Plugin
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

class CenterCommand(private val plugin: Plugin) : CommandExecutor {
    init {
        plugin.getCommand("center")!!.setExecutor(this)
        plugin.getCommand("CenterDistance")!!.setExecutor(this)
    }

    private val lastExecutionTime = HashMap<Player, Long>()
    private var center: Location? = null
    private var enabled: Boolean = false
    private var allowAutoQuery: Boolean = false
    private var delay = plugin.config.centerDistanceDelay
    private val autoQueryPlayer = HashMap<Player, BukkitTask>()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!enabled || !sender.hasPermission("uhc.center") || sender !is Player) {
            sender.sendMessage("You do not have permission to use this command.")
            return false
        }
        if (args.isNotEmpty()) {
            if (!args[0].equals("AutoQuery", true) || args.size != 2) {
                sender.sendMessage("Incorrect Argument.")
                return false
            }
            return changeAutoQuery(sender, args[1])
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
        getCenterDistance(sender, currentTime)
        return true
    }

    private fun getCenterDistance(player: Player, currentTime: Long) {
        val distance = player.location.distance(center!!)
        player.sendMessage("The distance to center is %.2f".format(distance))
        lastExecutionTime[player] = currentTime
    }

    private fun autoQueryCenterDistance(player: Player) {
        if (player.hasPermission("uhc.center")) {
            getCenterDistance(player, center!!.world!!.fullTime)
            val autoQueryTask = Bukkit.getScheduler().runTaskLater(plugin, Runnable { autoQueryCenterDistance(player) }, delay)
            autoQueryPlayer[player] = autoQueryTask
        } else {
            autoQueryPlayer.remove(player)
        }
    }

    fun startGame(center: Location) {
        if (plugin.config.enableCenterDistance) {
            this.center = center
            enabled = true
            allowAutoQuery = plugin.config.allowAutoQueryCenterDistance
            delay = plugin.config.centerDistanceDelay
            if (allowAutoQuery && plugin.config.enableAutoQueryCenterDistance) {
                Bukkit.getOnlinePlayers().forEach { it.enableAutoQuery() }
            }
        }
    }

    fun endGame() {
        enabled = false
        center = null
        autoQueryPlayer.forEach { it.value.cancel() }
        autoQueryPlayer.clear()
        lastExecutionTime.clear()
    }

    private fun changeAutoQuery(player: Player, arg: String): Boolean {
        if (arg.equals("on", true)) {
            player.enableAutoQuery()
        } else if (arg.equals("off", true)) {
            player.disableAutoQuery()
        } else {
            player.sendMessage("Incorrect Argument.")
            return false
        }
        return true
    }

    private fun Player.enableAutoQuery() {
        val currentTime = center!!.world!!.fullTime
        lastExecutionTime[this]?.let {
            val timeFromLastQuery = currentTime - it
            if (timeFromLastQuery < delay) {
                val waitingTime = delay - timeFromLastQuery
                val autoQueryTask = Bukkit.getScheduler()
                    .runTaskLater(plugin, Runnable { autoQueryCenterDistance(this) }, waitingTime)
                autoQueryPlayer[this] = autoQueryTask
                return
            }
        }
        autoQueryCenterDistance(this)
    }

    private fun Player.disableAutoQuery() {
        autoQueryPlayer.remove(this)?.cancel()
    }
}