# 小鹤双拼射击场 · 安卓版

参考 C# WinForms 版 `ShuangpinShooter-CS` 移植,使用 Kotlin + Jetpack Compose 实现。

游戏规则:屏幕上方不断有汉字敌机下落,你需要键入该字的**小鹤双拼编码**(2~3 个字母)将其击落。打错会回滚输入,生命值漏光或时间到则关卡结束。

## 项目结构

```
ShuangpinShooter-Android/
├── settings.gradle.kts
├── build.gradle.kts            顶层
├── gradle.properties
├── gradle/wrapper/             gradle-wrapper.properties
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/shuangpinshooter/
        │   ├── MainActivity.kt
        │   ├── Shuangpin.kt          拼音 → 双拼算法
        │   ├── WordPool.kt           词库 + 关卡
        │   ├── Storage.kt            SharedPreferences 存档
        │   ├── GameViewModel.kt      状态机 + 游戏循环
        │   └── ui/
        │       ├── GameScreen.kt     Canvas 渲染 + 输入 + 浮层
        │       ├── HelpDialog.kt     键位说明
        │       ├── SettingsDialog.kt 时长设置
        │       ├── VerifyDialog.kt   词库SP校验
        │       └── theme/
        │           ├── Color.kt
        │           └── Theme.kt
        └── res/
            ├── values/{strings,colors,themes}.xml
            ├── drawable/ic_launcher_*.xml
            └── mipmap-anydpi-v26/ic_launcher.xml
```

## 词库与关卡

与原 C# 版完全一致(共 6 关、约 250 个易错字):

| ID | 名称 | 目标命中 | 时长 | 词条数 |
|---|---|---|---|---|
| 1 | 前后鼻音 | 25 | 5 分钟 | ~70 |
| 2 | 平翘舌 | 25 | 5 分钟 | ~40 |
| 3 | n-l 之争 | 22 | 5 分钟 | ~30 |
| 4 | h-f 之争 | 20 | 5 分钟 | ~30 |
| 5 | 综合挑战 | 35 | 5 分钟 | 全部 |
| 6 | 川渝易错字 | 35 | 5 分钟 | ~130 |

时长可在 F2 设置里改为 60/120/180/300/600 秒。

## 构建与运行

### 方式 A:GitHub Actions 云端构建(零环境依赖,推荐)

工程里已经写好 `.github/workflows/build.yml`。**无需安装任何 Android 工具,推到 GitHub 即自动编译并提供 APK 下载**。

用法:
1. 把这个目录建成本地 git 仓库(如果还没建):
   ```bash
   cd ShuangpinShooter-Android
   git init && git add . && git commit -m "init"
   ```
2. 在 GitHub 上建一个空仓库,把本地 push 上去:
   ```bash
   git remote add origin https://github.com/<your-name>/ShuangpinShooter-Android.git
   git branch -M main
   git push -u origin main
   ```
3. 进 GitHub 仓库的 **Actions** 页 → 选 `Build Debug APK` → 点 ▶ Run workflow(或等待 PR 自动触发)
4. 跑完后,在该 workflow run 页面底部 **Artifacts** 区下载 `app-debug-apk.zip`,里面就是 `app-debug.apk`

跑在 `ubuntu-latest` runner 上,Linux 没有 Windows native lock 的坑,5-8 分钟出 APK。

### 方式 B:Android Studio(本地)

1. 打开 Android Studio(Hedgehog | Iguana | Jellyfish 任一版本均可)
2. `File > Open` 选择本目录 `ShuangpinShooter-Android`
3. 首次打开会自动下载 Gradle 8.5 + Android Gradle Plugin + Compose 依赖
4. 接入手机(开启 USB 调试)或启动模拟器,点 ▶ Run

### 方式 C:命令行(本机构建)

```bash
cd ShuangpinShooter-Android
./gradlew assembleDebug       # Linux / macOS / Git Bash
# 或 .\gradlew.bat assembleDebug  (Windows cmd/PowerShell)

# 产物:app/build/outputs/apk/debug/app-debug.apk

# 安装到已连接设备
./gradlew installDebug
# 或: adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 环境要求

- JDK 17(或 Android Studio 自带的 JBR)
- Android SDK Platform 34
- Android SDK Build-Tools 34.x
- minSdk 24(Android 7.0 Nougat 及以上)

> **Windows 注意事项**:本机有杀毒软件拦截 Gradle native 库加载时会构建失败(`native-platform.dll 拒绝访问`)。遇到请把 `D:\Tools`、`D:\.gradle` 加到杀软白名单,或直接走 GitHub Actions。

## 操作说明

| 操作 | 按键 |
|---|---|
| 输入双拼编码 | a-z 字母键(物理键盘或屏幕软键盘) |
| 删除一个字符 | Backspace |
| 暂停 / 继续 | Esc 或 F5 |
| 返回主菜单 | Esc(暂停时) |
| 帮助(键位表) | F1 或菜单按钮 |
| 设置时长 | F2 或菜单按钮 |
| 校验词库 | F3 或菜单按钮 |

### 屏幕软键盘

由于大多数安卓设备没有物理键盘,主屏默认展开屏幕软键盘(a-z 全 26 键)。需要时点右上角"收起"隐藏,再点右下角"键盘"按钮恢复。

### 关卡解锁

Lv1 默认解锁。通过 LvN 后解锁 LvN+1(命中数 > 0 即算通过,达成 lv.Target 算完成)。

## 存档位置

`SharedPreferences` 存储在应用私有目录:

- 成绩与解锁:`/data/data/com.shuangpinshooter/shared_prefs/shuangpin_shooter_save.xml`
- 时长设置:`/data/data/com.shuangpinshooter/shared_prefs/shuangpin_shooter_settings.xml`

卸载应用会清空。

## 与 C# 版的差异

| 项 | C# 版 | 安卓版 |
|---|---|---|
| 渲染 | GDI+ (System.Drawing) | Compose Canvas |
| 画布尺寸 | 固定 300×300 | 跟随屏幕(等比缩放) |
| 输入 | 物理键盘 | 物理键盘 + 屏幕软键盘 |
| 存档 | JSON 文件 (`~/.shuangpin_shooter/`) | SharedPreferences |
| 设置 | JSON 文件 | SharedPreferences |
| 计时器 | WinForms Timer | withFrameNanos (60fps) |
| 字号/坐标 | 像素绝对值 | 按 scale 等比缩放 |

游戏手感(下落速度、生成密度、连击奖励、判负逻辑)与原版完全一致。

## 已知限制

- 横屏暂未启用(Manifest 锁竖屏),需要横屏时可改 `android:screenOrientation="fullSensor"`。
- 屏幕软键盘的"重玩本关 / 进入下一关"按钮在浮层(结算)显示,需要触屏点击。
- 软键盘目前只支持小写 a-z,没有大写或符号。

## License

仅作个人学习使用。词库数据来源与原 C# 版保持一致。