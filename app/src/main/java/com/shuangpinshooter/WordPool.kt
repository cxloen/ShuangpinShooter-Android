package com.shuangpinshooter

/**
 * 一条词:汉字 + 拼音 + 双拼 + 分类
 */
data class Word(
    val hanzi: String,
    val pinyin: String,
    val sp: String,
    val cat: String
)

/**
 * 词库:易错双拼字(前后鼻音 / 平翘舌 / n-l / h-f / 川渝)
 * 数据源与 C# 版完全一致,SP 在构建时用 Shuangpin.toSp 计算。
 */
object WordPool {

    private val RAW: Array<Triple<String, String, String>> = arrayOf(
        // 前后鼻音
        Triple("qbh", "心", "xin"), Triple("qbh", "新", "xin"), Triple("qbh", "林", "lin"),
        Triple("qbh", "金", "jin"), Triple("qbh", "亲", "qin"), Triple("qbh", "银", "yin"),
        Triple("qbh", "您", "nin"), Triple("qbh", "信", "xin"), Triple("qbh", "紧", "jin"),
        Triple("qbh", "民", "min"), Triple("qbh", "听", "ting"), Triple("qbh", "情", "qing"),
        Triple("qbh", "清", "qing"), Triple("qbh", "轻", "qing"), Triple("qbh", "明", "ming"),
        Triple("qbh", "停", "ting"), Triple("qbh", "迎", "ying"), Triple("qbh", "应", "ying"),
        Triple("qbh", "平", "ping"), Triple("qbh", "影", "ying"), Triple("qbh", "行", "xing"),
        Triple("qbh", "领", "ling"), Triple("qbh", "宁", "ning"), Triple("qbh", "形", "xing"),
        Triple("qbh", "安", "an"), Triple("qbh", "男", "nan"), Triple("qbh", "南", "nan"),
        Triple("qbh", "看", "kan"), Triple("qbh", "谈", "tan"), Triple("qbh", "三", "san"),
        Triple("qbh", "山", "shan"), Triple("qbh", "半", "ban"), Triple("qbh", "干", "gan"),
        Triple("qbh", "然", "ran"), Triple("qbh", "当", "dang"), Triple("qbh", "帮", "bang"),
        Triple("qbh", "忙", "mang"), Triple("qbh", "长", "chang"), Triple("qbh", "场", "chang"),
        Triple("qbh", "旁", "pang"), Triple("qbh", "想", "xiang"), Triple("qbh", "刚", "gang"),
        Triple("qbh", "香", "xiang"), Triple("qbh", "强", "qiang"), Triple("qbh", "门", "men"),
        Triple("qbh", "根", "gen"), Triple("qbh", "本", "ben"), Triple("qbh", "认", "ren"),
        Triple("qbh", "真", "zhen"), Triple("qbh", "忍", "ren"), Triple("qbh", "奔", "ben"),
        Triple("qbh", "嫩", "nen"), Triple("qbh", "等", "deng"), Triple("qbh", "能", "neng"),
        Triple("qbh", "朋", "peng"), Triple("qbh", "蒙", "meng"), Triple("qbh", "灯", "deng"),
        Triple("qbh", "增", "zeng"), Triple("qbh", "层", "ceng"), Triple("qbh", "坑", "keng"),
        Triple("qbh", "疼", "teng"), Triple("qbh", "困", "kun"), Triple("qbh", "论", "lun"),
        Triple("qbh", "吞", "tun"), Triple("qbh", "村", "cun"), Triple("qbh", "滚", "gun"),
        Triple("qbh", "孙", "sun"), Triple("qbh", "损", "sun"), Triple("qbh", "中", "zhong"),
        Triple("qbh", "红", "hong"), Triple("qbh", "东", "dong"), Triple("qbh", "通", "tong"),
        Triple("qbh", "动", "dong"), Triple("qbh", "空", "kong"), Triple("qbh", "控", "kong"),
        Triple("qbh", "雄", "xiong"), Triple("qbh", "聪", "cong"), Triple("qbh", "笼", "long"),
        Triple("qbh", "总", "zong"), Triple("qbh", "龙", "long"), Triple("qbh", "群", "qun"),
        Triple("qbh", "军", "jun"), Triple("qbh", "运", "yun"), Triple("qbh", "训", "xun"),
        Triple("qbh", "云", "yun"), Triple("qbh", "熊", "xiong"), Triple("qbh", "穷", "qiong"),

        // 平翘舌
        Triple("pqs", "知", "zhi"), Triple("pqs", "之", "zhi"), Triple("pqs", "中", "zhong"),
        Triple("pqs", "桌", "zhuo"), Triple("pqs", "主", "zhu"), Triple("pqs", "住", "zhu"),
        Triple("pqs", "资", "zi"), Triple("pqs", "子", "zi"), Triple("pqs", "字", "zi"),
        Triple("pqs", "坐", "zuo"), Triple("pqs", "早", "zao"), Triple("pqs", "做", "zuo"),
        Triple("pqs", "吃", "chi"), Triple("pqs", "出", "chu"), Triple("pqs", "初", "chu"),
        Triple("pqs", "处", "chu"), Triple("pqs", "查", "cha"), Triple("pqs", "茶", "cha"),
        Triple("pqs", "词", "ci"), Triple("pqs", "此", "ci"), Triple("pqs", "从", "cong"),
        Triple("pqs", "错", "cuo"), Triple("pqs", "菜", "cai"), Triple("pqs", "草", "cao"),
        Triple("pqs", "是", "shi"), Triple("pqs", "时", "shi"), Triple("pqs", "事", "shi"),
        Triple("pqs", "书", "shu"), Triple("pqs", "熟", "shu"), Triple("pqs", "上", "shang"),
        Triple("pqs", "四", "si"), Triple("pqs", "思", "si"), Triple("pqs", "死", "si"),
        Triple("pqs", "送", "song"), Triple("pqs", "松", "song"), Triple("pqs", "算", "suan"),
        Triple("pqs", "说", "shuo"), Triple("pqs", "深", "shen"), Triple("pqs", "色", "se"),
        Triple("pqs", "真", "zhen"), Triple("pqs", "成", "cheng"), Triple("pqs", "层", "ceng"),
        Triple("pqs", "社", "she"),

        // n-l
        Triple("nl", "那", "na"), Triple("nl", "哪", "na"), Triple("nl", "你", "ni"),
        Triple("nl", "年", "nian"), Triple("nl", "念", "nian"), Triple("nl", "鸟", "niao"),
        Triple("nl", "牛", "niu"), Triple("nl", "努", "nu"), Triple("nl", "女", "nv"),
        Triple("nl", "暖", "nuan"), Triple("nl", "难", "nan"), Triple("nl", "内", "nei"),
        Triple("nl", "拉", "la"), Triple("nl", "来", "lai"), Triple("nl", "蓝", "lan"),
        Triple("nl", "兰", "lan"), Triple("nl", "浪", "lang"), Triple("nl", "老", "lao"),
        Triple("nl", "乐", "le"), Triple("nl", "了", "le"), Triple("nl", "力", "li"),
        Triple("nl", "里", "li"), Triple("nl", "两", "liang"), Triple("nl", "连", "lian"),
        Triple("nl", "脸", "lian"), Triple("nl", "量", "liang"), Triple("nl", "路", "lu"),
        Triple("nl", "落", "luo"), Triple("nl", "绿", "lv"), Triple("nl", "流", "liu"),
        Triple("nl", "类", "lei"), Triple("nl", "脑", "nao"),

        // h-f
        Triple("hf", "海", "hai"), Triple("hf", "好", "hao"), Triple("hf", "和", "he"),
        Triple("hf", "黑", "hei"), Triple("hf", "红", "hong"), Triple("hf", "后", "hou"),
        Triple("hf", "湖", "hu"), Triple("hf", "花", "hua"), Triple("hf", "黄", "huang"),
        Triple("hf", "回", "hui"), Triple("hf", "会", "hui"), Triple("hf", "混", "hun"),
        Triple("hf", "话", "hua"), Triple("hf", "坏", "huai"), Triple("hf", "换", "huan"),
        Triple("hf", "发", "fa"), Triple("hf", "法", "fa"), Triple("hf", "反", "fan"),
        Triple("hf", "方", "fang"), Triple("hf", "飞", "fei"), Triple("hf", "分", "fen"),
        Triple("hf", "风", "feng"), Triple("hf", "父", "fu"), Triple("hf", "复", "fu"),
        Triple("hf", "非", "fei"), Triple("hf", "房", "fang"), Triple("hf", "放", "fang"),
        Triple("hf", "费", "fei"), Triple("hf", "粉", "fen"), Triple("hf", "服", "fu"),
        Triple("hf", "虎", "hu"), Triple("hf", "扶", "fu"), Triple("hf", "灰", "hui"),

        // 川渝易错字（来源：词表-SC.md）
        Triple("sc", "七", "qi"), Triple("sc", "八", "ba"), Triple("sc", "出", "chu"),
        Triple("sc", "发", "fa"), Triple("sc", "黑", "hei"), Triple("sc", "哭", "ku"),
        Triple("sc", "摸", "mo"), Triple("sc", "滴", "di"), Triple("sc", "桌", "zhuo"),
        Triple("sc", "捉", "zhuo"), Triple("sc", "督", "du"), Triple("sc", "扑", "pu"),
        Triple("sc", "忽", "hu"), Triple("sc", "贴", "tie"), Triple("sc", "接", "jie"),
        Triple("sc", "歇", "xie"), Triple("sc", "跌", "die"), Triple("sc", "割", "ge"),
        Triple("sc", "搁", "ge"), Triple("sc", "喝", "he"), Triple("sc", "磕", "ke"),
        Triple("sc", "刮", "gua"), Triple("sc", "刷", "shua"), Triple("sc", "瞎", "xia"),
        Triple("sc", "掐", "qia"), Triple("sc", "压", "ya"), Triple("sc", "挖", "wa"),
        Triple("sc", "缺", "que"), Triple("sc", "戳", "chuo"), Triple("sc", "秃", "tu"),
        Triple("sc", "湿", "shi"), Triple("sc", "失", "shi"), Triple("sc", "汁", "zhi"),
        Triple("sc", "只", "zhi"), Triple("sc", "织", "zhi"), Triple("sc", "吃", "chi"),
        Triple("sc", "屋", "wu"), Triple("sc", "拍", "pai"), Triple("sc", "摘", "zhai"),
        Triple("sc", "拆", "chai"), Triple("sc", "杀", "sha"), Triple("sc", "插", "cha"),
        Triple("sc", "擦", "ca"), Triple("sc", "吸", "xi"), Triple("sc", "息", "xi"),
        Triple("sc", "析", "xi"), Triple("sc", "踢", "ti"), Triple("sc", "积", "ji"),
        Triple("sc", "激", "ji"), Triple("sc", "击", "ji"), Triple("sc", "逼", "bi"),
        Triple("sc", "约", "yue"), Triple("sc", "说", "shuo"), Triple("sc", "郭", "guo"),
        Triple("sc", "百", "bai"), Triple("sc", "北", "bei"), Triple("sc", "笔", "bi"),
        Triple("sc", "尺", "chi"), Triple("sc", "脚", "jiao"), Triple("sc", "铁", "tie"),
        Triple("sc", "雪", "xue"), Triple("sc", "骨", "gu"), Triple("sc", "谷", "gu"),
        Triple("sc", "渴", "ke"), Triple("sc", "索", "suo"), Triple("sc", "法", "fa"),
        Triple("sc", "塔", "ta"), Triple("sc", "甲", "jia"), Triple("sc", "帖", "tie"),
        Triple("sc", "色", "se"), Triple("sc", "客", "ke"), Triple("sc", "麦", "mai"),
        Triple("sc", "绿", "lü"), Triple("sc", "落", "luo"), Triple("sc", "药", "yao"),
        Triple("sc", "月", "yue"), Triple("sc", "日", "ri"), Triple("sc", "术", "shu"),
        Triple("sc", "物", "wu"), Triple("sc", "确", "que"), Triple("sc", "却", "que"),
        Triple("sc", "各", "ge"), Triple("sc", "设", "she"), Triple("sc", "涉", "she"),
        Triple("sc", "切", "qie"), Triple("sc", "列", "lie"), Triple("sc", "烈", "lie"),
        Triple("sc", "劣", "lie"), Triple("sc", "热", "re"), Triple("sc", "木", "mu"),
        Triple("sc", "目", "mu"), Triple("sc", "六", "liu"), Triple("sc", "录", "lu"),
        Triple("sc", "陆", "lu"), Triple("sc", "鹿", "lu"), Triple("sc", "腹", "fu"),
        Triple("sc", "复", "fu"), Triple("sc", "刻", "ke"), Triple("sc", "克", "ke"),
        Triple("sc", "腊", "la"), Triple("sc", "蜡", "la"), Triple("sc", "辣", "la"),
        Triple("sc", "跃", "yue"), Triple("sc", "悦", "yue"), Triple("sc", "越", "yue"),
        Triple("sc", "乐", "le"), Triple("sc", "鹤", "he"), Triple("sc", "获", "huo"),
        Triple("sc", "肉", "rou"), Triple("sc", "叶", "ye"), Triple("sc", "业", "ye"),
        Triple("sc", "页", "ye"), Triple("sc", "玉", "yu"), Triple("sc", "育", "yu"),
        Triple("sc", "浴", "yu"), Triple("sc", "域", "yu"), Triple("sc", "袜", "wa"),
        Triple("sc", "力", "li"), Triple("sc", "立", "li"), Triple("sc", "沥", "li"),
        Triple("sc", "密", "mi"), Triple("sc", "蜜", "mi"), Triple("sc", "默", "mo"),
        Triple("sc", "莫", "mo"), Triple("sc", "脉", "mai"), Triple("sc", "灭", "mie"),
        Triple("sc", "回", "hui"), Triple("sc", "来", "lai"), Triple("sc", "黄", "huang"),
        Triple("sc", "昏", "hun"), Triple("sc", "红", "hong"), Triple("sc", "绘", "hui"),
        Triple("sc", "画", "hua"), Triple("sc", "饮", "yin"), Triple("sc", "水", "shui"),
        Triple("sc", "呼", "hu"), Triple("sc", "唤", "huan"), Triple("sc", "花", "hua"),
        Triple("sc", "卉", "hui"), Triple("sc", "恍", "huang"), Triple("sc", "惚", "hu"),
        Triple("sc", "辉", "hui"), Triple("sc", "煌", "huang"), Triple("sc", "防", "fang"),
        Triple("sc", "护", "hu"), Triple("sc", "生", "sheng"), Triple("sc", "仿", "fang"),
        Triple("sc", "佛", "fu"), Triple("sc", "丰", "feng"), Triple("sc", "富", "fu"),
        Triple("sc", "废", "fei"), Triple("sc", "话", "hua"), Triple("sc", "房", "fang"),
        Triple("sc", "子", "zi"), Triple("sc", "风", "feng"), Triple("sc", "格", "ge"),
        Triple("sc", "访", "fang"), Triple("sc", "问", "wen"), Triple("sc", "放", "fang"),
        Triple("sc", "假", "jia")
    )

