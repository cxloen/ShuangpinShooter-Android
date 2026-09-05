package com.shuangpinshooter

import android.content.Context
import android.content.SharedPreferences

/**
 * 存档:每关最高分、最高连击、解锁到的关卡。
 * 用 SharedPreferences 单键存储,无需 JSON 解析。
 */
class SaveData(private val prefs: SharedPreferences) {

    var unlockedLevel: Int
        get() = prefs.getInt(KEY_UNLOCKED, 1).coerceAtLeast(1)
        set(v) { prefs.edit().putInt(KEY_UNLOCKED, v).apply() }

    fun bestScore(levelId: Int): Int = prefs.getInt(scoreKey(levelId), 0)

    fun setBestScore(levelId: Int, score: Int) {
        if (score > bestScore(levelId))
            prefs.edit().putInt(scoreKey(levelId), score).apply()
    }

    fun bestCombo(levelId: Int): Int = prefs.getInt(comboKey(levelId), 0)

    fun setBestCombo(levelId: Int, combo: Int) {
        if (combo > bestCombo(levelId))
            prefs.edit().putInt(comboKey(levelId), combo).apply()
    }

    companion object {
        private const val KEY_UNLOCKED = "unlocked_level"
        private fun scoreKey(id: Int) = "best_score_$id"
        private fun comboKey(id: Int) = "best_combo_$id"
    }
}

/**
 * 设置:每关时长(秒)
 */
class SettingsStore(private val prefs: SharedPreferences) {
    var levelTimeSecs: Int
        get() = prefs.getInt("level_time_secs", 300).coerceAtLeast(60)
        set(v) { prefs.edit().putInt("level_time_secs", v).apply() }
}

/**
 * 统一入口:从 ApplicationContext 创建实例。
 */
object Storage {
    private const val PREFS_SAVE = "shuangpin_shooter_save"
    private const val PREFS_SETTINGS = "shuangpin_shooter_settings"

    @Volatile private var saveInstance: SaveData? = null
    @Volatile private var settingsInstance: SettingsStore? = null

    fun save(ctx: Context): SaveData = saveInstance ?: synchronized(this) {
        saveInstance ?: SaveData(ctx.getSharedPreferences(PREFS_SAVE, Context.MODE_PRIVATE)).also { saveInstance = it }
    }

    fun settings(ctx: Context): SettingsStore = settingsInstance ?: synchronized(this) {
        settingsInstance ?: SettingsStore(ctx.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)).also { settingsInstance = it }
    }
}