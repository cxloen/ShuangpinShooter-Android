package com.shuangpinshooter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shuangpinshooter.ui.GameScreen
import com.shuangpinshooter.ui.HelpDialog
import com.shuangpinshooter.ui.SettingsDialog
import com.shuangpinshooter.ui.ShuangpinShooterTheme
import com.shuangpinshooter.ui.VerifyDialog

/**
 * 主入口:装载 Compose 主题、装载 GameScreen,处理 F1/F2/F3 全局快捷键。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShuangpinShooterTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val vm: GameViewModel = viewModel()
    var showHelp by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showVerify by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Transparent)
            // 全局快捷键 F1=帮助 F2=设置 F3=校验 F5=暂停
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.F1 -> { showHelp = true; true }
                    Key.F2 -> { showSettings = true; true }
                    Key.F3 -> { showVerify = true; true }
                    Key.F5 -> { vm.togglePause(); true }
                    else -> false
                }
            }
    ) {
        GameScreen(
            vm = vm,
            onShowHelp = { showHelp = true },
            onShowSettings = { showSettings = true },
            onShowVerify = { showVerify = true }
        )
    }

    if (showHelp) HelpDialog(onDismiss = { showHelp = false })
    if (showSettings) SettingsDialog(
        initialSecs = vm.levelTimeSecs(),
        onConfirm = { secs -> vm.setLevelTimeSecs(secs) },
        onDismiss = { showSettings = false }
    )
    if (showVerify) VerifyDialog(onDismiss = { showVerify = false })
}