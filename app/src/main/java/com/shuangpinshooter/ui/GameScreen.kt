package com.shuangpinshooter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shuangpinshooter.Enemy
import com.shuangpinshooter.GameViewModel
import com.shuangpinshooter.Levels
import com.shuangpinshooter.Phase
import com.shuangpinshooter.ui.theme.*
import kotlin.math.min

/**
 * 主游戏画面:Canvas 渲染 + 键盘/软键盘输入 + 浮层(菜单/暂停/结束)。
 */
@Composable
fun GameScreen(
    vm: GameViewModel = viewModel(),
    onShowHelp: () -> Unit,
    onShowSettings: () -> Unit,
    onShowVerify: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val textMeasurer = rememberTextMeasurer()
    var showSoftKbd by remember { mutableStateOf(true) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BgTop)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.Backspace -> { vm.handleBackspace(); true }
                    Key.Escape -> when (vm.phase) {
                        Phase.Playing, Phase.Paused -> { vm.togglePause(); true }
                        Phase.End -> { vm.backToMenu(); true }
                        else -> false
                    }
                    else -> {
                        val ch = keyToChar(event.key)
                        if (ch != null) { vm.handleKey(ch); true } else false
                    }
                }
            }
    ) {
        val wPx = with(LocalDensity.current) { maxWidth.toPx() }
        val hPx = with(LocalDensity.current) { maxHeight.toPx() }
        val scale = (min(wPx, hPx) / 300f).coerceAtLeast(0.5f)

        // 60fps 游戏循环
        LaunchedEffect(vm.phase) {
            if (vm.phase == Phase.Playing) {
                var last = 0L
                while (vm.phase == Phase.Playing) {
                    val now = withFrameNanos { it }
                    val dt = if (last == 0L) 0f else ((now - last) / 1_000_000_000f).coerceAtMost(0.1f)
                    last = now
                    vm.tick(dt, wPx, hPx)
                }
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackground()
            when (vm.phase) {
                Phase.Menu -> {} // 菜单走浮层
                Phase.Playing, Phase.Paused, Phase.End -> drawGameArea(vm, scale, textMeasurer)
            }
        }

        when (vm.phase) {
            Phase.Menu -> MenuOverlay(
                vm = vm,
                onShowHelp = onShowHelp,
                onShowSettings = onShowSettings,
                onShowVerify = onShowVerify
            )
            Phase.Paused -> PauseOverlay(
                onResume = { vm.togglePause() },
                onBack = { vm.backToMenu() }
            )
            Phase.End -> EndOverlay(
                vm = vm,
                onNext = {
                    if (vm.levelId < Levels.all.size) vm.startLevel(vm.levelId + 1)
                    else vm.startLevel(vm.levelId)
                },
                onRetry = { vm.startLevel(vm.levelId) },
                onBack = { vm.backToMenu() }
            )
            else -> {}
        }

        if (showSoftKbd && vm.phase == Phase.Playing) {
            SoftKeyboard(
                onKey = vm::handleKey,
                onBackspace = vm::handleBackspace,
                onClose = { showSoftKbd = false },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        if (vm.phase == Phase.Playing && !showSoftKbd) {
            FilledTonalButton(
                onClick = { showSoftKbd = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
            ) { Text("键盘") }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

/**
 * 把 Key 枚举映射成 a-z 小写字符,不在此范围返回 null。
 */
private fun keyToChar(k: Key): Char? = when (k) {
    Key.A -> 'a'; Key.B -> 'b'; Key.C -> 'c'; Key.D -> 'd'; Key.E -> 'e'
    Key.F -> 'f'; Key.G -> 'g'; Key.H -> 'h'; Key.I -> 'i'; Key.J -> 'j'
    Key.K -> 'k'; Key.L -> 'l'; Key.M -> 'm'; Key.N -> 'n'; Key.O -> 'o'
    Key.P -> 'p'; Key.Q -> 'q'; Key.R -> 'r'; Key.S -> 's'; Key.T -> 't'
    Key.U -> 'u'; Key.V -> 'v'; Key.W -> 'w'; Key.X -> 'x'; Key.Y -> 'y'
    Key.Z -> 'z'
    else -> null
}

// ============================================================
// 绘制
// ============================================================

private fun DrawScope.drawBackground() {
    val grad = Brush.verticalGradient(listOf(BgTop, BgBot))
    drawRect(brush = grad, size = size)
    drawCircle(Primary.copy(alpha = 0.06f),
        radius = size.minDimension * 0.6f,
        center = Offset(size.width * 0.2f, size.height * 0.2f))
    drawCircle(Accent.copy(alpha = 0.05f),
        radius = size.minDimension * 0.6f,
        center = Offset(size.width * 0.8f, size.height * 0.8f))
}

private fun DrawScope.drawGameArea(vm: GameViewModel, scale: Float, m: TextMeasurer) {
    drawRect(Card, topLeft = Offset.Zero, size = size)
    drawHud(vm, scale, m)
    drawExplosions(vm, scale)
    val matched = vm.matchedEnemy()
    for (e in vm.enemies) drawEnemy(e, matched === e, scale, m)
    drawPlayer(scale)
    drawInputBar(vm.inputBuf, scale, m)
}

private fun DrawScope.drawHud(vm: GameViewModel, scale: Float, m: TextMeasurer) {
    val hudH = GameViewModel.HUD_H_PX * scale
    drawRect(Color(0xFFF5F8FA), topLeft = Offset.Zero, size = Size(size.width, hudH))
    drawLine(Dark.copy(alpha = 0.15f), Offset(0f, hudH), Offset(size.width, hudH), strokeWidth = 1f)

    val style = TextStyle(
        fontSize = (7 * scale).sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif
    )
    drawText(m, "Lv${vm.levelId}",
        topLeft = Offset(4f * scale, 1f * scale), style = style.copy(color = Muted))
    drawText(m, "♥${vm.lives.coerceAtLeast(0)}",
        topLeft = Offset(26f * scale, 1f * scale), style = style.copy(color = Danger))

    val t = vm.timeLeft.toInt().coerceAtLeast(0)
    val timeStr = "T${"%02d".format(t)}"
    val ts = m.measure(timeStr, style)
    drawText(m, timeStr,
        topLeft = Offset((size.width - ts.size.width) / 2f, 1f * scale), style = style.copy(color = Dark))

    val rightStr = "${vm.score}  x${vm.combo}"
    val rs = m.measure(rightStr, style)
    drawText(m, rightStr,
        topLeft = Offset(size.width - rs.size.width - 4f * scale, 1f * scale), style = style.copy(color = Primary))
}

private fun DrawScope.drawEnemy(e: Enemy, isMatch: Boolean, scale: Float, m: TextMeasurer) {
    val w = 60f * scale
    val h = 70f * scale
    val cx = e.x
    val cy = e.y
    val x = cx - w / 2f
    val y = cy - h / 2f

    val path = Path().apply {
        moveTo(x, y + 6f * scale)
        cubicTo(x, y, x + w / 2f, y, x + w / 2f, y)
        cubicTo(x + w / 2f, y, x + w, y, x + w, y + 6f * scale)
        lineTo(x + w, y + 40f * scale)
        cubicTo(x + w, y + 56f * scale, x + w / 2f, y + 56f * scale, x + w / 2f, y + 56f * scale)
        cubicTo(x + w / 2f, y + 56f * scale, x, y + 56f * scale, x, y + 40f * scale)
        close()
    }
    val brush = Brush.verticalGradient(
        listOf(
            if (isMatch) AccentSoft else Color(0xFFF1F5F9),
            if (isMatch) Accent else Color(0xFFCBD5E1)
        ),
        startY = y, endY = y + 56f * scale
    )
    drawPath(path, brush)
    drawPath(path, Dark.copy(alpha = 0.4f), style = Stroke(1.5f * scale))

    val hanziStyle = TextStyle(
        fontSize = (18 * scale).sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif
    )
    val hanziColor = if (isMatch) Color.White else Primary
    val hsz = m.measure(e.hanzi, hanziStyle)
    drawText(m, e.hanzi,
        topLeft = Offset(cx - hsz.size.width / 2f, y + 18f * scale), style = hanziStyle.copy(color = hanziColor))

    val tagStyle = TextStyle(
        fontSize = (8 * scale).sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )
    val tagW = 44f * scale
    val tagH = 14f * scale
    val tagX = cx - tagW / 2f
    val tagY = y + 60f * scale
    val tagBg = if (isMatch) Accent else Color(0xFFEEEEEE)
    drawRoundRect(tagBg,
        topLeft = Offset(tagX, tagY),
        size = Size(tagW, tagH),
        cornerRadius = CornerRadius(6f * scale))
    val tagColor = if (isMatch) Color.White else Muted
    val tsz = m.measure(e.sp, tagStyle)
    drawText(m, e.sp,
        topLeft = Offset(tagX + (tagW - tsz.size.width) / 2f, tagY + (tagH - tsz.size.height) / 2f),
        style = tagStyle.copy(color = tagColor))
}

private fun DrawScope.drawExplosions(vm: GameViewModel, scale: Float) {
    for (ex in vm.explosions) {
        val ratio = (ex.t / 0.5f).coerceIn(0f, 1f)
        val r = (6f + ratio * 24f) * scale
        val alpha = (255f * (1f - ratio)).toInt().coerceIn(0, 255)
        if (alpha <= 0) continue
        val cx = ex.x; val cy = ex.y

        drawCircle(Color(red = 251, green = 191, blue = 36, alpha = alpha / 2),
            radius = r, center = Offset(cx, cy))
        val inner = (4f + ratio * 14f) * scale
        drawCircle(Color(red = 249, green = 115, blue = 22, alpha = alpha),
            radius = inner, center = Offset(cx, cy))

        val lineLen = (8f + ratio * 6f) * scale
        val penColor = Color(red = 251, green = 191, blue = 36, alpha = alpha)
        val sw = 2f * scale
        drawLine(penColor, Offset(cx, cy - r), Offset(cx, cy - r - lineLen), strokeWidth = sw)
        drawLine(penColor, Offset(cx, cy + r), Offset(cx, cy + r + lineLen), strokeWidth = sw)
        drawLine(penColor, Offset(cx - r, cy), Offset(cx - r - lineLen, cy), strokeWidth = sw)
        drawLine(penColor, Offset(cx + r, cy), Offset(cx + r + lineLen, cy), strokeWidth = sw)
    }
}

private fun DrawScope.drawPlayer(scale: Float) {
    val cx = size.width / 2f
    val cy = size.height - GameViewModel.INPUT_H_PX * scale - 14f * scale
    val s = 24f * scale

    drawOval(
        Primary.copy(alpha = 0.25f),
        topLeft = Offset(cx - s / 2f + 3f * scale, cy + s / 2f),
        size = Size(s - 6f * scale, 4f * scale)
    )
    val path = Path().apply {
        moveTo(cx, cy - s / 2f)
        lineTo(cx + s / 2f - 3f, cy + s / 2f - 3f)
        lineTo(cx, cy + s / 2f - 7f)
        lineTo(cx - s / 2f + 3f, cy + s / 2f - 3f)
        close()
    }
    val grad = Brush.verticalGradient(
        listOf(Color(0xFF60A5FA), Primary),
        startY = cy - s / 2f, endY = cy + s / 2f
    )
    drawPath(path, grad)
    drawPath(path, Color.White.copy(alpha = 0.4f), style = Stroke(1.5f))
    drawCircle(Color(0xFFFBBF24), 3f * scale, Offset(cx, cy - 1f))
}

private fun DrawScope.drawInputBar(buf: String, scale: Float, m: TextMeasurer) {
    val ib = GameViewModel.INPUT_H_PX * scale
    val rectY = size.height - ib
    drawRect(Color(0xFFF8FAFC), topLeft = Offset(0f, rectY), size = Size(size.width, ib))
    val border = if (buf.isEmpty()) Color(0xFFCBD5E1) else Primary
    drawLine(border, Offset(0f, rectY), Offset(size.width, rectY), strokeWidth = 1f)

    val text = if (buf.isEmpty()) "等待输入…" else buf
    val style = TextStyle(
        fontSize = (11 * scale).sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )
    val textColor = if (buf.isEmpty()) Color(0xFF94A3B8) else Primary
    val tsz = m.measure(text, style)
    drawText(m, text,
        topLeft = Offset((size.width - tsz.size.width) / 2f, rectY + (ib - tsz.size.height) / 2f),
        style = style.copy(color = textColor))
}

// ============================================================
// 屏幕软键盘
// ============================================================

@Composable
private fun SoftKeyboard(
    onKey: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 4.dp,
        color = Color(0xFF1E293B),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("屏幕键盘", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
                    modifier = Modifier.padding(start = 6.dp))
                TextButton(onClick = onClose) { Text("收起", color = Color.White) }
            }
            val rows = listOf(
                "qwertyuiop".toList(),
                "asdfghjkl".toList(),
                "zxcvbnm".toList()
            )
            for ((idx, row) in rows.withIndex()) {
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    // 三行不等长,左右各加 spacer 让中间对齐
                    val leftSpacer = if (idx == 1) 1 else if (idx == 2) 2 else 0
                    if (leftSpacer > 0) Spacer(Modifier.weight(leftSpacer.toFloat()))
                    for (ch in row) KeyBtn("$ch", Modifier.weight(1f)) { onKey(ch) }
                    val rightSpacer = if (idx == 0) 1 else 0
                    if (rightSpacer > 0) Spacer(Modifier.weight(rightSpacer.toFloat()))
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth()) {
                KeyBtn("Backspace", Modifier.weight(1f), onClick = onBackspace)
            }
        }
    }
}

@Composable
private fun KeyBtn(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.heightIn(min = 38.dp),
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(4.dp),
        onClick = onClick
    ) {
        Box(Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// ============================================================
// 浮层
// ============================================================

@Composable
private fun MenuOverlay(
    vm: GameViewModel,
    onShowHelp: () -> Unit,
    onShowSettings: () -> Unit,
    onShowVerify: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color(0xCCF8FAFC)), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.9f)
        ) {
            Column(Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("小鹤双拼射击场", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                Spacer(Modifier.height(4.dp))
                val total = com.shuangpinshooter.WordPool.all.size
                Text("$total 个易错字 · ${Levels.all.size} 关", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))

                val unlocked = vm.unlockedLevel()
                for (lv in Levels.all) {
                    val locked = lv.id > unlocked
                    val best = vm.bestScore(lv.id)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (locked) BgBot else Card
                        ),
                        shape = RoundedCornerShape(8.dp),
                        onClick = { if (!locked) vm.startLevel(lv.id) }
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Lv${lv.id}",
                                fontWeight = FontWeight.Bold, color = Primary,
                                modifier = Modifier.width(40.dp))
                            Text(lv.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            if (locked) {
                                Text("未解锁", color = Muted, fontSize = 12.sp)
                            } else if (best > 0) {
                                Text("★ $best", color = Accent, fontWeight = FontWeight.Bold)
                            } else {
                                Text("开始 →", color = Primary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onShowHelp) { Text("键位 F1") }
                    OutlinedButton(onClick = onShowSettings) { Text("时长 F2") }
                    OutlinedButton(onClick = onShowVerify) { Text("校验 F3") }
                }
            }
        }
    }
}

@Composable
private fun PauseOverlay(onResume: () -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xCCF8FAFC)), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.padding(16.dp)) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("已暂停", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                Spacer(Modifier.height(4.dp))
                Text("休息一下,回来继续", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onResume) { Text("继续") }
                    OutlinedButton(onClick = onBack) { Text("返回主菜单") }
                }
            }
        }
    }
}

@Composable
private fun EndOverlay(vm: GameViewModel, onNext: () -> Unit, onRetry: () -> Unit, onBack: () -> Unit) {
    val data = vm.endData ?: return
    Box(Modifier.fillMaxSize().background(Color(0xCCF8FAFC)), contentAlignment = Alignment.Center) {
        Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.9f)) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = vm.lastResultTitle ?: "结算",
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = if (data.reached) Success else if (data.passed) Success else Danger,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                StatRow("关卡", "${data.lvId}. ${data.lvName}")
                StatRow("完成 / 目标", "${data.hits} / ${data.target}")
                StatRow("本关得分", data.score.toString())
                StatRow("历史最高", data.best.toString())
                StatRow("最高连击", data.combo.toString())
                StatRow("命中率", "${data.accuracy}%")
                StatRow("剩余生命", "${data.lives}/${data.maxLives}")
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (data.passed && vm.levelId < Levels.all.size) {
                        Button(onClick = onNext, modifier = Modifier.weight(1f)) { Text("下一关") }
                    }
                    OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f)) { Text("重玩") }
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("菜单") }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, color = Muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = Dark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}