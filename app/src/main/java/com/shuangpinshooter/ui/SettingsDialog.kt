package com.shuangpinshooter.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shuangpinshooter.Fmt
import com.shuangpinshooter.ui.theme.BgTop
import com.shuangpinshooter.ui.theme.Dark
import com.shuangpinshooter.ui.theme.Muted
import com.shuangpinshooter.ui.theme.Primary

/**
 * 设置对话框(对应 C# 版 SettingsForm)
 * 选项:60/120/180/300/600 秒,默认 300 秒。
 */
@Composable
fun SettingsDialog(
    initialSecs: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(60, 120, 180, 300, 600)
    var selected by remember { mutableStateOf(initialSecs.coerceAtLeast(60)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = BgTop, shadowElevation = 8.dp) {
            Column(Modifier.padding(20.dp)) {
                Text("设置", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Primary,
                    modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("每关时长", fontWeight = FontWeight.Bold, color = Primary, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        for (secs in options) {
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = (selected == secs),
                                    onClick = { selected = secs })
                                Text(
                                    text = Fmt.duration(secs) + if (secs == 300) "(默认)" else "",
                                    color = Dark, fontSize = 13.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("修改后下次开始关卡生效。", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("取消") }
                    Button(onClick = { onConfirm(selected); onDismiss() }, modifier = Modifier.weight(1f)) {
                        Text("保存")
                    }
                }
            }
        }
    }
}