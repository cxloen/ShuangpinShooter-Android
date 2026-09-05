package com.shuangpinshooter

/**
 * 小鹤双拼键位 + 拼音 → 双拼
 * 直接对应 C# 版的 Shuangpin 类,键位表与零声母规则完全一致。
 */
object Shuangpin {

    private val SM_KEY = mapOf(
        "b" to "b", "p" to "p", "m" to "m", "f" to "f",
        "d" to "d", "t" to "t", "n" to "n", "l" to "l",
        "g" to "g", "k" to "k", "h" to "h",
        "j" to "j", "q" to "q", "x" to "x",
        "zh" to "v", "ch" to "i", "sh" to "u", "r" to "r",
        "z" to "z", "c" to "c", "s" to "s",
        "y" to "y", "w" to "w"
    )

    // 权威小鹤双拼韵母键位（flypy.cc, shuangpin.xyz, ulpb.app）
    private val YM_KEY = mapOf(
        "a" to "a", "o" to "o", "e" to "e", "i" to "i", "u" to "u", "ü" to "v",
        "ai" to "d", "ei" to "w", "ui" to "v", "ao" to "c", "ou" to "z", "iu" to "q",
        "ie" to "p", "üe" to "t", "ve" to "t", "er" to "r", "ue" to "t",
        "an" to "j", "en" to "f", "in" to "b", "un" to "y", "ün" to "y", "vn" to "y",
        "ang" to "h", "eng" to "g", "ing" to "k", "ong" to "s",
        "ian" to "m", "uan" to "r", "üan" to "r", "van" to "r",
        "iang" to "l", "uang" to "l",
        "iong" to "s", "ueng" to "g",
        "iao" to "n", "uai" to "k",
        "ua" to "x", "uo" to "o", "ia" to "x"
    )

    private val TONE_MAP = mapOf(
        'ā' to 'a', 'á' to 'a', 'ǎ' to 'a', 'à' to 'a',
        'ō' to 'o', 'ó' to 'o', 'ǒ' to 'o', 'ò' to 'o',
        'ē' to 'e', 'é' to 'e', 'ě' to 'e', 'è' to 'e',
        'ī' to 'i', 'í' to 'i', 'ǐ' to 'i', 'ì' to 'i',
        'ū' to 'u', 'ú' to 'u', 'ǔ' to 'u', 'ù' to 'u',
        'ǖ' to 'ü', 'ǘ' to 'ü', 'ǚ' to 'ü', 'ǜ' to 'ü'
    )

    private val SM_PRIORITY = listOf(
        "zh", "ch", "sh", "z", "c", "s",
        "b", "p", "m", "f", "d", "t", "n", "l",
        "g", "k", "h", "j", "q", "x", "y", "w", "r"
    )

    private val YM_PRIORITY = listOf(
        "iang", "uang", "iong", "ueng",
        "üan", "üen",
        "ian", "uan", "van", "vn",
        "iao", "uai", "ua", "uo", "ia",
        "ang", "eng", "ing", "ong",
        "ai", "ei", "ui", "ao", "ou", "iu",
        "ie", "üe", "ve", "er", "ue",
        "an", "en", "in", "un", "ün",
        "a", "o", "e", "i", "u", "ü"
    )

    /**
     * 把全拼(含声调)转换为小鹤双拼编码。
     * 例:"zhong" -> "vs"; "xin" -> "xb"; "ai" -> "ai"(零声母双字母)
     */
    fun toSp(pinyin: String): String {
        // 标准化:去声调、转小写
        val s = pinyin.lowercase().map { TONE_MAP[it] ?: it }.joinToString("")

        // 找声母
        var rest = s
        var sm = ""
        for (k in SM_PRIORITY) {
            if (rest.startsWith(k)) {
                sm = k
                rest = rest.substring(k.length)
                break
            }
        }

        // j/q/x/y 后 u 视为 ü
        if (rest.startsWith("u") && (sm == "j" || sm == "q" || sm == "x" || sm == "y")) {
            val sAlt = "ü" + rest.substring(1)
            if (sAlt == "ü") return SM_KEY[sm]!! + YM_KEY["ü"]!!
            for (y in listOf("üan", "üe", "ün")) {
                if (sAlt == y || sAlt.startsWith(y))
                    return SM_KEY[sm]!! + YM_KEY[y]!!
            }
        }

        // 找韵母
        var ym = ""
        for (y in YM_PRIORITY) {
            if (rest == y || rest.startsWith(y)) {
                ym = y
                break
            }
        }
        if (ym.isEmpty()) ym = rest
        val ymKey = YM_KEY[ym] ?: ym

        // 零声母处理（官方小鹤方案Ⅰ：零声母 = 韵母首字母）
        if (sm.isEmpty()) {
            // ü 在键盘上是 V 键,零声母也要写键位不能写 "ü"
            val head: Char = if (ym[0] == 'ü') 'v' else ym[0]
            val headStr = head.toString()
            // 单字母韵母:零声母 + 韵母所在键 → aa/oo/ee/ii/uu/vv
            if (ym.length == 1) return headStr + ymKey
            // 双字母韵母:零声母(首字母键位) + 韵母末字母 → an/ai/en/ou/er/ve/...
            if (ym.length == 2) return headStr + ym[1]
            // 三字母及以上:零声母(首字母键位) + 韵母键位键 → ah/im/ur/il/is/ug/vr/...
            return headStr + ymKey
        }

        return SM_KEY[sm]!! + ymKey
    }
}