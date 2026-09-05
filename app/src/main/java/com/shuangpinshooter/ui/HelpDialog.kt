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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shuangpinshooter.ui.theme.BgTop
import com.shuangpinshooter.ui.theme.Dark
import com.shuangpinshooter.ui.theme.Muted
import com.shuangpinshooter.ui.theme.Primary

/**
 * 小鹤双拼键位说明对话框(对应 C# 版 HelpForm)
 */
@Composable
fun HelpDialog(onDismiss: () -> Unit) {
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
                    Text("小鹤双拼键位", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Primary)
                    TextButton(onClick = onDismiss) { Text("关闭") }
                }
                Spacer(Modifier.height(8.dp))

                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    SectionHeader("声母 → 键")
                    KeyRow("b p m f", "b p m f")
                    KeyRow("d t n l", "d t n l")
                    KeyRow("g k h", "g k h")
                    KeyRow("j q x", "j q x")
                    KeyRow("zh ch sh", "v i u")
                    KeyRow("z c s", "z c s")
                    KeyRow("r y w", "r y w")
                    KeyRow("零声母", "韵母首字母")

                    Spacer(Modifier.height(12.dp))
                    SectionHeader("韵母 → 键")
                    KeyRow("a o e i u ü", "a o e i u v")
                    KeyRow("ai ei ui", "d w v")
                    KeyRow("ao ou iu", "c z q")
                    KeyRow("ie üe er", "p t r")
                    KeyRow("an en in", "j f b")
                    KeyRow("un ün", "y y")
                    KeyRow("ang eng ing ong", "h g k s")
                    KeyRow("ian uan üan", "m r r")
                    KeyRow("iang uang iong", "l l s")

                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("输入示例(官方方案Ⅰ):",
                                fontWeight = FontWeight.Bold, color = Dark, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "中 (zh+ong) -> vs\n新 (x+in)   -> xb\n安 (零声母+an) -> an\n熊 (x+iong) -> xs",
                                fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Dark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, fontWeight = FontWeight.Bold, color = Primary, fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun KeyRow(name: String, key: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(name, fontFamily = FontFamily.Monospace, color = Muted, fontSize = 13.sp,
            modifier = Modifier.weight(1f))
        Surface(
            color = androidx.compose.ui.graphics.Color(0xFFF1F5F9),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(key, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                color = Primary, fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}