    val all: List<Word> by lazy { buildPool() }

    private fun buildPool(): List<Word> {
        val seen = HashSet<String>()
        val pool = ArrayList<Word>()
        for ((cat, hanzi, pinyin) in RAW) {
            if (seen.add(hanzi))
                pool.add(Word(hanzi, pinyin, Shuangpin.toSp(pinyin), cat))
        }
        return pool
    }

    fun byCat(cat: String): List<Word> = all.filter { it.cat == cat }
}

/**
 * 一关:Id + 名字 + 词表 + 目标命中 + 时长 + 下落速度 + 生成间隔
 */
data class Level(
    val id: Int,
    val name: String,
    val words: List<Word>,
    val target: Int,
    val time: Int,
    val fall: Float,
    val spawn: Float
)

/**
 * 关卡配置,数值与 C# 版 Levels.All 完全一致。
 * 注意:fall/spawn 的具体物理含义在 GameViewModel 里按 dpi 缩放。
 */
object Levels {
    val all: List<Level> = listOf(
        Level(1, "前后鼻音",   WordPool.byCat("qbh"), 25, 300, 25f, 2.5f),
        Level(2, "平翘舌",     WordPool.byCat("pqs"), 25, 300, 30f, 2.2f),
        Level(3, "n-l 之争",   WordPool.byCat("nl"),  22, 300, 35f, 1.9f),
        Level(4, "h-f 之争",   WordPool.byCat("hf"),  20, 300, 40f, 1.7f),
        Level(5, "综合挑战",   WordPool.all,          35, 300, 45f, 1.5f),
        Level(6, "川渝易错字", WordPool.byCat("sc"),  35, 300, 35f, 1.8f)
    )
}

/**
 * 把秒数格式化成友好文本
 */
object Fmt {
    fun duration(secs: Int): String {
        if (secs <= 0) return "5分钟"
        return if (secs % 60 == 0) "${secs / 60}分钟" else "${secs}秒"
    }
}