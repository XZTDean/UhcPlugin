package me.deanx.uhc

import me.deanx.uhc.command.UhcCommand
import me.deanx.uhc.command.UhcCommandCompleter
import me.deanx.uhc.game.UhcGame
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {
    val config = Config(this)
    private var uhcGame: UhcGame? = null

    override fun onEnable() {
        UhcCommand(this)
        UhcCommandCompleter(this)
    }

    override fun onDisable() {
        config.saveConfig()
    }

    fun startGame(center: Location): Boolean {
        if (uhcGame != null) {
            return false
        }
        uhcGame = UhcGame(this, center)
        return true
    }

    fun stopGame(): Boolean {
        if (uhcGame == null) {
            return false
        }
        uhcGame!!.gameEnd()
        return true
    }

    fun removeGame() {
        uhcGame = null
    }
}