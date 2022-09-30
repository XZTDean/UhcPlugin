package me.deanx.uhc

import org.bukkit.Difficulty
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Config(private val plugin: Plugin) {
    enum class Configs(val key: String, val path: String) {
        Gamemode("gamemode", "gamemode"),
        Difficulty("difficulty", "difficulty"),
        InitBorder("initborder", "border_size.start"),
        EndBorder("endborder", "border_size.end"),
        TimeToShrink("timetoshrink", "border_size.time_to_shrink"),
        TimeBeforeShrink("timebeforeshrink", "border_size.time_before_shrink"),
        CenterDistanceDelay("centerdistancedelay", "center_distance.delay"),
        EnableCenterDistance("enablecenterdistance", "center_distance.enable"),
        AllowAutoQueryCenterDistance("allowautoquerycenterdistance", "center_distance.allow_auto_query"),
        EnableAutoQueryCenterDistance("enableautoquerycenterdistance", "center_distance.enable_auto_query"),
        KillReward("killreward", "kill_reward"),
    }

    val CONFIGS: Set<String> = Configs.values().map { it.key }.toHashSet()

    val BOOLEAN_CONFIGS = hashSetOf(Configs.EnableCenterDistance.key, Configs.AllowAutoQueryCenterDistance.key, Configs.EnableAutoQueryCenterDistance.key)

    val ITEM_CONFIGS = hashSetOf(Configs.KillReward.key)

    private val changedList = HashMap<Configs, Any?>()

    val itemSet = HashSet<Material>()

    init {
        plugin.saveDefaultConfig()
        plugin.getConfig().options().copyDefaults(true)
        generateItemList()
    }

    var initBorderSize = plugin.getConfig().getDouble(Configs.InitBorder.path)
        set(value) {
            field = value
            changedList[Configs.InitBorder] = value
        }

    var endBorderSize = plugin.getConfig().getDouble(Configs.EndBorder.path)
        set(value) {
            field = value
            changedList[Configs.EndBorder] = value
        }

    var timeToShrink = plugin.getConfig().getLong(Configs.TimeToShrink.path)
        set(value) {
            field = value
            changedList[Configs.TimeToShrink] = value
        }

    var timeBeforeShrink = plugin.getConfig().getLong(Configs.TimeBeforeShrink.path)
        set(value) {
            field = value
            changedList[Configs.TimeBeforeShrink] = value
        }

    var gameMode: GameMode = getGameModeFromString(plugin.getConfig().getString(Configs.Gamemode.path))
        set(value) {
            field = value
            changedList[Configs.Gamemode] = getGameModeName(value)
        }

    var difficulty: Difficulty = getDifficultyFromString(plugin.getConfig().getString(Configs.Difficulty.path))
        set(value) {
            field = value
            changedList[Configs.Difficulty] = getDifficultyName(value)
        }

    var enableCenterDistance: Boolean = plugin.getConfig().getBoolean(Configs.EnableCenterDistance.path)
        set(value) {
            field = value
            changedList[Configs.EnableCenterDistance] = value
        }

    var centerDistanceDelay: Long = plugin.getConfig().getLong(Configs.CenterDistanceDelay.path)
        set(value) {
            field = value
            changedList[Configs.CenterDistanceDelay] = value
        }

    var allowAutoQueryCenterDistance: Boolean = plugin.getConfig().getBoolean(Configs.AllowAutoQueryCenterDistance.path)
        set(value) {
            field = value
            changedList[Configs.AllowAutoQueryCenterDistance] = value
        }

    var enableAutoQueryCenterDistance: Boolean = plugin.getConfig().getBoolean(Configs.EnableAutoQueryCenterDistance.path)
        set(value) {
            field = value
            changedList[Configs.EnableAutoQueryCenterDistance] = value
        }

    var killReward: ItemStack? = getItemStackFromString(plugin.getConfig().getString(Configs.KillReward.path))
        set(value) {
            field = value
            changedList[Configs.KillReward] = itemStackInfo(value)
        }

    fun saveConfig() {
        changedList.forEach { entry ->
            plugin.getConfig().set(entry.key.path, entry.value)
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
        if (material !in itemSet) {
            return null
        }
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
                itemSet.add(material)
            }
        }
    }

    fun get(field: String): String {
        return when(field) {
            Configs.Gamemode.key -> getGameModeName(gameMode)
            Configs.Difficulty.key -> difficulty.name
            Configs.InitBorder.key -> initBorderSize.toString()
            Configs.EndBorder.key -> endBorderSize.toString()
            Configs.TimeToShrink.key -> timeToShrink.toString()
            Configs.TimeBeforeShrink.key -> timeBeforeShrink.toString()
            Configs.CenterDistanceDelay.key -> centerDistanceDelay.toString()
            Configs.EnableCenterDistance.key -> enableCenterDistance.toString()
            Configs.AllowAutoQueryCenterDistance.key -> allowAutoQueryCenterDistance.toString()
            Configs.EnableAutoQueryCenterDistance.key -> enableAutoQueryCenterDistance.toString()
            Configs.KillReward.key -> itemStackInfo(killReward).orEmpty()
            else -> ""
        }
    }

    fun set(field: String, values: List<String>): Boolean {
        when(field) {
            Configs.Gamemode.key -> gameMode = getGameModeFromString(values[0])
            Configs.Difficulty.key -> difficulty = getDifficultyFromString(values[0])
            Configs.InitBorder.key -> initBorderSize = values[0].toDouble()
            Configs.EndBorder.key -> endBorderSize = values[0].toDouble()
            Configs.TimeToShrink.key -> timeToShrink = values[0].toLong()
            Configs.TimeBeforeShrink.key -> timeBeforeShrink = values[0].toLong()
            Configs.CenterDistanceDelay.key -> centerDistanceDelay = values[0].toLong()
            Configs.EnableCenterDistance.key -> enableCenterDistance = values[0].lowercase().toBooleanStrict()
            Configs.AllowAutoQueryCenterDistance.key -> allowAutoQueryCenterDistance = values[0].lowercase().toBooleanStrict()
            Configs.EnableAutoQueryCenterDistance.key -> enableAutoQueryCenterDistance = values[0].lowercase().toBooleanStrict()
            Configs.KillReward.key -> killReward = getItemStackFromList(values)
            else -> return false
        }
        return true
    }
}