package me.deanx.uhc

import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Config(private val plugin: Plugin) {
    val CONFIGS: Set<String> = hashSetOf("gamemode", "difficulty", "initborder", "endborder", "timetoshrink",
        "timebeforeshrink","centerdistancedelay", "enablecenterdistance", "allowautoquerycenterdistance",
        "enableautoquerycenterdistance", "killreward")

    val BOOLEAN_CONFIGS = hashSetOf("enablecenterdistance", "allowautoquerycenterdistance", "enableautoquerycenterdistance")

    val ITEM_CONFIGS = hashSetOf("killreward")

    private val changedList = HashMap<String, Any?>()

    val itemList = mutableListOf<Material>()

    init {
        plugin.saveDefaultConfig()
        plugin.getConfig().options().copyDefaults(true)
        generateItemList()
    }

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

    var enableCenterDistance: Boolean = plugin.getConfig().getBoolean("center_distance.enable")
        set(value) {
            field = value
            changedList["center_distance.enable"] = value
        }

    var centerDistanceDelay: Long = plugin.getConfig().getLong("center_distance.delay")
        set(value) {
            field = value
            changedList["center_distance.delay"] = value
        }

    var allowAutoQueryCenterDistance: Boolean = plugin.getConfig().getBoolean("center_distance.allow_auto_query")
        set(value) {
            field = value
            changedList["center_distance.allow_auto_query"] = value
        }

    var enableAutoQueryCenterDistance: Boolean = plugin.getConfig().getBoolean("center_distance.enable_auto_query")
        set(value) {
            field = value
            changedList["center_distance.enable_auto_query"] = value
        }

    var killReward: ItemStack? = getItemStackFromString(plugin.getConfig().getString("kill_reward"))
        set(value) {
            field = value
            changedList["kill_reward"] = itemStackInfo(value)
        }

    fun saveConfig() {
        changedList.forEach { entry ->
            plugin.getConfig().set(entry.key, entry.value)
        }
        changedList.clear()
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
    
    private fun getItemStackFromString(itemInfo: String?): ItemStack? {
        if (itemInfo.isNullOrBlank()) {
            return null
        }
        return getItemStackFromList(itemInfo.split(" "))
    }

    private fun getItemStackFromList(itemInfo: List<String>): ItemStack? {
        if (itemInfo.isEmpty()) {
            return null
        }
        val material = Material.getMaterial(itemInfo[0].uppercase())
        var amount = if (itemInfo.size >= 2) itemInfo[1].toIntOrNull() else 1
        if (amount == null || amount < 1) {
            amount = 1
        }
        return material?.let { ItemStack(it, amount) }
    }

    private fun itemStackInfo(itemStack: ItemStack?): String? {
        var ret = itemStack?.type?.name
        if (ret != null) {
            ret += " " + itemStack!!.amount.toString()
        }
        return ret
    }

    private fun generateItemList() {
        Material.values().forEach { material ->
            if (material.isItem && !material.name.startsWith("LEGACY")) {
                itemList.add(material)
            }
        }
    }

    fun get(field: String): String {
        return when(field) {
            "gamemode" -> getGameModeName(gameMode)
            "difficulty" -> difficulty.name
            "initborder" -> initBorderSize.toString()
            "endborder" -> endBorderSize.toString()
            "timetoshrink" -> timeToShrink.toString()
            "timebeforeshrink" -> timeBeforeShrink.toString()
            "centerdistancedelay" -> centerDistanceDelay.toString()
            "enablecenterdistance" -> enableCenterDistance.toString()
            "allowautoquerycenterdistance" -> allowAutoQueryCenterDistance.toString()
            "enableautoquerycenterdistance" -> enableAutoQueryCenterDistance.toString()
            "killreward" -> itemStackInfo(killReward).orEmpty()
            else -> ""
        }
    }

    fun set(field: String, values: List<String>): Boolean {
        when(field) {
            "gamemode" -> gameMode = getGameModeFromString(values[0])
            "difficulty" -> difficulty = getDifficultyFromString(values[0])
            "initborder" -> initBorderSize = values[0].toDouble()
            "endborder" -> endBorderSize = values[0].toDouble()
            "timetoshrink" -> timeToShrink = values[0].toLong()
            "timebeforeshrink" -> timeBeforeShrink = values[0].toLong()
            "centerdistancedelay" -> centerDistanceDelay = values[0].toLong()
            "enablecenterdistance" -> enableCenterDistance = values[0].lowercase().toBooleanStrict()
            "allowautoquerycenterdistance" -> allowAutoQueryCenterDistance = values[0].lowercase().toBooleanStrict()
            "enableautoquerycenterdistance" -> enableAutoQueryCenterDistance = values[0].lowercase().toBooleanStrict()
            "killreward" -> killReward = getItemStackFromList(values)
            else -> return false
        }
        return true
    }
}