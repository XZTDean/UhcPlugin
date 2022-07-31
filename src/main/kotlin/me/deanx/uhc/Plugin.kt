package me.deanx.uhc

import me.deanx.uhc.command.CenterCommand
import me.deanx.uhc.command.CenterCommandCompleter
import me.deanx.uhc.command.UhcCommand
import me.deanx.uhc.command.UhcCommandCompleter
import me.deanx.uhc.game.UhcGame
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin

class Plugin : JavaPlugin() {
    val config = Config(this)
    private var uhcGame: UhcGame? = null
    private lateinit var centerCommand: CenterCommand

    override fun onEnable() {
        UhcCommand(this)
        UhcCommandCompleter(this)
        centerCommand = CenterCommand(this)
        CenterCommandCompleter(this)
    }

    override fun onDisable() {
        config.saveConfig()
    }

    fun startGame(center: Location): Boolean {
        if (hasGame()) {
            return false
        }
        uhcGame = UhcGame.newGame(this, center)
        if (uhcGame != null) {
            centerCommand.startGame(center)
        }
        return true
    }

    fun stopGame(): Boolean {
        if (!hasGame()) {
            return false
        }
        uhcGame!!.gameEnd()
        return true
    }

    fun removeGame() {
        uhcGame = null
        centerCommand.endGame()
    }

    fun hasGame() : Boolean {
        return uhcGame != null
    }
}