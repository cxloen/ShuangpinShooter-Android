package com.shuangpinshooter

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlin.random.Random

/**
 * 游戏阶段
 */
enum class Phase { Menu, Playing, Paused, End }

/**
 * 敌机:汉字 + 双拼 + 屏幕坐标(像素,已按 scale 放大)
 */
data class Enemy(
    val hanzi: String,
    val pinyin: String,
    val sp: String,
    var x: Float,
    var y: Float
)

/**
 * 爆炸动画(生命期 0.5s)
 */
data class Explosion(var x: Float, var y: Float, var t: Float)

/**
 * 关卡结算数据
 */
data class EndData(
    val reached: Boolean,
    val passed: Boolean,
    val lvName: String,
    val lvId: Int,
    val target: Int,
    val hits: Int,
    val score: Int,
    val best: Int,
    val combo: Int,
    val accuracy: Int,
    val lives: Int,
    val maxLives: Int
)

/**
 * 游戏状态机 + 游戏循环 + 输入匹配。
 *
 * 设计说明:
 *  - 渲染坐标系统一用"实际像素",敌机 x/y 也是像素值。
 *  - 关卡参数 fall/spawn 来源于 Level,会乘以 scale(屏幕宽 / 300)再使用,
 *    让关卡手感与原 WinForms 300×300 画布尽量一致。
 *  - 60fps 游戏循环由 Composable 端 LaunchedEffect + withFrameNanos 驱动,
 *    这里只暴露 tick(dt, w, h) 接口。
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    // ---- 状态(响应式) ----
    var phase by mutableStateOf(Phase.Menu); private set
    var levelId by mutableStateOf(1); private set
    var lives by mutableStateOf(5); private set
    val maxLives = 5

    var score by mutableStateOf(0); private set
    var hits by mutableStateOf(0); private set
    var combo by mutableStateOf(0); private set
    var maxCombo by mutableStateOf(0); private set
    var misses by mutableStateOf(0); private set

    var timeLeft by mutableStateOf(60f); private set
    private var totalTime = 60f

    var inputBuf by mutableStateOf(""); private set

    var endData: EndData? = null
        private set

    var lastResultTitle by mutableStateOf<String?>(null)
        private set

    // ---- 内部可变集合 ----
    val enemies = mutableStateListOf<Enemy>()
    val explosions = mutableStateListOf<Explosion>()

    private var matchedEnemy: Enemy? = null
    private var spawnT = 0f
    private var fallSpeed = 60f
    private var spawnInterval = 1.8f
    private var startMs = 0L
    private var lastMs = 0L
    private val rng = Random(System.currentTimeMillis())

    // ---- 存档 ----
    private val save get() = Storage.save(getApplication())
    private val settings get() = Storage.settings(getApplication())

    // ============================================================
    // 关卡控制
    // ============================================================

    fun startLevel(id: Int) {
        val lv = Levels.all[id - 1]
        val time = settings.levelTimeSecs
        phase = Phase.Playing
        levelId = id
        lives = maxLives
        score = 0; hits = 0; combo = 0; maxCombo = 0; misses = 0
        timeLeft = time.toFloat(); totalTime = time.toFloat()
        inputBuf = ""; matchedEnemy = null
        enemies.clear(); explosions.clear()
        spawnT = 0f
        // 这里 fall/spawn 仍是相对 300×300 基准画布的"逻辑速度",tick 时再乘 scale
        fallSpeed = lv.fall
        spawnInterval = lv.spawn
        startMs = System.currentTimeMillis()
        lastMs = startMs
        endData = null
        lastResultTitle = null
    }

    fun togglePause() {
        when (phase) {
            Phase.Playing -> { phase = Phase.Paused; lastMs = System.currentTimeMillis() }
            Phase.Paused -> {
                // 修正时间漂移:把 startMs 往后推
                val pausedMs = System.currentTimeMillis() - lastMs
                startMs += pausedMs
                phase = Phase.Playing
            }
            else -> {}
        }
    }

    fun backToMenu() {
        phase = Phase.Menu
        inputBuf = ""
        matchedEnemy = null
        enemies.clear(); explosions.clear()
    }

    // ============================================================
    // 游戏循环
    // ============================================================

    /**
     * @param dt      距离上帧的秒数
     * @param canvasW 游戏画布宽(像素)
     * @param canvasH 游戏画布高(像素)
     */
    fun tick(dt: Float, canvasW: Float, canvasH: Float) {
        if (phase != Phase.Playing) return

        // 像素 / 300 基准的比例,保证手感一致
        val scale = (minOf(canvasW, canvasH) / 300f).coerceAtLeast(0.5f)

        // 剩余时间
        timeLeft = (totalTime - (System.currentTimeMillis() - startMs) / 1000f)
            .coerceAtLeast(0f)

        // 生成敌机
        spawnT += dt
        if (spawnT >= spawnInterval) {
            spawnT = 0f
            spawnEnemy(canvasW, scale)
        }

        // 敌机下落
        val speed = fallSpeed * scale
        enemies.forEach { it.y += speed * dt }

        // 漏掉检测
        val floorY = canvasH - INPUT_H_PX * scale - 60f * scale
        var lost = 0
        for (i in enemies.indices.reversed()) {
            if (enemies[i].y > floorY) {
                lost++
                enemies.removeAt(i)
            }
        }
        if (lost > 0) {
            lives = (lives - lost).coerceAtLeast(0)
            misses += lost
            combo = 0
            matchedEnemy = null
            inputBuf = ""
            if (lives <= 0) { endLevel(false, scale, canvasW); return }
        }

        // 爆炸动画
        for (i in explosions.indices.reversed()) {
            explosions[i].t += dt
            if (explosions[i].t >= 0.5f) explosions.removeAt(i)
        }

        if (timeLeft <= 0f) { endLevel(true, scale, canvasW); return }

        updateMatch()
    }

    private fun spawnEnemy(canvasW: Float, scale: Float) {
        val lv = Levels.all[levelId - 1]
        val w = lv.words[rng.nextInt(lv.words.size)]
        // 敌机 X 范围在游戏画布内,留 20×scale 边距
        val margin = 20f * scale
        val x = rng.nextFloat() * (canvasW - 2 * margin) + margin
        enemies.add(Enemy(w.hanzi, w.pinyin, w.sp, x, -40f * scale))
    }

    private fun endLevel(success: Boolean, scale: Float, canvasW: Float) {
        phase = Phase.End
        val lv = Levels.all[levelId - 1]
        val total = hits + misses
        val accuracy = if (total > 0) Math.round(hits * 100f / total) else 0
        val reached = hits >= lv.target
        val passed = reached || hits > 0

        val prevBest = save.bestScore(lv.id)
        save.setBestScore(lv.id, score)
        save.setBestCombo(lv.id, maxCombo)
        if (passed && levelId >= save.unlockedLevel && levelId < Levels.all.size)
            save.unlockedLevel = levelId + 1

        endData = EndData(
            reached = reached,
            passed = passed,
            lvName = lv.name,
            lvId = lv.id,
            target = lv.target,
            hits = hits,
            score = score,
            best = maxOf(score, prevBest),
            combo = maxCombo,
            accuracy = accuracy,
            lives = lives,
            maxLives = maxLives
        )
        lastResultTitle = if (reached) "关卡完成!" else if (passed) "通过!" else "再接再厉"
    }

    // ============================================================
    // 输入
    // ============================================================

    fun handleKey(ch: Char) {
        if (phase != Phase.Playing) return
        if (inputBuf.length >= 3) return
        inputBuf += ch.lowercaseChar()
        checkMatch()
    }

    fun handleBackspace() {
        if (phase != Phase.Playing) return
        if (inputBuf.isNotEmpty()) inputBuf = inputBuf.dropLast(1)
        updateMatch()
    }

    private fun checkMatch() {
        for (e in enemies) {
            if (e.sp == inputBuf) { onHit(e); return }
        }
        updateMatch()
        // 回滚:没有任何敌机以当前输入开头
        val hasPartial = enemies.any { it.sp.startsWith(inputBuf) }
        if (!hasPartial && inputBuf.isNotEmpty()) {
            inputBuf = inputBuf.dropLast(1)
            updateMatch()
        }
    }

    private fun onHit(enemy: Enemy) {
        hits++
        combo++
        maxCombo = maxOf(maxCombo, combo)
        val bonus = minOf(20, (combo / 3) * 2)
        score += 10 + bonus
        explosions.add(Explosion(enemy.x, enemy.y, 0f))
        enemies.remove(enemy)
        inputBuf = ""
        matchedEnemy = null
    }

    private fun updateMatch() {
        matchedEnemy = null
        if (inputBuf.isNotEmpty()) {
            for (e in enemies) {
                if (e.sp == inputBuf) { matchedEnemy = e; return }
                if (matchedEnemy == null && e.sp.startsWith(inputBuf)) matchedEnemy = e
            }
        }
    }

    fun matchedEnemy(): Enemy? = matchedEnemy

    // ============================================================
    // 存档 / 关卡解锁
    // ============================================================

    fun unlockedLevel(): Int = save.unlockedLevel

    fun bestScore(levelId: Int): Int = save.bestScore(levelId)

    fun levelTimeSecs(): Int = settings.levelTimeSecs

    fun setLevelTimeSecs(secs: Int) { settings.levelTimeSecs = secs }

    companion object {
        /** HUD 顶部高度(按 scale 缩放前的基准,14 px) */
        const val HUD_H_PX = 14f
        /** 屏幕底部输入条高度(基准,18 px) */
        const val INPUT_H_PX = 18f
    }
}