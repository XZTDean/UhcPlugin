package me.deanx.uhc

import org.bukkit.Difficulty
import org.bukkit.GameMode

class Config(private val plugin: Plugin) {
    init {
        plugin.saveDefaultConfig()
        plugin.getConfig().options().copyDefaults(true)
    }

    val CONFIGS: Set<String> = hashSetOf("gamemode", "difficulty", "initborder", "endborder", "timetoshrink", "timebeforeshrink")

    private val changedList = HashMap<String, Any>()

    var initBorderSize = plugin.getConfig().getDouble("border_size.start")
        set(value) {
            field = value
            changedList["border_size.start"] = value
        }

    var endBorderSize = plugin.getConfig().getDouble("border_size.end")
        set(value) {
            field = value
            changedList["border_size.end"] = value
        }

    var timeToShrink = plugin.getConfig().getLong("border_size.time_to_shrink")
        set(value) {
            field = value
            changedList["border_size.time_to_shrink"] = value
        }

    var timeBeforeShrink = plugin.getConfig().getLong("border_size.time_before_shrink")
        set(value) {
            field = value
            changedList["border_size.time_before_shrink"] = value
        }

    var gameMode: GameMode = getGameModeFromString(plugin.getConfig().getString("gamemode"))
        set(value) {
            field = value
            changedList["gamemode"] = getGameModeName(value)
        }

    var difficulty: Difficulty = getDifficultyFromString(plugin.getConfig().getString("difficulty"))
        set(value) {
            field = value
            changedList["difficulty"] = getDifficultyName(value)
        }

    fun saveConfig() {
        plugin.saveConfig()
    }

    private fun getGameModeFromString(name: String?): GameMode = when (name?.uppercase()) {
        "SURVIVAL" -> GameMode.SURVIVAL
        "CREATIVE" -> GameMode.CREATIVE
        "ADVENTURE" -> GameMode.ADVENTURE
        "SPECTATOR" -> GameMode.SPECTATOR
        else -> GameMode.SURVIVAL
    }

    private fun getGameModeName(gameMode: GameMode): String = when (gameMode) {
        GameMode.SURVIVAL -> "SURVIVAL"
        GameMode.CREATIVE -> "CREATIVE"
        GameMode.ADVENTURE -> "ADVENTURE"
        GameMode.SPECTATOR -> "SPECTATOR"
    }

    private fun getDifficultyFromString(name: String?): Difficulty = when (name?.uppercase()) {
        "HARD" -> Difficulty.HARD
        "NORMAL" -> Difficulty.NORMAL
        "EASY" -> Difficulty.EASY
        "PEACEFUL" -> Difficulty.PEACEFUL
        else -> Difficulty.HARD
    }

    private fun getDifficultyName(difficulty: Difficulty): String = when (difficulty) {
        Difficulty.HARD -> "HARD"
        Difficulty.NORMAL -> "NORMAL"
        Difficulty.EASY -> "EASY"
        Difficulty.PEACEFUL -> "PEACEFUL"
    }

    fun get(field: String): String {
        return when(field.lowercase()) {
            "gamemode" -> getGameModeName(gameMode)
            "difficulty" -> difficulty.name
            "initborder" -> initBorderSize.toString()
            "endborder" -> endBorderSize.toString()
            "timetoshrink" -> timeToShrink.toString()
            "timebeforeshrink" -> timeBeforeShrink.toString()
            else -> ""
        }
    }

    fun set(field: String, value: String): Boolean {
        when(field.lowercase()) {
            "gamemode" -> gameMode = getGameModeFromString(value)
            "difficulty" -> difficulty = getDifficultyFromString(value)
            "initborder" -> initBorderSize = value.toDouble()
            "endborder" -> endBorderSize = value.toDouble()
            "timetoshrink" -> timeToShrink = value.toLong()
            "timebeforeshrink" -> timeBeforeShrink = value.toLong()
            else -> return false
        }
        return true
    }
}