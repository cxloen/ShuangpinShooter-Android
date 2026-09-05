package com.shuangpinshooter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shuangpinshooter.Shuangpin
import com.shuangpinshooter.WordPool
import com.shuangpinshooter.ui.theme.BgTop
import com.shuangpinshooter.ui.theme.Danger
import com.shuangpinshooter.ui.theme.Dark
import com.shuangpinshooter.ui.theme.Muted
import com.shuangpinshooter.ui.theme.Primary
import com.shuangpinshooter.ui.theme.Success

/**
 * 词库双拼校验(对应 C# 版 VerifyForm)
 * 把所有字的 Sp 和 Shuangpin.toSp(Pinyin) 对比,不一致的项目以红色标出。
 */
@Composable
fun VerifyDialog(onDismiss: () -> Unit) {
    val mismatches = WordPool.all.mapNotNull { w ->
        val exp = Shuangpin.toSp(w.pinyin)
        if (exp != w.sp) Mismatch(w.cat, w.hanzi, w.pinyin, w.sp, exp) else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BgTop,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (mismatches.isEmpty())
                            "✓ 全部一致 · 共 ${WordPool.all.size} 个字"
                        else "✗ 发现 ${mismatches.size} 个不一致",
                        fontWeight = FontWeight.Bold,
                        color = if (mismatches.isEmpty()) Success else Danger,
                        fontSize = 14.sp
                    )
                    TextButton(onClick = onDismiss) { Text("关闭") }
                }
                Spacer(Modifier.height(8.dp))

                if (mismatches.isNotEmpty()) {
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        for (m in mismatches) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(m.cat, color = Muted, fontSize = 11.sp,
                                        modifier = Modifier.width(40.dp))
                                    Text(m.hanzi, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                        color = Danger, modifier = Modifier.width(36.dp))
                                    Text(m.pinyin, fontFamily = FontFamily.Monospace,
                                        color = Dark, fontSize = 12.sp,
                                        modifier = Modifier.width(60.dp))
                                    Text(m.sp, fontFamily = FontFamily.Monospace,
                                        color = Danger, fontSize = 12.sp,
                                        modifier = Modifier.width(40.dp))
                                    Text("→", color = Muted, fontSize = 12.sp,
                                        modifier = Modifier.width(20.dp))
                                    Text(m.exp, fontFamily = FontFamily.Monospace,
                                        color = Primary, fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("全部双拼编码都符合权威小鹤方案!", color = Muted)
                    }
                }
            }
        }
    }
}

private data class Mismatch(
    val cat: String, val hanzi: String, val pinyin: String,
    val sp: String, val exp: String
)